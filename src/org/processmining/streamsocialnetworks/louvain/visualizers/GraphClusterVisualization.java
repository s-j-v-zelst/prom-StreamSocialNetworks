package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.processmining.streamsocialnetworks.louvain.LSocialNetworkClustered;
import org.processmining.streamsocialnetworks.louvain.Node;
import org.processmining.streamsocialnetworks.louvain.NodesPair;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;

public class GraphClusterVisualization {
	
	public enum Type {
		DIRECTED, UNDIRECTED;
	}
	
	public static Graph createGraph(Type graphType, LSocialNetworkClustered network) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new SingleGraph("Graph");
		
		// Get the resources
		Set<Node> nodes = new HashSet<>();
		
		for (NodesPair np : network.keySet()) {
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();

			if (!nodes.contains(nodeA)) {
				nodes.add(nodeA);
			}
			
			if (!nodes.contains(nodeB)) {
				nodes.add(nodeB);
			}
		}
		
		// Add the resources as vertices in the network				
		for (Node n : nodes) {
			Set<String> community = n.getResources();
			
			StringBuilder label = new StringBuilder();
			for (String resource : community) {
				label.append(resource + "-");
			}
			
			graph.addNode(label.toString());
		}
		
		// Add edges between vertices 
		if (graphType == Type.UNDIRECTED) { // Create undirected edges
			for (Node nodeA : nodes) {
				for (Node nodeB : nodes) {
					NodesPair np = new NodesPair(nodeA, nodeB);
				
					// Get resources of A
					StringBuilder resourcesNodeA = new StringBuilder();
					Set<String> communityA = nodeA.getResources();
					
					for (String resource : communityA) {
						resourcesNodeA.append(resource + "-");
					}
					
					// Get resources of B
					StringBuilder resourcesNodeB = new StringBuilder();
					Set<String> communityB = nodeB.getResources();
					
					for (String resource : communityB) {
						resourcesNodeB.append(resource + "-");
					}
					
					if (network.contains(np) && graph.getEdge(resourcesNodeB.toString() + "-" + resourcesNodeA.toString()) == null) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString());
					} 				
				}
			}
		} else { // Create directed edges
			for (Node nodeA : nodes) {
				for (Node nodeB : nodes) {
					NodesPair np = new NodesPair(nodeA, nodeB);
					
					// Get resources of A
					StringBuilder resourcesNodeA = new StringBuilder();
					Set<String> communityA = nodeA.getResources();
					
					for (String resource : communityA) {
						resourcesNodeA.append(resource + "-");
					}
					
					// Get resources of B
					StringBuilder resourcesNodeB = new StringBuilder();
					Set<String> communityB = nodeB.getResources();
					
					for (String resource : communityB) {
						resourcesNodeB.append(resource + "-");
					}
				
					if (network.contains(np)) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString(), true);
					} 				
				}
			}		
		}
		
		// Graph visualization options
		for (org.graphstream.graph.Node node : graph) {
			// Add names to the vertices
	        node.addAttribute("ui.label", node.getId());
	        
	        // Color the vertices
			node.addAttribute("ui.style", 
					"fill-color: rgb(204,0,0);" +
					"size: 15px, 15px;" +
					"text-background-mode: rounded-box;" +
					"text-alignment: at-right;");
	    }
		
		// graph.addAttribute("ui.stylesheet", "node:selected {fill-color: blue;}");
		
		
		return graph;
	}

}