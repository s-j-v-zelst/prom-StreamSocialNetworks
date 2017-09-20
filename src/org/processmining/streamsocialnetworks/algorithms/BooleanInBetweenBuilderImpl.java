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

public class BooleanInBetweenBuilderImpl extends AbstractHOWInBetweenSSNBuilderImpl {

	public BooleanInBetweenBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
		setK(2);
	}

	public Type getType() {
		return Type.BOOLEAN_INBETWEEN;
	}

	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		setDivisor(getDivisor() + (cardinality * getBetaPowerSeriesSum()[Math.min(trace.size() - 1, getK()) - 2]));
		processCompleteTrace(trace, cardinality, false);
	}

	protected boolean performSanityCheck(TObjectDoubleMap<Pair<String, String>> resourcePairCounts,
			TObjectDoubleMap<Pair<String, String>> relativeValues) {
		return true;
	}

	protected void updateDivisorForRemovedCase(List<ActivityResourcePair> trace) {
		setDivisor(getDivisor() - (getBetaPowerSeriesSum()[Math.min(trace.size() - 1, getK()) - 2]));

	}

	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		processCompleteTrace(trace, -1, true);
	}

	protected void processCompleteTrace(List<ActivityResourcePair> trace, int cardinality, boolean networkRefresh) {
		for (int d = 2; d <= Math.min(trace.size() - 1, getK()); d++) {
			Collection<Pair<String, String>> seen = new THashSet<>();
			for (int i = 0; i < trace.size() - d; i++) {
				for (int j = i + 1; j < i + d; j++) {
					if (trace.get(i).equals(trace.get(i + d))) {
						Pair<String, String> pair = new Pair<>(trace.get(i).getResource(), trace.get(j).getResource());
						if (!seen.contains(pair)) {
							double inc = getBetaPowerSeries()[d - 2] * cardinality;
							getResourcePairCount().adjustOrPutValue(pair, inc, inc);
							if (networkRefresh) {
								double relative = getResourcePairCount().get(pair) / getDivisor();
								getRelativeResourcePairValues().put(pair, relative);
							}
						}
					}
				}
			}
		}
	}

	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdge) {
		List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie, newEdge.getTo(),
				Integer.MAX_VALUE, false);
		for (int d = 2; d <= Math.min(trace.size() - 1, getK()); d++) {
			Collection<Pair<String, String>> seen = new THashSet<>();
			int i = trace.size() - d;
			for (int j = i + 1; j < i + d; j++) {
				if (trace.get(i).equals(trace.get(i + d))) {
					Pair<String, String> pair = new Pair<>(trace.get(i).getResource(), trace.get(j).getResource());
					if (!seen.contains(pair)) {
						double inc = getBetaPowerSeries()[d - 2];
						getResourcePairCount().adjustOrPutValue(pair, inc, inc);
						double relative = getResourcePairCount().get(pair) / getDivisor();
						getRelativeResourcePairValues().put(pair, relative);
					}
				}
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
