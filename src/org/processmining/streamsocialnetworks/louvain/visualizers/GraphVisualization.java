package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.ArrayList;
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
import org.processmining.streamsocialnetworks.louvain.LouvainSingleIteration;
import org.processmining.streamsocialnetworks.louvain.Node;
import org.processmining.streamsocialnetworks.louvain.NodesPair;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;
import org.processmining.streamsocialnetworks.louvain.networks.LouvainWorkingTogetherNetwork;

import gnu.trove.map.TObjectDoubleMap;

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
		
		LouvainSingleIteration clustering = new LouvainSingleIteration();
	
		// Initialize the network such that each resource is has its own community
		TObjectDoubleMap<NodesPair> communityNetwork = clustering.initializeNetwork(network);
		
		// Compute communities of different levels
		ArrayList<TObjectDoubleMap<NodesPair>> clusterLevel = new ArrayList<>();
		
		boolean stop = false; // Iterate until no improvement is possible
		
		while (!stop) {
			for (int i = 0; i < 8; i++) { // maximum number of levels
			
				TObjectDoubleMap<NodesPair> cluster = clustering.louvain(communityNetwork);			
				
				if (communityNetwork.equals(cluster)) {
					stop = true;
					break;
				}
				
				clusterLevel.add(cluster);
				
				// New communityNetwork of higher level for next iteration
				communityNetwork = cluster;
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
				
		
		/**
		 *  Cluster the resources of the same community 
		 */
		
		// Get the communities 
		ArrayList<Set<Node>> communitiesLevel = new ArrayList<>();
		
		for (int i = 0; i < clusterLevel.size(); i++) {
			Set<Node> communities = new HashSet<>();
			
			for (NodesPair np : clusterLevel.get(i).keySet()) {
				Node nodeA = np.getNodeA();
				Node nodeB = np.getNodeB(); 
	
				if (!communities.contains(nodeA)) {
					communities.add(nodeA);
				}
				
				if (!communities.contains(nodeB)) {
					communities.add(nodeB);
				}
			}
			
			communitiesLevel.add(communities);
		}
		
		// Group the resources based on the final community structure
		for (Node n : communitiesLevel.get(communitiesLevel.size() - 1)) {
			Set<String> community = n.getResources();
			StringBuilder label = new StringBuilder();
			String centerResource = null;
			
			GraphicNode gn;
			Double positionX;
			Double positionY;
			
			// Color of the community
			Random rand = new Random();
			String rgb = "rgb("+ rand.nextInt(255) +", "+ rand.nextInt(255) +", "+ rand.nextInt(255) + ")"; 
			
			for (String resource : community) {
				// Give same color to the resources in the community
				graph.getNode(resource).addAttribute("ui.style", "fill-color:" + rgb + ";");
		
				if (centerResource != null) {
					gn = viewer.getGraphicGraph().getNode(centerResource);
					positionX = gn.getX();
					positionY = gn.getY();
					
					graph.getNode(resource).setAttribute("x", positionX + rand.nextDouble());
					graph.getNode(resource).setAttribute("y", positionY + rand.nextDouble());
				}
				
				if (centerResource == null) {
					centerResource = resource;
					
					gn = viewer.getGraphicGraph().getNode(centerResource);
					positionX = gn.getX();
					positionY = gn.getY();
				
					graph.getNode(centerResource).setAttribute("x", positionX + rand.nextDouble());
					graph.getNode(centerResource).setAttribute("y", positionY + rand.nextDouble());
				}
			}
			
			// Add the community nodes of higher levels
			for (int i = 0; i < communitiesLevel.size(); i++) {
				Set<Node> level = communitiesLevel.get(i);
				
				System.out.println("level: " + i);
				for (Node nodeX : level) {
					System.out.println("Community consists of:");
					
					for (String resourceX : nodeX.getResources()) {
						System.out.println(resourceX);
					}
					
				}
				
				
				for (Node node : level) {
					Set<String> individualResources = node.getResources();
					boolean nodeInCommunity = false;
					
					// Find the nodes in the level that correspond to the community
					for (String resource : individualResources) {
						if (graph.getNode(resource).hasAttribute("ui.style")) {
							if (graph.getNode(resource).getAttribute("ui.style").equals("fill-color:" + rgb + ";")) {
								nodeInCommunity = true;
							}
						} 
					}
					
					// Only add corresponding community nodes in higher levels
					if (nodeInCommunity == true) {
						for (String resource : individualResources) {
							label.append(resource);
							label.append(", ");
						}
						
						System.out.println("add node at level " + i + ":" + label.toString());
						// Add the community node
						graph.addNode(i + label.toString());
						graph.getNode(i + label.toString()).addAttribute("ui.class", "community");
						graph.getNode(i + label.toString()).addAttribute("ui.style", "fill-color:" + rgb + ";");
						
						// Add text to the community node
						int communitySize = individualResources.size();
						String resourceLabel = "#resources " + communitySize + ": " + label.toString().substring(0, label.toString().length() - 2);
						graph.getNode(i + label.toString()).addAttribute("ui.label", resourceLabel);
						
						// Position the community node
						gn = viewer.getGraphicGraph().getNode(centerResource);
						positionX = gn.getX();
						positionY = gn.getY();
						
						graph.getNode(i + label.toString()).setAttribute("x", positionX);
						graph.getNode(i + label.toString()).setAttribute("y", positionY);	
						
						// Add invisible edge from community node to center resource to improve placement
						graph.addEdge("x" + i + label.toString(), i + label.toString(), centerResource);
						graph.getEdge("x" + i + label.toString()).addAttribute("ui.class", "invisible");	
						
						// Clear the label
						label.setLength(0);
					}
				}
				
				// Recompute the layout
				Layout layoutRecomputed = new SpringBox(false);;
				computeLayout(graph, layoutRecomputed);	
			}
		}	
		
		/**
		 * Add the edges in the network of communities
		 */
		
		/*
		// Add edges between vertices 
		if (graphType == Type.UNDIRECTED) { // Create undirected edges
			for (Node nodeA : communitiesLevel.get(0)) {
				for (Node nodeB : communitiesLevel.get(0)) {
					NodesPair np = new NodesPair(nodeA, nodeB);
				
					// Get resources of A
					StringBuilder resourcesNodeA = new StringBuilder();
					Set<String> communityA = nodeA.getResources();
					
					for (String resource : communityA) {
						resourcesNodeA.append(resource + ", ");
					}
					
					// Get resources of B
					StringBuilder resourcesNodeB = new StringBuilder();
					Set<String> communityB = nodeB.getResources();
					
					for (String resource : communityB) {
						resourcesNodeB.append(resource + ", ");
					}
					
					if (clusterLevel.get(0).containsKey(np) && graph.getEdge(resourcesNodeB.toString() + "-" + resourcesNodeA.toString()) == null) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString());
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.class", "community");
					
						// Label the edge
						String label = new Double(clusterLevel.get(0).get(np)).toString();
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.label", label);
					} 				
				}
			}
		} else { // Create directed edges
			for (Node nodeA : communitiesLevel.get(0)) {
				for (Node nodeB : communitiesLevel.get(0)) {
					NodesPair np = new NodesPair(nodeA, nodeB);
					
					// Get resources of A
					StringBuilder resourcesNodeA = new StringBuilder();
					Set<String> communityA = nodeA.getResources();
					
					for (String resource : communityA) {
						resourcesNodeA.append(resource + ", ");
					}
					
					// Get resources of B
					StringBuilder resourcesNodeB = new StringBuilder();
					Set<String> communityB = nodeB.getResources();
					
					for (String resource : communityB) {
						resourcesNodeB.append(resource + ", ");
					}
				
					if (clusterLevel.get(0).containsKey(np)) {
						graph.addEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString(), 
								resourcesNodeA.toString(), resourcesNodeB.toString(), true);
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.class", "community");
					
						// Label the edge
						String label = new Double(clusterLevel.get(0).get(np)).toString();
						graph.getEdge(resourcesNodeA.toString() + "-" + resourcesNodeB.toString()).addAttribute("ui.label", label);
					} 				
				}
			}		
		}	
		*/
		
		// Graph visualization options
		graph.addAttribute("ui.stylesheet", 
				"	node.individual {"
				+ "		size: 15px, 15px;"
				+ "		text-background-mode: rounded-box;"
				+ "		text-alignment: at-right;"
				+ "		text-size: 14px;"
				+ "		visibility: 1;"
				+ "		visibility-mode: under-zoom;}"
				+ " node.community {"
				+ "		size: 25px, 25px;"
				+ "		text-mode: hidden;"
				+ "		visibility: 1;"
				+ "		visibility-mode: over-zoom;}"
				+ " node:selected {"
				+ "		text-mode: normal;"
				+ "		text-background-mode: rounded-box;"
				+ "		text-alignment: at-right;"
				+ "		text-size: 14px;}"
				+ ""
				+ " edge.individual {"
				+ "		visibility: 1;"
				+ "		visibility-mode: under-zoom;}"
				+ " edge.community {"
				+ "		visibility: 1;"
				+ "		visibility-mode: over-zoom;"
				+ "		text-visibility-mode: hidden;}"
				+ ""
				+ " edge.invisible {"
				+ "		visibility-mode: hidden;}");
		
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
