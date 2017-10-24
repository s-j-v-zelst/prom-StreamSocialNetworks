package org.processmining.streamsocialnetworks.louvain;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Louvain Network Clustering", parameterLabels = { "Event Log" }, returnLabels = {
		"Clustering" }, returnTypes = { LSocialNetwork.class })
public class LouvainPlugin {

	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Louvain Network Clustering", requiredParameterLabels = { 0 })
	public static LSocialNetwork runPlugin(PluginContext context, XLog log) {
		//TODO: make social network....
		return new LSocialNetwork(LSocialNetwork.Type.SIMILAR_TASK);
	}
}
