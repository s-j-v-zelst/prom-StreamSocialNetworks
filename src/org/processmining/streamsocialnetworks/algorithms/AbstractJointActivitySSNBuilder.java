package org.processmining.streamsocialnetworks.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public abstract class AbstractJointActivitySSNBuilder extends AbstractStreamSocialNetworkBuilder {

	private final TObjectIntMap<String> activities = new TObjectIntHashMap<>();

	private int[] activityOcc = new int[1024];

	private List<Integer> freeActivityIndices = new ArrayList<>();
	private List<Integer> freeResourceIndices = new ArrayList<>();
	private int[][] matrix = new int[1024][1024];
	private Integer maxActivityIndex = -1;

	private Integer maxResourceIndex = -1;
	private int[] resourceOcc = new int[1024];
	private final TObjectIntMap<String> resources = new TObjectIntHashMap<>();
	private int totalActivityOcc;

	private TObjectDoubleMap<Pair<String, String>> networkLinkValues = new TObjectDoubleHashMap<>();

	public AbstractJointActivitySSNBuilder(StreamSocialNetwork<String> network) {
		super(network);
	}

	protected void clearColumn(final int c) {
		for (int r = 0; r < matrix.length; r++) {
			matrix[r][c] = 0;
		}
	}

	protected void clearRow(final int r) {
		for (int c = 0; c < matrix[r].length; c++) {
			matrix[r][c] = 0;
		}
	}

	protected abstract double distanceForPair(Pair<String, String> pair);

	protected void expandColumns() {
		for (int r = 0; r < matrix.length; r++) {
			matrix[r] = Arrays.copyOf(matrix[r], matrix[r].length * 2);
		}
	}

	protected void expandRows() {
		int currentLength = matrix.length;
		int[][] newMatrix = new int[matrix.length * 2][matrix[0].length];
		for (int r = 0; r < newMatrix.length; r++) {
			if (r < currentLength) {
				newMatrix[r] = matrix[r];
			}
		}
		matrix = newMatrix;
	}

	protected int fetchOrConstructActivityIndex(String activity) {
		if (!activities.containsKey(activity)) {
			int activityIndex = indexOf(activity, freeActivityIndices, maxActivityIndex, matrix);
			if (activityIndex == -1) {
				expandColumns();
				activityIndex = indexOf(activity, freeActivityIndices, maxActivityIndex, matrix);
			}
			activities.put(activity, activityIndex);
			maxActivityIndex = Math.max(activityIndex, maxActivityIndex);
			return activityIndex;
		} else {
			return activities.get(activity);
		}
	}

	protected int fetchOrConstructResrouceIndex(String resource) {
		if (!resources.containsKey(resource)) {
			int resourceIndex = indexOf(resource, freeResourceIndices, maxResourceIndex, matrix);
			if (resourceIndex == -1) {
				expandRows();
				resourceIndex = indexOf(resource, freeResourceIndices, maxResourceIndex, matrix);
			}
			resources.put(resource, resourceIndex);
			maxResourceIndex = Math.max(resourceIndex, maxResourceIndex);
			return resourceIndex;
		} else {
			return activities.get(resource);
		}
	}

	protected TObjectIntMap<String> getActivites() {
		return activities;
	}

	protected int[][] getMatrix() {
		return matrix;
	}

	protected TObjectIntMap<String> getResources() {
		return resources;
	}

	protected int getTotalActivityOcc() {
		return totalActivityOcc;
	}

	/**
	 * 
	 * @param obj
	 * @param lookup
	 * @param maxIndex
	 * @return -1 iff no more index available (=> matrix needs to be enlarged)
	 */
	protected int indexOf(final String obj, List<Integer> freeIndices, Integer maxIndex, int[][] matrix) {
		if (!freeIndices.isEmpty()) {
			int res = freeIndices.get(0);
			freeIndices.remove(0);
			return res;
		} else if (maxIndex >= matrix.length) {
			return -1;
		} else {
			return maxIndex + 1;
		}
	}

	protected void initializeForTrie(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		initRecursive(trie, trie.getRoot());
	}

	protected TObjectDoubleMap<Pair<String, String>> initializeNetwork() {
		refresh();
		return new TObjectDoubleHashMap<>(networkLinkValues);
	}

	protected void initRecursive(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			VertexImpl<ActivityResourcePair> vertex) {
		for (EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> e : trie.getOutEdges(vertex)) {
			VertexImpl<ActivityResourcePair> to = e.getTo();
			updateMatrixForVertex(to, to.getCount());
			initRecursive(trie, to);
		}
	}

	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		updateMatrixForVertex(newEdgeInCaseTrie.getTo(), 1);
		//TODO: Can we do this faster? -> probably yes, metric only changes for the new resource.
		refresh();

	}

	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edgeList : removedEdges) {
			for (EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> edge : edgeList) {
				updateMatrixForVertex(edge.getTo(), -1);
			}
		}
		refresh();
		return new TObjectDoubleHashMap<>(networkLinkValues);
	}

	protected void refresh() {
		for (String r1 : resources.keySet()) {
			for (String r2 : resources.keySet()) {
				Pair<String, String> pair = new Pair<>(r1, r2);
				networkLinkValues.put(pair, distanceForPair(pair));
			}
		}
	}

	protected void updateMatrixForVertex(final VertexImpl<ActivityResourcePair> v, final int val) {
		ActivityResourcePair arp = v.getVertexObject();
		int activityIndex = fetchOrConstructActivityIndex(arp.getActivity());
		int resourceIndex = fetchOrConstructResrouceIndex(arp.getResource());
		matrix[resourceIndex][activityIndex] += val;
		activityOcc[activityIndex] += val;
		totalActivityOcc += val;
		resourceOcc[resourceIndex] += val;
		if (val < 0) {
			if (activityOcc[activityIndex] <= 0) {
				clearColumn(activityIndex);
				freeActivityIndices.add(activityIndex);
				activities.remove(arp.getActivity());

			}
			if (resourceOcc[resourceIndex] <= 0) {
				clearRow(resourceIndex);
				freeResourceIndices.add(resourceIndex);
			}
		}
	}

}
