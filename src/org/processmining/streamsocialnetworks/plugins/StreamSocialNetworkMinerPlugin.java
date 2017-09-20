package org.processmining.streamsocialnetworks.plugins;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.eventstream.core.interfaces.XSEvent;
import org.processmining.eventstream.core.interfaces.XSEventStream;
import org.processmining.eventstream.readers.trie.StreamTrieImpl;
import org.processmining.eventstream.readers.trie.VertexImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.stream.core.interfaces.XSReader;
import org.processmining.streamsocialnetworks.algorithms.StreamSocialNetworkMinerImpl;
import org.processmining.streamsocialnetworks.dialogs.ResourceIdentifierDialogImpl;
import org.processmining.streamsocialnetworks.models.ActivityResourcePair;
import org.processmining.streamsocialnetworks.models.StreamSocialNetworkManagerImpl;
import org.processmining.streamsocialnetworks.parameters.StreamSocialNetworkMinerParametersImpl;
import org.processmining.widgets.wizard.Wizard;
import org.processmining.widgets.wizard.WizardResult;

@Plugin(name = "Discover Cooperative Network(s)", parameterLabels = { "Event Stream", "Parameters" }, returnLabels = {
		"Online Cooperative Network Miner" }, returnTypes = { XSReader.class })
public class StreamSocialNetworkMinerPlugin {

	@UITopiaVariant(author = "S.J. van Zelst", email = "s.j.v.zelst@tue.nl", affiliation = "Eindhoven University of Technology")
	@PluginVariant(variantLabel = "Discover Cooperative Network(s)", requiredParameterLabels = { 0 })
	public static XSReader<XSEvent, StreamSocialNetworkManagerImpl> apply(final UIPluginContext context,
			final XSEventStream stream) {
		WizardResult<StreamSocialNetworkMinerParametersImpl> wizRes = Wizard.show(context,
				new ResourceIdentifierDialogImpl(context, "Configure Online Cooperative Network Miner",
						new StreamSocialNetworkMinerParametersImpl(), null));
		if (wizRes.getInteractionResult().equals(TaskListener.InteractionResult.FINISHED)) {
			return apply(context, stream, wizRes.getParameters());
		} else if (wizRes.getInteractionResult().equals(TaskListener.InteractionResult.CANCEL)) {
			context.getFutureResult(0).cancel(true);
		}
		return null;
	}

	@PluginVariant(variantLabel = "Discover Cooperative Network(s)", requiredParameterLabels = { 0, 1 })
	public static XSReader<XSEvent, StreamSocialNetworkManagerImpl> apply(final PluginContext context,
			final XSEventStream stream, final StreamSocialNetworkMinerParametersImpl parameters) {

		List<String> mandatoryKeys = new ArrayList<>();
		mandatoryKeys.add(parameters.getActivityIdentifier());
		mandatoryKeys.add(parameters.getResourceIdentifier());
		parameters.setMandatoryKeys(mandatoryKeys);

		ActivityResourcePair rootObject = new ActivityResourcePair("root", "root");
		VertexImpl<ActivityResourcePair> root = new VertexImpl<ActivityResourcePair>(rootObject);
		StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>> trie = new StreamTrieImpl<ActivityResourcePair, VertexImpl<ActivityResourcePair>>(
				root);
		StreamSocialNetworkManagerImpl mgr = new StreamSocialNetworkManagerImpl();
		mgr.setSocialNetworkType(parameters.getDefaultNetworkType(), trie);
		XSReader<XSEvent, StreamSocialNetworkManagerImpl> reader = new StreamSocialNetworkMinerImpl(
				"stream social network miner", parameters, mgr, trie);
		reader.start();
		stream.connect(reader);
		return reader;
	}

}
