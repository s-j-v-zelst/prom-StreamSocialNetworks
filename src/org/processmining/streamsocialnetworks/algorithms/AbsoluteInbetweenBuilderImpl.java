package org.processmining.streamsocialnetworks.algorithms;

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

public class AbsoluteInbetweenBuilderImpl extends AbstractHOWInBetweenSSNBuilderImpl {

	public AbsoluteInbetweenBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
		setK(2);
	}

	public Type getType() {
		return Type.ABSOLUTE_INBETWEEN;
	}

	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		updateDivisorForTrace(trace, cardinality);
		if (trace.size() > 2) {
			for (int dist = 2; dist <= Math.min(trace.size() - 1, getK()); dist++) {
				for (int i = 0; i < trace.size() - dist; i++) {
					for (int j = i + 1; j < i + dist; j++) {
						// check whether we get work back
						updateResourcePairCountForTraceIndexAndDistance(trace, dist, i, j, cardinality, false);
					}
				}
			}
		}
	}

	protected void updateDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		double value = 0d;
		for (int n = 2; n <= Math.min(trace.size() - 1, getK()); n++) {
			value += (getBetaPowerSeries()[n - 2] * (trace.size() - n) * (n - 1));
		}
		setDivisor(getDivisor() + (value * cardinality));
	}

	protected boolean performSanityCheck(TObjectDoubleMap<Pair<String, String>> resourcePairCounts,
			TObjectDoubleMap<Pair<String, String>> relativeValues) {
		boolean result = true;
		for (Pair<String, String> resourcePair : getRelativeResourcePairValues().keySet()) {
			double value = getRelativeResourcePairValues().get(resourcePair);
			if (value < 0 || value > 1) {
				System.out.println("Absolute In Between sanity check failed for pair: " + resourcePair.toString()
						+ ", value: " + value);
				result = false;
			}
		}
		return result;
	}

	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
				newEdgeInCaseTrie.getTo(), getK() + 1, false);
		incrementalDivisorUpdate(trace);
		if (trace.size() > 2) {
			for (int dist = 2; dist <= Math.min(trace.size() - 1, getK()); dist++) {
				int i = trace.size() - dist - 1;
				for (int j = i + 1; j < i + dist; j++) {
					updateResourcePairCountForTraceIndexAndDistance(trace, dist, i, j, 1, true);
				}
			}
		}
	}

	protected void updateDivisorForRemovedCase(List<ActivityResourcePair> trace) {
		updateDivisorForTrace(trace, -1);
	}

	protected void incrementalDivisorUpdate(List<ActivityResourcePair> trace) {
		// check if length of prefix -1 >= k.
		if (trace.size() - 1 - 1 >= getK()) {
			//TODO: Store in some array in stead of iterating for loop
			for (int n = 2; n <= getK(); n++) {
				setDivisor(getDivisor() + (getBetaPowerSeries()[n - 2] * (n - 1)));
			}
		} else {
			for (int n = 2; n <= trace.size() - 1; n++) {
				setDivisor(getDivisor() + (getBetaPowerSeries()[n - 2] * (n - 1)));
			}
		}
	}

	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		if (trace.size() >= 3) {
			for (int dist = 2; dist <= Math.min(trace.size() - 1, getK()); dist++) {
				for (int i = 0; i < trace.size() - dist; i++) {
					for (int j = i + 1; j < i + dist; j++) {
						// check whether we get work back
						updateResourcePairCountForTraceIndexAndDistance(trace, dist, i, j, -1, true);
					}
				}
			}
		}
	}

	protected void updateResourcePairCountForTraceIndexAndDistance(final List<ActivityResourcePair> trace,
			final int dist, final int i, final int j, final int traceCardinality, boolean propagateToNetwork) {
		if (trace.get(i).getResource().equals(trace.get(i + dist).getResource())) {
			Pair<String, String> resourcePair = new Pair<>(trace.get(i).getResource(), trace.get(j).getResource());
			double value = getBetaPowerSeries()[dist - 2] * traceCardinality;
			getResourcePairCount().adjustOrPutValue(resourcePair, value, value);
			if (propagateToNetwork) {
				double rel = getResourcePairCount().get(resourcePair) / getDivisor();
				getRelativeResourcePairValues().put(resourcePair, rel);
			}
		}
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
}
