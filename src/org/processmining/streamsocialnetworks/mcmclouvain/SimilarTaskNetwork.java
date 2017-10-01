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
	 * Returns a collection of resource activity pairs that denotes the number of times a resource performs an activity
	 */
	public TObjectDoubleMap<ResourceActivityPair> countActivitiesPerformed() {	
		String resource = null;
		String activity = null;
		
		TObjectDoubleMap<ResourceActivityPair> rapSetActivityCount = new TObjectDoubleHashMap<>();
		

		for (XTrace trace : bpiLog) {
			// Loop over all events of the case
			for (int i = 0; i < trace.size(); i++) {
				XEvent event = trace.get(i); 
				
				// Ignore the events that do not contain resource information		
				if (event.getAttributes().containsKey(XOrganizationalExtension.KEY_RESOURCE)) {		
					resource = event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE).toString();
					activity = event.getAttributes().get(XConceptExtension.KEY_NAME).toString();
				
					ResourceActivityPair rap = new ResourceActivityPair(resource, activity);

					// Add the resource activity pair to the set
					if (!rapSetActivityCount.containsKey(rap)) {
						rapSetActivityCount.put(rap, 0);
					}
					
					// Increase the number of times the resource performs an activity with 1
					rapSetActivityCount.increment(rap);
				}
			}
		}
		
		return rapSetActivityCount;
	}
	
	
	public List<List<Integer>> countSimilarTasks(TObjectDoubleMap<ResourceActivityPair> nrOfActivitiesPerformed) {
		// Matrix indicating the number of similar tasks that resources perform in common.	
		List<List<Integer>> nrOfSimilarTasks = new ArrayList<>();
	
		// Initialize entry [0,0] to be -1
		nrOfSimilarTasks.add(new ArrayList<Integer>());
		nrOfSimilarTasks.get(0).add(0, -1);
				
		Set<ResourceActivityPair> rapSet =  new HashSet<>();
		rapSet = nrOfActivitiesPerformed.keySet();
		
		// Iterate over all resource activity pairs
		for (ResourceActivityPair rap : rapSet) {
			int resource = Integer.parseInt(rap.getResource());
			
			// Update the similar tasks matrix when resource is not seen yet
			if (!nrOfSimilarTasks.get(0).contains(resource)) {				
				nrOfSimilarTasks = updateMatrix(nrOfSimilarTasks, resource);			
			} 		
			
			for (ResourceActivityPair rap2 : rapSet) {
				int resource2 = Integer.parseInt(rap2.getResource());
				
				// Update the similar tasks matrix when resource is not seen yet
				if (!nrOfSimilarTasks.get(0).contains(resource2)) {				
					nrOfSimilarTasks = updateMatrix(nrOfSimilarTasks, resource2);			
				} 	
				
				// Compare the activity of resource with activities of other resources
				if (resource != resource2) {
					// If similar activities are performed add 1 to the correct entry of similar task matrix
					if (rap.getActivity().equals(rap2.getActivity())) {
						// Get the index of the resources
						int indexA = nrOfSimilarTasks.get(0).indexOf(resource);				
						int indexB = nrOfSimilarTasks.get(0).indexOf(resource2);						
						int oldValue = nrOfSimilarTasks.get(indexA).get(indexB);

						nrOfSimilarTasks.get(indexA).set(indexB, oldValue + 1);
					}
				}
			}
		}
		
		return nrOfSimilarTasks;
	}
	
	// Update the matrix when a new resource is seen (= add new row and column and fill entries with 0)
	public List<List<Integer>> updateMatrix(List<List<Integer>> matrix, int resource) {
		// Update the matrix with a new row and column
		matrix.add(new ArrayList<Integer>());	
		
		// Assign value 0 to the new entries to fill up the matrix
		for (int i = 0; i < matrix.size()-1; i++) {
			matrix.get(matrix.size()-1).add(i, 0);
		}	
		
		for (int i = 0; i < matrix.size(); i++) {
			matrix.get(i).add(matrix.size()-1, 0);
		}					
		
		// Add the resource ID to the row and column	
		matrix.get(0).set(matrix.size()-1, resource);	// Add resource to the first row					
		matrix.get(matrix.size()-1).set(0, resource);	// Add resource to first column 		
		
		return matrix;
	}

	public static void main(final String[] args) {
		XLog bpiLog = XESImporter.importXLog(new File("C:\\Users\\s145283\\Desktop\\2IMI05 - Capita Selecta\\BPI_Challenge_2012.xes"));
		
		SimilarTaskNetwork network = new SimilarTaskNetwork(bpiLog);
		
		// Computes the number of times resources perform activities
		TObjectDoubleMap<ResourceActivityPair> nrOfActivitiesPerformed = network.countActivitiesPerformed();
		
		// Return a matrix with how many tasks two resources perform in common.
		List<List<Integer>> matrix = network.countSimilarTasks(nrOfActivitiesPerformed);
		
		for (int i = 0; i < matrix.size(); i++) {
			System.out.println(matrix.get(i));
		}
	}
}
