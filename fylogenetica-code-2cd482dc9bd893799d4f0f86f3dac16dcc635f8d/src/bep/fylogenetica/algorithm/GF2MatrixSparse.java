package bep.fylogenetica.algorithm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.imageio.ImageIO;

import bep.fylogenetica.model.Quartet;

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
public class GF2MatrixSparse extends GF2Matrix{
	
	private HashMap<Triple, ArrayList<SparseVector>> vectors;
	private int rowCount;
	private boolean isConsistent;
	
	/**
	 * Creates a new, empty matrix over the given set of taxa.
	 * @param taxonCount The amount of taxa to construct the matrix for.
	 */
	public GF2MatrixSparse(int taxonCount) {
		super(taxonCount);
		vectors = new HashMap<>();
		for (int i = 1; i < taxonCount - 1; i++) {
			for (int j = i + 1; j < taxonCount; j++) {
				ArrayList<SparseVector> column = new ArrayList<>();
				Triple t = new Triple(0, i, j);
				vectors.put(t, column);
			}
		}
		rowCount = 0;
		isConsistent = true;
	}
	
	/**
	 * Adds an explicitly-given row to this matrix. The row is inserted at the
	 * bottom.
	 * 
	 * @param vector The row. This represents the coefficients of the equations and has the result.
	 */
	public void addRow(SparseVector vector) {
		for (Triple t : vector.values) {
			vectors.get(t).add(vector);
		}
		rowCount++;
	}
	
	/**
	 * Adds a row to this matrix corresponding to the given quartet. The row is
	 * inserted at the bottom.
	 * 
	 * @param q The quartet to insert a row for.
	 */
	@Override
	public void addRowForQuartet(Quartet q) {
		
		q.toCanonicalForm();
		
		// we keep the result, and swap its value every time it needs to be changed
		boolean result = false;
		
		SparseVector vector = new SparseVector(taxonCount);
		
		if (q.left1 == 0) {
			
			Triple t1 = new Triple(q.left1, q.left2, q.right1);
			result ^= t1.makeAscending();
			vector.setElement(t1, true);
			
			Triple t2 = new Triple(q.left1, q.left2, q.right2);
			result ^= t2.makeAscending();
			vector.setElement(t2, true);
			
		} else {
			Triple t1 = new Triple(0, q.left1, q.right1);
			result ^= t1.makeAscendingAlt();
			vector.setElement(t1, true);
			
			Triple t2 = new Triple(0, q.left2, q.right1);
			result ^= t2.makeAscendingAlt();
			vector.setElement(t2, true);
			
			Triple t3 = new Triple(0, q.left1, q.right2);
			result ^= t3.makeAscendingAlt();
			vector.setElement(t3, true);
			
			Triple t4 = new Triple(0, q.left2, q.right2);
			result ^= t4.makeAscendingAlt();
			vector.setElement(t4, true);
		}

		vector.setResult(result);
		
		addRow(vector);
	}
	
	/**
	 * Returns the number of rows in this matrix.
	 * @return The number of rows.
	 */
	@Override
	public int getRowCount() {
		return rowCount;
	}
	
	/**
	 * Creates an image depicting the elements of this matrix, using white pixels
	 * for 0 and black pixels for 1. This is meant for visualisation of large
	 * matrices.
	 * 
	 * @return The image.
	 */
	@Override
	public BufferedImage toImage() {
		BufferedImage result = new BufferedImage(2*getColumnCount() + 1, 2*getRowCount() + 1, BufferedImage.TYPE_3BYTE_BGR);

		int row = 0;
		int col = 0;

		for (int i = 1; i < taxonCount - 1; i++) {
			for (int j = i+1; j < taxonCount; j++) {
				Triple t = new Triple(0, i, j);
				ArrayList<SparseVector> rows = vectors.get(t);
				for(SparseVector v : rows) {
					if(v.isFirstVector(t)) {
						col = 0;
						for (int k = 1; k < taxonCount - 1; k++) {
							for (int l = k + 1; l < taxonCount; l++) {
								Triple t2 = new Triple(0, k, l);
								if (!v.values.contains(t2)) {
									result.setRGB(2*col, 2*row, 0xffffff);
								}
								col++;
							}
						}
						if(!v.getResult()) {
							result.setRGB(2*getColumnCount(), 2*row, 0xffffff);
						}
						row++;
					}
				}
			}
		}

		for (int i = 0; i < 2*getColumnCount()+1; i++) {
			for (int j = 0; j < 2*getRowCount()+1; j++) {
				if (i%2==1||j%2==1) {
					result.setRGB(i, j, 0x7f7f7f);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Brings the matrix in reduced row-echelon form.
	 * 
	 * @param debug Whether to debug. TODO should be removed!
	 */
	@Override
	public void rowReduce(boolean debug) {

		int col = 0;

		for (int i = 1; i < taxonCount - 1; i++) {
			for (int j = i + 1; j < taxonCount; j++) {
				
				if (debug) {
					// create an image for fun
					try {
						ImageIO.write(toImage(), "png", new File("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/Bep_test/" + col + ".png"));
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					System.out.println("col " + col);
				}

				Triple t = new Triple(0, i, j);
				col++;

				// look for a pivot
				SparseVector pivot = null;
				for (SparseVector v : vectors.get(t)) {
					if (v.isFirstVector(t)) {
						pivot = v;
						v.isPivot = true;
						break;
					}
				}
				
				if (pivot == null) {
					// no pivot found? then don't increase the row number and continue with
					// the next column
					continue;
				}
				
				while (!(vectors.get(t).size() == 1 && vectors.get(t).get(0).isPivot)) {
					SparseVector v = vectors.get(t).get(0);
					if(v.isPivot) {
						v = vectors.get(t).get(1);
					}
					addRowTo(pivot, v);
				}

				pivot.isPivot = false;
			}
		}
	}
	
	/**
	 * Adds the given row to another row.
	 * This is not done with the internal method of {@link SparseVector}
	 * because we can efficiently compute the change in the {@link #vectors} hashmap
	 * If w becomes empty we remove it from the system and set the matrix to inconsistent whenever the result is true
	 * 
	 * @param v The row to add to the other row.
	 * @param w The row to add the other row to.
	 */
	private void addRowTo(SparseVector v, SparseVector w) {
		if(v.equals(w)) {
			if (w.getResult() ^ v.getResult()) {
				isConsistent = false;
			}
			removeRow(w);
			return;
		}
		ArrayList<Triple> newValues = new ArrayList<>();
		int x = 0;
		int y = 0;
		while (x < w.values.size() && y < v.values.size()) {
			if (w.values.get(x).lessThan(v.values.get(y))) {
				newValues.add(w.values.get(x));
				x++;
			} else if (v.values.get(y).lessThan(w.values.get(x))) {
				newValues.add(v.values.get(y));
				vectors.get(v.values.get(y)).add(w);
				y++;
			} else if (w.values.get(x).equals(v.values.get(y))) {
				// if there are 2 remove them because they cancel out
				vectors.get(w.values.get(x)).remove(w);
				x++;
				y++;
			}
		}
		while (x < w.values.size() || y < v.values.size()) {
			if (x == w.values.size()) {
				newValues.add(v.values.get(y));
				vectors.get(v.values.get(y)).add(w);
				y++;
			} else if (y == v.values.size()) {
				newValues.add(w.values.get(x));
				x++;
			}
		}
		w.setResult(w.getResult() ^ v.getResult());
		w.values = newValues;

		if (w.values.isEmpty() || 
				vectors.get(w.values.get(0)).indexOf(w) != vectors.get(w.values.get(0)).lastIndexOf(w)) {
			if (w.getResult()) {
				isConsistent = false;
			}
			removeRow(w);
		}
	}

	private void removeRow(SparseVector v) {
		if (v.values.isEmpty()) {
			rowCount--;
			return;
		}
		Triple t = v.values.get(0);
		while (vectors.get(t).contains(v)) {
			for (Triple t2 : v.values) {
				vectors.get(t2).remove(v);
			}
			rowCount--;
		}
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
	@Override
	public boolean isConsistent() {
		return isConsistent;
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
	@Override
	public DenseVector determineConformingVector() throws MatrixInconsistentException {
		
		if (!isConsistent()) {
			throw new MatrixInconsistentException();
		}
		
		DenseVector result = new DenseVector(taxonCount);
		
		for (int i = 1; i < taxonCount - 1; i++) {
			for (int j = i + 1; j < taxonCount; j++) {
				Triple t = new Triple(0, i, j);
				ArrayList<SparseVector> rows = vectors.get(t);
				if(rows.size() == 1 && rows.get(0).isFirstVector(t)) {
					result.setElement(t, rows.get(0).getResult());
				}
			}
		}

		try {
			BufferedImage image = toImage();
			int col = 0;
			for (int i = 1; i < taxonCount - 1; i++) {
				for (int j = i + 1; j < taxonCount; j++) {
					if (!result.getElement(new Triple(0, i, j))) {
						image.setRGB(2*col, 2*getRowCount(), 0xffffff);
					}
					col++;
				}
			}
			for (int i = 1; i < 2*getColumnCount()+1; i+=2) {
				image.setRGB(2*col, 2*getRowCount(), 0x7f7f7f);
			}
			ImageIO.write(image, "png", new File("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/Bep_test/conform.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assert conformsToMatrix(result);
		
		return result;
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
	@Override
	public boolean conformsToMatrix(DenseVector v) {
		
		for (int i = 1; i < taxonCount - 1; i++) {
			for (int j = i + 1; j < taxonCount; j++) {
				Triple t = new Triple(0, i, j);
				ArrayList<SparseVector> rows = vectors.get(t);
				if(rows.size() == 1 && rows.get(0).isFirstVector(t) && 
						rows.get(0).multiplyWithVector(v) != rows.get(0).getResult()) {
					return false;
				}
			}
		}
		return true;
	}
}
