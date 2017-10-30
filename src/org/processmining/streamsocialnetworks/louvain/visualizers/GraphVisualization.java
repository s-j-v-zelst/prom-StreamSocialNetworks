package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.LSocialNetworkClustered;
import org.processmining.streamsocialnetworks.louvain.Node;
import org.processmining.streamsocialnetworks.louvain.NodesPair;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;
import org.processmining.streamsocialnetworks.louvain.networks.LouvainWorkingTogetherNetwork;

/**
 * Creates a graph consisting of communities and of individual resources
 * When zoomed out, the communities are visible 
 * When zoomed in, the individual resources are visible
 */
public class GraphVisualization {
	
	public enum Type {
		DIRECTED, UNDIRECTED;
	}
	
	public static Graph createGraph(Type graphType, LSocialNetwork network) {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new SingleGraph("Graph");
		
		// Compute communities
		LouvainWorkingTogetherNetwork clustering = new LouvainWorkingTogetherNetwork();
		LSocialNetworkClustered networkClustered =  clustering.louvain(network);
		
		/**
		 * Create the network of communities
		 */
		
		// Get the nodes
		Set<Node> nodes = new HashSet<>();
		
		for (NodesPair np : networkClustered.keySet()) {
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();

			if (!nodes.contains(nodeA)) {
				nodes.add(nodeA);
			}
			
			if (!nodes.contains(nodeB)) {
				nodes.add(nodeB);
			}
		}
		
		// Add the nodes as vertices in the network				
		for (Node n : nodes) {
			Set<String> community = n.getResources();
			
			StringBuilder label = new StringBuilder();
			
			for (String resource : community) {
				label.append(resource);
				label.append("-");
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
					
					if (networkClustered.contains(np) && graph.getEdge(resourcesNodeB.toString() + "-" + resourcesNodeA.toString()) == null) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString());
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.class", "community");
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
				
					if (networkClustered.contains(np)) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString(), true);
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.class", "community");
					} 				
				}
			}		
		}
	
		for (Edge e : graph.getEachEdge()) {
			e.addAttribute("ui.class", "community");
		}
		
		for (org.graphstream.graph.Node n : graph) {
			n.addAttribute("ui.class", "community");
		}
		
		
		/**
		 * Create the network of individual resources
		 */
		
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
			graph.getNode(resource).addAttribute("ui.class", "individual");
		}
		
		// Add edges between vertices 
		if (graphType == Type.UNDIRECTED) { // Create undirected edges
			for (String resourceA : resources) {
				for (String resourceB : resources) {
					ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
				
					if (network.contains(rp) && graph.getEdge(resourceB + "-" + resourceA) == null) {
						graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB);
						graph.getEdge(resourceA + "-" + resourceB).addAttribute("ui.class", "individual");
					} 				
				}
			}
		} else { // Create directed edges
			for (String resourceA : resources) {
				for (String resourceB : resources) {
					ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
				
					if (network.contains(rp)) {
						graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB, true);
						graph.getEdge(resourceA + "-" + resourceB).addAttribute("ui.class", "individual");
					} 				
				}
			}		
		}
		
		for (org.graphstream.graph.Node node : graph) {
			// Add names to the nodes
	        node.addAttribute("ui.label", node.getId());
	        
	    }

		// Graph visualization options
		graph.addAttribute("ui.stylesheet", 
				"	node.individual {"
				+ "		fill-color: rgb(181, 0, 15);"
				+ "		size: 15px, 15px;"
				+ "		text-background-mode: rounded-box;"
				+ "		text-alignment: at-right;"
				+ "		text-size: 14px;"
				+ "		visibility-mode: normal;}"
				+ " edge.individual {"
				+ "		visibility-mode: normal;}"
				+ " node.community {"
				+ "		fill-color: green;"
				+ "		size: 20px, 20px;"
				+ "		text-mode: hidden;"
				+ "		visibility-mode: normal;}"
				+ " edge.community {"
				+ "		visibility-mode: normal;}");
		
		
		return graph;
	}

}
