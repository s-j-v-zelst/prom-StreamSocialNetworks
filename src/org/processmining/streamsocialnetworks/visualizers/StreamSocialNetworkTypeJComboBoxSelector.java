package org.processmining.streamsocialnetworks.visualizers;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork;
import org.processmining.streamsocialnetworks.models.StreamSocialNetwork.Type;

public class StreamSocialNetworkTypeJComboBoxSelector extends ProMComboBox<StreamSocialNetwork.Type> {

	public StreamSocialNetworkTypeJComboBoxSelector(Iterable<Type> values) {
		super(values);
	}

	private static final long serialVersionUID = 2663022400575140745L;

}
