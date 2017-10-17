package org.processmining.streamsocialnetworks.mcmclouvain;

import java.io.File;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.streamsocialnetworks.util.XESImporter;

public class ExampleLogIterator {

	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		for (XTrace trace : bpiLog) {
			String traceIdentifier = trace.getAttributes().get(XConceptExtension.KEY_NAME).toString();
			for (int i = 0; i < trace.size(); i++) {
				XEvent event = trace.get(i);
				String activityName = event.getAttributes().get(XConceptExtension.KEY_NAME).toString();
				String resource = event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)
						? event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString()
						: "magic";
				String lifeCycle = event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION).toString();
				System.out.println("for trace " + traceIdentifier + ", " + "event " + (i + 1) + " has name: "
						+ activityName + "_" + lifeCycle + " and was executed by: " + resource);
			}
		}
	}

}
