package org.processmining.streamsocialnetworks.models;

import java.util.Collection;

import javax.swing.JComponent;

/**
 * A (stream) social network is a directed graph. If an undirected graph is
 * desired, the link values of (r,r') and (r',r) equal eachother.
 * 
 * @author svzelst
 *
 * @param <T>
 *            type of vertices in network
 */
public interface StreamSocialNetwork<T> {

	enum Implementation {
		GRAPH_STREAM;
	}

	//@formatter:off
	enum Type {
		ABSOLUTE_HANDOVER_OF_WORK("Handover of work (Absolute, Def. 4.4.1)"),
		BOOLEAN_HANDOVER_OF_WORK("Handover of work (Boolean, Def 4.4.2)"), 
		ABSOLUTE_CAUSAL_HAND_OVER_OF_WORK("Handover of work (Absolute & Causal, Def 4.4.3)"), 
		BOOLEAN_CAUSAL_HAND_OVER_OF_WORK("Handover of work (Boolean & Causal, Def 4.4.4)"),
		ABSOLUTE_INBETWEEN("In-between (Absolute, Def 4.7.1)"),
		BOOLEAN_CAUSAL_INBETWEEN("In-between (Boolean, Def 4.7.2)"),
		ABSOLUTE_CAUSAL_INBETWEEN("In-between (Absolute & Causal, Def 4.7.3)"),
		BOOLEAN_INBETWEEN("In-between (Boolean & Causal, Def 4.7.4)"),
		WORKING_TOGETHER("Working together (Def 4.8)"),
		JOINT_ACTIVITY_MINKOWSKI_DISTANCE("Joint Activities (Minkowski Distance, Def 4.10.1)"),
		JOINT_ACTIVITY_HAMMING_DISTANCE("Joint Activities (Hamming Distance, Def. 4.10.2"),
		JOINT_ACTIVITY_PEARSON_CORRELATION("Joint Activities (Pearson's Correlation, Def. 4.10.3)");
//		BOOLEAN_CAUSAl_HOW_ALWAYS_REFRESH("Handover of work (ALWAYS Refresh)"),

		
		private final String name;
		
		private Type(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	//@formatter:on

	static Type DEFAULT_NETWORK = Type.ABSOLUTE_HANDOVER_OF_WORK;

	void addLink(SSNLink<T> link);

	void addResource(T resource);

	JComponent asComponent();

	void clear();

	Implementation getImplementation();

	SSNLink<T> getLink(T from, T to);

	Collection<SSNLink<T>> getLinks();

	Collection<SSNLink<T>> getLinks(boolean excludeZero);

	Collection<SSNLink<T>> getLinks(boolean excludeZero, boolean excludeNegative);

	void removeLink(SSNLink<T> link);

	void setLinkLabel(SSNLink<T> link, String label);

	void setLinkStyle(SSNLink<T> link, SSNEdgeStyle style);

	void setLinkStyle(SSNLink<T> link, SSNEdgeStyle... styles);

	void setLinkLabelStyle(SSNLink<T> link, SSNEdgeLabelStyle style);

	void setLinkValue(SSNLink<T> link, double value);

	@Deprecated
	void setLinkValueWithChange(SSNLink<T> link, double value, double change);

	void setNodeLabel(T node, String label);

	void setNodeStyle(T node, SSNNodeStyle style);

}
