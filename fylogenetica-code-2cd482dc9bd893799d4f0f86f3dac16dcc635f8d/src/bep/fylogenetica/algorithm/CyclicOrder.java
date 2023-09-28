package bep.fylogenetica.algorithm;

import java.util.ArrayList;

/**
 * A cyclic order of taxa.
 */
public class CyclicOrder {
	
	/**
	 * This array contains the actual ordering. This array is a permutation
	 * of <code>{0, 1, ..., n - 1}</code> where <code>n</code> is the number
	 * of taxa.
	 */
	public ArrayList<Integer> taxa;
	
	/**
	 * Creates the unity order (that is, <code>[0, 1, ..., n - 1]</code>) over
	 * <code>taxonCount</code> taxa.
	 * 
	 * @param taxonCount The number of taxa.
	 */
	public CyclicOrder(int taxonCount) {
		taxa = new ArrayList<>();
		
		for (int i = 0; i < taxonCount; i++) {
			taxa.add(i);
		}
	}
	
	/**
	 * Creates a copy of the given {@link CyclicOrder}. The new object will
	 * be completely independent from the given object (i.e. you can modify it,
	 * and the original object will not be modified).
	 * 
	 * @param other The {@link CyclicOrder} to copy.
	 */
	public CyclicOrder(CyclicOrder other) {
		taxa = new ArrayList<>();
		
		for (int i : other.taxa) {
			taxa.add(i);
		}
	}

	public void changeAll(CyclicOrder c) {
		taxa = new ArrayList<>();
		
		for (int i : c.taxa) {
			taxa.add(i);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		
		for (int i = 0; i < taxa.size(); i++) {
			sb.append(taxa.get(i));
			sb.append(" ");
		}
		
		sb.replace(sb.length() - 1, sb.length(), "]");
		
		return sb.toString();
	}
	
	/**
	 * Adds a taxon to the cyclic order, based on the given vector
	 * <code>x<sup>f</sup></code>.
	 * 
	 * <p>For example, if the current cyclic order is <code>[0, 2, 1]</code>,
	 * then this method will search in the given vector <code>v</code> where
	 * <code>i = 3</code> should be placed.</p>
	 * 
	 * @param i The number of the taxon. This should be <code>taxa.size()</code>,
	 * but is passed as a parameter for clarity.
	 * @param v The vector to decide the position with.
	 * @return whether the cyclic order is no longer cyclic
	 */
	public boolean addTaxonBasedOnVector(int i, DenseVector v) {
		
		assert i == taxa.size();

		if (taxa.size() < 2) {
			taxa.add(i);
			return false;
		}
		
		Triple t;
		int correctIndex = taxa.size();

		for(int j = 1; j < taxa.size(); j++) {
			t = new Triple(taxa.get(0), i, taxa.get(j));
			boolean earlier = v.getElement(t);
			if(earlier && correctIndex == taxa.size()) {
				correctIndex = j;
			}
			if(!earlier && correctIndex != taxa.size()) {
				return true;
			}
		}
		
		taxa.add(correctIndex, i);
		
		return false;
	}
	
	/**
	 * Searches for a taxon in this cyclic order.
	 * 
	 * @param t The taxon.
	 * @return The index in the {@link #taxa} ArrayList on which this taxon is
	 * placed, or <code>-1</code> if the taxon isn't present.
	 */
	private int searchTaxon(int t) {
		return taxa.indexOf(t);
	}

	/**
	 * Checks whether this order is consistent with a certain vector, by looking
	 * at every triple. For every such triple it is checked whether the actual value
	 * (in this ordering) is equal to the expected value (in the vector).
	 * 
	 * <p>When the ordering is not consistent with the vector, the <i>first</i>
	 * triple in lexicographic order that is violated is returned. When the ordering
	 * is consistent, <code>null</code> is returned.</p>
	 * 
	 * @param v The vector.
	 * @return A violating triple or <code>null</code>, as described above.
	 */
	public Triple consistentWithVector(DenseVector v) {
		
		// loop over all element vectors and see if their values in the vector
		// correspond to the actual consistency with the triples
		for (int i = 0; i < v.smartToStupid.length; i++) {
			boolean shouldBeConsistent = v.getElementOnIndex(i);
			Triple t = v.indexToTriple(i);
			if (consistentWithTriple(t) == shouldBeConsistent) {
				return t;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns whether the given triple is positioned in this order by
	 * this {@link CyclicOrder}.
	 * 
	 * @param t The triple.
	 * @return Whether the triple holds for this cyclic order.
	 */
	private boolean consistentWithTriple(Triple t) {
		
		int p1 = searchTaxon(t.i1);
		int p2 = searchTaxon(t.i2);
		int p3 = searchTaxon(t.i3);
		
		return new Triple(p1, p2, p3).makeAscending();
	}
	
	/**
	 * Reverses the order of certain taxa on the order.
	 * 
	 * <p>The taxa to reverse are indicated by the <code>begin</code> and <code>end</code>
	 * parameters. The block of taxa starting with the <code>begin</code><sup>th</sup>
	 * taxon (inclusive) and ending with the <code>end</code><sup>th</sup> taxon
	 * (inclusive) will be reversed. This "reverse" means that the first element of the
	 * block of taxa will now be last, the second will now be one-before-last, and so on.</p>
	 * 
	 * <p>For example: assume the circle contains the following:
	 * <pre>
	 * a, b, c, d, e, f, g
	 * </pre>
	 * If you now call <code>reverse(0, 3)</code>, the following circle is
	 * returned:
	 * <pre>
	 * c, b, a, d, e, f, g
	 * </pre>
	 * And if (from the original circle) you call <code>reverse(1, 5)</code>,
	 * the following circle is returned:
	 * <pre>
	 * a, e, d, c, b, f, g
	 * </pre>
	 * </p>
	 * 
	 * <p>Note: <code>end</code> should be <code>&gt;= begin</code>.</p>
	 * 
	 * @param begin The index of the first element of the block of taxa to reverse
	 * (inclusive).
	 * @param end The index of the last element of the block of taxa to reverse
	 * (exclusive).
	 */
	public void reverse(int begin, int end) {
		
		for (int i = begin; i < end; i++) {
			moveToIndex(end - 1, i);
		}
	}
	
	/**
	 * Moves the taxon on <code>oldIndex</code> and moves it to
	 * <code>newIndex</code>. This is of course equivalent with removing
	 * the taxon on <code>oldIndex</code> and re-inserting it on
	 * <code>newIndex</code>.
	 * 
	 * @param oldIndex The index of the taxon to move.
	 * @param newIndex The new index of the taxon.
	 */
	private void moveToIndex(int oldIndex, int newIndex) {
		Integer taxonToMove = taxa.remove(oldIndex);
		
		taxa.add(newIndex, taxonToMove);
	}
	
	/**
	 * Determines the encoding of this cyclic order.
	 * 
	 * <p>This method is like the inverse of {@link DenseVector#determineOrder()}.</p>
	 * 
	 * @return A {@link DenseVector} containing the encoding.
	 */
	public DenseVector determineVector() {
		DenseVector xf = new DenseVector(taxa.size());
		
		for (int i = 0; i < ((taxa.size() - 1) * (taxa.size() - 2) / 2); i++) {
			Triple t = xf.indexToTriple(i);
			
			if (consistentWithTriple(t)) {
				xf.setElementOnIndex(i, true);
			}
		}
		
		return xf;
	}
}
