package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
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
		Graph graph = new SingleGraph("Graph Clustered");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		switch (network.getNetworkType()) {
			case SIMILAR_TASK :
				// Visualize undirected graph with vertices representing resources
				graph = GraphClusterVisualization.createGraph(GraphClusterVisualization.Type.UNDIRECTED, network);
			
				break;
			case HANDOVER :
				// Visualize directed graph with vertices representing resources
				graph = GraphClusterVisualization.createGraph(GraphClusterVisualization.Type.DIRECTED, network);
			
				break;
			case WORKING_TOGETHER :
				// Visualize directed graph with vertices representing resources
				graph = GraphClusterVisualization.createGraph(GraphClusterVisualization.Type.DIRECTED, network);
	        
				break;
		}
		
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		ViewPanel viewPanel = viewer.addDefaultView(false);
		viewPanel.setOpaque(false);

		panel.setLayout(new BorderLayout());
        panel.add(viewPanel, BorderLayout.CENTER);
        viewer.enableAutoLayout();

		return panel;
	}

}
