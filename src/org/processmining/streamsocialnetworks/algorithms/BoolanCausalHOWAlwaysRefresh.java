package org.processmining.streamsocialnetworks.algorithms;

import java.util.Collection;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

import gnu.trove.map.TObjectDoubleMap;

public class BoolanCausalHOWAlwaysRefresh extends BooleanCausalHOWSSNBuilderImpl {

	public BoolanCausalHOWAlwaysRefresh(StreamSocialNetwork<String> network) {
		super(network);
	}

	public Type getType() {
		//		return Type.BOOLEAN_CAUSAl_HOW_ALWAYS_REFRESH;
		return null;
	}

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		if (!newEdgeInCaseTrie.getFrom().equals(trie.getRoot())) {
			//			Pair<String, String> df = new Pair<String, String>(
			//					newEdgeInCaseTrie.getTo().getVertexObject().getActivity(),
			//					newEdgeInCaseTrie.getFrom().getVertexObject().getActivity());
			//			if (!dfg.containsKey(df) || dfg.get(df) == 0) { // causality will change...
			//				Pair<String, String> dfRev = PairUtils.reverse(df);
			//				if (dfg.containsKey(dfRev) && dfg.get(dfRev) > 0) {
			//					causal.remove(dfRev);
			//				} else {
			//					causal.add(df);
			//				}
			//			}
			recalculate(trie);
		}
	}

	@Override
	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		if (!removedEdges.isEmpty()) {
			//			for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edgeSequence : removedEdges) {
			//				List<ActivityResourcePair> trace = StreamTrieUtils.constructTraceFromListOfEdges(edgeSequence,
			//						Integer.MAX_VALUE, true, true);
			//				for (int i = 0; i < trace.size() - 1; i++) {
			//					Pair<String, String> df = new Pair<String, String>(trace.get(i).getActivity(),
			//							trace.get(i + 1).getActivity());
			//					dfg.adjustValue(df, -1);
			//					if (dfg.get(df) == 0) {
			//						Pair<String, String> dfRev = PairUtils.reverse(df);
			//						if (dfg.containsKey(dfRev) && dfg.get(dfRev) > 0) {
			//							causal.add(dfRev);
			//						} else {
			//							causal.remove(df);
			//						}
			//					}
			//				}
			//			}
			recalculate(trie);
		}
		return null;
	}

}
