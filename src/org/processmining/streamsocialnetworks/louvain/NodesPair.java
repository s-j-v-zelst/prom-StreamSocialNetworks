package org.processmining.streamsocialnetworks.louvain;

public class NodesPair {
	Node nodeA;
	Node nodeB;
	
	public NodesPair(Node nodeA, Node nodeB) {
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}
	
	public Node getNodeA() {
		return nodeA;
	}
	
	public Node getNodeB() {
		return nodeB;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeA == null) ? 0 : nodeA.hashCode());
		result = prime * result + ((nodeB == null) ? 0 : nodeB.hashCode());
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
		NodesPair other = (NodesPair) obj;
		if (nodeA == null) {
			if (other.nodeA != null)
				return false;
		} else if (!nodeA.equals(other.nodeA))
			return false;
		if (nodeB == null) {
			if (other.nodeB != null)
				return false;
		} else if (!nodeB.equals(other.nodeB))
			return false;
		return true;
	}

	

}