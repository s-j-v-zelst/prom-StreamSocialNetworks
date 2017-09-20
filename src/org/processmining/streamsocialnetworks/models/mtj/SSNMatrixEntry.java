package org.processmining.streamsocialnetworks.models.mtj;

/**
 * utility class for the purpose of pointing to an entry within a matrix.
 * additionally a resource/group can be specified ( used fo initialization
 * purposes, i.e. if row == col == -1)
 * 
 * @author svzelst
 *
 * @param <T>
 *            type used to declare resource/group etc.
 */
public class SSNMatrixEntry<T> {

	private final int col;

	private final int row;

	private final T resource;

	public SSNMatrixEntry(int row, int col) {
		this(row, col, null);
	}

	public SSNMatrixEntry(int row, int col, T resource) {
		this.row = row;
		this.col = col;
		this.resource = resource;
	}

	public T getResource() {
		return resource;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = o instanceof SSNMatrixEntry;
		if (result) {
			SSNMatrixEntry<?> cast = (SSNMatrixEntry<?>) o;
			result &= cast.getCol() == col;
			result &= cast.getRow() == row;
			result &= cast.getResource().equals(resource);
		}
		return result;
	}

}
