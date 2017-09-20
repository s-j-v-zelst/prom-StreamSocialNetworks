package org.processmining.streamsocialnetworks.algorithms;

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
import gnu.trove.set.hash.THashSet;

public class BooleanHOWSSNBuilderImpl extends AbstractHOWInBetweenSSNBuilderImpl {

	public BooleanHOWSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	protected boolean containsResourcePairForDistance(List<ActivityResourcePair> trace, Pair<String, String> pair,
			int dist) {
		for (int i = dist; i < trace.size(); i++) {
			String from = trace.get(i - dist).getResource();
			String to = trace.get(i).getResource();
			if (pair.getFirst().equals(from) && pair.getSecond().equals(to)) {
				return true;
			}
		}
		return false;
	}

	public Type getType() {
		return Type.BOOLEAN_HANDOVER_OF_WORK;
	}

	@Override
	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		updateDivisorForTrace(trace, cardinality);
		for (int dist = 1; dist <= getK(); dist++) {
			Collection<Pair<String, String>> seen = new THashSet<Pair<String, String>>();
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (!seen.contains(resourcePair)) {
					double v = getBetaPowerSeries()[dist - 1] * cardinality;
					getResourcePairCount().adjustOrPutValue(resourcePair, v, v);
					seen.add(resourcePair);
				}
			}
		}
	}

	protected void updateDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		if (trace.size() > 1) {
			int n = Math.min(trace.size() - 1, getK()); // 1 <= n <= min(|c|, k);
			setDivisor(getDivisor() + (getBetaPowerSeriesSum()[n - 1] * cardinality));
		}
	}

	protected boolean performSanityCheck(TObjectDoubleMap<Pair<String, String>> resourcePairCounts,
			TObjectDoubleMap<Pair<String, String>> relativeValues) {
		boolean result = true;
		for (Pair<String, String> resourcePair : getRelativeResourcePairValues().keySet()) {
			double value = getRelativeResourcePairValues().get(resourcePair);
			if (value < 0 || value > 1) {
				System.out.println(
						"Boolean HOW sanity check failed for pair: " + resourcePair.toString() + ", value: " + value);
				result = false;
			}
		}
		return result;
	}

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
				newEdgeInCaseTrie.getTo(), Integer.MAX_VALUE, false);
		if (trace.size() > 1) {
			if (trace.size() - 1 <= getK()) {
				setDivisor(getDivisor() + getBetaPowerSeries()[trace.size() - 2]);
			}
			for (int dist = 1; dist <= getK(); dist++) {
				if (trace.size() - 1 - dist >= 0) {
					Pair<String, String> resourcePair = new Pair<>(trace.get(trace.size() - 1 - dist).getResource(),
							trace.get(trace.size() - 1).getResource());
					if (!containsResourcePairForDistance(trace.subList(0, trace.size() - 1), resourcePair, dist)) {
						getResourcePairCount().adjustOrPutValue(resourcePair, getBetaPowerSeries()[dist - 1],
								getBetaPowerSeries()[dist - 1]);
						double relativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
						getRelativeResourcePairValues().put(resourcePair, relativeValue);
					}
				} else {
					break;
				}
			}
		}
	}

	protected void updateDivisorForRemovedCase(List<ActivityResourcePair> trace) {
		updateDivisorForTrace(trace, -1);
	}

	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		for (int dist = 1; dist <= getK(); dist++) {
			Collection<Pair<String, String>> seen = new THashSet<Pair<String, String>>();
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (!seen.contains(resourcePair)) {
					double v = getBetaPowerSeries()[dist - 1] * -1;
					getResourcePairCount().adjustValue(resourcePair, v);
					double relativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
					getRelativeResourcePairValues().put(resourcePair, relativeValue);
					seen.add(resourcePair);
				}
			}
		}
	}

	protected void sanityCheck() {
		for (Pair<String, String> resourcePair : getRelativeResourcePairValues().keySet()) {
			double metricVal = getRelativeResourcePairValues().get(resourcePair);
			if (metricVal > 1.0) {
				System.out.println(
						"BOOLEAN HOW Sanity Check Failed: " + resourcePair.toString() + ", metric value: " + metricVal);
			}
		}

	}

	protected void refreshAllNetworkValues() {
		// TODO Auto-generated method stub		
	}

	protected long measureMemoryConsumption() {
		// TODO Auto-generated method stub
		return -1;
	}

}
