package org.processmining.streamsocialnetworks.mcmclouvain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	
	/**
	 * Returns a matrix representation of the network
	 */
	public List<List<Double>> visualizeNetwork(TObjectDoubleMap<ResourcesPair> workingTogetherNetwork) {
		// Matrix indicating the values of the working together network
		List<List<Double>> network = new ArrayList<>();
		
		// Initialize entry [0,0] to be -1
		network.add(new ArrayList<Double>());
		network.get(0).add(0, -1.0);
				
		Set<ResourcesPair> rpSet =  new HashSet<>();
		rpSet = workingTogetherNetwork.keySet();
		
		for (ResourcesPair rp : rpSet) {
			double resourceA = Double.parseDouble(rp.getResourceA());
			double resourceB = Double.parseDouble(rp.getResourceB());
			
			// Update the similar tasks matrix when resourceA is not seen yet
			if (!network.get(0).contains(resourceA)) {				
				network = updateMatrix(network, resourceA);			
			} 
			
			// Update the similar tasks matrix when resourceB is not seen yet
			if (!network.get(0).contains(resourceB)) {				
				network = updateMatrix(network, resourceB);			
			} 
			
			// Get the index of the resources
			int indexA = network.get(0).indexOf(resourceA);				
			int indexB = network.get(0).indexOf(resourceB);						

			// Assign the working together value of the resources pair
			network.get(indexA).set(indexB, workingTogetherNetwork.get(rp));		
		}
			
		return network;
	}
	
	/**
	 * Update the matrix when a new resource is seen (= add new row and column and fill entries with 0)
	 */
	public List<List<Double>> updateMatrix(List<List<Double>> matrix, double resource) {
		// Update the matrix with a new row and column
		matrix.add(new ArrayList<Double>());	
		
		// Assign value 0 to the new entries to fill up the matrix
		for (int i = 0; i < matrix.size()-1; i++) {
			matrix.get(matrix.size()-1).add(i, 0.0);
		}	
		
		for (int i = 0; i < matrix.size(); i++) {
			matrix.get(i).add(matrix.size()-1, 0.0);
		}					
		
		// Add the resource ID to the row and column	
		matrix.get(0).set(matrix.size()-1, resource);	// Add resource to the first row					
		matrix.get(matrix.size()-1).set(0, resource);	// Add resource to first column 		
		
		return matrix;
	}
	

	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		
		HandoverOfWorkNetwork network = new HandoverOfWorkNetwork(bpiLog);
		
		// Compute the values for the similar task network
		TObjectDoubleMap<ResourcesPair> handoverOfWorkNetwork = network.computeNetwork();
		// Visualize the network
		List<List<Double>> networkVisualization = network.visualizeNetwork(handoverOfWorkNetwork);
				
		for (int i = 0; i < networkVisualization.size(); i++) {
			System.out.println(networkVisualization.get(i));
		}	
	}
}
