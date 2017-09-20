package org.processmining.streamsocialnetworks.factories;

import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.streamsocialnetworks.algorithms.AbsoluteCausalHOWSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.AbsoluteCausalInBetweenBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.AbsoluteHOWSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.AbsoluteInbetweenBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.BooleanCausalHOWSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.BooleanCausalInBetweenSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.BooleanHOWSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.BooleanInBetweenBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.JAHammingDistanceSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.JAMinkowskiSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.JAPearsonDistanceSSNBuilderImpl;
import org.processmining.streamsocialnetworks.algorithms.WorkingTogetherBuilderImpl;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetworkBuilder;
import org.processmining.streamsocialnetworks.models.graphstream.GSSingleGraphStreamSocialNetworkImpl;

public class StreamSocialNetworkFactory {

	public static StreamSocialNetworkBuilder<String, ActivityResourcePair, VertexImpl<ActivityResourcePair>> constructStreamSocialNetworkBuilder(
			StreamSocialNetwork.Type type, StreamSocialNetwork<String> emptyNetwork) {
		switch (type) {
			case ABSOLUTE_HANDOVER_OF_WORK :
				return new AbsoluteHOWSSNBuilderImpl(emptyNetwork);
			case BOOLEAN_HANDOVER_OF_WORK :
				return new BooleanHOWSSNBuilderImpl(emptyNetwork);
			case ABSOLUTE_CAUSAL_HAND_OVER_OF_WORK :
				return new AbsoluteCausalHOWSSNBuilderImpl(emptyNetwork);
			case BOOLEAN_CAUSAL_HAND_OVER_OF_WORK :
				return new BooleanCausalHOWSSNBuilderImpl(emptyNetwork);
			//			case BOOLEAN_CAUSAl_HOW_ALWAYS_REFRESH :
			//				return new BoolanCausalHOWAlwaysRefresh(emptyNetwork);
			case ABSOLUTE_INBETWEEN :
				return new AbsoluteInbetweenBuilderImpl(emptyNetwork);
			case ABSOLUTE_CAUSAL_INBETWEEN :
				return new AbsoluteCausalInBetweenBuilderImpl(emptyNetwork);
			case WORKING_TOGETHER :
				return new WorkingTogetherBuilderImpl(emptyNetwork);
			case JOINT_ACTIVITY_MINKOWSKI_DISTANCE :
				return new JAMinkowskiSSNBuilderImpl(emptyNetwork);
			case BOOLEAN_INBETWEEN :
				return new BooleanInBetweenBuilderImpl(emptyNetwork);
			case JOINT_ACTIVITY_HAMMING_DISTANCE :
				return new JAHammingDistanceSSNBuilderImpl(emptyNetwork);
			case JOINT_ACTIVITY_PEARSON_CORRELATION :
				return new JAPearsonDistanceSSNBuilderImpl(emptyNetwork);
			case BOOLEAN_CAUSAL_INBETWEEN :
				return new BooleanCausalInBetweenSSNBuilderImpl(emptyNetwork);
			default :
				return new AbsoluteHOWSSNBuilderImpl(emptyNetwork);
		}
	}

	public static <T> StreamSocialNetwork<T> constructEmptyGraphStreamSingleGraphStreamSocialNetwork(String name) {
		return new GSSingleGraphStreamSocialNetworkImpl<>(name);
	}

}
