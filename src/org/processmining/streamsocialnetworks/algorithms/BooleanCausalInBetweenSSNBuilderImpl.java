package org.processmining.streamsocialnetworks.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.StreamTrieUtils;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.eventstream.utils.PairUtils;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;
import org.processmining.streamsocialnetworks.models.TripleImpl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class BooleanCausalInBetweenSSNBuilderImpl extends BooleanInBetweenBuilderImpl {

	private final Collection<Pair<String, String>> causal = new HashSet<>();
	private final TObjectIntMap<Pair<String, String>> dfg = new TObjectIntHashMap<>();

	public BooleanCausalInBetweenSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	private boolean causalityChanges(Pair<String, String> newDf) {
		if (causal.contains(PairUtils.reverse(newDf))) {
			return true;
		} else if (!causal.contains(newDf)
				&& (!dfg.containsKey(newDf) || (dfg.containsKey(newDf) && dfg.get(newDf) == 0))) {
			return true;
		}
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		dfg.clear();
		causal.clear();
	}

	/**
	 * 
	 * @param trace
	 * @return true iff causality changed...
	 */
	private boolean decreaseCausalityForGivenTrace(List<ActivityResourcePair> trace) {
		boolean causalityChanged = false;
		for (int i = 1; i < trace.size(); i++) {
			Pair<String, String> df = new Pair<String, String>(trace.get(i - 1).getActivity(),
					trace.get(i).getActivity());
			dfg.adjustValue(df, -1);
			if (dfg.get(df) == 0) {
				dfg.remove(df);
				if (causal.contains(df)) {
					causal.remove(df);
				} else {
					causal.add(PairUtils.reverse(df));
				}
				causalityChanged = true;
			}
		}
		return causalityChanged;
	}

	@Override
	public Type getType() {
		//		return Type.BOOLEAN_CAUSAL_INBETWEEN;
		return null;
	}

	private void initializeCausalGraph() {
		for (Pair<String, String> df : dfg.keySet()) {
			if (!dfg.containsKey(PairUtils.reverse(df))) {
				causal.add(df);
			}
		}
	}

	private void initializeDfg(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			VertexImpl<ActivityResourcePair> vertex) {
		for (EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> e : trie.getOutEdges(vertex)) {
			if (!vertex.equals(trie.getRoot())) {
				Pair<String, String> df = new Pair<>(e.getFrom().getVertexObject().getActivity(),
						e.getTo().getVertexObject().getActivity());
				int count = e.getCount();
				dfg.adjustOrPutValue(df, count, count);
			}
			initializeDfg(trie, e.getTo());
		}
	}

	@Override
	protected void initializeResourcePairCount(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			VertexImpl<ActivityResourcePair> vertex, final List<ActivityResourcePair> trace) {
		initializeDfg(trie, vertex);
		initializeCausalGraph();
		super.initializeResourcePairCount(trie, vertex, trace);
	}

	protected void processCompleteTrace(List<ActivityResourcePair> trace, int cardinality, boolean networkRefresh) {
		for (int d = 2; d <= Math.min(trace.size() - 1, getK()); d++) {
			Collection<Pair<String, String>> seen = new THashSet<>();
			for (int i = 0; i < trace.size() - d; i++) {
				for (int j = i + 1; j < i + d; j++) {
					if (trace.get(i).equals(trace.get(i + d))) {
						Pair<String, String> pair = new Pair<>(trace.get(i).getResource(), trace.get(j).getResource());
						TripleImpl<String, String, String> activityTriple = new TripleImpl<String, String, String>(
								trace.get(i).getActivity(), trace.get(j).getActivity(), trace.get(i + d).getActivity());
						if (!seen.contains(pair) && causal.contains(activityTriple.getPairOfFirstAndSecond())
								&& causal.contains(activityTriple.getPairOfSecondAndThird())) {
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

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdge) {
		if (!newEdge.getFrom().equals(trie.getRoot())) {
			Pair<String, String> df = new Pair<>(newEdge.getFrom().getVertexObject().getActivity(),
					newEdge.getTo().getVertexObject().getActivity());
			if (causalityChanges(df)) {
				init(trie);
			} else {
				List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
						newEdge.getTo(), Integer.MAX_VALUE, false);
				for (int d = 2; d <= Math.min(trace.size() - 1, getK()); d++) {
					Collection<Pair<String, String>> seen = new THashSet<>();
					int i = trace.size() - d;
					for (int j = i + 1; j < i + d; j++) {
						if (trace.get(i).equals(trace.get(i + d))) {
							Pair<String, String> pair = new Pair<>(trace.get(i).getResource(),
									trace.get(j).getResource());
							TripleImpl<String, String, String> activityTriple = new TripleImpl<String, String, String>(
									trace.get(i).getActivity(), trace.get(j).getActivity(),
									trace.get(i + d).getActivity());
							if (!seen.contains(pair) && causal.contains(activityTriple.getPairOfFirstAndSecond())
									&& causal.contains(activityTriple.getPairOfSecondAndThird())) {
								double inc = getBetaPowerSeries()[d - 2];
								getResourcePairCount().adjustOrPutValue(pair, inc, inc);
								double relative = getResourcePairCount().get(pair) / getDivisor();
								getRelativeResourcePairValues().put(pair, relative);
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edgeSequence : removedEdges) {
			List<ActivityResourcePair> trace = StreamTrieUtils.constructTraceFromListOfEdges(edgeSequence,
					Integer.MAX_VALUE, true, true);
			if (decreaseCausalityForGivenTrace(trace)) {
				init(trie);
				break;
			}
			updateDivisorForRemovedCase(trace);
			updateResourcePairCountAndRelativeValuesForRemovedCase(trace);
		}
		refresh();
		return new TObjectDoubleHashMap<>(getRelativeResourcePairValues());
	}

}
