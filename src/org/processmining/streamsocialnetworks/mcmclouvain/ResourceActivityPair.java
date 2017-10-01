package org.processmining.streamsocialnetworks.mcmclouvain;

public class ResourceActivityPair {
	String resource;
	String activity;
	
	public ResourceActivityPair(String resource, String activity) {
		this.resource = resource;
		this.activity = activity;
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getActivity() {
		return activity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activity == null) ? 0 : activity.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResourceActivityPair other = (ResourceActivityPair) obj;
		if (activity == null) {
			if (other.activity != null)
				return false;
		} else if (!activity.equals(other.activity))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}
}