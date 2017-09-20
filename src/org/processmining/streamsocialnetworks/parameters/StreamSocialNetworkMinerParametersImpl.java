package org.processmining.streamsocialnetworks.parameters;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.processmining.eventstream.readers.trie.StreamCaseTrieAlgorithmParameters;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

public class StreamSocialNetworkMinerParametersImpl extends StreamCaseTrieAlgorithmParameters {

	private boolean doSanityCheck = false;
	private String groupIdentifier = "xsevent:data:" + XOrganizationalExtension.KEY_GROUP;
	private String resourceIdentifier = "xsevent:data:" + XOrganizationalExtension.KEY_RESOURCE;
	private String roleIdentifier = "xsevent:data:" + XOrganizationalExtension.KEY_ROLE;
	private StreamSocialNetwork.Type defaultNetworkType = Type.ABSOLUTE_HANDOVER_OF_WORK;
	

	public String getGroupIdentifier() {
		return groupIdentifier;
	}

	public String getResourceIdentifier() {
		return resourceIdentifier;
	}

	public String getRoleIdentifier() {
		return roleIdentifier;
	}

	public boolean isDoSanityCheck() {
		return doSanityCheck;
	}

	public void setDoSanityCheck(boolean doSanityCheck) {
		this.doSanityCheck = doSanityCheck;
	}

	public void setGroupIdentifier(String groupIdentifier) {
		this.groupIdentifier = groupIdentifier;
	}

	public void setResourceIdentifier(String resourceIdentifier) {
		this.resourceIdentifier = resourceIdentifier;
	}

	public void setRoleIdentifier(String roleIdentifier) {
		this.roleIdentifier = roleIdentifier;
	}

	public StreamSocialNetwork.Type getDefaultNetworkType() {
		return defaultNetworkType;
	}

	public void setDefaultNetworkType(StreamSocialNetwork.Type defaultNetworkType) {
		this.defaultNetworkType = defaultNetworkType;
	}
	

}
