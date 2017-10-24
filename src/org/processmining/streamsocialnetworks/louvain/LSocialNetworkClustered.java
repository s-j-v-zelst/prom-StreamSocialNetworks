package org.processmining.streamsocialnetworks.louvain;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public class LSocialNetworkClustered extends TObjectDoubleHashMap<NodesPair> {

	public enum Type {
		WORKING_TOGETHER, SIMILAR_TASK, HANDOVER;
	}

	private final Type networkType;

	public LSocialNetworkClustered(final Type networkType) {
		this.networkType = networkType;
	}

	public Type getNetworkType() {
		return networkType;
	}

}
