package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphReplay;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.Viewer;
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
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		
		// Compute communities
		LouvainWorkingTogetherNetwork clustering = new LouvainWorkingTogetherNetwork();
		LSocialNetworkClustered networkClustered =  clustering.louvain(network);	
		
		// Get the communities 
		Set<Node> communities = new HashSet<>();
		
		for (NodesPair np : networkClustered.keySet()) {
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();

			if (!communities.contains(nodeA)) {
				communities.add(nodeA);
			}
			
			if (!communities.contains(nodeB)) {
				communities.add(nodeB);
			}
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
		
		// Compute the layout
		Layout layout = new SpringBox(false);;
		computeLayout(graph, layout);
				
		// Cluster the resources of the same community 
		for (Node n : communities) {
			Set<String> community = n.getResources();
			
			String centreResource = null;
			for (String resource : community) {
				
				if (centreResource != null) {
					GraphicNode gn = viewer.getGraphicGraph().getNode(centreResource);
					Double positionX = gn.getX();
					Double positionY = gn.getY();
					
					Random randP = new Random();
					graph.getNode(resource).setAttribute("x", positionX + randP.nextDouble());
					graph.getNode(resource).setAttribute("y", positionY + randP.nextDouble());
				}
				
				if (centreResource == null) {
					centreResource = resource;
					
					GraphicNode gn = viewer.getGraphicGraph().getNode(centreResource);
					Double positionX = gn.getX();
					Double positionY = gn.getY();
					
					Random randP = new Random();
					graph.getNode(centreResource).setAttribute("x", positionX + randP.nextDouble());
					graph.getNode(centreResource).setAttribute("y", positionY + randP.nextDouble());
				}
			}
			
			// Recompute the layout
			Layout layoutRecomputed = new SpringBox(false);;
			computeLayout(graph, layoutRecomputed);
		}
		
		
		
		/**
		 * Create the network of communities
		 */
		
		// Add the nodes as vertices in the network				
		for (Node n : communities) {
			Set<String> community = n.getResources();
			
			StringBuilder label = new StringBuilder();
			
			// Position of one of the resources in the community
			String position = null; 
			
			// Color of the community
			Random rand = new Random();
			String rgb = "rgb("+ rand.nextInt(255) +", "+ rand.nextInt(255) +", "+ rand.nextInt(255) + ")"; 
			
			for (String resource : community) {
				label.append(resource);
				label.append("-");
				
				// Get the position of one of the resources in the community 
				if (position == null) {
					position = resource;
				}
				
				// Give same color to the resources in the community
				graph.getNode(resource).addAttribute("ui.style", "fill-color:" + rgb + ";");
			}
			
			graph.addNode(label.toString());
			graph.getNode(label.toString()).addAttribute("ui.class", "community");
			graph.getNode(label.toString()).addAttribute("ui.style", "fill-color:" + rgb + ";");
			
			// Position the community node
			GraphicNode gn = viewer.getGraphicGraph().getNode(position);
			Double positionX = gn.getX();
			Double positionY = gn.getY();
			
			graph.getNode(label.toString()).addAttribute("x", positionX);
			graph.getNode(label.toString()).addAttribute("y", positionY);
		}
		
		// Add edges between vertices 
		if (graphType == Type.UNDIRECTED) { // Create undirected edges
			for (Node nodeA : communities) {
				for (Node nodeB : communities) {
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
					
						// Label the edge
						String label = new Double(networkClustered.get(np)).toString();
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.label", label);
					} 				
				}
			}
		} else { // Create directed edges
			for (Node nodeA : communities) {
				for (Node nodeB : communities) {
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
					
						// Label the edge
						String label = new Double(networkClustered.get(np)).toString();
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.label", label);
					} 				
				}
			}		
		}	
		
		// Graph visualization options
		graph.addAttribute("ui.stylesheet", 
				"	node.individual {"
				+ "		size: 15px, 15px;"
				+ "		text-background-mode: rounded-box;"
				+ "		text-alignment: at-right;"
				+ "		text-size: 14px;"
				+ "		visibility: 1;"
				+ "		visibility-mode: under-zoom;}"
				+ " edge.individual {"
				+ "		visibility: 1;"
				+ "		visibility-mode: under-zoom;}"
				+ " node.community {"
				+ "		size: 20px, 20px;"
				+ "		text-mode: hidden;"
				+ "		visibility: 1;"
				+ "		visibility-mode: over-zoom;}"
				+ " edge.community {"
				+ "		visibility: 1;"
				+ "		visibility-mode: over-zoom;"
				+ "		text-visibility-mode: hidden;}");
		
		return graph;
	}
	
	public static void computeLayout(Graph g, Layout layout) {
		GraphReplay r = new GraphReplay(g.getId());

		layout.addAttributeSink(g);
		r.addSink(layout);
		r.replay(g);
		r.removeSink(layout);

		layout.shake();
		layout.compute();

		while (layout.getStabilization() < 1) {
			layout.compute();
		}
		
		layout.removeAttributeSink(g);
	}

}
