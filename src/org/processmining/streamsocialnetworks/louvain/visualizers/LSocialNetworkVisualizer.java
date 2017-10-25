package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

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
			case SIMILAR_TASK :
				// transform the input network to visualization library interface, e.g. graphstream
				// visualize undirected edges;
				// return the visualization
				// panel.add(SlickerFactory.instance().createLabel("This should become an undirected graph."));		
				Graph graph = new SingleGraph("Tutorial 1");
				
				graph.addNode("A" );
				graph.addNode("B" );
				graph.addNode("C" );
				graph.addEdge("AB", "A", "B");
				graph.addEdge("BC", "B", "C");
				graph.addEdge("CA", "C", "A");
				
				Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
				ViewPanel viewPanel = viewer.addDefaultView(false);
				viewPanel.setOpaque(false);
		
				panel.setLayout(new BorderLayout());
		        panel.add(viewPanel, BorderLayout.CENTER);
		        viewer.enableAutoLayout();
     
				break;
			case HANDOVER :
			case WORKING_TOGETHER :
				panel.add(SlickerFactory.instance().createLabel("This should become a directed graph."));
				// transform the input network to visualization library interface, e.g. graphstream
				// visualize directed edges;
				// return the visualization
				break;
		}
		return panel;
	}

}
