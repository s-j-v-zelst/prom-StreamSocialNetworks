package org.processmining.streamsocialnetworks.mcmclouvain;

public class ResourcesPair {
	String resourceA;
	String resourceB;
	
	public ResourcesPair(String resourceA, String resourceB) {
		this.resourceA = resourceA;
		this.resourceB = resourceB;
	}
	
	public String getResourceA() {
		return resourceA;
	}
	
	public String getResourceB() {
		return resourceB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resourceA == null) ? 0 : resourceA.hashCode());
		result = prime * result + ((resourceB == null) ? 0 : resourceB.hashCode());
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
		ResourcesPair other = (ResourcesPair) obj;
		if (resourceA == null) {
			if (other.resourceA != null)
				return false;
		} else if (!resourceA.equals(other.resourceA))
			return false;
		if (resourceB == null) {
			if (other.resourceB != null)
				return false;
		} else if (!resourceB.equals(other.resourceB))
			return false;
		return true;
	}
}