package org.processmining.streamsocialnetworks.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ArrayUtils;
import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.SSNEdgeLabelStyle;
import org.processmining.streamsocialnetworks.models.SSNEdgeStyle;
import org.processmining.streamsocialnetworks.models.SSNLink;
import org.processmining.streamsocialnetworks.models.SSNNodeStyle;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetworkBuilder;

import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.set.hash.THashSet;

public abstract class AbstractStreamSocialNetworkBuilder
		implements StreamSocialNetworkBuilder<String, ActivityResourcePair, VertexImpl<ActivityResourcePair>> {

	private final static String THRESHOLD_KEY = "threshold";

	private TObjectDoubleMap<Pair<String, String>> avgWindow1 = null;
	private StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> latestTrieAfterInit = null;

	private final BidiMap<Pair<String, String>, SSNLink<String>> links = new DualHashBidiMap<>();

	private final Object monitor = new Object();

	private final List<String> parameterKeys = new ArrayList<>();

	private double threshold = 0.01;

	private StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie = null;
	private final StreamSocialNetwork<String> visualization;
	private List<TObjectDoubleMap<Pair<String, String>>> window1 = new ArrayList<>();
	private Collection<Pair<String, String>> window1Deleted = new THashSet<>();
	private List<TObjectDoubleMap<Pair<String, String>>> window2 = new ArrayList<>();

	private int windowSize = 50;

	public AbstractStreamSocialNetworkBuilder(final StreamSocialNetwork<String> network) {
		this.visualization = network;
		parameterKeys.add(THRESHOLD_KEY);
	}

	protected void clear() {
		links.clear();
		getNetwork().clear();
		window1.clear();
		window2.clear();
		avgWindow1 = null;
		window1Deleted.clear();
	}

	private TObjectDoubleMap<Pair<String, String>> computeAverage(
			final List<TObjectDoubleMap<Pair<String, String>>> window) {
		TObjectDoubleMap<Pair<String, String>> average = new TObjectDoubleHashMap<>();
		for (TObjectDoubleMap<Pair<String, String>> nw : window) {
			for (TObjectDoubleIterator<Pair<String, String>> it = nw.iterator(); it.hasNext();) {
				it.advance();
				average.adjustOrPutValue(it.key(), it.value(), it.value());
			}
		}
		// divide
		int divisor = window.size();
		for (TObjectDoubleIterator<Pair<String, String>> it = average.iterator(); it.hasNext();) {
			it.advance();
			it.setValue(it.value() / divisor);
		}
		return average;
	}

	private TObjectDoubleMap<String> computeNormalizedResourceValues(TObjectDoubleMap<Pair<String, String>> avgWindow2,
			TObjectDoubleMap<Pair<String, String>> relativeChanges) {
		TObjectDoubleMap<String> relativeResValues = new TObjectDoubleHashMap<>();
		TObjectDoubleMap<String> absoluteResValues = new TObjectDoubleHashMap<>();
		for (TObjectDoubleIterator<Pair<String, String>> it = relativeChanges.iterator(); it.hasNext();) {
			it.advance();
			if (it.value() == Double.MIN_VALUE) {
				absoluteResValues.adjustOrPutValue(it.key().getFirst(), 0, 0);
			} else {
				double edgeVal = avgWindow2.get(it.key());
				absoluteResValues.adjustOrPutValue(it.key().getFirst(), edgeVal, edgeVal);
				absoluteResValues.adjustOrPutValue(it.key().getSecond(), edgeVal, edgeVal);
			}
		}
		Collection<Double> absoluteValues = Arrays.asList(ArrayUtils.toObject(absoluteResValues.values()));
		double max = Collections.max(absoluteValues);
		double min = Collections.min(absoluteValues);
		double div = max - min;
		for (TObjectDoubleIterator<String> it = absoluteResValues.iterator(); it.hasNext();) {
			it.advance();
			double relative = (it.value() - min) / div;
			relativeResValues.put(it.key(), relative);
		}
		return relativeResValues;
	}

	private TObjectDoubleMap<Pair<String, String>> computeRelativeChanges(
			TObjectDoubleMap<Pair<String, String>> avgWindow1, TObjectDoubleMap<Pair<String, String>> avgWindow2) {
		TObjectDoubleMap<Pair<String, String>> relativeChanges = new TObjectDoubleHashMap<>();
		for (TObjectDoubleIterator<Pair<String, String>> it = avgWindow2.iterator(); it.hasNext();) {
			it.advance();
			if (avgWindow1.containsKey(it.key())) {
				double val = (it.value() / avgWindow1.get(it.key())) - 1;
				relativeChanges.put(it.key(), val);
			} else {
				relativeChanges.put(it.key(), Double.MAX_VALUE);
			}
		}
		Set<Pair<String, String>> leavers = new THashSet<>(avgWindow1.keySet());
		leavers.removeAll(avgWindow2.keySet());
		for (Pair<String, String> leaver : leavers) {
			relativeChanges.put(leaver, Double.MIN_VALUE);
		}
		return relativeChanges;
	}

	private SSNEdgeStyle getEdgeStyleBasedOnNormalizedValue(double val) {
		if (val < 0.2) {
			return SSNEdgeStyle.VERY_LOW;
		} else if (val < 0.4) {
			return SSNEdgeStyle.LOW;
		} else if (val < 0.6) {
			return SSNEdgeStyle.NEUTRAL;
		} else if (val < 0.8) {
			return SSNEdgeStyle.HIGH;
		} else {
			return SSNEdgeStyle.VERY_HIGH;
		}
	}

	protected StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> getLatestTrieAfterInit() {
		return latestTrieAfterInit;
	}

	protected Object getMonitor() {
		return monitor;
	}

	public StreamSocialNetwork<String> getNetwork() {
		return visualization;
	}

	protected BidiMap<Pair<String, String>, SSNLink<String>> getNetworkLinks() {
		return links;
	}

	private SSNNodeStyle getNodeStyleBasedOnNormalizedValue(double val) {
		if (val < 0.2) {
			return SSNNodeStyle.VERY_LOW;
		} else if (val < 0.4) {
			return SSNNodeStyle.LOW;
		} else if (val < 0.6) {
			return SSNNodeStyle.NEUTRAL;
		} else if (val < 0.8) {
			return SSNNodeStyle.HIGH;
		} else {
			return SSNNodeStyle.VERY_HIGH;
		}
	}

	private SSNLink<String> getOrAddLink(final Pair<String, String> pair) {
		SSNLink<String> link = links.containsKey(pair) ? links.get(pair)
				: new SSNLink<String>(pair.getFirst(), pair.getSecond(), 0);
		if (!links.containsKey(pair)) {
			links.put(pair, link);
		}
		SSNLink<String> linkInNetwork = getNetwork().getLink(pair.getFirst(), pair.getSecond());
		if (linkInNetwork == null) {
			getNetwork().addLink(link);
		}
		getNetwork().setNodeLabel(pair.getFirst(), pair.getFirst());
		getNetwork().setNodeLabel(pair.getSecond(), pair.getSecond());
		return link;
	}

	public List<String> getParameterKeys() {
		return parameterKeys;
	}

	public List<String> getParameterValues() {
		List<String> values = new ArrayList<>();
		values.add(Double.toString(threshold));
		return values;
	}

	public double getThreshold() {
		return threshold;
	}

	public StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> getTrie() {
		return trie;
	}

	@SuppressWarnings("unchecked")
	public final StreamSocialNetwork<String> init(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		synchronized (getMonitor()) {
			this.trie = trie;
			initializeForTrie(trie);
			updateWindows(initializeNetwork());
			latestTrieAfterInit = (StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>) trie.clone();
			sanityCheck();
			return getNetwork();
		}
	}

	protected abstract void initializeForTrie(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie);

	protected abstract TObjectDoubleMap<Pair<String, String>> initializeNetwork();

	protected abstract void processNewlyAddedEdgeInTrie(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie);

	protected abstract TObjectDoubleMap<Pair<String, String>> processRemovedCases(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges);

	protected abstract void refresh();

	/**
	 * mainly intended for debugging purposes.
	 */
	protected abstract void sanityCheck();

	public void setLatestTrieAfterInit(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> latestTrieAfterInit) {
		this.latestTrieAfterInit = latestTrieAfterInit;
	}

	public void setParameter(final String key, final String value) {
		synchronized (getMonitor()) {
			if (key.equals(THRESHOLD_KEY)) {
				threshold = Double.valueOf(value);
				refresh();
			}
		}
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	protected void setTrie(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie) {
		this.trie = trie;
	}

	private void shiftWindows() {
		if (avgWindow1 == null) {
			avgWindow1 = computeAverage(window1);
		}
		TObjectDoubleMap<Pair<String, String>> avgWindow2 = computeAverage(window2);
		TObjectDoubleMap<Pair<String, String>> relativeChanges = computeRelativeChanges(avgWindow1, avgWindow2);
		updateNetworkWithNewWindow(avgWindow2, relativeChanges);
		window1Deleted.removeAll(avgWindow2.keySet());
		for (Pair<String, String> remove : window1Deleted) {
			getNetwork().removeLink(links.get(remove));
			links.remove(remove);
		}
		window1Deleted.clear();
		for (TObjectDoubleIterator<Pair<String, String>> it = relativeChanges.iterator(); it.hasNext();) {
			it.advance();
			if (it.value() == Double.MIN_VALUE) {
				window1Deleted.add(it.key());
			}
		}
		window1 = window2;
		window2 = new ArrayList<TObjectDoubleMap<Pair<String, String>>>();
		avgWindow1 = avgWindow2;
	}

	public StreamSocialNetwork<String> update(
			final StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			final EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdgeInCaseTrie,
			final Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedPaths) {
		synchronized (getMonitor()) {
			setTrie(trie);
			if (latestTrieAfterInit == null || !latestTrieAfterInit.equals(trie)) {
				processNewlyAddedEdgeInTrie(trie, newEdgeInCaseTrie);
				TObjectDoubleMap<Pair<String, String>> newNw = processRemovedCases(trie, removedPaths);
				refreshAllNetworkValues();
				updateWindows(newNw);
				latestTrieAfterInit = null;
				return getNetwork();
			}
			return getNetwork();
		}
	}

	/**
	 * for instrumentation / experimental use
	 */
	protected abstract void refreshAllNetworkValues();

	/**
	 * for instrumentation / experimental use
	 */
	protected abstract long measureMemoryConsumption();

	protected abstract long getNumResourcePairsActiveInDataStructure();

	private <T> TObjectDoubleMap<T> normalize(final TObjectDoubleMap<T> input, final TObjectDoubleMap<T> normalized) {
		Collection<Double> absoluteValues = Arrays.asList(ArrayUtils.toObject(input.values()));
		double max = Collections.max(absoluteValues);
		double min = Collections.min(absoluteValues);
		double div = max - min;
		for (TObjectDoubleIterator<T> it = input.iterator(); it.hasNext();) {
			it.advance();
			double value = (it.value() - min) / div;
			normalized.put(it.key(), value);
		}
		return normalized;
	}

	private void updateNetworkWithNewWindow(TObjectDoubleMap<Pair<String, String>> avgWindow2,
			TObjectDoubleMap<Pair<String, String>> relativeChanges) {
		TObjectDoubleMap<Pair<String, String>> normalizedAvgW2 = normalize(avgWindow2,
				new TObjectDoubleHashMap<Pair<String, String>>());
		// decorate edges...
		for (TObjectDoubleIterator<Pair<String, String>> it = relativeChanges.iterator(); it.hasNext();) {
			it.advance();
			if (avgWindow2.get(it.key()) > threshold || it.value() == Double.MIN_VALUE) {
				SSNLink<String> link = getOrAddLink(it.key());
				if (it.value() == Double.MIN_VALUE) {
					getNetwork().setLinkValue(link, 0d);
					getNetwork().setLinkStyle(link, SSNEdgeStyle.DELETED);
					getNetwork().setLinkLabel(link, "X");
					getNetwork().setLinkLabelStyle(link, SSNEdgeLabelStyle.DOWN);
				} else {
					getNetwork().setLinkValue(link, avgWindow2.get(it.key()));
					double normalizedLinkValue = normalizedAvgW2.get(it.key());
					if (it.value() == Double.MAX_VALUE) {
						getNetwork().setLinkStyle(link, getEdgeStyleBasedOnNormalizedValue(normalizedLinkValue));
						getNetwork().setLinkLabel(link, "new");
						getNetwork().setLinkLabelStyle(link, SSNEdgeLabelStyle.UP);
					} else {
						getNetwork().setLinkStyle(link, getEdgeStyleBasedOnNormalizedValue(normalizedLinkValue));
						boolean up = it.value() > 0;
						String label = up ? "\u2191\t\t\t" : "\u2193\t\t\t";
						getNetwork().setLinkLabel(link, label);
						if (up) {
							getNetwork().setLinkLabelStyle(link, SSNEdgeLabelStyle.UP);
						} else {
							getNetwork().setLinkLabelStyle(link, SSNEdgeLabelStyle.DOWN);
						}
					}
				}
			} else if (avgWindow1.containsKey(it.key())) {
				// no longer above threshold
				if (links.containsKey(it.key())) {
					getNetwork().removeLink(links.get(it.key()));
					links.remove(it.key());
				}
			}
		}
		TObjectDoubleMap<String> resourceNormalized = computeNormalizedResourceValues(avgWindow2, relativeChanges);
		for (TObjectDoubleIterator<String> it = resourceNormalized.iterator(); it.hasNext();) {
			it.advance();
			getNetwork().setNodeStyle(it.key(), getNodeStyleBasedOnNormalizedValue(it.value()));
		}
	}

	private void updateWindows(final TObjectDoubleMap<Pair<String, String>> updatedNetwork) {
		if (window1.size() < windowSize) {
			window1.add(updatedNetwork);
		} else if (window2.size() < windowSize) {
			window2.add(updatedNetwork);
		} else if (window2.size() % windowSize == 0) {
			shiftWindows();
		}
	}

}
