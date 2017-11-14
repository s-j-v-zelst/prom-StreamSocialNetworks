package org.processmining.streamsocialnetworks.louvain.networks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.File;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.streamsocialnetworks.louvain.LSocialNetwork;
import org.processmining.streamsocialnetworks.louvain.ResourcesPair;
import org.processmining.streamsocialnetworks.louvain.MatrixVisualization;
import org.processmining.streamsocialnetworks.util.XESImporter;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class WorkingTogetherNetwork {
	XLog bpiLog;
	
	public WorkingTogetherNetwork(XLog bpiLog) {
		this.bpiLog = bpiLog; 
	}
	
	/**
	 * Returns a working together network.
	 */
	public LSocialNetwork computeNetwork() {
		// Indicates the number of times resources work together
		TObjectDoubleMap<ResourcesPair> workingTogether = new TObjectDoubleHashMap<>();
		
		// Indicates the number of times a resource is working
		TObjectDoubleMap<String> working = new TObjectDoubleHashMap<>();				
		
		// Indicates the values for the working together network
		LSocialNetwork network = new LSocialNetwork(LSocialNetwork.Type.WORKING_TOGETHER);
		
		// List of resources 
		List<String> resources = new ArrayList<>();
		
		// Loop over all traces of the event log
		for (XTrace trace : bpiLog) {
			// Loop over all events of the case
			for (int i = 0; i < trace.size(); i++) {
				XEvent event = trace.get(i); 
				
				// Ignore the events that do not contain resource information		
				if (event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)) {	
					// Resource that executes the event
					String resource = event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString();
					
					// Add resource to the list of resources
					if (!resources.contains(resource)) {
						resources.add(resource);
					}	
				}
			}
			
			// Update the working together values of the resources and the working value of resourceA
			for (String resourceA : resources) {
				// Increase the number of times resourceA is working with 1
				if (!working.containsKey(resourceA)) {
					working.put(resourceA, 0);
				}
				
				// For all pairs in the resources list
				for (String resourceB : resources) {
					if (!resourceA.equals(resourceB)) {
						ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
						
						// Add the resource activity pair to the set when it is not present
						if (!workingTogether.containsKey(rp)) {
							workingTogether.put(rp, 0);
						}
						
						// Increase the number of times the resources work together with 1
						workingTogether.increment(rp);
						
						// Increase the number of times the resources work together with 1
						working.increment(resourceA);
					}					
				}
			}
			
			// Clear the resources list for this case such that an empty list is used for the next iteration
			resources.clear();
					
		}
		network = computeValuesForNetwork(workingTogether, working);
		
		return network;	
	}
	
	/**
	 * Computes the values for the working together network
	 */
	public LSocialNetwork computeValuesForNetwork(TObjectDoubleMap<ResourcesPair> workingTogether, 
			TObjectDoubleMap<String> working) {
		LSocialNetwork network = new LSocialNetwork(LSocialNetwork.Type.HANDOVER);
		
		Set<ResourcesPair> rpSet =  new HashSet<>();
		rpSet = workingTogether.keySet();
		
		for (ResourcesPair rp : rpSet) {
			// The number of times the resources pair works together divided by the number of times resourcesA works
			double value = workingTogether.get(rp) / working.get(rp.getResourceA());
			
			network.put(rp, value);
		}
		
		return network;
	}
	
	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\Logs\\teleclaim.xes"));
		
		WorkingTogetherNetwork network = new WorkingTogetherNetwork(bpiLog);
		
		// Compute the values for the working together network
		LSocialNetwork workingTogetherNetwork = network.computeNetwork();
		
		// Visualize the network
		MatrixVisualization visualization = new MatrixVisualization();
		List<List<Double>> networkVisualization = visualization.visualizeNetwork(workingTogetherNetwork);
				
		for (int i = 0; i < networkVisualization.size(); i++) {
			System.out.println(networkVisualization.get(i));
		}	
	}
}
