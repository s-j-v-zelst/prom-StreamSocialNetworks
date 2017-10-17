package org.processmining.streamsocialnetworks.louvain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gnu.trove.map.TObjectDoubleMap;

public class MatrixVisualization {
	
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
}
