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

public class AbsoluteHOWSSNBuilderImpl extends AbstractHOWInBetweenSSNBuilderImpl {

	//	private final MemoryMeter memoryMeter = new MemoryMeter();

	public AbsoluteHOWSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	public Type getType() {
		return Type.ABSOLUTE_HANDOVER_OF_WORK;
	}

	protected void incrementalDivisorUpdate(int formerTraceLength) {
		setDivisor(getDivisor() + getBetaPowerSeriesSum()[Math.min(formerTraceLength - 1, getK() - 1)]);
	}

	@Override
	protected void initializeResourcePairCountAndDivisorForTrace(List<ActivityResourcePair> trace,
			int traceCardinality) {
		updateDivisorForTrace(trace, traceCardinality);
		for (int dist = 1; dist <= getK(); dist++) {
			for (int i = dist; i < trace.size(); i++) {
				double v = getBetaPowerSeries()[dist - 1] * traceCardinality;
				getResourcePairCount().adjustOrPutValue(
						new Pair<>(trace.get(i - dist).getResource(), trace.get(i).getResource()), v, v);
			}
		}
	}

	protected void processNewlyAddedEdgeInTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie) {
		if (!newEdgeInCaseTrie.getFrom().equals(trie.getRoot())) {
			incrementalDivisorUpdate(newEdgeInCaseTrie.getFrom().getDepth());
			List<ActivityResourcePair> trace = StreamTrieUtils.constructSequenceEndingInVertex(trie,
					newEdgeInCaseTrie.getTo(), getK() + 1, false);
			String toRes = trace.get(trace.size() - 1).getResource();
			for (int dist = 1; dist <= Math.min(trace.size() - 1, getK()); dist++) {
				processNewRelationOfDistance(
						new Pair<String, String>(trace.get(trace.size() - 1 - dist).getResource(), toRes), dist);
			}
		}
		//		Disable sanity check for instrumentation
		//		sanityCheck();
	}

	protected void processNewRelationOfDistance(Pair<String, String> resourcePair, int dist) {
		double updateAbsoluteValue = getBetaPowerSeries()[dist - 1];
		getResourcePairCount().adjustOrPutValue(resourcePair, updateAbsoluteValue, updateAbsoluteValue);
		double newRelativeValue = getResourcePairCount().get(resourcePair) / getDivisor();
		getRelativeResourcePairValues().put(resourcePair, newRelativeValue);
	}

	protected void refreshAllNetworkValues() {
		getRelativeResourcePairValues().clear();
		for (Pair<String, String> p : getResourcePairCount().keySet()) {
			getRelativeResourcePairValues().put(p, getResourcePairCount().get(p) / getDivisor());
		}
	}

	protected void sanityCheck() {
		if (!getRelativeResourcePairValues().keySet().isEmpty()) {
			refresh();
			double sum = 0;
			for (Pair<String, String> pair : getRelativeResourcePairValues().keySet()) {
				sum += getRelativeResourcePairValues().get(pair);
			}
			if (sum < 0.999 || sum > 1.0000001) {
				System.out.println("Sanity check failded: " + sum);
			}
		}
	}

	protected void updateDivisorForRemovedCase(List<ActivityResourcePair> trace) {
		updateDivisorForTrace(trace, -1);
	}

	protected void updateDivisorForTrace(List<ActivityResourcePair> trace, int traceCardinality) {
		for (int i = 1; i <= Math.min(trace.size() - 1, getK()); i++) {
			setDivisor(getDivisor() + getBetaPowerSeries()[i - 1] * (trace.size() - i) * traceCardinality);
		}
	}

	protected void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace) {
		for (int dist = 1; dist <= Math.min(trace.size() - 1, getK()); dist++) {
			double v = getBetaPowerSeries()[dist - 1] * -1;
			for (int i = dist; i < trace.size(); i++) {
				Pair<String, String> pair = new Pair<>(trace.get(i - dist).getResource(), trace.get(i).getResource());
				getResourcePairCount().adjustOrPutValue(pair, v, v);
				if (getResourcePairCount().get(pair) == 0.0) {
					getResourcePairCount().remove(pair);
				} else {
					double newRelativeValue = getResourcePairCount().get(pair) / getDivisor();
					getRelativeResourcePairValues().put(pair, newRelativeValue);
				}
			}
		}
	}

	protected long measureMemoryConsumption() {
		//		long mem;
		try {
			//			mem = memoryMeter.measureDeep(this.getResourcePairCount());
			//			mem += memoryMeter.measureDeep(this.getDivisor());
			return -1;
		} catch (Exception e) {
			// please try: -javaagent:<path to>/jamm.jar in JVM arguments
			return -1;
		}

	}

}
