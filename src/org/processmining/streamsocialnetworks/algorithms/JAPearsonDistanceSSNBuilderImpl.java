package org.processmining.streamsocialnetworks.algorithms;

import org.processmining.framework.util.Pair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

// TODO: asses whether we can do some incremental updates here...
public class JAPearsonDistanceSSNBuilderImpl extends AbstractJointActivitySSNBuilder {

	public JAPearsonDistanceSSNBuilderImpl(StreamSocialNetwork<String> network) {
		super(network);
	}

	public Type getType() {
		return Type.JOINT_ACTIVITY_PEARSON_CORRELATION;
	}

	protected double distanceForPair(Pair<String, String> pair) {
		double sum = 0;
		double sumR1 = 0;
		double sumR2 = 0;
		for (String a : getActivites().keySet()) {
			String r1 = pair.getFirst();
			String r2 = pair.getSecond();
			int[][] matrix = getMatrix();
			double X = sumOverActivities(r1);
			double Y = sumOverActivities(r2);
			double r1MinusX = matrix[getResources().get(r1)][getActivites().get(a)] - X;
			double r2MinusY = matrix[getResources().get(r2)][getActivites().get(a)] - Y;
			sum += r1MinusX * r2MinusY;
			sumR1 += r1MinusX;
			sumR2 += r2MinusY;
		}
		return sum / Math.sqrt(sumR1 * sumR2);

	}

	//TODO: candidate for storage
	protected double sumOverActivities(String res) {
		double sum = 0;
		for (String a : getActivites().keySet()) {
			sum += getMatrix()[getResources().get(res)][getActivites().get(a)];
		}
		return sum / getTotalActivityOcc();
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
