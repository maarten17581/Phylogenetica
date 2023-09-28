package bep.fylogenetica.algorithm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.imageio.ImageIO;

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
public class GF2MatrixDense extends GF2Matrix {
	
	public ArrayList<DenseVector> vectors = new ArrayList<DenseVector>();
	public ArrayList<Boolean> results = new ArrayList<Boolean>();
	
	/**
	 * Creates a new, empty matrix over the given set of taxa.
	 * @param taxonCount The amount of taxa to construct the matrix for.
	 */
	public GF2MatrixDense(int taxonCount) {
		super(taxonCount);
	}
	
	/**
	 * Adds an explicitly-given row to this matrix. The row is inserted at the
	 * bottom.
	 * 
	 * @param vector The first part of the row (everything except for the last
	 * element). This represents the coefficients of the equations.
	 * @param result The last element of the row. This represents the result.
	 */
	public void addRow(DenseVector vector, boolean result) {
		vectors.add(0, vector);
		results.add(0, result);
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
		
		DenseVector vector = new DenseVector(taxonCount);
		
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
		
		addRow(vector, result);
	}
	
	/**
	 * Returns the number of rows in this matrix.
	 * @return The number of rows.
	 */
	@Override
	public int getRowCount() {
		return vectors.size();
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
		BufferedImage result = new BufferedImage(getColumnCount() + 1, getRowCount(), BufferedImage.TYPE_3BYTE_BGR);

		for (int y = 0; y < getRowCount(); y++) {
			
			DenseVector vector = vectors.get(y);
			
			for (int x = 0; x < getColumnCount(); x++) {
				if (!vector.getElementOnIndex(x)) {
					result.setRGB(x, y, 0xffffff);
				}
			}
			if (!results.get(y)) {
				result.setRGB(getColumnCount(), y, 0xffffff);
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
		
		int row = 0;
		
		for (int col = 0; col < getColumnCount(); col++) { // TODO in de laatste kolom vegen?
			
			if (debug) {
				// create an image for fun
				try {
					ImageIO.write(toImage(), "png", new File("C:/Users/20202991/Dropbox/My PC (S20202991)/Desktop/Bep_test/" + row + "." + col + ".png"));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				System.out.println("row " + row + ", col " + col);
			}

			// look for a pivot
			int pivot = -1;
			for (int i = row; i < getRowCount(); i++) {
				if (vectors.get(i).getElementOnIndex(col) == true) {
					pivot = i;
					break;
				}
			}
			
			if (pivot == -1) {
				// no pivot found? then don't increase the row number and continue with
				// the next column
				continue;
			}
			
			// swap the rows to make it easier
			swapRows(row, pivot);

			// the first part of the row should contain only zeroes now 
			// assert vectors.get(row).containsOnlyZeroes(0, col);
			
			for (int i = 0; i < row; i++) {
				if (vectors.get(i).getElementOnIndex(col) == true) {
					addRowTo(row, i, col);
				}
			}
			
			for (int i = row + 1; i < getRowCount(); i++) {
				if (vectors.get(i).getElementOnIndex(col) == true) {
					addRowTo(row, i, col);
				}
			}
			
			row++;
		}
	}
	
	/**
	 * Adds the given row to another row.
	 * 
	 * @param row The row to add to the other row.
	 * @param i The row to add the other row to.
	 * @param startIndex When reducing, it is most often not needed to add the whole
	 * row, since it is already known that parts of the row to add are already zero.
	 * Therefore this parameter can be used. For example, if <code>startIndex == 3</code>
	 * the first element added is on index 3; the elements on index 0, 1 and 2 will
	 * remain unchanged. Use <code>startIndex == 0</code> if you just want to add
	 * the whole row.
	 */
	private void addRowTo(int row, int i, int startIndex) {
		// add vectors
		vectors.get(i).addVector(vectors.get(row), startIndex);
		
		// add results
		results.set(i, results.get(i) ^ results.get(row));
	}

	/**
	 * Swaps the rows on the given indices.
	 * @param row1 The first row.
	 * @param row2 The second row.
	 */
	private void swapRows(int row1, int row2) {
		// swap vectors
		Collections.swap(vectors, row1, row2);
		
		// swap results
		Collections.swap(results, row1, row2);
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
		
		for (int i = 0; i < getRowCount(); i++) {
			if (results.get(i) == true) {
				if (vectors.get(i).isZeroVector()) {
					return false;
				}
			}
		}
		
		return true;
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
		
		int row = 0;
		
		for (int col = 0; col < getColumnCount() && row < getRowCount(); col++) {
			if (vectors.get(row).getElementOnIndex(col) == true) {
				result.setElementOnIndex(col, results.get(row));
				row++;
			}
		}
		
		assert conformsToMatrix(result);
		
		return result;
	}

	public GF2MatrixDense getKernel() {
		// Create W from {v+(yW)T | y \in GF(2)^n} = {x | Ax=b}
		GF2MatrixDense w = new GF2MatrixDense(taxonCount);

		int row = 0;
		ArrayList<Integer> identityIndex = new ArrayList<>();
		for (int col = 0; col < getColumnCount()-1; col++) {
			if (row < vectors.size() && vectors.get(row).getElementOnIndex(col)) {
				identityIndex.add(col);
				row++;
			} else {
				DenseVector v = new DenseVector(taxonCount);
				v.setElementOnIndex(col, true);
				for (int i = 0; i < row; i++) {
					v.setElementOnIndex(identityIndex.get(i), vectors.get(i).getElementOnIndex(col));
				}
				w.addRow(v, false);
			}
		}
		
		return w;
	}

	public GF2MatrixDense select(ArrayList<Integer> taxa) {
		GF2MatrixDense m = new GF2MatrixDense(taxa.size());
		for (DenseVector v : vectors) {
			m.addRow(v.select(taxa), false);
		}
		return m;
	}

	/**
	 * Will use linear algebra to find all witnesses, requires it to be in reduced row-echelon form
	 * @return a list of all witnesses
	 */
	@Override
	public ArrayList<Witness> findWitnesses() {

		ArrayList<Witness> witnesses = new ArrayList<>();
		//ArrayList<Witness> witnesses2 = new ArrayList<>();

		// Create W from {v+(yW)T | y \in GF(2)^n} = {x | Ax=b}
		GF2MatrixDense w = getKernel();

		// Bring W to reduced row-echelon form
		w.rowReduce(false);

		// For each quadruple with 0 check if it is witness
		for (int i = 0; i < taxonCount - 3; i++) {
			for (int j = i+1; j < taxonCount - 2; j++) {
				for (int k = j+1; k < taxonCount - 1; k++) {
					for (int l = k+1; l < taxonCount; l++) {
						ArrayList<Integer> taxa = new ArrayList<>(Arrays.asList(i,j,k,l));
						GF2MatrixDense m = w.select(taxa);
						m.rowReduce(false);
						int rank = m.detemineRank();
						if (rank == 3) {
							witnesses.add(new Witness(i, j, k, l));
						} else if (rank > 3) {
							System.out.println("Witness went wrong");
						}
					}
				}
			}
		}

		/*
		// Get all other witnesses
		//System.out.println(witnesses + " all witnesses with 0");
		ArrayList<Witness> extra = new ArrayList<>();
		for (Witness w1 : witnesses) {
			for (int i = 0; i < taxonCount; i++) {
				if (!w1.contains(i)) {
					boolean toContinue = false;
					for (Witness w2 : extra) {
						Witness w3 = new Witness(w1, 0, i);
						if (w2.taxa[0] == w3.taxa[0] && w2.taxa[1] == w3.taxa[1] && w2.taxa[2] == w3.taxa[2] && w2.taxa[3] == w3.taxa[3]) {
							toContinue = true;
							break;
						}
					}
					if (toContinue) {
						continue;
					}
					boolean pairWithin = false;
					for (Witness w2 : witnesses) {
						if (w2.contains(i) && w1.isPair(w2)) {
							pairWithin = true;
							// still check if it is a witness
							GF2MatrixDense m = new GF2MatrixDense(5);
							Witness wTest = new Witness(w1, 0, i);
							int[] witnessTaxa = wTest.taxa;
							for (DenseVector v : w.vectors) {
								DenseVector vSmall = new DenseVector(5);
								// We add triple ijk, ijl, ikl, jkl
								// This results in 0ij, 0il, 0jl
								// 0ij, 0il, 0jl
								// 0ik, 0il, 0kl
								// 0jk, 0jl, 0kl
								// We can remove duplicates
								for (int j = 0; j < 4; j++) {
									for (int k = j+1; k < 4; k++) {
										Boolean t = v.getElement(new Triple(0, witnessTaxa[j], witnessTaxa[k]));
										vSmall.setElement(new Triple(0, j+1, k+1), t);
									}
								}
								m.addRow(vSmall, false);
							}
							m.rowReduce(false);
							int rank = m.detemineRank();
							if (rank >= 5) {
								//System.out.println("Was otherwise missed "+wTest);
								extra.add(wTest);
							} else if (rank > 6) {
								System.out.println("Witness went wrong");
							}

							break;
						}
					}
					if (!pairWithin) {
						System.out.println(new Witness(w1, 0, i));
						extra.add(new Witness(w1, 0, i));
					}
				}
			}
		}

		ArrayList<Witness> extra2 = new ArrayList<>();
		for (int i = 1; i < taxonCount - 3; i++) {
			for (int j = i+1; j < taxonCount - 2; j++) {
				for (int k = j+1; k < taxonCount - 1; k++) {
					for (int l = k+1; l < taxonCount; l++) {
						GF2MatrixDense m = new GF2MatrixDense(4);
						for (DenseVector v : w.vectors) {
							DenseVector vSmall = new DenseVector(4);
							Boolean t1 = v.getElement(new Triple(i, j, k));
							Boolean t2 = v.getElement(new Triple(i, j, l));
							Boolean t3 = v.getElement(new Triple(i, k, l));
							vSmall.setElement(new Triple(0, 1, 2), t1);
							vSmall.setElement(new Triple(0, 1, 3), t2);
							vSmall.setElement(new Triple(0, 2, 3), t3);
							m.addRow(vSmall, false);
						}
						m.rowReduce(false);
						int rank = m.detemineRank();
						if (rank == 3) {
							extra2.add(new Witness(i, j, k, l));
						} else if (rank > 3) {
							System.out.println("Witness went wrong");
						}
					}
				}
			}
		}
		//System.out.println(witnesses +"----");
		//System.out.println(extra + " all that got added from that");
		System.out.println(extra);
		System.out.println(extra2);
		System.out.println(extra.size() + " " + extra2.size());
		witnesses.addAll(extra);*/

		return witnesses;
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
		
		for (int i = 0; i < getRowCount(); i++) {
			if (vectors.get(i).multiplyWithVector(v) != results.get(i)) {
				return false;
			}
			
			if (vectors.get(i).isZeroVector()) {
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the rank of this matrix.
	 * 
	 * <p>The matrix needs to be in reduced row-echelon form before calling this
	 * method. If it is not, call {@link #rowReduce(boolean)} before to reduce
	 * the matrix.</p>
	 * 
	 * @return The number of rows before a (0, 0, 0, ..., 0, 0) row appears.
	 */
	public int detemineRank() {
		
		int count = 0;
		
		for (int i = 0; i < getRowCount(); i++) {
			if (results.get(i) == false && vectors.get(i).isZeroVector()) {
				return count;
			}
			count++;
		}
		
		return count;
	}

	@Override
	public String toString() {
		String out = "";
		for (int i = 0; i < detemineRank(); i++) {
			out += vectors.get(i).toString();
			out += " | "+(results.get(i) ? "1" : "0") + "\n";
		}
		return out;
	}
}
