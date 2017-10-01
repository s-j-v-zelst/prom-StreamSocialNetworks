package org.processmining.streamsocialnetworks.mcmclouvain;

import java.util.ArrayList;
import java.util.List;

import java.io.File;

import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.streamsocialnetworks.util.XESImporter;

public class WorkingTogetherNetwork {
	XLog bpiLog;
	
	public WorkingTogetherNetwork(XLog bpiLog) {
		this.bpiLog = bpiLog; 
	}
	
	/**
	 * Returns a matrix with the number of times resource i,j are working together
	 */
	public List<List<Integer>> countWorkingTogether() {
		// Matrix indicating the number of cases that resources work together	
		List<List<Integer>> nrOfCasesTogether = new ArrayList<>();
		
		// Initialize entry [0,0] to be -1
		nrOfCasesTogether.add(new ArrayList<Integer>());
		nrOfCasesTogether.get(0).add(0, -1);
		
		// List of resources that are involved in the case
		List<Integer> resources = new ArrayList<>();

		// Loop over all cases
		for (XTrace trace : bpiLog) {
			// Loop over all events of the case
			for (int i = 0; i < trace.size(); i++) {
				XEvent event = trace.get(i); 
				
				// Ignore the events that do not contain resource information		
				if (event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)) {			
					// Resource that executes the event
					int resource = Integer.parseInt(event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString());
					
					// Add resource to the list of resources
					if (!resources.contains(resource)) {
						resources.add(resource);
					}	
				}					
			}

			// Update the nrOfCasesTogether matrix
			for (int resource : resources) {
				// Add the resource to the arraylist when it is not already added
				if (!nrOfCasesTogether.get(0).contains(resource)) {
					
					// Update the matrix with a new row and column
					nrOfCasesTogether.add(new ArrayList<Integer>());	
					
					// Assign value 0 to the new entries to fill up the matrix
					for (int i = 0; i < nrOfCasesTogether.size()-1; i++) {
						nrOfCasesTogether.get(nrOfCasesTogether.size()-1).add(i, 0);
					}	
					
					for (int i = 0; i < nrOfCasesTogether.size(); i++) {
						nrOfCasesTogether.get(i).add(nrOfCasesTogether.size()-1, 0);
					}					
					
					// Add the resource ID to the row and column	
					nrOfCasesTogether.get(0).set(nrOfCasesTogether.size()-1, resource);	// Add resource to the first row					
					nrOfCasesTogether.get(nrOfCasesTogether.size()-1).set(0, resource);	// Add resource to first column 			
				} 		
			}
			
			// Update the values of the resources
			for (int resource : resources) { 
				for (int r : resources) { 
					if (r != resource) {
						// Get the index of the resource
						int indexA = nrOfCasesTogether.get(0).indexOf(resource);				
						int indexB = nrOfCasesTogether.get(0).indexOf(r);						
						int oldValue = nrOfCasesTogether.get(indexA).get(indexB);

						nrOfCasesTogether.get(indexA).set(indexB, oldValue + 1);
					}
				}
			}	
			
			resources.clear(); // Clear the list of resources 
		}
		
		return nrOfCasesTogether;	
	}
	
	/**
	 * Returns a matrix with the values of the working together network
	 */
	public List<List<Double>> computeWorkingTogetherNetwork(List<List<Integer>> matrixCounts) {
		// Matrix indicating the values of the working together network
		List<List<Double>> network = new ArrayList<>();
		
		double resource; // integer ID resources are denoted by doubles in the network matrix
		
		// Initialize the network matrix
		for (int i = 0; i < matrixCounts.size(); i++) {
			network.add(new ArrayList<Double>());
			
			for (int j = 0; j < matrixCounts.size(); j++) {
				network.get(i).add(0.0);
			}
		}
		
		for (int i = 1; i < matrixCounts.size(); i++) {
			resource = matrixCounts.get(0).get(i);
			network.get(0).set(i, resource);
			network.get(i).set(0, resource);
		}
		
		int workingNrOfCases = 0; // Number of cases resource i is working
		double workingTogether = 0.0; // Value for the network matrix
		
		for (int i = 1; i < matrixCounts.size(); i++) {
			for (int j = 1; j < matrixCounts.size(); j++) {
				// Compute the total number of cases
				workingNrOfCases = workingNrOfCases + matrixCounts.get(i).get(j);
			}
				
			for (int j = 1; j < matrixCounts.size(); j++) {
				// Compute the working together value of i with resource j
				workingTogether = (double) matrixCounts.get(i).get(j) / (double) workingNrOfCases;
				
				// Fill in the network matrix with the computed value
				network.get(i).set(j, workingTogether);
			}
		}
		
		return network;
	}

	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		
		WorkingTogetherNetwork network = new WorkingTogetherNetwork(bpiLog);
		
		List<List<Integer>> matrixCounts = network.countWorkingTogether();
		
		List<List<Double>> matrix = network.computeWorkingTogetherNetwork(matrixCounts);
		
		for (int i = 0; i < matrix.size(); i++) {
			System.out.println(matrix.get(i));
		}		
	}
}
