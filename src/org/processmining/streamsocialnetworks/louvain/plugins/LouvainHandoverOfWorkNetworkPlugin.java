package org.processmining.streamsocialnetworks.louvain.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.LSocialNetworkClustered;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;
import org.processmining.streamsocialnetworks.louvain.networks.LouvainSimilarTaskNetwork;
import org.processmining.streamsocialnetworks.louvain.networks.SimilarTaskNetwork;

@Plugin(name = "Louvain Handover Of Work Network Clustering", parameterLabels = { "Event Log" }, returnLabels = {
		"Clustering" }, returnTypes = { LSocialNetworkClustered.class })
public class LouvainHandoverOfWorkNetworkPlugin {
	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Louvain Handover Of Work Network Clustering", requiredParameterLabels = { 0 })
	public static LSocialNetworkClustered runPlugin(PluginContext context, XLog log) {
		// Define the type of the network
		SimilarTaskNetwork network = new SimilarTaskNetwork(log);
		
		// Compute the network
		LSocialNetwork result = new LSocialNetwork(LSocialNetwork.Type.HANDOVER);
		result =  network.computeNetwork();
		
		LSocialNetworkClustered resultClustered = new LSocialNetworkClustered(LSocialNetworkClustered.Type.HANDOVER);
		
		// Compute the clustering
		LouvainSimilarTaskNetwork clustering = new LouvainSimilarTaskNetwork();
		resultClustered =  clustering.louvain(result);
	
		return resultClustered;
	}
}
