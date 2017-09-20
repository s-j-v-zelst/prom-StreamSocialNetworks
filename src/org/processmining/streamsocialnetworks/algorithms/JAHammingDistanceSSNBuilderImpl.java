package org.processmining.streamsocialnetworks.algorithms;

import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

// TODO: asses whether we can do some incremental updates here...
public class JAHammingDistanceSSNBuilderImpl extends AbstractJointActivitySSNBuilder {

	public JAHammingDistanceSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	public Type getType() {
		return Type.JOINT_ACTIVITY_HAMMING_DISTANCE;
	}

	protected double distanceForPair(Pair<String, String> pair) {
		double sum = 0;
		for (String activity : getActivites().keySet()) {
			int activityIndex = getActivites().get(activity);
			double v1 = getMatrix()[getResources().get(pair.getFirst())][activityIndex];
			double v2 = getMatrix()[getResources().get(pair.getSecond())][activityIndex];
			if ((v1 > 0d && v2 > 0d) || (v1 == 0d && v2 == 0d)) {
				sum++;
			}
		}
		return sum / getActivites().keySet().size();
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
