package bep.fylogenetica.algorithm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Witness;

/**
 * A dense matrix over GF(2) for a certain set of taxa.
 * 
 * <p>The rows of the matrix are made up of {@link DenseVector}s and result values:
 * <pre>
 * [ vectors | results ]
 * </pre>
 * </p>
 * 
 * <p>The first element of the taxon set (the taxon with ID 0) is chosen as the
 * base element.</p>
 */
public abstract class GF2Matrix {
	
	public int taxonCount;
	
	/**
	 * Creates a new, empty matrix over the given set of taxa.
	 * @param taxonCount The amount of taxa to construct the matrix for.
	 */
	public GF2Matrix(int taxonCount) {
		this.taxonCount = taxonCount;
	}
	
	/**
	 * Adds a row to this matrix corresponding to the given quartet. The row is
	 * inserted at the bottom.
	 * 
	 * @param q The quartet to insert a row for.
	 */
	public void addRowForQuartet(Quartet q) {
		System.out.println("wrong");
	}
	
	/**
	 * Returns the number of columns in this matrix, including the results column.
	 * @return The number of columns.
	 */
	public int getColumnCount() {
		return (taxonCount - 1) * (taxonCount - 2) / 2 + 1;
	}
	
	/**
	 * Returns the number of rows in this matrix.
	 * @return The number of rows.
	 */
	public int getRowCount() {
		System.out.println("wrong");
		return 0;
	}
	
	/**
	 * Creates an image depicting the elements of this matrix, using white pixels
	 * for 0 and black pixels for 1. This is meant for visualisation of large
	 * matrices.
	 * 
	 * @return The image.
	 */
	public BufferedImage toImage() {
		System.out.println("wrong");
		return null;
	}
	
	/**
	 * Brings the matrix in reduced row-echelon form.
	 * 
	 * @param debug Whether to debug. TODO should be removed!
	 */
	public void rowReduce(boolean debug) {
		System.out.println("wrong");
	}
	
	/**
	 * Returns whether the matrix is <i>consistent</i>.
	 * 
	 * <p>The matrix needs to be in reduced row-echelon form before calling this
	 * method. If it is not, call {@link #rowReduce(boolean)} before to reduce
	 * the matrix.</p>
	 * 
	 * <p>A matrix is inconsistent if it doesn't contain (0, 0, 0, ..., 0, 1) rows.
	 * Such rows would signify 0 + 0 + 0 + ... + 0 = 1, so the matrix signifies
	 * an inconsistent system if they are included. Note: in this definition
	 * we assume the matrix to be in reduced row-echelon form.</p>
	 * 
	 * @return <code>false</code> if the matrix contains one or more
	 * (0, 0, 0, ..., 0, 1) rows, <code>true</code> otherwise.
	 */
	public boolean isConsistent() {
		System.out.println("wrong");
		return false;
	}
	
	/**
	 * Determines a vector in the space defined by this matrix. So if we call
	 * this matrix
	 * <pre>M = [A | b]</pre>
	 * (where <code>b</code> is the last column), this method will return some
	 * <code>v</code> such that <code>Av = b</code>.
	 * 
	 * <p>The matrix needs to be in reduced row-echelon form before calling this
	 * method. If it is not, call {@link #rowReduce(boolean)} before to reduce
	 * the matrix.</p>
	 * 
	 * <p>If this matrix contains a (0, 0, 0, ..., 0, 1) row, this method will
	 * throw a {@link MatrixInconsistentException} (indeed, a conforming vector
	 * doesn't exist then). To avoid this, first check for such rows using
	 * {@link #isConsistent()}.
	 * 
	 * @return A vector <code>x</code> such that <code>Ax = b</code>.
	 * @throws MatrixInconsistentException If the matrix is not consistent.
	 */
	public DenseVector determineConformingVector() throws MatrixInconsistentException {
		System.out.println("wrong");
		return null;
	}
	
	/**
	 * Will use linear algebra to find all witnesses, requires it to be in reduced row-echelon form
	 * @return a list of all witnesses
	 */
	public ArrayList<Witness> findWitnesses() {
		System.out.println("wrong");
		return null;
	}
	
	/**
	 * Checks whether the given vector <code>v</code> conforms to this matrix. So if we call
	 * this matrix
	 * <pre>M = [A | b]</pre>
	 * (where <code>b</code> is the last column), this method will check if <code>Av = b</code>.
	 * 
	 * <p>This method is optimized to stop when it encounters a <code>(0, 0, ..., 0)</code> row
	 * in the matrix. Therefore this method should only be called when the matrix is in
	 * reduced row-echelon form. If it is not, call {@link #rowReduce(boolean)} before to reduce
	 * the matrix.</p>
	 * 
	 * <p>Note: this implementation is rather stupid and should be optimized, for example by
	 * using matrix multiplication instead of just checking row-by-row.</p>
	 * 
	 * @param v The vector to check for.
	 * @return <code>true</code> if <code>Av = b</code>, <code>false</code> otherwise.
	 */
	public boolean conformsToMatrix(DenseVector v) {
		System.out.println("wrong");
		return false;
	}
}
