package org.processmining.streamsocialnetworks.models;

import java.util.Collection;
import java.util.List;

import org.processmining.eventstream.readers.trie.EdgeImpl;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;

/**
 * A StreamSocialNetworkBuilder is able to build some social network based on a
 * stream based case trie. Given such trie it can initialize the network.
 * Moreover the builder is able to perform some (incremental) update function
 * given that the trie grows.
 * 
 * @author svzelst
 *
 * @param <N>
 * @param <T>
 * @param <V>
 */
public interface StreamSocialNetworkBuilder<N, T, V extends VertexImpl<T>> {

	StreamSocialNetwork.Type getType();

	StreamSocialNetwork<N> getNetwork();

	StreamSocialNetwork<N> init(StreamTrieImpl<T, V> caseTrie);

	/**
	 * Signal the builder to update the social network it maintains
	 * 
	 * @param caseTrie
	 * @param newEdgeInCaseTrie
	 * @param reducedPaths
	 * @return
	 */
	StreamSocialNetwork<N> update(StreamTrieImpl<T, V> caseTrie, EdgeImpl<T, V> newEdgeInCaseTrie,
			Collection<List<EdgeImpl<T, V>>> reducedPaths);

	List<String> getParameterKeys();

	List<String> getParameterValues();

	void setParameter(String key, String value);

	StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> getTrie();

}
