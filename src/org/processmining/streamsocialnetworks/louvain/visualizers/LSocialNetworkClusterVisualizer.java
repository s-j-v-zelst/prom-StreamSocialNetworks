package org.processmining.streamsocialnetworks.louvain.visualizers;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetworkClustered;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Social Network Cluster Visualizer", parameterLabels = { "Social Network Clustering" }, returnLabels = {
		"Social Network Cluster Visualizer" }, returnTypes = { JComponent.class })
@Visualizer
public class LSocialNetworkClusterVisualizer {

	@UITopiaVariant(author = "C. Verhoef", email = "c.verhoef@student.tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Social Network Cluster Visualizer", requiredParameterLabels = { 0 })
	@Visualizer
	public static JComponent visualize(final PluginContext context, final LSocialNetworkClustered network) {
		return visualize(network);
	}

	public static JComponent visualize(final LSocialNetworkClustered network) {
		JComponent panel = new JPanel();
		switch (network.getNetworkType()) {
			case HANDOVER :
			case WORKING_TOGETHER :
				panel.add(SlickerFactory.instance().createLabel("This should become a directed clustered graph."));
				// transfrom the input network to visualization library interface, e.g. graphstream
				// visualize directed edges;
				// return the visualization
				break;
			case SIMILAR_TASK :
				panel.add(SlickerFactory.instance().createLabel("This should become an undirected clustered graph."));
				// transfrom the input network to visualization library interface, e.g. graphstream
				// visualize undirected edges;
				// return the visualization
				break;
		}
		return panel;
	}

}
