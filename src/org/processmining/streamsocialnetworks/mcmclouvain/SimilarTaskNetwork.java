package org.processmining.streamsocialnetworks.mcmclouvain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.File;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.streamsocialnetworks.util.XESImporter;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class SimilarTaskNetwork {
	XLog bpiLog;
	
	public SimilarTaskNetwork(XLog bpiLog) {
		this.bpiLog = bpiLog; 
	}
	
	/**
	 * Returns a similar tasks network.
	 */
	public TObjectDoubleMap<ResourcesPair> computeNetwork() {
		// Indicates the number of times a resource performs an activity
		TObjectDoubleMap<ResourceActivityPair> activityCount = new TObjectDoubleHashMap<>();
		
		// Indicates the values for the working together network
		TObjectDoubleMap<ResourcesPair> network = new TObjectDoubleHashMap<>();
		
		// Loop over all traces of the event log
		for (XTrace trace : bpiLog) {
			// Loop over all events of the case
			for (int i = 0; i < trace.size(); i++) {
				XEvent event = trace.get(i); 
				
				// Ignore the events that do not contain resource information		
				if (event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)) {		
					String resource = event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString();
					String activity = event.getAttributes().get(XConceptExtension.KEY_NAME).toString();
				
					ResourceActivityPair rap = new ResourceActivityPair(resource, activity);

					// Add the resource activity pair to the set when it is not already present
					if (!activityCount.containsKey(rap)) {
						activityCount.put(rap, 0);
					}
					
					// Increase the number of times the resource performs an activity with 1
					activityCount.increment(rap);
				}
			}
		}
		
		network = computeValuesForNetwork(activityCount);
		
		return network;
	}
	
	/**
	 * Computes the values for the similar task network
	 */
	public TObjectDoubleMap<ResourcesPair> computeValuesForNetwork(TObjectDoubleMap<ResourceActivityPair> activityCount) {
		TObjectDoubleMap<ResourcesPair> network = new TObjectDoubleHashMap<>();
		
		Set<ResourceActivityPair> rpSet =  new HashSet<>();
		rpSet = activityCount.keySet();
		
		// Iterate over all resource activity pairs 
		for (ResourceActivityPair rap : rpSet) {
			for (ResourceActivityPair rap2 : rpSet) {
				String resourceA = rap.getResource();
				String resourceB = rap2.getResource();
				
				// Increment the similar task value with 1 if two resources are different and they perform the same task
				if (!resourceA.equals(resourceB)) {
					if (rap.getActivity().equals(rap2.getActivity())) {
						ResourcesPair rp = new ResourcesPair(resourceA, resourceB);
						
						// Add the resource activity pair to the set when it is not present
						if (!network.containsKey(rp)) {
							network.put(rp, 0);
						}
						
						network.increment(rp);
					}
				}
			}	
		}
		
		return network;
	}
	
	/**
	 * Returns a matrix representation of the working together network
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
		
		SimilarTaskNetwork network = new SimilarTaskNetwork(bpiLog);
		
		// Compute the values for the similar task network
		TObjectDoubleMap<ResourcesPair> similarTaskNetwork = network.computeNetwork();
		// Visualize the network
		List<List<Double>> similarTaskNetworkVisualization = network.visualizeNetwork(similarTaskNetwork);
				
		for (int i = 0; i < similarTaskNetworkVisualization.size(); i++) {
			System.out.println(similarTaskNetworkVisualization.get(i));
		}	
	}
}
