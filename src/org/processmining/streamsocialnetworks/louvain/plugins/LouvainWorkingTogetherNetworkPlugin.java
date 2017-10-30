package org.processmining.streamsocialnetworks.louvain.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.LSocialNetworkClustered;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;
import org.processmining.streamsocialnetworks.louvain.networks.LouvainWorkingTogetherNetwork;
import org.processmining.streamsocialnetworks.louvain.networks.WorkingTogetherNetwork;

@Plugin(name = "Louvain Working Together Network Clustering", parameterLabels = { "Event Log" }, returnLabels = {
		"Clustering" }, returnTypes = { LSocialNetworkClustered.class })
public class LouvainWorkingTogetherNetworkPlugin {
	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Louvain Similar Task Network Clustering", requiredParameterLabels = { 0 })
	public static LSocialNetworkClustered runPlugin(PluginContext context, XLog log) {
		// Define the type of the network
		WorkingTogetherNetwork network = new WorkingTogetherNetwork(log);
		
		// Compute the network
		LSocialNetwork result = new LSocialNetwork(LSocialNetwork.Type.SIMILAR_TASK);
		result =  network.computeNetwork();
		
		LSocialNetworkClustered resultClustered = new LSocialNetworkClustered(LSocialNetworkClustered.Type.SIMILAR_TASK);
		
		// Compute the clustering
		LouvainWorkingTogetherNetwork clustering = new LouvainWorkingTogetherNetwork();
		resultClustered =  clustering.louvain(result);
	
		return resultClustered;
	}
}
