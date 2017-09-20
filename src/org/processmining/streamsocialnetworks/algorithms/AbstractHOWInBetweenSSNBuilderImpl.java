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

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public abstract class AbstractHOWInBetweenSSNBuilderImpl extends AbstractStreamSocialNetworkBuilder {

	private final static String BETA_KEY = "beta";
	private final static String K_KEY = "k";

	private double beta = 1;
	private double[] betaPowerSeries;
	private double[] betaPowerSeriesSum;

	private double divisor = 0;

	private int k = 1;

	private final TObjectDoubleHashMap<Pair<String, String>> relativeResourcePairValues = new TObjectDoubleHashMap<>(
			5000);
	private final TObjectDoubleHashMap<Pair<String, String>> resourcePairCount = new TObjectDoubleHashMap<>(5000);

	public AbstractHOWInBetweenSSNBuilderImpl(final StreamSocialNetwork<String> network) {
		super(network);
		getParameterKeys().add(BETA_KEY);
		getParameterKeys().add(K_KEY);
		relativeResourcePairValues.setAutoCompactionFactor(0);
		resourcePairCount.setAutoCompactionFactor(0);
	}

	@Override
	protected void clear() {
		super.clear();
		betaPowerSeries = null;
		betaPowerSeriesSum = null;
		resourcePairCount.clear();
		relativeResourcePairValues.clear();
		divisor = 0;
	}

	public double getBeta() {
		return beta;
	}

	public double[] getBetaPowerSeries() {
		return betaPowerSeries;
	}

	public double[] getBetaPowerSeriesSum() {
		return betaPowerSeriesSum;
	}

	protected double getDivisor() {
		return divisor;
	}

	public int getK() {
		return k;
	}

	public List<String> getParameterValues() {
		List<String> values = super.getParameterValues();
		values.add(Double.toString(beta));
		values.add(Integer.toString(k));
		return values;
	}

	protected TObjectDoubleMap<Pair<String, String>> getRelativeResourcePairValues() {
		return relativeResourcePairValues;
	}

	protected TObjectDoubleMap<Pair<String, String>> getResourcePairCount() {
		return resourcePairCount;
	}

	public void initializeForTrie(final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		synchronized (getMonitor()) {
			clear();
			preCalculateBetaValues(getK());
			initializeResourcePairCount(trie, trie.getRoot(), new ArrayList<ActivityResourcePair>());
			initializeRelativeResourcePairValues();
		}
	}

	@Override
	protected TObjectDoubleMap<Pair<String, String>> initializeNetwork() {
		return new TObjectDoubleHashMap<>(relativeResourcePairValues);
	}

	protected void initializeRelativeResourcePairValues() {
		refresh();
	}

	protected void initializeResourcePairCount(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			VertexImpl<ActivityResourcePair> vertex, final List<ActivityResourcePair> trace) {
		if (!trie.getOutEdges(vertex).isEmpty()) {
			for (EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> e : trie.getOutEdges(vertex)) {
				VertexImpl<ActivityResourcePair> newVertex = e.getTo();
				trace.add(newVertex.getVertexObject());
				initializeResourcePairCount(trie, newVertex, trace);
				trace.remove(trace.size() - 1);
			}
			int numPointers;
			if (trace.size() > 1 && (numPointers = StreamTrieUtils.numberOfPointersToVertex(trie, vertex)) > 0) {
				initializeResourcePairCountAndDivisorForTrace(trace, numPointers);
			}
		} else {
			initializeResourcePairCountAndDivisorForTrace(trace, vertex.getCount());
		}
	}

	protected abstract void initializeResourcePairCountAndDivisorForTrace(final List<ActivityResourcePair> trace,
			final int cardinality);

	protected void preCalculateBetaValues(final int k) {
		betaPowerSeries = new double[getK() + 1];
		betaPowerSeriesSum = new double[getK() + 1];
		betaPowerSeries[0] = 1.0;
		betaPowerSeriesSum[0] = 1.0;
		for (int i = 1; i <= getK(); i++) {
			betaPowerSeries[i] = Math.pow(getBeta(), i);
			betaPowerSeriesSum[i] = betaPowerSeriesSum[i - 1] + betaPowerSeries[i];
		}
	}

	protected TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		for (List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>> edgeSequence : removedEdges) {
			List<ActivityResourcePair> trace = StreamTrieUtils.constructTraceFromListOfEdges(edgeSequence,
					Integer.MAX_VALUE, true, true);
			updateDivisorForRemovedCase(trace);
			updateResourcePairCountAndRelativeValuesForRemovedCase(trace);
		}
		refresh();
		return new TObjectDoubleHashMap<>(relativeResourcePairValues);
	}

	@Override
	protected void refresh() {
		synchronized (getMonitor()) {
			getRelativeResourcePairValues().clear();
			for (Pair<String, String> resourcePair : getResourcePairCount().keySet()) {
				double relative = getResourcePairCount().get(resourcePair) / getDivisor();
				getRelativeResourcePairValues().put(resourcePair, relative);
			}
		}
	}

	public void setBeta(final double beta) {
		this.beta = beta;
	}

	public void setBetaPowerSeriesSum(double[] betaPowerSeriesSum) {
		this.betaPowerSeriesSum = betaPowerSeriesSum;
	}

	protected void setDivisor(final double divisor) {
		this.divisor = divisor;
	}

	public void setK(int k) {
		this.k = k;
	}

	@Override
	public void setParameter(final String key, final String value) {
		synchronized (getMonitor()) {
			super.setParameter(key, value);
			if (key.equals(BETA_KEY)) {
				beta = Double.valueOf(value);
				clear();
				synchronized (getTrie().getLock()) {
					init(getTrie());
				}
			} else if (key.equals(K_KEY)) {
				k = Integer.valueOf(value);
				clear();
				synchronized (getTrie().getLock()) {
					init(getTrie());
				}
			}
		}
	}

	protected abstract void updateDivisorForRemovedCase(List<ActivityResourcePair> trace);

	protected abstract void updateResourcePairCountAndRelativeValuesForRemovedCase(List<ActivityResourcePair> trace);

	protected long getNumResourcePairsActiveInDataStructure() {
		return resourcePairCount.keySet().size();
	}
}
