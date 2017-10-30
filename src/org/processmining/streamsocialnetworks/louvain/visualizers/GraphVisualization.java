package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;

public class GraphVisualization {
	
	public enum Type {
		DIRECTED, UNDIRECTED;
	}
	
	public static Graph createGraph(Type graphType, LSocialNetwork network) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new SingleGraph("Graph");
		
		// Get the resources
		Set<String> resources = new HashSet<>();
		
		for (ResourcesPair rp : network.keySet()) {
			String resourceA = rp.getResourceA();
			String resourceB = rp.getResourceB();

			if (!resources.contains(resourceA)) {
				resources.add(resourceA);
			}
			
			if (!resources.contains(resourceB)) {
				resources.add(resourceB);
			}
		}
		
		// Add the resources as vertices in the network				
		for (String resource : resources) {
			graph.addNode(resource);
		}
		
		// Add edges between vertices 
		if (graphType == Type.UNDIRECTED) { // Create undirected edges
			for (String resourceA : resources) {
				for (String resourceB : resources) {
					ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
				
					if (network.contains(rp) && graph.getEdge(resourceB + "-" + resourceA) == null) {
						graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB);
					} 				
				}
			}
		} else { // Create directed edges
			for (String resourceA : resources) {
				for (String resourceB : resources) {
					ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
				
					if (network.contains(rp)) {
						graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB, true);
					} 				
				}
			}		
		}
		
		// Graph visualization options
		for (Node node : graph) {
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
