package org.processmining.streamsocialnetworks.models;

import java.util.Collection;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.streamsocialnetworks.factories.StreamSocialNetworkFactory;

public class StreamSocialNetworkManagerImpl {

	private StreamSocialNetworkBuilder<String, ActivityResourcePair, VertexImpl<ActivityResourcePair>> builder;

	public StreamSocialNetworkBuilder<String, ActivityResourcePair, VertexImpl<ActivityResourcePair>> getBuilder() {
		return builder;
	}

	private StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> lastTrieAfterInit = null;

	@SuppressWarnings("unchecked")
	public StreamSocialNetwork<String> setSocialNetworkType(StreamSocialNetwork.Type type,
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> caseTrie) {
		StreamSocialNetwork<String> ssn = StreamSocialNetworkFactory
				.constructEmptyGraphStreamSingleGraphStreamSocialNetwork("ssn_" + type.toString());
		builder = StreamSocialNetworkFactory.constructStreamSocialNetworkBuilder(type, ssn);
		synchronized (caseTrie.getLock()) {
			StreamSocialNetwork<String> network = builder.init(caseTrie);
			lastTrieAfterInit = (StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>) caseTrie
					.clone();
			return network;
		}
	}

	public StreamSocialNetwork<String> updateCurrentSocialNetwork(
			StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> caseTrie,
			EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> newEdge,
			Collection<List<EdgeImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>>> removedEdges) {
		if (lastTrieAfterInit != null) {
			//			System.out.println("Swap occurred");
			//			System.out.println("Trie used for init: nodes: " + StreamTrieUtils.countNodesInTrie(lastTrieAfterInit)
			//					+ " sum_nodes: " + StreamTrieUtils.countCummulativeNodeCount(lastTrieAfterInit));
			//			System.out.println("New trie: " + StreamTrieUtils.countNodesInTrie(caseTrie) + " sum_nodes: "
			//					+ StreamTrieUtils.countCummulativeNodeCount(caseTrie));
			//			System.out.println(newEdge.toString());
			//			System.out.println(removedEdges.toString());
		}
		if (lastTrieAfterInit == null || !lastTrieAfterInit.equals(caseTrie)) {
			//			System.out.println("Check Decided that the tries are NOT equal!");
			lastTrieAfterInit = null;
			return builder.update(caseTrie, newEdge, removedEdges);
		} else {
			return builder.getNetwork();
		}

	}

}
