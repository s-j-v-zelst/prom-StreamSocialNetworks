package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphReplay;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.LouvainSingleIteration;
import org.processmining.streamsocialnetworks.louvain.Node;
import org.processmining.streamsocialnetworks.louvain.NodesPair;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;

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
				+ "		text-mode: hidden;"
				+ "		visibility-mode: zoom-range;}"
				+ " node.noCommunity {"
				+ "		size: 15px, 15px;"
				+ "		text-background-mode: rounded-box;"
				+ "		text-alignment: at-right;"
				+ "		text-size: 14px;"
				+ "		visibility-mode: normal;}"
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
				+ "		visibility-mode: zoom-range;}"
				+ " edge.noCommunity {"
				+ "		visibility-mode: normal;}"
				+ ""
				+ " edge.invisible {"
				+ "		visibility-mode: hidden;}");
				
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		
		Random rand = new Random();
		
		LouvainSingleIteration clustering = new LouvainSingleIteration();
	
		// Initialize the network such that each resource is has its own community
		TObjectDoubleMap<NodesPair> communityNetwork = clustering.initializeNetwork(network);
		
		// Compute communities of different levels
		ArrayList<TObjectDoubleMap<NodesPair>> clusterLevel = new ArrayList<>();
		
		boolean stop = false; // Iterate until no improvement is possible
		
		while (!stop) {
			for (int i = 0; i < 8; i++) { // maximum number of levels
				
				TObjectDoubleMap<NodesPair> cluster = clustering.louvain(communityNetwork, graphType);			
				
				
				if (communityNetwork.equals(cluster)) {
					stop = true;
					break;
				}
				
				clusterLevel.add(cluster);
				
				// New communityNetwork of higher level for next iteration
				communityNetwork = cluster;
			}
		}	
		
		// No communities exist
		if (clusterLevel.size() == 0) {
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
				graph.getNode(resource).addAttribute("ui.class", "noCommunity");
			}
			
			// Add edges between vertices 
			if (graphType == Type.UNDIRECTED) { // Create undirected edges
				for (String resourceA : resources) {
					for (String resourceB : resources) {
						ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
					
						if (network.contains(rp) && graph.getEdge(resourceB + "-" + resourceA) == null) {
							graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB);
							graph.getEdge(resourceA + "-" + resourceB).addAttribute("ui.class", "noCommunity");
						} 				
					}
				}
			} else { // Create directed edges
				for (String resourceA : resources) {
					for (String resourceB : resources) {
						ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
					
						if (network.contains(rp)) {
							graph.addEdge(resourceA + "-" + resourceB, resourceA, resourceB, true);
							graph.getEdge(resourceA + "-" + resourceB).addAttribute("ui.class", "noCommunity");
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
			
			for (org.graphstream.graph.Node node : graph) {
				String rgb = "rgb(" + rand.nextInt(255) + ", " + rand.nextInt(255) + ", " + rand.nextInt(255) + ")"; 
				node.addAttribute("ui.style", "fill-color:" + rgb + ";");
			}
			
			return graph;
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
		
		// Group the resources based on the final community structure -- if community structure exists
		for (Node n : communitiesLevel.get(communitiesLevel.size() - 1)) {
			Set<String> community = n.getResources();
			StringBuilder label = new StringBuilder();
			String centerResource = null;
			
			GraphicNode gn;
			Double positionX;
			Double positionY;
			
			// Color of the community
			String rgb = "rgb(" + rand.nextInt(255) + ", " + rand.nextInt(255) + ", " + rand.nextInt(255) + ")"; 
			
			for (String resource : community) {
				// Give same color to the resources in the community
				graph.getNode(resource).addAttribute("ui.style", "fill-color:" + rgb + ";");
		
				if (centerResource != null) {
					gn = viewer.getGraphicGraph().getNode(centerResource);
					
					if (gn != null) {
						positionX = gn.getX();
						positionY = gn.getY();
						
						graph.getNode(resource).setAttribute("x", positionX + rand.nextDouble());
						graph.getNode(resource).setAttribute("y", positionY + rand.nextDouble());
					}
				}
				
				if (centerResource == null) {
					centerResource = resource;
					
					gn = viewer.getGraphicGraph().getNode(centerResource);
					
					if (gn != null) {
						positionX = gn.getX();
						positionY = gn.getY();
					
						graph.getNode(centerResource).setAttribute("x", positionX + rand.nextDouble());
						graph.getNode(centerResource).setAttribute("y", positionY + rand.nextDouble());
					}
				}
			}
			
			// Add the community nodes of higher levels
			for (int i = 0; i < communitiesLevel.size(); i++) {
				Set<Node> level = communitiesLevel.get(i);
				
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
							label.append(resource + ", ");
						}
						
						// Add the community node
						graph.addNode(i + label.toString());
						graph.getNode(i + label.toString()).addAttribute("ui.class", "community");
						
						// Specify color and visibility of the community node
						double zoomLow = 1 + (i * 3 / (double) clusterLevel.size());
						double zoomHigh = 1 + (i * 3 / (double) clusterLevel.size()) + (3 / (double) clusterLevel.size());	
						
						if (zoomHigh == 4) {
							zoomHigh = 100;
						}
						
						// Size of the community
						int communitySize = individualResources.size();
						
						if (communitySize <= 10) {
							graph.getNode(i + label.toString()).addAttribute("ui.style", "fill-color:" + rgb + "; visibility: " + zoomLow + "," + zoomHigh + "; size: 15px, 15px;");
						} else if (communitySize > 10 && communitySize <= 20) {
							graph.getNode(i + label.toString()).addAttribute("ui.style", "fill-color:" + rgb + "; visibility: " + zoomLow + "," + zoomHigh + "; size: 25px, 25px;");
						} else { // community size greater than 20
							graph.getNode(i + label.toString()).addAttribute("ui.style", "fill-color:" + rgb + "; visibility: " + zoomLow + "," + zoomHigh + "; size: 35px, 35px;");
						}
						
						// Add text to the community node
						String resourceLabel = "#resources " + communitySize + ": " + label.toString().substring(0, label.toString().length() - 2);
						graph.getNode(i + label.toString()).addAttribute("ui.label", resourceLabel);
						
						// Position the community node
						gn = viewer.getGraphicGraph().getNode(centerResource);
						
						if (gn != null) {
							positionX = gn.getX();
							positionY = gn.getY();
							
							graph.getNode(i + label.toString()).setAttribute("x", positionX);
							graph.getNode(i + label.toString()).setAttribute("y", positionY);	
						}
						
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
		
		// Create undirected edges
		if (graphType == Type.UNDIRECTED) { 
			// Edges between nodes in each level
			for (int i = 0; i < communitiesLevel.size(); i++) {
				// All nodes at level i
				Set<Node> level = communitiesLevel.get(i);
				
				for (Node nodeA : level) {
					for (Node nodeB : level) {
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
						
						if (clusterLevel.get(i).containsKey(np) && graph.getEdge(i + resourcesNodeB.toString() + ", " + resourcesNodeA.toString()) == null) {
							graph.addEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString(), 
									i + resourcesNodeA.toString(), i + resourcesNodeB.toString());
							graph.getEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString()).addAttribute("ui.class", "community");
							
							// Specify visibility of the edge
							double zoomLow = 1 + (i * 3 / (double) clusterLevel.size());
							double zoomHigh = 1 + (i * 3 / (double) clusterLevel.size()) + (3 / (double) clusterLevel.size());
							
							if (zoomHigh == 4) {
								zoomHigh = 100;
							}
							
							graph.getEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString()).addAttribute("ui.style", "visibility: " + zoomLow + "," + zoomHigh + ";");
						} 				
					}
				}
			}
		} else { // Create directed edges
			// Edges between nodes in each level
			for (int i = 0; i < communitiesLevel.size(); i++) {
				// All nodes at level i
				Set<Node> level = communitiesLevel.get(i);
				
				for (Node nodeA : level) {
					for (Node nodeB : level) {
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
					
						if (clusterLevel.get(i).containsKey(np)) {
							graph.addEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString(), 
									i + resourcesNodeA.toString(), i + resourcesNodeB.toString(), true);
							graph.getEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString()).addAttribute("ui.class", "community");
							
							// Specify visibility of the edge
							double zoomLow = 1 + (i * 3 / (double) clusterLevel.size());
							double zoomHigh = 1 + (i * 3 / (double) clusterLevel.size()) + (3 / (double) clusterLevel.size());	
							
							if (zoomHigh == 4) {
								zoomHigh = 100;
							}
							
							graph.getEdge(i + resourcesNodeA.toString() + ", " + resourcesNodeB.toString()).addAttribute("ui.style", "visibility: " + zoomLow + "," + zoomHigh + ";");
						} 				
					}
				}
			}		
		}
		
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
