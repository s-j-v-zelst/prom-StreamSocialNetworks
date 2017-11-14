package org.processmining.streamsocialnetworks.louvain.plugins;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork.Type;
import org.processmining.streamsocialnetworks.louvain.networks.SimilarTaskNetwork;

@Plugin(name = "Louvain clustering similar task network", parameterLabels = { "Event Log" }, returnLabels = {
		"Social Network" }, returnTypes = { LSocialNetwork.class })
public class SimilarTaskNetworkPlugin {
	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Similar Task Network", requiredParameterLabels = { 0 })
	public static LSocialNetwork runPlugin(PluginContext context, XLog log) {
		// Define the type of the network
		SimilarTaskNetwork network = new SimilarTaskNetwork(log);
		LSocialNetwork result = new LSocialNetwork(LSocialNetwork.Type.SIMILAR_TASK);
		
		// Compute the network
		result =  network.computeNetwork();
	
		return result;
	}
}
