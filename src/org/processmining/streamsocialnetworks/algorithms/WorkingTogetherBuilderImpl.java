package org.processmining.streamsocialnetworks.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.StreamTrieUtils;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class WorkingTogetherBuilderImpl extends AbstractStreamSocialNetworkBuilder {

	private TObjectDoubleMap<Pair<String, String>> jointCases = new TObjectDoubleHashMap<>();
	private TObjectDoubleMap<Pair<String, String>> networkLinkValues = new TObjectDoubleHashMap<>();
	private TObjectDoubleMap<String> resourceCaseCount = new TObjectDoubleHashMap<>();

	public WorkingTogetherBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	public Type getType() {
		return Type.WORKING_TOGETHER;
	}

	private void initForTrace(List<ActivityResourcePair> trace) {
		Collection<String> seen = new THashSet<>();
		Collection<String> selfLoopCandidates = new THashSet<>();
		for (ActivityResourcePair arp : trace) {
			String resource = arp.getResource();
			if (!seen.contains(resource)) {
				resourceCaseCount.adjustOrPutValue(arp.getResource(), 1, 1);
				seen.add(resource);
			} else {
				selfLoopCandidates.add(resource);
			}
		}
		for (String r1 : seen) {
			for (String r2 : seen) {
				if (!r1.equals(r2) || (r1.equals(r2) && selfLoopCandidates.contains(r1))) {
					jointCases.adjustOrPutValue(new Pair<String, String>(r1, r2), 1, 1);
				}
			}
		}
	}

	protected void initializeForTrie(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		initRecursive(trie, trie.getRoot(), new ArrayList<ActivityResourcePair>());
	}

	protected TObjectDoubleMap<Pair<String, String>> initializeNetwork() {
		refresh();
		return new TObjectDoubleHashMap<>(networkLinkValues);
	}

	private void initRecursive(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			VertexImpl<ActivityResourcePair> vertex, List<ActivityResourcePair> trace) {
		Collection<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> outEdges;
		if (!(outEdges = trie.getOutEdges(vertex)).isEmpty()) {
			for (EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> edge : outEdges) {
				trace.add(edge.getTo().getVertexObject());
				initRecursive(trie, vertex, trace);
				trace.remove(trace.size() - 1);
				int pointers = StreamTrieUtils.numberOfPointersToVertex(trie, vertex);
				if (trace.size() > 1 && pointers > 0) {
					initForTrace(trace);
				}
			}
		} else {
			initForTrace(trace);
		}
	}

	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		String newResource = newEdgeInCaseTrie.getTo().getVertexObject().getResource();
		List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
				newEdgeInCaseTrie.getTo(), Integer.MAX_VALUE, false);
		TObjectIntMap<String> analyzed = resourcesPresentInTrace(trace.subList(0, trace.size() - 1));
		if (!analyzed.keySet().contains(newResource) || analyzed.isEmpty()) {
			resourceCaseCount.adjustOrPutValue(newResource, 1, 1);
			for (String r1 : analyzed.keySet()) {
				Pair<String, String> newOld = new Pair<String, String>(newResource, r1);
				Pair<String, String> oldNew = new Pair<String, String>(r1, newResource);
				jointCases.adjustOrPutValue(newOld, 1, 1);
				jointCases.adjustOrPutValue(oldNew, 1, 1);
				double newOldRel = jointCases.get(newOld) / resourceCaseCount.get(newResource);
				double oldNewRel = jointCases.get(oldNew) / resourceCaseCount.get(r1);
				networkLinkValues.put(newOld, newOldRel);
				networkLinkValues.put(oldNew, oldNewRel);
			}
			if (analyzed.isEmpty()) {
				updateAllRelativePairsContainingResourceAsFirst(newResource);
			}
		} else if (analyzed.get(newResource) == 1.0) { // the new resource occurred exactly once
			Pair<String, String> self = new Pair<String, String>(newResource, newResource);
			jointCases.adjustOrPutValue(self, 1, 1);
			double rel = jointCases.get(self) / resourceCaseCount.get(newResource);
			networkLinkValues.put(self, rel);
		}
	}

	private void updateAllRelativePairsContainingResourceAsFirst(String resource) {
		double div = resourceCaseCount.get(resource);
		for (Pair<String, String> pair : jointCases.keySet()) {
			if (pair.getFirst().equals(resource)) {
				double val = jointCases.get(pair) / div;
				networkLinkValues.put(pair, val);
			}
		}
	}

	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edges : removedEdges) {
			TObjectIntMap<String> removed = resourcesPresentInTrace(
					StreamTrieUtils.constructTraceFromListOfEdges(edges, Integer.MAX_VALUE, true, true));
			for (String r : removed.keySet()) {
				resourceCaseCount.adjustValue(r, -1);
			}
			for (String r1 : removed.keySet()) {
				for (String r2 : removed.keySet()) {
					if (!r1.equals(r2) || (r1.equals(r2) && removed.get(r1) > 1.0)) {
						Pair<String, String> pair = new Pair<>(r1, r2);
						jointCases.adjustValue(pair, -1);
						double relative = jointCases.get(pair) / resourceCaseCount.get(r1);
						networkLinkValues.put(pair, relative);
					}
				}
			}
		}
		refresh();
		return new TObjectDoubleHashMap<>(networkLinkValues);
	}

	protected void refresh() {
		for (Pair<String, String> pair : jointCases.keySet()) {
			double relative = jointCases.get(pair) / resourceCaseCount.get(pair.getFirst());
			networkLinkValues.put(pair, relative);
		}
	}

	private TObjectIntMap<String> resourcesPresentInTrace(List<ActivityResourcePair> trace) {
		TObjectIntMap<String> result = new TObjectIntHashMap<>();
		for (ActivityResourcePair arp : trace) {
			result.adjustOrPutValue(arp.getResource(), 1, 1);
		}
		return result;
	}

	protected void sanityCheck() {
		// TODO Auto-generated method stub
	}

	protected void refreshAllNetworkValues() {
		// TODO Auto-generated method stub

	}

	protected long measureMemoryConsumption() {
		// TODO Auto-generated method stub
		return -1;
	}

	protected long getNumResourcePairsActiveInDataStructure() {
		// TODO Auto-generated method stub
		return -1;
	}

}
