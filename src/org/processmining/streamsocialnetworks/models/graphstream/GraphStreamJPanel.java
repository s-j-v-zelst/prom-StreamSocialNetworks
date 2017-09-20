package org.processmining.streamsocialnetworks.models.graphstream;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.graphstream.graph.Graph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

public class GraphStreamJPanel extends JPanel {

	private static final long serialVersionUID = 4170148308605905327L;
	private final Viewer viewer;
	private final ViewPanel view;

	public GraphStreamJPanel(Graph graph) {
		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		view = viewer.addDefaultView(false);
		view.setOpaque(false);
		setLayout(new BorderLayout());
		add(view, BorderLayout.CENTER);
		viewer.enableAutoLayout();
	}

	public Viewer getViewer() {
		return viewer;
	}
	
	

}
