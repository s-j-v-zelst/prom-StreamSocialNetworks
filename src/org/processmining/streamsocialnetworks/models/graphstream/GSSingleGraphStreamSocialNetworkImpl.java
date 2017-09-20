package org.processmining.streamsocialnetworks.models.graphstream;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComponent;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.processmining.streamsocialnetworks.models.SSNEdgeLabelStyle;
import org.processmining.streamsocialnetworks.models.SSNEdgeStyle;
import org.processmining.streamsocialnetworks.models.SSNLink;
import org.processmining.streamsocialnetworks.models.SSNNodeStyle;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;

public class GSSingleGraphStreamSocialNetworkImpl<T> extends SingleGraph implements StreamSocialNetwork<T> {

	static {
		// use a more advanced renderer for the graph
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	}

	private final GraphStreamJPanel graphPanel;
	private final Map<T, Collection<SSNLink<T>>> inArcs = new HashMap<>();
	private final Map<SSNLink<T>, Edge> links = new ConcurrentHashMap<>();
	private final Map<T, Collection<SSNLink<T>>> outArcs = new HashMap<>();
	private SpriteManager sman;
	private final Map<T, Node> vertices = new ConcurrentHashMap<>();

	public GSSingleGraphStreamSocialNetworkImpl(String id) {
		super(id);
		graphPanel = new GraphStreamJPanel(this);
		sman = new SpriteManager(this);
		addAttribute("ui.stylesheet", GSSingleGraphDefaultLayout.LAYOUT);
		addAttribute("ui.quality");
		addAttribute("ui.antialias");
	}

	public void addLink(SSNLink<T> link) {
		if (getNode(link.getFrom().toString()) == null) {
			addResource(link.getFrom());
		}
		if (getNode(link.getTo().toString()) == null) {
			addResource(link.getTo());
		}
		Edge e = addEdge(constructLinkId(link), link.getFrom().toString(), link.getTo().toString(), true);
		Sprite eFreq = sman.addSprite("s_" + e.getId());
		eFreq.setPosition(0.5, 0, 0);
		//		eFreq.addAttribute("ui.label",
		//				Double.toString(new BigDecimal(link.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue()));
		eFreq.attachToEdge(e.getId());

		outArcs.get(link.getFrom()).add(link);
		inArcs.get(link.getTo()).add(link);
		links.put(link, e);
	}

	public void addResource(T resource) {
		Node n = addNode(resource.toString());
		if (outArcs.get(resource) == null) {
			outArcs.put(resource, new HashSet<SSNLink<T>>());
		}
		if (inArcs.get(resource) == null) {
			inArcs.put(resource, new HashSet<SSNLink<T>>());
		}
		vertices.put(resource, n);
		n.setAttribute("ui.color", 0.5);
	}

	public JComponent asComponent() {
		return graphPanel;
	}

	private void checkAndRemoveIfIsolated(T t) {
		if (outArcs.get(t).isEmpty() && inArcs.get(t).isEmpty()) {
			removeNode(vertices.get(t));
			vertices.remove(t);
			inArcs.remove(t);
			outArcs.remove(t);
		}
	}

	public void clear() {
		super.clear();
		inArcs.clear();
		links.clear();
		outArcs.clear();
		vertices.clear();
		sman = new SpriteManager(this);
		addAttribute("ui.stylesheet", GSSingleGraphDefaultLayout.LAYOUT);
	}

	private String constructLinkId(SSNLink<T> link) {
		return link.getFrom() + "->" + link.getTo().toString();
	}

	public org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Implementation getImplementation() {
		return Implementation.GRAPH_STREAM;
	}

	public SSNLink<T> getLink(T from, T to) {
		if (outArcs.containsKey(from)) {
			for (SSNLink<T> link : outArcs.get(from)) {
				if (link.getTo().equals(to)) {
					return link;
				}
			}
		}
		return null;
	}

	public Collection<SSNLink<T>> getLinks() {
		return links.keySet();
	}

	public Collection<SSNLink<T>> getLinks(boolean excludeZero) {
		if (excludeZero) {
			Collection<SSNLink<T>> result = new HashSet<>();
			for (SSNLink<T> link : links.keySet()) {
				if (link.getValue() != 0d) {
					result.add(link);
				}
			}
			return result;
		}
		return getLinks();
	}

	public Collection<SSNLink<T>> getLinks(boolean excludeZero, boolean excludeNegative) {
		if (excludeZero && !excludeNegative) {
			return getLinks(excludeZero);
		} else if (excludeZero && excludeNegative) {
			Collection<SSNLink<T>> result = new HashSet<>();
			for (SSNLink<T> link : links.keySet()) {
				if (link.getValue() > 0d) {
					result.add(link);
				}
			}
			return result;
		}
		return getLinks();
	}

	public double getLinkValue(T from, T to) {
		return getLink(from, to).getValue();
	}

	public void removeLink(SSNLink<T> link) {
		if (links.containsKey(link)) { // check if the link is actually defined....
			try {
				removeEdge(links.get(link));
			} catch (NullPointerException e) {
				// link not present in the graph...
			}
			links.remove(link);
			T from = link.getFrom();
			T to = link.getTo();
			outArcs.get(from).remove(link);
			inArcs.get(to).remove(link);
			sman.removeSprite("s_" + constructLinkId(link));
			checkAndRemoveIfIsolated(from);
			if (!from.equals(to)) {
				checkAndRemoveIfIsolated(to);
			}
		}

	}

	public void setLinkCount(SSNLink<T> link, int count) {
		link.setCount(count);
	}

	public void setLinkLabel(SSNLink<T> link, String label) {
		if (getLink(link.getFrom(), link.getTo()) != null) {
			Sprite s = sman.getSprite("s_" + constructLinkId(link));
			//			double linkValRounded = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
			//			Double.toString(linkValRounded)
			s.changeAttribute("ui.label", label);
		}
	}

	private String getCSSStyleClass(SSNEdgeStyle style) {
		switch (style) {
			case HIGH :
				return GSSingleGraphDefaultLayout.CLASS_HIGH;
			case LOW :
				return GSSingleGraphDefaultLayout.CLASS_LOW;
			case NEUTRAL :
				return GSSingleGraphDefaultLayout.CLASS_NEUTRAL;
			case VERY_HIGH :
				return GSSingleGraphDefaultLayout.CLASS_VERY_HIGH;
			case VERY_LOW :
				return GSSingleGraphDefaultLayout.CLASS_VERY_LOW;
			case DELETED :
				return GSSingleGraphDefaultLayout.CLASS_DELETED;
			case NEW :
				return GSSingleGraphDefaultLayout.CLASS_NEW;
		}
		return GSSingleGraphDefaultLayout.CLASS_NEUTRAL;
	}

	private String getCSSStyleClass(SSNEdgeLabelStyle style) {
		switch (style) {
			case DOWN :
				return GSSingleGraphDefaultLayout.CLASS_DOWN;
			case UP :
				return GSSingleGraphDefaultLayout.CLASS_UP;
			default :
				return null;
		}
	}

	public void setLinkStyle(SSNLink<T> link, SSNEdgeStyle style) {
		if (links.containsKey(link)) {
			Edge e = links.get(link);
			e.addAttribute("ui.class", getCSSStyleClass(style));
		}
	}

	public void setLinkStyle(SSNLink<T> link, SSNEdgeStyle... styles) {
		if (links.containsKey(link)) {
			Edge e = links.get(link);
			String stylesStr = "";
			for (int i = 0; i < styles.length; i++) {
				stylesStr += getCSSStyleClass(styles[i]);
				if (i < styles.length - 1) {
					stylesStr += ", ";
				}
			}
			e.addAttribute("ui.class", stylesStr);
		}

	}

	public void setLinkValue(SSNLink<T> link, double value) {
		link.setValue(value);
	}

	@Deprecated
	public void setLinkValueWithChange(SSNLink<T> link, double value, double change) {
		link.setValue(value);
		if (getLink(link.getFrom(), link.getTo()) != null) {
			Sprite s = sman.getSprite("s_" + constructLinkId(link));
			if (value == Double.MIN_VALUE && change == Double.MIN_VALUE) {
				s.changeAttribute("ui.label", "inactive");
				setLinkStyle(link, SSNEdgeStyle.DELETED);
			} else if (value == Double.MAX_VALUE && change == Double.MAX_VALUE) {
				s.changeAttribute("ui.label", "new");
				setLinkStyle(link, SSNEdgeStyle.NEW);
			} else {
				s.removeAttribute("ui.label");
				//				double linkValRounded = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
				//				double changeValRounded = new BigDecimal(change * 100).setScale(0, RoundingMode.HALF_UP).doubleValue();
				//				String spriteLabel = Double.toString(linkValRounded) + " (";
				//				if (change > 0) {
				//					spriteLabel += "+";
				//				}
				//				spriteLabel += changeValRounded + "%)";
				//				s.changeAttribute("ui.label", spriteLabel);
				if (change > 0.5) {
					setLinkStyle(link, SSNEdgeStyle.VERY_HIGH);
				} else if (change > 0.1) {
					setLinkStyle(link, SSNEdgeStyle.HIGH);
				} else if (change > -0.1) {
					setLinkStyle(link, SSNEdgeStyle.NEUTRAL);
				} else if (change > -0.5) {
					setLinkStyle(link, SSNEdgeStyle.LOW);
				} else {
					setLinkStyle(link, SSNEdgeStyle.VERY_LOW);
				}
			}
		}
	}

	public void setNodeLabel(T node, String label) {
		Node n = vertices.get(node);
		if (n != null) {
			n.addAttribute("ui.label", label);
		}
	}

	public void setNodeStyle(T node, SSNNodeStyle style) {
		if (vertices.containsKey(node)) {
			Node nodeInGraph = vertices.get(node);
			switch (style) {
				case HIGH :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_HIGH);
					break;
				case LOW :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_LOW);
					break;
				case NEUTRAL :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_NEUTRAL);
					break;
				case VERY_HIGH :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_VERY_HIGH);
					break;
				case VERY_LOW :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_VERY_LOW);
					break;
				case NEW :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_NEW);
					break;
				case DELETED :
					nodeInGraph.addAttribute("ui.class", GSSingleGraphDefaultLayout.CLASS_DELETED);
					break;
				default :
					break;

			}
		}

	}

	public void setLinkLabelStyle(SSNLink<T> link, SSNEdgeLabelStyle style) {
		if (getLink(link.getFrom(), link.getTo()) != null) {
			Sprite s = sman.getSprite("s_" + constructLinkId(link));
			s.changeAttribute("ui.class", getCSSStyleClass(style));
		}
	}

}
