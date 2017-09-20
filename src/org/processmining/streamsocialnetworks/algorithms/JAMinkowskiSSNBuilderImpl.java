package org.processmining.streamsocialnetworks.algorithms;

import java.util.List;

import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

// TODO: asses whether we can do some incremental updates here...
public class JAMinkowskiSSNBuilderImpl extends AbstractJointActivitySSNBuilder {

	private final static String N_KEY = "n";
	private int n = 1;

	public JAMinkowskiSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
		getParameterKeys().add(N_KEY);
	}

	protected double distanceForPair(Pair<String, String> pair) {
		double sum = 0;
		for (String activity : getActivites().keySet()) {
			int activityIndex = getActivites().get(activity);
			double abs = Math.abs(getMatrix()[getResources().get(pair.getFirst())][activityIndex]
					- getMatrix()[getResources().get(pair.getSecond())][activityIndex]);
			sum += Math.pow(abs, getN());
		}
		return Math.pow(sum, 1 / (double) getN());
	}

	public int getN() {
		return n;
	}

	public List<String> getParameterValues() {
		List<String> values = super.getParameterValues();
		values.add(Integer.toString(n));
		return values;
	}

	public Type getType() {
		return Type.JOINT_ACTIVITY_MINKOWSKI_DISTANCE;
	}

	public void setN(int n) {
		this.n = n;
	}

	@Override
	public void setParameter(final String key, final String value) {
		synchronized (getMonitor()) {
			super.setParameter(key, value);
			if (key.equals(N_KEY)) {
				n = Integer.valueOf(value);
				synchronized (getTrie().getLock()) {
					init(getTrie());
				}
			}
		}
	}

	protected void sanityCheck() {
		// TODO Auto-generated method stub

	}

	protected void refreshAllNetworkValues() {
		// TODO Auto-generated method stub

	}

	protected long measureMemoryConsumption() {
		// TODO Auto-generated method stub
		return -1;
	}

	protected long getNumResourcePairsActiveInDataStructure() {
		// TODO Auto-generated method stub
		return -1;
	}

}
