package org.processmining.streamsocialnetworks.dialogs;

import java.util.EnumSet;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;
import org.processmining.streamsocialnetworks.parameters.StreamSocialNetworkMinerParametersImpl;
import org.processmining.widgets.wizard.AbstractDialog;
import org.processmining.widgets.wizard.Dialog;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ResourceIdentifierDialogImpl extends AbstractDialog<StreamSocialNetworkMinerParametersImpl> {

	private static final long serialVersionUID = 5295373828699342868L;
	private final ProMTextField resourceIdentifier;
	@SuppressWarnings("unchecked")
	private final JComboBox<StreamSocialNetwork.Type> defaultNetwork = new ProMComboBox<StreamSocialNetwork.Type>(
			EnumSet.allOf(StreamSocialNetwork.Type.class));

	public ResourceIdentifierDialogImpl(UIPluginContext context, String title,
			StreamSocialNetworkMinerParametersImpl parameters,
			Dialog<StreamSocialNetworkMinerParametersImpl> previous) {
		super(context, title, parameters, previous);
		resourceIdentifier = new ProMTextField(parameters.getResourceIdentifier(),
				"Select what \"key\" to use in order to identify the resource within this data packet.");
	}

	private void addResourceIdentificationParameter() {
		JLabel label = SlickerFactory.instance().createLabel("Resource Identifier:");
		add(label);
		add(resourceIdentifier);
	}

	private void addDefaultNetworkSelector() {
		JLabel label = SlickerFactory.instance().createLabel("Select Default Collaborative Network:");
		add(label);
		add(defaultNetwork);
	}

	public boolean hasNextDialog() {
		return false;
	}

	public void updateParametersOnGetNext() {
		getParameters().setResourceIdentifier(resourceIdentifier.getText());
		getParameters().setDefaultNetworkType((Type) defaultNetwork.getSelectedItem());

	}

	public void updateParametersOnGetPrevious() {
		updateParametersOnGetNext();
	}

	public JComponent visualize() {
		removeAll();
		addResourceIdentificationParameter();
		addDefaultNetworkSelector();
		revalidate();
		repaint();
		return this;
	}

	protected boolean canProceedToNext() {
		return resourceIdentifier.getText().length() != 0;
	}

	protected Dialog<StreamSocialNetworkMinerParametersImpl> determineNextDialog() {
		return null;
	}

	protected String getUserInputProblems() {
		String message = "<html><p> Please provide: </p><ul>";
		message += resourceIdentifier.getText().length() == 0 ? "<li> A Resource Identifier </li>" : "";
		message += "</ul></html>";
		return message;
	}

}
