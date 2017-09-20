package org.processmining.streamsocialnetworks.models.mtj;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

public class MTJMatrixUtils {

	public static Matrix copyIntoDense(Matrix m, int rows, int cols) {
		Matrix res = new DenseMatrix(rows, cols);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (i < m.numRows() && j < m.numColumns()) {
					res.set(i, j, m.get(i, j));
				}
			}
		}
		return res;
	}

}
