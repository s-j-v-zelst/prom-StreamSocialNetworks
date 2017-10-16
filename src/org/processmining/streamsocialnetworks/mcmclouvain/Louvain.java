package org.processmining.streamsocialnetworks.mcmclouvain;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.processmining.streamsocialnetworks.util.XESImporter;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class Louvain {
	boolean stop;
	
	/**
	 * Determines the communities with the highest modularity gain with the use of Louvain
	 */
	public TObjectDoubleMap<NodesPair> louvain(TObjectDoubleMap<ResourcesPair> network) {			
		// Initialize the network consisting of nodes in which each node is a community of one or several resources
		TObjectDoubleMap<NodesPair> communityNetwork = initializeNetwork(network);
		
		// Get the set of nodes
		Set<Node> nodes = new HashSet<>();
		nodes = getNodesOfNetwork(communityNetwork);
		
		// The set of communities 
		Set<Set<Node>> communities = new HashSet<>();
		
		// Iterate over the two phases of the algorithm. 		
		while (!stop) {
			// The community network at the start of the iteration
			TObjectDoubleMap<NodesPair> communityNetworkStart = communityNetwork;
			
			// Get the communities of the nodes
			communities = optimization(communityNetwork, nodes);
			// Build a new network based on the found communities
			communityNetwork = aggregation(communityNetwork, communities);
 
			// Get the set of nodes of the new network
			nodes = getNodesOfNetwork(communityNetwork);	
			
			// The community network at the end of the iteration
			TObjectDoubleMap<NodesPair> communityNetworkEnd = communityNetwork;
			
			if (communityNetworkStart.equals(communityNetworkEnd)) {
				stop = true;
			}
		}
		
		return communityNetwork;	
	}
	
	/**
	 * Determines the set of nodes in the network.
	 */
	public Set<Node> getNodesOfNetwork(TObjectDoubleMap<NodesPair> network) {
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
		
		return nodes;
	}
	
	/**
	 * Each node is moved to one of its neighbor clusters to maximize the modularity index.
	 * 
	 * For each node i we consider the neighbors j of i and we evaluate the gain
	 * of modularity that would take place by removing i from its community and by
	 * placing it in the community of j. The node i is then placed in the community for 
	 * which this gain is maximum (only when gain is positive). This process is applied 
	 * repeatedly and sequentially for all nodes until no further improvement can be 
	 * achieved. 
	 */
	public Set<Set<Node>> optimization(TObjectDoubleMap<NodesPair> communityNetwork, Set<Node> nodes) {
		// The set of communities 
		Set<Set<Node>> communities = new HashSet<>();
		
		// Initialize the communities -- each community consists of one node
		for (Node node : nodes) {
			Set<Node> initialCommunity = new HashSet<>();
			initialCommunity.add(node);
			communities.add(initialCommunity);
		}
		
		// Stop indicator
		boolean stopPhase1 = false;
		
		while (!stopPhase1) {
			// The communities at the beginning of phase 1
			Set<Set<Node>> communitiesStart = communities;
			
			// Move each node to the community of its neighbor resulting in the highest modularity gain 
			for (Node node : nodes) {
				// Determines the community with highest modularity gain 
				Set<Node> communityTo = maximumGain(node, communityNetwork, communities);
				Set<Node> communityFrom = findCommunity(node, communities);
				
				// Only move the node when an other community is found 
				if (!communityTo.equals(communityFrom)) {
					// Remove the community from the set to update it
					communities.remove(communityFrom);
					
					// Update the community -- community is now without the node
					communityFrom.remove(node);
					
					// Add community when it is not empty
					if (!communityFrom.isEmpty()) {
						communities.add(communityFrom);
					}
					
					// Remove the community from the set to update it
					communities.remove(communityTo);
					
					// Move the node to the new community
					communityTo.add(node);		
					communities.add(communityTo);
				}
			}
			
			Set<Set<Node>> communitiesEnd = communities;
			
			if (communitiesStart.equals(communitiesEnd)) {
				stopPhase1 = true;
			}
		}
		
		return communities;
	}
	
	/**
	 * Build a new network whose nodes are now the communities found during the first phase.
	 * The weights of the links between the new nodes are given by the sum of the weight of the links
	 * between nodes in the corresponding two communities.
	 */
	public TObjectDoubleMap<NodesPair> aggregation(TObjectDoubleMap<NodesPair> communityNetwork, Set<Set<Node>> communities) {
		// The new community network
		TObjectDoubleMap<NodesPair> newCommunityNetwork = new TObjectDoubleHashMap<>();;
		
		// Add the communities as nodes to the network with corresponding edge values
		for (Set<Node> communityA : communities) {
			for (Set<Node> communityB : communities) {
				
				// Add the communities as pair to the network when at least one of the nodes in each community are linked
				for (Node nodeA : communityA) {
					for (Node nodeB : communityB) {
						NodesPair np = new NodesPair(nodeA, nodeB);
						
						// Link between nodes is found -- add communities as nodes in the new community network
						if (communityNetwork.containsKey(np)) {
							
							/* Create node in new community network if it not exists yet.
							 * CommunityA becomes a node 
							 * CommunityB becomes a node
							 * Community becomes node: set of resources of all nodes in the community are combined in one set
							 */
							
							// Set of all resources in the community
							Set<String> resourcesOfCommunityA = new HashSet<>();
							Set<String> resourcesOfCommunityB = new HashSet<>();
							
							// Add resources of the node to the set of resources of the communities
							for (Node node : communityA) {
								Set<String> resourcesOfNode = node.getResources();
								
								for (String resource : resourcesOfNode) {
									resourcesOfCommunityA.add(resource);
								}
							}
							
							for (Node node : communityB) {
								Set<String> resourcesOfNode = node.getResources();
								
								// Add resources of the node to the set of resources of the community
								for (String resource : resourcesOfNode) {
									resourcesOfCommunityB.add(resource);
								}
							}
							
							// Define the new nodes
							Node newNodeA = new Node(resourcesOfCommunityA);
							Node newNodeB = new Node(resourcesOfCommunityB);
							
							/* Add the new nodes to the community network
							 * Determine the value of the edge from new node A to new node B
							 */
							double value = 0;
							
							// Iterate over all nodes to determine value of the edge from new node A to new node B
							NodesPair newNp = new NodesPair(newNodeA, newNodeB);
							
							for (Node nodeOfCommunityA : communityA) {
								for (Node nodeOfCommunityB : communityB) {
									NodesPair nodesPair = new NodesPair(nodeOfCommunityA, nodeOfCommunityB);
									
									if (communityNetwork.containsKey(nodesPair)) {
										value = value + communityNetwork.get(nodesPair);
									}
								}
							}
							
							// Add nodes pair with corresponding edge value to new community network
							newCommunityNetwork.adjustOrPutValue(newNp, value, value);
							
							break;
						}
					}
		
					break;		
				}		
			}
		}
				
		return newCommunityNetwork;
	}
	
	/**
	 * Create the initial network consisting of nodes
	 */
	public TObjectDoubleMap<NodesPair> initializeNetwork(TObjectDoubleMap<ResourcesPair> network) {
		// The network consisting of nodes in which each node is a community of one or several resources
		TObjectDoubleMap<NodesPair> communityNetwork = new TObjectDoubleHashMap<>();
		
		for (ResourcesPair rp : network.keySet()) {
			String resourceA = rp.getResourceA();
			String resourceB = rp.getResourceB();

			Set<String> setA = new HashSet<>();
			setA.add(resourceA);
			Node nodeA = new Node(setA);
			
			Set<String> setB = new HashSet<>();
			setB.add(resourceB);
			Node nodeB = new Node(setB);
			
			NodesPair np = new NodesPair(nodeA, nodeB);
			communityNetwork.adjustOrPutValue(np, network.get(rp), network.get(rp)); 
		}
		
		return communityNetwork;
	}
	

	/**
	 * Determine the neighbor community that result in the maximum modularity gain of moving the node. 
	 */
	public Set<Node> maximumGain(Node node, TObjectDoubleMap<NodesPair> communityNetwork, Set<Set<Node>> communities) {
		// Initialize the community with maximum gain the original community of the node
		Set<Node> communityMax = findCommunity(node, communities); 
		double maxGain = 0;
		
		// Get the neighbors of the node
		Set<Node> neighbors = new HashSet<>();
				
		for (NodesPair np : communityNetwork.keySet()) {
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();
		
			if (nodeA.equals(node)) {
				neighbors.add(nodeB);
			}
			
			if (nodeB.equals(node)) {
				neighbors.add(nodeA);
			}
		}
		
		for (Node neighbor : neighbors) {
			double modularityGain = computeModularityGain(node, neighbor, communityNetwork, communities);
			
			if (modularityGain > maxGain) {
				maxGain = modularityGain;
				
				// Find the community of neighbor
				communityMax = findCommunity(neighbor, communities);
			}
		}
		
		return communityMax;
	}
	
	/**
	 * Computes the gain of modularity obtained by adding the node the community of the neighbor
	 */
	public double computeModularityGain(Node node, Node neighbor, TObjectDoubleMap<NodesPair> communityNetwork,
			Set<Set<Node>> communities) {
		double gain = 0;
			
		double degreeC = 0; // sum of the edges from/to the node and nodes in the community
		double degreeIn = 0; // sum of the edges to the node
		double degreeOut = 0; // sum of the edges from the node 
		double sumTotIn = 0; // sum of the edges to nodes in the community
		double sumTotOut = 0; // sum of the edges from nodes in the community
		double m = 0; // sum of all edges in the network
		
		// Find the community of the neighbor
		Set<Node> communityNeighbor = findCommunity(neighbor, communities);
		
		// Compute the values
		for (NodesPair np : communityNetwork.keySet()) {
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();
			
			// degreeC
			if (node.equals(nodeA) && communityNeighbor.contains(nodeB)) {
				degreeC = degreeC + communityNetwork.get(np);
			}
			
			if (node.equals(nodeB) && communityNeighbor.contains(nodeA)) {
				degreeC = degreeC + communityNetwork.get(np);
			}
			
			// degreeIn
			if (node.equals(nodeB)) {
				degreeIn = degreeIn + communityNetwork.get(np);
			}
			
			// degreeOut
			if (node.equals(nodeA)) {
				degreeIn = degreeIn + communityNetwork.get(np);
			}
			
			// sumTotIn
			if (communityNeighbor.contains(nodeB)) {
				sumTotIn = sumTotIn + communityNetwork.get(np);
			}
			
			// sumTotOut
			if (communityNeighbor.contains(nodeA)) {
				sumTotOut = sumTotOut + communityNetwork.get(np);
			}
			
			// m
			m = m + communityNetwork.get(np);
		}
	
		// Compute modularity gain -- see formula in paper directed Louvain by Nicolas Dugue and Anthony Perez
		gain = (degreeC / m) - ((degreeOut * sumTotIn) + (degreeIn * sumTotOut)) / (m * m);
			
		return gain;
	}
	
	/**
	 * Find the community the node belongs to.
	 */
	public Set<Node> findCommunity(Node node, Set<Set<Node>> communities) {
		Set<Node> community = new HashSet<>();
		
		for (Set<Node> c : communities) {
			
			if (c.contains(node)) {
				community = c;
				break;
			}
		}
		
		return community;
	}
	
	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		
		// Choose a network type
		WorkingTogetherNetwork network = new WorkingTogetherNetwork(bpiLog);
		// SimilarTaskNetwork network = new SimilarTaskNetwork(bpiLog);
		// HandoverOfWorkNetwork network = new HandoverOfWorkNetwork(bpiLog);
		
		// Compute the values of the network
		TObjectDoubleMap<ResourcesPair> workingTogetherNetwork = network.computeNetwork();
		// TObjectDoubleMap<ResourcesPair> similarTaskNetwork = network.computeNetwork();
		// TObjectDoubleMap<ResourcesPair> handoverOfWorkNetwork = network.computeNetwork();
		
		// Detect communities for the network
		Louvain communityDetection = new Louvain();
		TObjectDoubleMap<NodesPair> communityNetwork = communityDetection.louvain(workingTogetherNetwork);
		// TObjectDoubleMap<NodesPair> communityNetwork = communityDetection.louvain(similarTaskNetwork);
		// TObjectDoubleMap<NodesPair> communityNetwork = communityDetection.louvain(handoverOfWorkNetwork);
		
		// How many number of resources?
		Set<String> resources = new HashSet<>();
		
		for (ResourcesPair rp : workingTogetherNetwork.keySet()) {	
		// for (ResourcesPair rp : similarTaskNetwork.keySet()) {	
		// for (ResourcesPair rp : handoverOfWorkNetwork.keySet()) {		
			String resourceA = rp.getResourceA();
			String resourceB = rp.getResourceB();
			
			if (!resources.contains(resourceA)) {
				resources.add(resourceA);
			}
			
			if (!resources.contains(resourceB)) {
				resources.add(resourceB);
			}
		}
		
		System.out.println("resources " + resources.size());
		System.out.println("resourcesPair " + workingTogetherNetwork.keySet().size());
		// System.out.println("resourcesPair " + similarTaskNetwork.keySet().size());
		// System.out.println("resourcesPair " + handoverOfWorkNetwork.keySet().size());
		
		// How many number of communities? -- each node in the community network is represents a community
		Set<Node> communities = new HashSet<>();
		
		for (NodesPair np : communityNetwork.keySet()) {		
			Node nodeA = np.getNodeA();
			Node nodeB = np.getNodeB();
			
			if (!communities.contains(nodeA)) {
				communities.add(nodeA);
			}
			
			if (!communities.contains(nodeB)) {
				communities.add(nodeB);
			}
		}
		
		System.out.println("communities " + communities.size());
		System.out.println("communitiesPair " + communityNetwork.keySet().size());
		
		
		// What are the communities?
		for (Node node : communities) {
			Set<String> resourcesInCommunity = node.getResources();
			
			System.out.println("Community consists of resources: ");
			for (String r : resourcesInCommunity) {
				System.out.println(r);
			}
		}
		
	}
}