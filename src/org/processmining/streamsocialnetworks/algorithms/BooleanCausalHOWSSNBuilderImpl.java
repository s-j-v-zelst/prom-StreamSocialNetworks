package org.processmining.streamsocialnetworks.algorithms;

import java.util.ArrayList;
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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class BooleanCausalHOWSSNBuilderImpl extends BooleanHOWSSNBuilderImpl {

	//	private final MemoryMeter memoryMeter = new MemoryMeter();

	protected Collection<Pair<String, String>> causal = new HashSet<>();
	protected TObjectIntMap<Pair<String, String>> dfg = new TObjectIntHashMap<>();
	private boolean isRecalculate = false;

	public BooleanCausalHOWSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	//returns true if a causal relation was removed
	private boolean updateCausalRelationsForNewEvent(Pair<String, String> newDf) {
		boolean dfgZero = !dfg.containsKey(newDf) || dfg.get(newDf) == 0;
		dfg.adjustOrPutValue(newDf, 1, 1);
		Pair<String, String> dfRev = PairUtils.reverse(newDf);
		if (!dfg.containsKey(dfRev) || dfg.get(dfRev) == 0) { // the new df is causal
			causal.add(newDf);
		} else { // paralellism, if the newDf did not exist, the reverse is no longer causal.
			if (dfgZero) {
				causal.remove(dfRev);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void clear() {
		super.clear();
		dfg.clear();
		causal.clear();
	}

	public void initializeForTrie(final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		synchronized (getMonitor()) {
			clear();
			preCalculateBetaValues(getK());
			initializeDfg(trie, trie.getRoot());
			initializeCausalGraph();
			initializeResourcePairCount(trie, trie.getRoot(), new ArrayList<ActivityResourcePair>());
			initializeRelativeResourcePairValues();
		}
	}

	/**
	 * 
	 * @param trace
	 * @return true iff causality changed...
	 */
	private boolean decreasedCausalityForGivenTrace(List<ActivityResourcePair> trace) {
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
					causalityChanged = true;
				}
			}
		}
		return causalityChanged;
	}

	public Type getType() {
		return Type.BOOLEAN_CAUSAL_HAND_OVER_OF_WORK;
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

	//	bug: the super.initializeResourcePairCount(trie, vertex,trace) calls this function again!
	//	@Override
	//	protected void initializeResourcePairCount(
	//			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
	//			VertexImpl<ActivityResourcePair> vertex, final List<ActivityResourcePair> trace) {
	//		initializeDfg(trie, vertex);
	//		initializeCausalGraph();
	//		super.initializeResourcePairCount(trie, vertex, trace);
	//	}

	@Override
	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		updateDivisorForTrace(trace, cardinality);
		for (int dist = 1; dist <= getK(); dist++) {
			Collection<Pair<String, String>> seen = new THashSet<Pair<String, String>>();
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> activityPair = new Pair<>(trace.get(i - dist).getActivity(),
						trace.get(i).getActivity());
				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (!seen.contains(resourcePair) && causal.contains(activityPair)) {
					double v = getBetaPowerSeries()[dist - 1] * cardinality;
					getResourcePairCount().adjustOrPutValue(resourcePair, v, v);
					seen.add(resourcePair);
				}
			}
		}
	}

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> caseTrie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		if (!newEdgeInCaseTrie.getFrom().equals(caseTrie.getRoot())) {
			Pair<String, String> df = new Pair<>(newEdgeInCaseTrie.getFrom().getVertexObject().getActivity(),
					newEdgeInCaseTrie.getTo().getVertexObject().getActivity());
			if (updateCausalRelationsForNewEvent(df)) {
				recalculate(caseTrie);
				isRecalculate = true;
			} else {
				processTraceAfterNewEventAddition(StreamTrieUtils.constructSequenceEndingInVertex(caseTrie,
						newEdgeInCaseTrie.getTo(), Integer.MAX_VALUE, false));
			}
		}
	}

	@Override
	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		if (!removedEdges.isEmpty() && !isRecalculate) {
			boolean refresh = false;
			for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edgeSequence : removedEdges) {
				List<ActivityResourcePair> trace = StreamTrieUtils.constructTraceFromListOfEdges(edgeSequence,
						Integer.MAX_VALUE, true, true);
				if (decreasedCausalityForGivenTrace(trace)) {
					refresh = true;
				}
				if (!refresh) { // only do this as long as we do not have to refresh....
					updateDivisorForRemovedCase(trace);
					updateResourcePairCountAndRelativeValuesForRemovedCase(trace);
				}
			}
			if (refresh) {
				recalculate(trie);
			}
		}
		isRecalculate = false;
		//		refresh();
		//		return new TObjectDoubleHashMap<>(getRelativeResourcePairValues());
		return null;
	}

	@Override
	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		for (int dist = 1; dist <= getK(); dist++) {
			Collection<Pair<String, String>> seen = new THashSet<Pair<String, String>>();
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> activityPair = new Pair<>(trace.get(i - dist).getActivity(),
						trace.get(i).getActivity());
				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (!seen.contains(resourcePair) && causal.contains(activityPair)) {
					double v = getBetaPowerSeries()[dist - 1] * -1;
					getResourcePairCount().adjustValue(resourcePair, v);
					if (getResourcePairCount().get(resourcePair) == 0.0) {
						getResourcePairCount().remove(resourcePair);
					} else {
						double relativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
						getRelativeResourcePairValues().put(resourcePair, relativeValue);
					}
					seen.add(resourcePair);
				}
			}
		}
	}

	private void processTraceAfterNewEventAddition(List<ActivityResourcePair> trace) {
		for (int dist = 1; dist <= getK(); dist++) {
			Collection<Pair<String, String>> seen = new THashSet<Pair<String, String>>();
			// here used to be the following code that does not seem to check whether the relation was already accounted for in the trace.
			//			int i = trace.size() - 1;
			//			if (i - dist >= 0) {
			//				Pair<String, String> activityPair = new Pair<>(trace.get(i - dist).getActivity(),
			//						trace.get(i).getActivity());
			//				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
			//						trace.get(i).getResource());
			//				if (i < trace.size() - 1) {
			//					if (causal.contains(activityPair)) {
			//						seen.add(resourcePair);
			//					}
			//				} else {
			//					if (!seen.contains(resourcePair) && causal.contains(activityPair)) {
			//						double v = getBetaPowerSeries()[dist - 1];
			//						getResourcePairCount().adjustOrPutValue(resourcePair, v, v);
			//						seen.add(resourcePair);
			//					}
			//				}
			//			} else
			//				break;
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> activityPair = new Pair<>(trace.get(i - dist).getActivity(),
						trace.get(i).getActivity());
				Pair<String, String> resourcePair = new Pair<>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (i < trace.size() - 1) {
					if (causal.contains(activityPair)) {
						seen.add(resourcePair);
					}
				} else {
					if (!seen.contains(resourcePair) && causal.contains(activityPair)) {
						double v = getBetaPowerSeries()[dist - 1];
						getResourcePairCount().adjustOrPutValue(resourcePair, v, v);
						seen.add(resourcePair);
					}
				}
			}
		}
	}

	@Override
	protected void refreshAllNetworkValues() {
		getRelativeResourcePairValues().clear();
		for (Pair<String, String> p : getResourcePairCount().keySet()) {
			getRelativeResourcePairValues().put(p, getResourcePairCount().get(p) / getDivisor());
		}
	}

	@Override
	protected long measureMemoryConsumption() {
		//		long mem;
		try {
			return -1;
			//			mem = memoryMeter.measureDeep(this.getResourcePairCount());
			//			mem += memoryMeter.measureDeep(this.getDivisor());
			//			mem += memoryMeter.measureDeep(dfg);
			//			mem += memoryMeter.measureDeep(causal);
			//			return mem;
		} catch (Exception e) {
			// please try: -javaagent:<path to>/jamm.jar in JVM arguments
			return -1;
		}
	}

	protected void recalculate(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		//		getResourcePairCount().clear();
		//		getRelativeResourcePairValues().clear();
		//		setDivisor(0);
		//		super.initializeResourcePairCount(trie, trie.getRoot(), new ArrayList<ActivityResourcePair>());
		//		initializeRelativeResourcePairValues();
		initializeForTrie(trie);
	}

}
