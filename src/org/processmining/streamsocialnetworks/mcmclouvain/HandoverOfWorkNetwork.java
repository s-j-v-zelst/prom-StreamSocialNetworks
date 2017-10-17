package org.processmining.streamsocialnetworks.mcmclouvain;

import java.util.List;
import java.io.File;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.streamsocialnetworks.util.XESImporter;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class HandoverOfWorkNetwork {
	XLog bpiLog;
	
	public HandoverOfWorkNetwork(XLog bpiLog) {
		this.bpiLog = bpiLog; 
	}
	
	/**
	 * Returns a handover of work network with direct succession.
	 */
	public TObjectDoubleMap<ResourcesPair> computeNetwork() {
		// Indicates the values for the handover of work network
		TObjectDoubleMap<ResourcesPair> network = new TObjectDoubleHashMap<>();
		
		// Indicates the number of times resources hand over work
		TObjectDoubleMap<ResourcesPair> handover = new TObjectDoubleHashMap<>();	
		
		// Keeps track of the total number of handovers
		int nrOfHandovers = 0;
		
		// Loop over all traces of the event log
		for (XTrace trace : bpiLog) {
			// Loop over all events of the case
			for (int i = 0; i < trace.size() - 1; i++) {	
				XEvent eventA;
				XEvent eventB;
				
				// Consider only completed events
				String lifeCycle = trace.get(i).getAttributes().get(XLifecycleExtension.KEY_TRANSITION).toString();
				
				// Find completed event
				while (!lifeCycle.equals("COMPLETE") && i < trace.size() - 1) {
					lifeCycle = trace.get(i).getAttributes().get(XLifecycleExtension.KEY_TRANSITION).toString();
					i++;	
				}
				
				if (i < trace.size() - 1) {
					eventA = trace.get(i); 
				} else {
					break;
				}
				
				// Follow up event 
				lifeCycle = trace.get(i + 1).getAttributes().get(XLifecycleExtension.KEY_TRANSITION).toString();
				
				while (!lifeCycle.equals("COMPLETE") && i < trace.size() - 1) {
					lifeCycle = trace.get(i + 1).getAttributes().get(XLifecycleExtension.KEY_TRANSITION).toString();
					i++;	
				}
				
				if (i < trace.size() - 1) {
					eventB = trace.get(i + 1); 
				} else {
					break;
				}
				
				// Ignore the events that do not contain resource information		
				if (eventA.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE) &&
						eventB.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)) {	
					// Get the resources of event A and event B
					String resourceA = eventA.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString();
					String resourceB = eventB.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString();
				
					ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
	
					// Add the resources pair to the set when it is not already present
					if (!handover.containsKey(rp)) {
						handover.put(rp, 0);
					}
					
					// Increase the number of times the resource A hands work over to resource B
					handover.increment(rp);
					
					// Increase the total number of handovers
					nrOfHandovers = nrOfHandovers + 1;
				}
				
			}
		}
		
		network = computeValuesForNetwork(handover, nrOfHandovers);
		
		return network;
	}
	
	/**
	 * Computes the values for the handover of work network
	 */
	public TObjectDoubleMap<ResourcesPair> computeValuesForNetwork(TObjectDoubleMap<ResourcesPair> handover, int nrOfHandovers) {
		TObjectDoubleMap<ResourcesPair> network = new TObjectDoubleHashMap<>();
		double value = 0;
		
		// Iterate over all resources pairs
		for (ResourcesPair rp : handover.keySet()) {
			value = handover.get(rp) / (double) nrOfHandovers;
			
			network.put(rp, value);
		}
	
		return network;
	}

	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		
		HandoverOfWorkNetwork network = new HandoverOfWorkNetwork(bpiLog);
		
		// Compute the values for the similar task network
		TObjectDoubleMap<ResourcesPair> handoverOfWorkNetwork = network.computeNetwork();
		
		// Visualize the network
		MatrixVisualization visualization = new MatrixVisualization();
		List<List<Double>> networkVisualization = visualization.visualizeNetwork(handoverOfWorkNetwork);
				
		for (int i = 0; i < networkVisualization.size(); i++) {
			System.out.println(networkVisualization.get(i));
		}	
	}
}
