package org.processmining.streamsocialnetworks.louvain.visualizers;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Camera;
import org.graphstream.ui.view.Viewer;
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
	public JComponent visualize(final PluginContext context, final LSocialNetwork network) {
		return visualize(network);
		
	}

	public JComponent visualize(final LSocialNetwork network) {
		JComponent panel = new JPanel();
		Graph graph = new SingleGraph("Graph");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		if (network.keySet().isEmpty()) {
			panel.add(SlickerFactory.instance().createLabel("This network does not exist for the imported log."));
			return panel;
		}
		
		switch (network.getNetworkType()) {
			case SIMILAR_TASK :
				// Visualize undirected graph with vertices representing resources
				graph = GraphVisualization.createGraph(GraphVisualization.Type.UNDIRECTED, network);
				
				break;
			case HANDOVER :
				// Visualize directed graph with vertices representing resources
				graph = GraphVisualization.createGraph(GraphVisualization.Type.DIRECTED, network);
				
				break;
			case WORKING_TOGETHER :
				// Visualize directed graph with vertices representing resources
				graph = GraphVisualization.createGraph(GraphVisualization.Type.DIRECTED, network);
		        
				break;
		}
		
		Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.disableAutoLayout();
		
		ViewPanel viewPanel = viewer.addDefaultView(false);
		viewPanel.setOpaque(false);
		
		Camera camera = viewPanel.getCamera();
		camera.setViewPercent(4);
	
		panel.setLayout(new BorderLayout());
        panel.add(viewPanel, BorderLayout.CENTER);
        
		return panel;
	}
}
