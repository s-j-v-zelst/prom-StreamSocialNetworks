package org.processmining.streamsocialnetworks.algorithms;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.StreamTrieUtils;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;
import org.processmining.streamsocialnetworks.models.TripleImpl;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class AbsoluteCausalInBetweenBuilderImpl extends AbsoluteInbetweenBuilderImpl {

	private final Map<TripleImpl<String, String, String>, TObjectDoubleMap<Pair<String, String>>> activityBasedContribution = new HashMap<>();
	private final Collection<Pair<String, String>> causal = new HashSet<>();
	private final TObjectIntMap<Pair<String, String>> dfg = new TObjectIntHashMap<>();

	public AbsoluteCausalInBetweenBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	protected void adjustActivityBasedContribution(List<ActivityResourcePair> trace, int dist, int i, int j,
			int traceCardinality, boolean updateRelative) {
		if (trace.get(i).getResource().equals(trace.get(i + dist).getResource())) {
			Pair<String, String> resourcePair = new Pair<>(trace.get(i).getResource(), trace.get(j).getResource());
			TripleImpl<String, String, String> activityTriple = new TripleImpl<String, String, String>(
					trace.get(i).getActivity(), trace.get(j).getActivity(), trace.get(i + dist).getActivity());
			double value = getBetaPowerSeries()[dist - 2] * traceCardinality;
			if (!activityBasedContribution.containsKey(activityTriple)) {
				activityBasedContribution.put(activityTriple, new TObjectDoubleHashMap<Pair<String, String>>());
			}
			activityBasedContribution.get(activityTriple).adjustOrPutValue(resourcePair, value, value);
			if (updateRelative && causal.contains(activityTriple.getPairOfFirstAndSecond())
					&& causal.contains(activityTriple.getPairOfSecondAndThird())) {
				getResourcePairCount().adjustOrPutValue(resourcePair, value, value);
				double newRelativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
				getRelativeResourcePairValues().put(resourcePair, newRelativeValue);
			}
		}
	}

	@Override
	protected void clear() {
		super.clear();
		activityBasedContribution.clear();
		causal.clear();
		dfg.clear();
	}

	private Collection<TripleImpl<String, String, String>> getTriplesContainingDfRelation(Pair<String, String> df) {
		Collection<TripleImpl<String, String, String>> result = new HashSet<>();
		for (TripleImpl<String, String, String> t : activityBasedContribution.keySet()) {
			if (t.getPairOfFirstAndSecond().equals(df) || t.getPairOfSecondAndThird().equals(df)) {
				result.add(t);
			}
		}
		return result;
	}

	public Type getType() {
		return Type.ABSOLUTE_CAUSAL_INBETWEEN;
	}

	@Override
	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		updateDivisorForTrace(trace, cardinality);
		updateDfgAndCausalForTraceAddition(trace, cardinality);
		for (int dist = 2; dist <= getK(); dist++) {
			for (int i = 0; i < trace.size() - dist; i++) {
				for (int j = i + 1; j < i + dist; j++) {
					adjustActivityBasedContribution(trace, dist, i, j, cardinality, false);
				}
			}
		}
	}

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		if (!(newEdgeInCaseTrie.getFrom().equals(trie.getRoot()))) {
			List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
					newEdgeInCaseTrie.getTo(), getK() + 1, false);
			incrementalDivisorUpdate(trace);
			updateResourcePairCountsBasedOnNewDfgRelation(
					new Pair<String, String>(newEdgeInCaseTrie.getFrom().getVertexObject().getActivity(),
							newEdgeInCaseTrie.getTo().getVertexObject().getActivity()));
			for (int dist = 2; dist <= getK(); dist++) {
				int i = trace.size() - dist - 1;
				for (int j = i + 1; j < i + dist; j++) {
					adjustActivityBasedContribution(trace, dist, i, j, 1, false);
				}
			}
		}
	}

	private int updateDfgAndCausalForPairAddition(Pair<String, String> dfgPair, int numOcc) {
		dfg.adjustOrPutValue(dfgPair, numOcc, numOcc);
		Pair<String, String> reverse = new Pair<String, String>(dfgPair.getSecond(), dfgPair.getFirst());
		if (!dfg.containsKey(reverse) && !causal.contains(dfgPair)) {
			causal.add(dfgPair);
			return 1;
		} else if (causal.contains(reverse)) {
			causal.remove(reverse);
			return -1;
		}
		return 0;
	}

	private int updateDfgAndCausalForPairRemoval(Pair<String, String> dfgPair, int numOcc) {
		dfg.adjustOrPutValue(dfgPair, numOcc, numOcc);
		if (dfg.get(dfgPair) == 0) {
			if (!(causal.contains(dfgPair))) {
				causal.add(new Pair<String, String>(dfgPair.getSecond(), dfgPair.getFirst()));
				return 1;
			} else {
				causal.remove(dfgPair);
				return -1;
			}
		}
		return 0;
	}

	protected void updateDfgAndCausalForTraceAddition(List<ActivityResourcePair> trace, int cardinality) {
		for (int i = 1; i < trace.size(); i++) {
			updateDfgAndCausalForPairAddition(
					new Pair<String, String>(trace.get(i - 1).getActivity(), trace.get(i).getActivity()), cardinality);
		}
	}

	@Override
	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		for (int i = 1; i < trace.size(); i++) {
			Pair<String, String> activityPair = new Pair<>(trace.get(i - 1).getActivity(), trace.get(i).getActivity());
			int delta = updateDfgAndCausalForPairRemoval(activityPair, -1);
			if (delta > 0) { // reverse became causal
				updateResourcePairCountBasedOnCausalAddition(
						new Pair<>(activityPair.getSecond(), activityPair.getFirst()), true);
			} else if (delta < 0) {
				updateResourcePairCountBasedOnCausalRemoval(activityPair, true);
			}
		}
		for (int dist = 2; dist <= Math.min(trace.size() - 1, getK()); dist++) {
			for (int i = 0; i < trace.size() - dist; i++) {
				for (int j = i + 1; j < i + dist; j++) {
					adjustActivityBasedContribution(trace, dist, i, j, -1, true);
				}
			}
		}
	}

	protected void updateResourcePairCountBasedOnCausalAddition(Pair<String, String> newCausalRel,
			boolean refreshRelative) {
		for (TripleImpl<String, String, String> t : getTriplesContainingDfRelation(newCausalRel)) {
			if (causal.contains(t.getPairOfFirstAndSecond()) && causal.contains(t.getPairOfSecondAndThird())) {
				TObjectDoubleMap<Pair<String, String>> dfgContribution = activityBasedContribution.get(t);
				for (Pair<String, String> resourcePair : dfgContribution.keySet()) {
					getResourcePairCount().adjustValue(resourcePair, dfgContribution.get(resourcePair));
				}
			}
		}
		refresh();
	}

	protected void updateResourcePairCountBasedOnCausalRemoval(Pair<String, String> removedCausalRel,
			boolean refreshRelative) {
		for (TripleImpl<String, String, String> t : getTriplesContainingDfRelation(removedCausalRel)) {
			// if some triple still has one causal component, it was valid, though is no longer valid.
			if (causal.contains(t.getPairOfFirstAndSecond()) || causal.contains(t.getPairOfSecondAndThird())) {
				TObjectDoubleMap<Pair<String, String>> dfgContribution = activityBasedContribution.get(t);
				for (Pair<String, String> resourcePair : dfgContribution.keySet()) {
					// remove the contribution of this dfg relation
					getResourcePairCount().adjustValue(resourcePair, -1 * dfgContribution.get(resourcePair));
				}
			}
		}
		refresh();
	}

	private void updateResourcePairCountsBasedOnNewDfgRelation(Pair<String, String> newDfg) {
		int delta = updateDfgAndCausalForPairAddition(newDfg, 1);
		if (delta < 0) { // the "reverse" is removed from the causal set.
			updateResourcePairCountBasedOnCausalRemoval(new Pair<>(newDfg.getSecond(), newDfg.getFirst()), true);
		} else if (delta > 0) { // a causal relation was added
			updateResourcePairCountBasedOnCausalAddition(newDfg, true);
		}
	}

}
