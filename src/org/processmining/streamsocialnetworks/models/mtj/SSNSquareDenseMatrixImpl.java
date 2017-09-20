package org.processmining.streamsocialnetworks.models.mtj;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.MatrixNotSPDException;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.matrix.Vector;

public class SSNSquareDenseMatrixImpl<T> implements SSNSquareMatrix<T> {

	private Matrix delegate;

	private final int DELTA = 50;

	private final List<T> objects = new ArrayList<>();

	public SSNSquareDenseMatrixImpl(int numRows, int numColumns) {
		delegate = new DenseMatrix(numRows, numColumns);
	}

	public Matrix add(double alpha, Matrix B) {
		return delegate.add(alpha, B);
	}

	public void add(int row, int column, double value) {
		delegate.add(row, column, value);
	}

	public Matrix add(Matrix B) {
		return delegate.add(B);
	}

	public int addObject(T object) {
		//TODO: add check for null entries
		objects.add(object);
		if (objects.size() > numRows()) {
			delegate = MTJMatrixUtils.copyIntoDense(delegate, numRows() + DELTA, numColumns() + DELTA);
		}
		return objects.size() - 1;
	}

	public void clearObject(int index) {
		objects.set(index, null);

	}

	public void clearObject(T object) {
		int index = getIndexOfObject(object);
		if (index > -1) {
			objects.set(index, null);
		}
	}

	public Matrix copy() {
		return delegate.copy();
	}

	public double get(int row, int column) {
		return delegate.get(row, column);
	}

	public int getIndexOfObject(T obj) {
		return objects.indexOf(obj);
	}

	public int getNumberOfObjects() {
		return objects.size();
	}

	public T getObjectOfIndex(int index) {
		return objects.get(index);
	}

	public List<T> getObjects() {
		return objects;
	}

	public boolean isSquare() {
		return delegate.isSquare();
	}

	public Iterator<MatrixEntry> iterator() {
		return delegate.iterator();
	}

	public Matrix mult(double alpha, Matrix B, Matrix C) {
		return delegate.mult(alpha, B, C);
	}

	public Vector mult(double alpha, Vector x, Vector y) {
		return delegate.mult(alpha, x, y);
	}

	public Matrix mult(Matrix B, Matrix C) {
		return delegate.mult(B, C);
	}

	public Vector mult(Vector x, Vector y) {
		return delegate.mult(x, y);
	}

	public Matrix multAdd(double alpha, Matrix B, Matrix C) {
		return delegate.multAdd(alpha, B, C);
	}

	public Vector multAdd(double alpha, Vector x, Vector y) {
		return delegate.multAdd(alpha, x, y);
	}

	public Matrix multAdd(Matrix B, Matrix C) {
		return delegate.multAdd(B, C);
	}

	public Vector multAdd(Vector x, Vector y) {
		return delegate.multAdd(x, y);
	}

	public double norm(Norm type) {
		return delegate.norm(type);
	}

	public int numColumns() {
		return delegate.numColumns();
	}

	public int numRows() {
		return delegate.numRows();
	}

	public Matrix rank1(double alpha, Matrix C) {
		return delegate.rank1(alpha, C);
	}

	public Matrix rank1(double alpha, Vector x) {
		return delegate.rank1(alpha, x);
	}

	public Matrix rank1(double alpha, Vector x, Vector y) {
		return delegate.rank1(alpha, x, y);
	}

	public Matrix rank1(Matrix C) {
		return delegate.rank1(C);
	}

	public Matrix rank1(Vector x) {
		return delegate.rank1(x);
	}

	public Matrix rank1(Vector x, Vector y) {
		return delegate.rank1(x, y);
	}

	public Matrix rank2(double alpha, Matrix B, Matrix C) {
		return delegate.rank2(alpha, B, C);
	}

	public Matrix rank2(double alpha, Vector x, Vector y) {
		return delegate.rank2(alpha, x, y);
	}

	public Matrix rank2(Matrix B, Matrix C) {
		return delegate.rank2(B, C);
	}

	public Matrix rank2(Vector x, Vector y) {
		return delegate.rank2(x, y);
	}

	public Matrix scale(double alpha) {
		return delegate.scale(alpha);
	}

	public Matrix set(double alpha, Matrix B) {
		return delegate.set(alpha, B);
	}

	public void set(int row, int column, double value) {
		delegate.set(row, column, value);
	}

	public Matrix set(Matrix B) {
		return delegate.set(B);
	}

	public void setObject(int index, T object) {
		if (index == getNumberOfObjects()) {
			objects.add(object);
		} else if (index < getNumberOfObjects()) {
			objects.set(index, object);
		}
		if (index > numRows() - 1) {
			int delta = index - numRows();
			int i = 1;
			while (i < delta) {
				objects.add(null);
			}
			objects.add(object);
			delegate = MTJMatrixUtils.copyIntoDense(delegate, numRows() + delta, numColumns() + delta);
		}
	}

	public Matrix solve(Matrix B, Matrix X) throws MatrixSingularException, MatrixNotSPDException {
		return delegate.solve(B, X);
	}

	public Vector solve(Vector b, Vector x) throws MatrixSingularException, MatrixNotSPDException {
		return delegate.solve(b, x);
	}

	public Matrix transABmult(double alpha, Matrix B, Matrix C) {
		return delegate.transABmult(alpha, B, C);
	}

	public Matrix transABmult(Matrix B, Matrix C) {
		return delegate.transABmult(B, C);
	}

	public Matrix transABmultAdd(double alpha, Matrix B, Matrix C) {
		return delegate.transABmultAdd(alpha, B, C);
	}

	public Matrix transABmultAdd(Matrix B, Matrix C) {
		return delegate.transABmultAdd(B, C);
	}

	public Matrix transAmult(double alpha, Matrix B, Matrix C) {
		return delegate.transAmult(alpha, B, C);
	}

	public Matrix transAmult(Matrix B, Matrix C) {
		return delegate.transAmult(B, C);
	}

	public Matrix transAmultAdd(double alpha, Matrix B, Matrix C) {
		return delegate.transAmultAdd(alpha, B, C);
	}

	public Matrix transAmultAdd(Matrix B, Matrix C) {
		return delegate.transAmultAdd(B, C);
	}

	public Matrix transBmult(double alpha, Matrix B, Matrix C) {
		return delegate.transBmult(alpha, B, C);
	}

	public Matrix transBmult(Matrix B, Matrix C) {
		return delegate.transBmult(B, C);
	}

	public Matrix transBmultAdd(double alpha, Matrix B, Matrix C) {
		return delegate.transBmultAdd(alpha, B, C);
	}

	public Matrix transBmultAdd(Matrix B, Matrix C) {
		return delegate.transBmultAdd(B, C);
	}

	public Vector transMult(double alpha, Vector x, Vector y) {
		return delegate.transMult(alpha, x, y);
	}

	public Vector transMult(Vector x, Vector y) {
		return delegate.transMult(x, y);
	}

	public Vector transMultAdd(double alpha, Vector x, Vector y) {
		return delegate.transMultAdd(alpha, x, y);
	}

	public Vector transMultAdd(Vector x, Vector y) {
		return delegate.transMultAdd(x, y);
	}

	public Matrix transpose() {
		return delegate.transpose();
	}

	public Matrix transpose(Matrix B) {
		return delegate.transpose(B);
	}

	public Matrix transRank1(double alpha, Matrix C) {
		return delegate.transRank1(alpha, C);
	}

	public Matrix transRank1(Matrix C) {
		return delegate.transRank1(C);
	}

	public Matrix transRank2(double alpha, Matrix B, Matrix C) {
		return delegate.transRank2(alpha, B, C);
	}

	public Matrix transRank2(Matrix B, Matrix C) {
		return delegate.transRank2(B, C);
	}

	public Matrix transSolve(Matrix B, Matrix X) throws MatrixSingularException, MatrixNotSPDException {
		return delegate.transSolve(B, X);
	}

	public Vector transSolve(Vector b, Vector x) throws MatrixSingularException, MatrixNotSPDException {
		return delegate.transSolve(b, x);
	}

	public Matrix zero() {
		return delegate.zero();
	}

}
