package org.processmining.streamsocialnetworks.models.graphstream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.processmining.framework.util.Pair;
import org.processmining.stream.core.abstracts.AbstractXSRunnable;
import org.processmining.stream.core.interfaces.XSVisualization;
import org.processmining.streamsocialnetworks.models.mtj.SSNSquareMatrix;

import no.uib.cipr.matrix.Matrix;

public class SSNSquareMatrixGraphStreamVisualization<T> extends AbstractXSRunnable
		implements XSVisualization<SSNSquareMatrix<T>> {
	
	static {
		// use a more advanced renderer for the graph
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	}

	private Graph graph;
	private final GraphStreamJPanel graphPanel;

	private List<String> labels;

	public SSNSquareMatrixGraphStreamVisualization(String name, SSNSquareMatrix<T> initialMatrix) {
		super(name);
		graph = new SingleGraph("Social Network");
		graphPanel = new GraphStreamJPanel(graph);
		graph.setStrict(false);
		this.labels = copyLabels(initialMatrix.getObjects());
		updateVertices(labels);
		updateEdges(initialMatrix);
	}

	public JComponent asComponent() {
		return graphPanel;
	}

	private List<String> copyLabels(List<T> objects) {
		List<String> l = new ArrayList<>();
		for (T t : objects) {
			if (t != null) {
				l.add(t.toString());
			} else {
				l.add(null);
			}
		}
		return l;
	}

	@Deprecated
	public void update(Pair<Date, String> message) {
	}

	@Deprecated
	public void update(String object) {
	}

	private void updateEdges(Matrix matrix) {
		for (int r = 0; r < matrix.numRows(); r++) {
			for (int c = 0; c < matrix.numColumns(); c++) {
				//TODO: ADD NULL CHECK
				if (r < labels.size() && c < labels.size()) {
					String edgeLabel = labels.get(r) + "_" + labels.get(c);
					if (matrix.get(r, c) != 0d) {
						if (graph.getEdge(edgeLabel) == null) {
							graph.addEdge(edgeLabel, labels.get(r), labels.get(c), true);
						}
					} else {
						if (graph.getEdge(edgeLabel) != null) {
							graph.removeEdge(labels.get(r) + "_" + labels.get(c));
						}
					}
				}
			}
		}
	}

	private void updateVertices(List<String> labels) {
		for (String l : labels) {
			if (graph.getNode(l) == null) {
				Node n = graph.addNode(l);
				n.addAttribute("ui.label", l);
			}
		}
		Set<Node> remove = new HashSet<>();
		for (Node n : graph.getNodeSet()) {
			if (!labels.contains(n.getId())) {
				remove.add(n);
			}
		}
		for (Node n : remove) {
			graph.removeNode(n);
		}
	}

	public void updateVisualization(Pair<Date, SSNSquareMatrix<T>> newArtifact) {
		updateVisualization(newArtifact);
	}

	public void updateVisualization(SSNSquareMatrix<T> newArtifact) {
		visualize(newArtifact);
	}

	public JComponent visualize(SSNSquareMatrix<T> streamBasedObject) {
		labels = copyLabels(streamBasedObject.getObjects());
		updateVertices(labels);
		updateEdges(streamBasedObject);
		graphPanel.revalidate();
		graphPanel.repaint();
		return graphPanel;
	}

	protected void workPackage() {
	}

}
