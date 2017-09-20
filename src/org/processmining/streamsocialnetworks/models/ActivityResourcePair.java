package org.processmining.streamsocialnetworks.models;

public class ActivityResourcePair {

	private final String activity;

	private final String resource;

	public ActivityResourcePair(String activity, String resource) {
		this.activity = activity;
		this.resource = resource;
	}

	public String getActivity() {
		return activity;
	}

	public String getResource() {
		return resource;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ActivityResourcePair) {
			ActivityResourcePair c = (ActivityResourcePair) o;
			boolean r = c.getActivity().equals(getActivity());
			return r && c.getResource().equals(getResource());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 2 * getActivity().hashCode() + 5 * getResource().hashCode();
	}
	
	@Override
	public String toString() {
		return "(a: " + activity +", r: " + resource +")";
	}

}
