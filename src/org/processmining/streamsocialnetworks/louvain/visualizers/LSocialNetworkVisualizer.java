package org.processmining.streamsocialnetworks.louvain.visualizers;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Social Network Visualizer", parameterLabels = { "Social Network" }, returnLabels = {
		"Social Network Visualizer" }, returnTypes = { JComponent.class })
@Visualizer
public class LSocialNetworkVisualizer {

	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Social Network Visualizer", requiredParameterLabels = { 0 })
	@Visualizer
	public static JComponent visualize(final PluginContext context, final LSocialNetwork network) {
		return visualize(network);
	}

	public static JComponent visualize(final LSocialNetwork network) {
		JComponent panel = new JPanel();
		switch (network.getNetworkType()) {
			case HANDOVER :
			case WORKING_TOGETHER :
				panel.add(SlickerFactory.instance().createLabel("This should become a directed graph."));
				// transfrom the input network to visualization library interface, e.g. graphstream
				// visualize directed edges;
				// return the visualization
				break;
			case SIMILAR_TASK :
				panel.add(SlickerFactory.instance().createLabel("This should become an undirected graph."));
				// transfrom the input network to visualization library interface, e.g. graphstream
				// visualize undirected edges;
				// return the visualization
				break;
		}
		return panel;
	}

}
