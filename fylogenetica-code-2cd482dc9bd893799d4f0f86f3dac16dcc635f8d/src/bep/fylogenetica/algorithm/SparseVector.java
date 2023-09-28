package bep.fylogenetica.algorithm;

import java.util.ArrayList;

// TODO change this text
/**
 * A (sparse) vector in GF(2)^[T] that is 'smart' about filling in values.
 * 
 * <p>Only the values corresponding to (a, b, c) in T starting with a certain value
 * with a, b, c in ascending order are saved.</p>
 * 
 * <p>The elements in the vector are lexicographically ordered, so for example:
 * <pre>
 * (a, b, c) (a, b, d) (a, c, d)
 * </pre>
 * </p>
 * 
 * Using the {@link #getElement(Triple)} method, you can request the value for any
 * triple in T.
 * 
 * <h3>Implementation details</h3>
 * <p>A problem in the implementation of this class was the conversion between a
 * {@link Triple} and an index in the array. To make this efficient, a conversion
 * table is created between
 * <ul>
 * <li>the so-called <i>smart</i> indexing scheme, which means, the index in the
 * array, and</li>
 * <li>the so-called <i>stupid</i> indexing scheme, which means that every 3-tuple
 * gets a number, so (a, a, a) gets 0, (a, a, b) gets 1, and also for example
 * (a, b, a) will be numbered. The advantage of the stupid scheme is that it can
 * be derived easily from the actual triple and the other way round.
 * </ul>
 * </p>
 * 
 * <p>Of course, this conversion table is exactly the same between all {@link DenseVector}
 * objects with the same taxon count. Because all vectors will have the same taxon
 * count, a reference to the last created table is maintained (in a static object)
 * and if the same taxon count is found during construction, this object is just picked
 * as the conversion table.</p>
 */
public class SparseVector {
	
	/**
	 * The triples that are true.
	 */
	ArrayList<Triple> values;

	/**
	 * The value that its equal to
	 */
	boolean result;
	
	/**
	 * The number of taxa in the taxon set. Note that the complete set of taxa is
	 * not stored.
	 */
	int taxonCount;

	/**
	 * The boolean used for determining if this row is the pivot for row reducing in {@link GF2Matrix}
	 */
	boolean isPivot;
	
	
	/**
	 * Produces a new vector over the given taxa set.
	 * @param taxonCount The amount of taxa to construct the vector for.
	 */
	public SparseVector(int taxonCount) {
		
		this.taxonCount = taxonCount;
		values = new ArrayList<>();
		result = false;
	}
	
	/**
	 * Gets the element corresponding to the given triple. This method is called
	 * "unsafe" because if you ask for triple (0, 2, 1) for example, an exception
	 * will be thrown. Use {@link #getElement(Triple)} when you want to be able
	 * to ask for any triple.
	 * 
	 * @param triple The triple that indicates the element to retrieve. This triple must
	 * be ascending, and starting with ID 0.
	 * @return The value.
	 */
	public boolean getElementUnsafe(Triple triple) {
		assert triple.i1 == 0;
		assert triple.i2 > triple.i1;
		assert triple.i3 > triple.i2;
		
		return values.contains(triple);
	}
	
	/**
	 * Gets the element corresponding to the given triple. This method is transparent,
	 * in that if you ask for a triple that is not stored, it will calculate it and
	 * return the calculated value (instead of returning nonsense like the method
	 * {@link #getElementUnsafe(Triple)} does).
	 * 
	 * <p>This method may modify the Triple object you give it.</p>
	 * 
	 * @param triple The triple that indicates the element to retrieve. This triple
	 * may contain any value (as long as they are in the set of taxa, of course).
	 * @return The value.
	 */
	public boolean getElement(Triple triple) {
		assert triple.i1 != triple.i2;
		assert triple.i1 != triple.i3;
		assert triple.i2 != triple.i3;
		
		boolean correction = triple.makeAscending();
		
		if (triple.i1 == 0) {
			return correction ^ getElementUnsafe(triple);
		}
		
		Triple t1 = new Triple(0, triple.i1, triple.i2);
		Triple t2 = new Triple(0, triple.i1, triple.i3);
		Triple t3 = new Triple(0, triple.i2, triple.i3);
		
		return correction ^ getElementUnsafe(t1) ^ getElementUnsafe(t2) ^ getElementUnsafe(t3);
	}
	
	/**
	 * Sets the element corresponding to the given triple. This method is called
	 * "unsafe" because if you ask to modify triple (0, 2, 1) for example, an exception
	 * will be thrown Use {@link #setElement(Triple, boolean)} when you want to be able
	 * to set any triple (that starts with taxon 0).
	 * 
	 * @param triple The triple that indicates the element to change. This triple must
	 * be ascending, and starting with ID 0.
	 * @param value The new value.
	 */
	public void setElementUnsafe(Triple triple, boolean value) {
		assert triple.i1 == 0;
		assert triple.i2 > triple.i1;
		assert triple.i3 > triple.i2;
		
		int contains = values.indexOf(triple);
		if (value && contains == -1) {
			// add sorted
			boolean added = false;
			for (int i = 0; i < values.size(); i++) {
				if (triple.lessThanEqual(values.get(i))) {
					values.add(i, triple);
					added = true;
					break;
				}
			}
			if(!added) {
				values.add(triple);
			}
		} else if (!value && contains != -1) {
			values.remove(contains);
		}
	}
	
	/**
	 * Sets the element corresponding to the given triple. This method is transparent,
	 * in that if you ask to modify a triple that is not stored, it will determine the
	 * triple that should be modified instead and executes that change (instead of
	 * doing nothing like the method {@link #setElementUnsafe(Triple)} does).
	 * 
	 * <p>This method does not support triples that do not contain taxon 0. This
	 * would be easy to implement (using transitivity -- see {@link #getElement(Triple)})
	 * however it is not needed in this program. If you give a triple that does not
	 * contain 0, the behaviour is undefined.</p>
	 * 
	 * <p>This method may modify the Triple object you give it.</p>
	 * 
	 * @param triple The triple that indicates the element to change. This triple
	 * may contain any value, but must contain 0.
	 * @param value The new value.
	 */
	public void setElement(Triple triple, boolean value) {
		assert triple.i1 != triple.i2;
		assert triple.i1 != triple.i3;
		assert triple.i2 != triple.i3;
		
		boolean correction = triple.makeAscending();
		assert triple.i1 == 0;
		
		setElementUnsafe(triple, correction ^ value);
	}
	
	/**
	 * This method returns the result value of the vector
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * Sets the result of this vector to {@code result}
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * This method checks if the first element of this vector equals the triple {@code t}
	 * @param t To check against the first element of the values
	 * @return Whether the first element of the values equals {@code t} or not
	 */
	public boolean isFirstVector(Triple t) {
		return values.get(0).equals(t);
	}
	
	/**
	 * Adds another vector to this vector in-place.
	 * 
	 * @param v The vector to add to this vector. This vector should be over
	 * the same set of taxa as this vector, else the behaviour is unspecified.
	 */
	public void addVector(SparseVector v) {
		ArrayList<Triple> newValues = new ArrayList<>();
		int x = 0;
		int y = 0;
		while (x < values.size() && y < v.values.size()) {
			if (values.get(x).lessThan(v.values.get(y))) {
				newValues.add(values.get(x));
				x++;
			} else if (v.values.get(y).lessThan(values.get(x))) {
				newValues.add(v.values.get(y));
				y++;
			} else if (values.get(x).equals(v.values.get(y))) {
				// if there are 2 remove them cause they cancel
				x++;
				y++;
			}
		}
		while (x < values.size() || y < v.values.size()) {
			if (x == values.size()) {
				newValues.add(v.values.get(y));
				y++;
			} else if (y == v.values.size()) {
				newValues.add(values.get(x));
				x++;
			}
		}
		setResult(getResult() ^ v.getResult());
		values = newValues;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		
		for (int i = 0; i < values.size(); i++) {
			sb.append(values.get(i));
			sb.append(" ");
		}
		
		sb.replace(sb.length() - 1, sb.length(), "]");
		
		return sb.toString();
	}
	
	/**
	 * Returns whether this vector contains only zeroes.
	 * 
	 * @return <code>true</code> if this vector contains only zeroes, <code>false</code>
	 * otherwise.
	 */
	public boolean isZeroVector() {
		return values.isEmpty();
	}
	
	/**
	 * Multiplies a denxe vector with this vector and returns the result.
	 * 
	 * <p>This vector and <code>v</code> should have the same taxon count.</p>
	 * 
	 * @param v The other vector.
	 * @return The result (interpret as 0 or 1).
	 */
	public boolean multiplyWithVector(DenseVector v) {
		
		assert taxonCount == v.taxonCount;
		
		boolean result = false;
		
		for (int i = 0; i < values.size(); i++) {
			if (((v.values[v.tripleToIndex(values.get(i))/64] >> (v.tripleToIndex(values.get(i)) % 64)) & 1L) == 1L) {
				result ^= true;
			}
		}
		
		return result;
	}

	/*@Override
	public boolean equals(Object o) {
		if (!(o instanceof SparseVector)) {
			return false;
		}
		SparseVector v = (SparseVector) o;
		if (taxonCount != v.taxonCount) {
			return false;
		}
		if (values.size() != v.values.size()) {
			return false;
		}
		if (isPivot != v.isPivot) {
			return false;
		}
		for (int i = 0; i < values.size(); i++) {
			if (!values.get(i).equals(v.values.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = getResult() ? 0 : 123456789;
		hash ^= isPivot ? 0 : 987654321;
		for (int i = 0; i < values.size(); i++) {
			hash ^= (values.get(i).hashCode() >>> (i%32)) | (values.get(i).hashCode() << ((32-i)%32));
		}
		hash ^= taxonCount;
		return hash;
	}*/
}
