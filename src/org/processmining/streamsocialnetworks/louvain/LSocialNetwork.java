package org.processmining.streamsocialnetworks.louvain;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public class LSocialNetwork extends TObjectDoubleHashMap<ResourcesPair> {

	public enum Type {
		WORKING_TOGETHER, SIMILAR_TASK, HANDOVER;
	}

	private final Type networkType;

	public LSocialNetwork(final Type networkType) {
		this.networkType = networkType;
	}

	public Type getNetworkType() {
		return networkType;
	}

}
