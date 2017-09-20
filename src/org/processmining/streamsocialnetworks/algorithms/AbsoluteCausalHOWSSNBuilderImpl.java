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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

public class AbsoluteCausalHOWSSNBuilderImpl extends AbsoluteHOWSSNBuilderImpl {

	private Map<Pair<String, String>, TObjectDoubleMap<Pair<String, String>>> activityBasedContribution = new HashMap<>();
	private Collection<Pair<String, String>> causal = new HashSet<>();
	private TObjectIntMap<Pair<String, String>> dfg = new TObjectIntHashMap<>();

	public AbsoluteCausalHOWSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	protected void adjustActivityPairBasedContribution(List<ActivityResourcePair> trace, int dist, int toIndex,
			int traceCardinality, boolean updateRelative) {
		Pair<String, String> resourcePair = new Pair<>(trace.get(toIndex - dist).getResource(),
				trace.get(toIndex).getResource());
		Pair<String, String> activityPair = new Pair<>(trace.get(toIndex - dist).getActivity(),
				trace.get(toIndex).getActivity());
		double value = getBetaPowerSeries()[dist - 1] * traceCardinality;
		if (!activityBasedContribution.containsKey(activityPair)) {
			activityBasedContribution.put(activityPair, new TObjectDoubleHashMap<Pair<String, String>>());
		}
		activityBasedContribution.get(activityPair).adjustOrPutValue(resourcePair, value, value);
		if (updateRelative && causal.contains(activityPair)) {
			getResourcePairCount().adjustOrPutValue(resourcePair, value, value);
			double newRelativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
			getRelativeResourcePairValues().put(resourcePair, newRelativeValue);
		}
	}

	@Override
	protected void clear() {
		super.clear();
		activityBasedContribution.clear();
		causal.clear();
		dfg.clear();
	}

	public Type getType() {
		return Type.ABSOLUTE_CAUSAL_HAND_OVER_OF_WORK;
	}

	@Override
	protected void initializeRelativeResourcePairValues() {
		for (Pair<String, String> causalRelation : causal) {
			TObjectDoubleMap<Pair<String, String>> causalContribution = activityBasedContribution.get(causalRelation);
			for (Pair<String, String> resourcePair : causalContribution.keySet()) {
				double adjust = causalContribution.get(resourcePair);
				getResourcePairCount().adjustOrPutValue(resourcePair, adjust, adjust);
			}
		}
		super.initializeRelativeResourcePairValues();
	}

	@Override
	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace, int cardinality) {
		updateDivisorForTrace(trace, cardinality);
		updateDfgAndCausalForTraceAddition(trace, cardinality);
		for (int dist = 1; dist <= getK(); dist++) {
			for (int i = dist; i < trace.size(); i++) {
				adjustActivityPairBasedContribution(trace, dist, i, cardinality, false);
			}
		}
	}

	@Override
	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		if (!(newEdgeInCaseTrie.getFrom().equals(trie.getRoot()))) {
			incrementalDivisorUpdate(newEdgeInCaseTrie.getFrom().getDepth());
			updateResourcePairCountsBasedOnNewDfgRelation(
					new Pair<String, String>(newEdgeInCaseTrie.getFrom().getVertexObject().getActivity(),
							newEdgeInCaseTrie.getTo().getVertexObject().getActivity()));
			List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
					newEdgeInCaseTrie.getTo(), getK() + 1, false);
			for (int dist = 1; dist <= Math.min(trace.size() - 1, getK()); dist++) {
				int i = trace.size() - 1;
				adjustActivityPairBasedContribution(trace, dist, i, 1, true);
			}
		}
	}

	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		for (int dist = 1; dist <= Math.min(trace.size() - 1, getK()); dist++) {
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> activityPair = new Pair<String, String>(trace.get(i - dist).getActivity(),
						trace.get(i).getActivity());
				Pair<String, String> resourcePair = new Pair<String, String>(trace.get(i - dist).getResource(),
						trace.get(i).getResource());
				if (dist == 1) {
					int delta = updateDfgAndCausalForPairRemoval(activityPair, -1);
					if (delta > 0) { // reverse became causal
						Pair<String, String> reverseActivityPair = new Pair<>(activityPair.getSecond(),
								activityPair.getFirst());
						updateAbsoluteCountBasedOnCausalAdditionOrRemoval(reverseActivityPair, 1);
						refresh();
					} else if (delta < 0) {
						updateAbsoluteCountBasedOnCausalAdditionOrRemoval(activityPair, -1);
						refresh();
					}
				}
				double remove = getBetaPowerSeries()[dist - 1] * -1;
				activityBasedContribution.get(activityPair).adjustValue(resourcePair, remove);
				if (causal.contains(activityPair)) {
					getResourcePairCount().adjustValue(resourcePair, remove);
					double newRelative = getResourcePairCount().get(resourcePair) / getDivisor();
					getRelativeResourcePairValues().put(resourcePair, newRelative);
				}
			}
		}
	}

	private void updateAbsoluteCountBasedOnCausalAdditionOrRemoval(Pair<String, String> activityPair,
			int multiplicationFactor) {
		TObjectDoubleMap<Pair<String, String>> reverseContribution = activityBasedContribution.get(activityPair);
		for (Pair<String, String> r : reverseContribution.keySet()) {
			double amount = reverseContribution.get(r) * multiplicationFactor;
			getResourcePairCount().adjustOrPutValue(r, amount, amount);
			double newRelative = getResourcePairCount().get(r) / getDivisor();
			getRelativeResourcePairValues().put(r, newRelative);
		}
	}

	/**
	 * 
	 * @param dfgPair
	 * @param numOcc
	 * @return 1 means added causal, -1 implies removed causal relation
	 */
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

	private void updateResourcePairCountsBasedOnNewDfgRelation(Pair<String, String> newDfg) {
		int delta = updateDfgAndCausalForPairAddition(newDfg, 1);
		if (delta < 0) { // a causal relation was removed.
			Pair<String, String> reverse = new Pair<>(newDfg.getSecond(), newDfg.getFirst());
			TObjectDoubleMap<Pair<String, String>> dfgContribution = activityBasedContribution.get(reverse);
			for (Pair<String, String> resourcePair : dfgContribution.keySet()) {
				// remove the contribution of this dfg relation
				getResourcePairCount().adjustValue(resourcePair, -1 * dfgContribution.get(resourcePair));
			}
			refresh();
		} else if (delta > 0) { // a causal relation was added
			if (!activityBasedContribution.containsKey(newDfg)) {
				activityBasedContribution.put(newDfg, new TObjectDoubleHashMap<Pair<String, String>>());
			}
			TObjectDoubleMap<Pair<String, String>> dfgContribution = activityBasedContribution.get(newDfg);
			for (Pair<String, String> resourcePair : dfgContribution.keySet()) {
				getResourcePairCount().adjustValue(resourcePair, dfgContribution.get(resourcePair));
			}
			refresh();
		}
	}

	@Override
	protected void sanityCheck() {
		if (!getRelativeResourcePairValues().keySet().isEmpty()) {
			refresh();
			double sum = 0;
			for (Pair<String, String> pair : getRelativeResourcePairValues().keySet()) {
				sum += getRelativeResourcePairValues().get(pair);
			}
			if (sum > 1.0000001) {
				System.out.println("Sanity check failded: " + sum);
			}
		}
	}

}
