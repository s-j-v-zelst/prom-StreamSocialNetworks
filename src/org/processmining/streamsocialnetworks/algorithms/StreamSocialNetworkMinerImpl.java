package org.processmining.streamsocialnetworks.algorithms;

import java.util.Collection;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamCaseTrieAlgorithmImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetworkManagerImpl;
import org.processmining.streamsocialnetworks.parameters.StreamSocialNetworkMinerParametersImpl;
import org.processmining.streamsocialnetworks.visualizers.StreamSocialNetworkManagerVisualizerImpl;

public class StreamSocialNetworkMinerImpl extends
		StreamCaseTrieAlgorithmImpl<ActivityResourcePair, StreamSocialNetworkManagerImpl, StreamSocialNetworkMinerParametersImpl> {

	private final StreamSocialNetworkManagerImpl mgr;

	public StreamSocialNetworkMinerImpl(String name, StreamSocialNetworkMinerParametersImpl parameters,
			StreamSocialNetworkManagerImpl mgr,
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> graph) {
		super(name, new StreamSocialNetworkManagerVisualizerImpl("SSN Manager", mgr, parameters), parameters, graph);
		this.mgr = mgr;
	}

	public Class<XSEvent> getTopic() {
		return XSEvent.class;
	}

	protected StreamSocialNetworkManagerImpl computeCurrentResult() {
		return mgr;
	}

	protected String castNewlyReceivedXAttribute(XAttribute attr) {
		return attr.toString();
	}

	protected ActivityResourcePair createTargetObjectFromEvent(XSEvent event) {
		String activity = event.get(getParameters().getActivityIdentifier()).toString();
		String resource = event.get(getParameters().getResourceIdentifier()).toString();
		return new ActivityResourcePair(activity, resource);
	}

	@Override // in synchronized block due to parent
	protected void handleNextTrie(StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdge,
			Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		mgr.updateCurrentSocialNetwork(trie, newEdge, removedEdges);
		getVisualization().updateVisualization(mgr);
	}

}
