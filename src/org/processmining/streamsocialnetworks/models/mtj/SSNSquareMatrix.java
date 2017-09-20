package org.processmining.streamsocialnetworks.models.mtj;

import java.util.List;

import no.uib.cipr.matrix.Matrix;

public interface SSNSquareMatrix<T> extends Matrix {
	
	List<T> getObjects();
	
	int addObject(T object);
	
	int getIndexOfObject(T obj);
	
	T getObjectOfIndex(int index);
		
	int getNumberOfObjects();
	
	void setObject(int index, T object);
	
	void clearObject(T object);
	
	void clearObject(int index);
	
}
