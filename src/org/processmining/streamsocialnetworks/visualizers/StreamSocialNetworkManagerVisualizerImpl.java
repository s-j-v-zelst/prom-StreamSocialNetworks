package org.processmining.streamsocialnetworks.visualizers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.stream.core.abstracts.AbstractXSVisualization;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetworkManagerImpl;
import org.processmining.streamsocialnetworks.parameters.StreamSocialNetworkMinerParametersImpl;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class StreamSocialNetworkManagerVisualizerImpl extends AbstractXSVisualization<StreamSocialNetworkManagerImpl> {

	private final JSplitPane splitPane;
	private final JComponent interactionContainer = new JPanel();
	private final JComponent networkContainer = new JPanel();
	@SuppressWarnings("unchecked")
	private final JComboBox<StreamSocialNetwork.Type> ssnSelector = new ProMComboBox<StreamSocialNetwork.Type>(EnumSet.allOf(StreamSocialNetwork.Type.class));
	private final StreamSocialNetworkManagerImpl mgr;

	public StreamSocialNetworkManagerVisualizerImpl(String name, StreamSocialNetworkManagerImpl mgr, StreamSocialNetworkMinerParametersImpl params) {
		super(name);
		for (StreamSocialNetwork.Type t : EnumSet.allOf(StreamSocialNetwork.Type.class)) {
			ssnSelector.addItem(t);
		}
		ssnSelector.setSelectedItem(params.getDefaultNetworkType());
		mgr.setSocialNetworkType(params.getDefaultNetworkType(), mgr.getBuilder().getTrie());
		ssnSelector.addItemListener(new NetworkTypeItemChangeListener());
		this.mgr = mgr;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, networkContainer, interactionContainer);
		setupLayout();
	}

	private void setupLayout() {
		networkContainer.removeAll();

		networkContainer.setLayout(new BorderLayout());
		networkContainer.add(ssnSelector, BorderLayout.NORTH);
		networkContainer.add(mgr.getBuilder().getNetwork().asComponent(), BorderLayout.CENTER);

		setupParameterContainer();
		splitPane.setDividerLocation(0.9);
		splitPane.setResizeWeight(0.9);
		
		splitPane.revalidate();
		splitPane.repaint();
	}

	private void setupParameterContainer() {
		interactionContainer.removeAll();
		interactionContainer.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		List<String> keys = mgr.getBuilder().getParameterKeys();
		List<String> values = mgr.getBuilder().getParameterValues();
		c.gridx = 0;
		c.gridy = 0;
		interactionContainer.add(SlickerFactory.instance().createLabel("Parameters:"));
		c.gridy++;
		for (int i = 0; i < keys.size(); i++) {
			final ProMTextField parameterField = new ProMTextField(values.get(i));
			ParameterFieldFocusListener listener = new ParameterFieldFocusListener(keys.get(i), i, parameterField);
			parameterField.addFocusListener(listener);
			parameterField.addActionListener(listener);
			interactionContainer.add(packComponent(keys.get(i), parameterField), c);
			c.gridy++;
		}
		interactionContainer.revalidate();
		interactionContainer.repaint();
	}

	private class NetworkTypeItemChangeListener implements ItemListener {

		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				StreamSocialNetwork.Type type = (StreamSocialNetwork.Type) e.getItem();
				mgr.setSocialNetworkType(type, mgr.getBuilder().getTrie());
				setupLayout();
			}

		}

	}

	private class ParameterFieldFocusListener implements FocusListener, ActionListener {

		private final String parameterKey;
		private final int keyIndex;
		private final ProMTextField field;

		public ParameterFieldFocusListener(final String key, final int keyIndex, final ProMTextField textfield) {
			parameterKey = key;
			this.keyIndex = keyIndex;
			field = textfield;
		}

		public void focusGained(FocusEvent e) {
			//NOP
		}

		public void focusLost(FocusEvent e) {
			updateParameterValue();
		}

		public void actionPerformed(ActionEvent e) {
			updateParameterValue();

		}

		private void updateParameterValue() {
			String newValue = field.getText();
			String oldValue = mgr.getBuilder().getParameterValues().get(keyIndex);
			if (!newValue.equals(oldValue)) {
				mgr.getBuilder().setParameter(parameterKey, newValue);
			}
		}
	}

	public JComponent asComponent() {
		return splitPane;
	}

	public void update(Pair<Date, String> message) {
		// NOP
	}

	public void update(String object) {
		//
	}

	public void updateVisualization(Pair<Date, StreamSocialNetworkManagerImpl> newArtifact) {
		// NOP
	}

	public void updateVisualization(StreamSocialNetworkManagerImpl newArtifact) {
	}

	protected void workPackage() {
		// NOP
	}

	/**
	 * stolen from promPropertiesPanel
	 * 
	 * @param name
	 * @param component
	 * @return
	 */
	protected RoundedPanel packComponent(final String name, final JComponent component) {
		final RoundedPanel packed = new RoundedPanel(10, 0, 0);
		packed.setBackground(new Color(60, 60, 60, 160));
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));

		component.setMinimumSize(new Dimension(150, 20));
		component.setMaximumSize(new Dimension(500, 50));
		component.setPreferredSize(new Dimension(150, 30));

		final JLabel nameLabel = new JLabel(name);
		nameLabel.setOpaque(false);
		nameLabel.setForeground(WidgetColors.TEXT_COLOR);
		nameLabel.setFont(nameLabel.getFont().deriveFont(10f));
		nameLabel.setMinimumSize(new Dimension(50, 20));
		nameLabel.setMaximumSize(new Dimension(50, 50));
		nameLabel.setPreferredSize(new Dimension(50, 30));

		packed.add(Box.createHorizontalStrut(5));
		packed.add(nameLabel);
		packed.add(Box.createHorizontalGlue());
		packed.add(component);
		packed.add(Box.createHorizontalStrut(5));
		packed.revalidate();
		packed.repaint();
		return packed;
	}

}
