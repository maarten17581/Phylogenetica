package bep.fylogenetica.algorithm;

import java.util.ArrayList;
import java.util.Arrays;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Quartet;
import bep.fylogenetica.model.Inference;

/**
 * A (dense) vector in GF(2)^[T] that is 'smart' about filling in values.
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
public class DenseVector {
	
	/**
	 * Taxon count of the previously saved conversion table.
	 */
	private static int savedTaxonCount = -1;

	/**
	 * Cache of {@link #smartToStupid}. See the class comment.
	 */
	private static int[] savedSmartToStupid;
	
	/**
	 * Cache of {@link #stupidToSmart}. See the class comment.
	 */
	private static int[] savedStupidToSmart;
	
	/**
	 * The values. This array has length <code>taxa.size() ^ 2</code>.
	 */
	long[] values;
	
	/**
	 * The number of taxa in the taxon set. Note that the complete set of taxa is
	 * not stored.
	 */
	int taxonCount;
	
	/**
	 * Conversion lookup table from the smart to the stupid representation. See the
	 * class comment.
	 */
	int[] smartToStupid;
	
	/**
	 * Conversion lookup table from the stupid to the smart representation. See the
	 * class comment.
	 * 
	 * <p>If a stupid index doesn't have a counterpart in the array (that is, if
	 * {@link #isStored(Triple)} returns false for the corresponding Triple), then
	 * the value in this array is -1.</p>
	 */
	int[] stupidToSmart;

	/**
	 * Mutex lock
	 */
	private static Object mutex = new Object();
	
	/**
	 * Produces a new vector over the given taxa set.
	 * @param taxonCount The amount of taxa to construct the vector for.
	 */
	public DenseVector(int taxonCount) {
		synchronized (mutex) {
			this.taxonCount = taxonCount;
			values = new long[(int)Math.ceil(((taxonCount - 1) * (taxonCount - 2) / 2)/64.0)];
			
			createLookupTables();
		}
	}
	
	/**
	 * Generates the {@link #smartToStupid} and {@link #stupidToSmart} lookup
	 * tables.
	 */
	private void createLookupTables() {
		
		assert smartToStupid == null;
		assert stupidToSmart == null;
		
		if (taxonCount != savedTaxonCount) {
			savedTaxonCount = taxonCount;
			savedSmartToStupid = new int[(taxonCount - 1) * (taxonCount - 2) / 2];
			savedStupidToSmart = new int[taxonCount * taxonCount];
			
			int smartIndex = 0;
			
			for (int i = 0; i < taxonCount * taxonCount; i++) {
				if (isStored(stupidIndexToTriple(i))) {
					savedStupidToSmart[i] = smartIndex;
					savedSmartToStupid[smartIndex] = i;
					smartIndex++;
				} else {
					savedStupidToSmart[i] = -1;
				}
			}
			
			assert smartIndex == savedSmartToStupid.length;
		}
		
		smartToStupid = savedSmartToStupid;
		stupidToSmart = savedStupidToSmart;
	}

	public void changeAll(DenseVector v) {
		this.values = v.values;
		this.taxonCount = v.taxonCount;
		this.smartToStupid = v.smartToStupid;
		this.stupidToSmart = v.stupidToSmart;
	}
	
	/**
	 * Checks whether the given triple is explicitly stored in the vector.
	 * @param triple The triple to check.
	 * @return <code>true</code> if the triple is stored, <code>false</code> otherwise.
	 */
	private boolean isStored(Triple triple) {
		if (triple.i1 != 0 || triple.i2 <= triple.i1 || triple.i3 <= triple.i2) {
			return false;
		}
		return true;
	}

	/**
	 * Converts the given triple to the corresponding index in the array.
	 * 
	 * @param triple The triple that indicates the element to get the index of.
	 */
	public int tripleToIndex(Triple triple) {
		assert triple.i1 == 0;
		
		return stupidToSmart[taxonCount * triple.i2 + triple.i3];
	}
	
	/**
	 * Converts the given array index to the corresponding triple.
	 * 
	 * @param index The index to get the triple of.
	 */
	public Triple indexToTriple(int index) {
		assert index >= 0;

		int stupid = smartToStupid[index];
		
		return stupidIndexToTriple(stupid);
	}
	
	/**
	 * Converts the given 'stupid' index (see the class comment) to the
	 * corresponding triple.
	 * 
	 * @param index The index to get the triple of.
	 */
	private Triple stupidIndexToTriple(int stupid) {
		assert stupid >= 0;
		
		int i2 = stupid / taxonCount;
		int i3 = stupid - taxonCount * i2;
		
		return new Triple(0, i2, i3);
	}

	/**
	 * Gets a certain element from the vector, based on its index in the array.
	 * 
	 * <p>This is equivalent with using
	 * <pre>
	 * getElement(indexToTriple(index))
	 * </pre>
	 * but it is more efficient.
	 * </p>
	 * 
	 * <p>This method is mainly used for creating visualisations of the data.</p>
	 * 
	 * @param index The index the element to retrieve resides on.
	 * @return The value.
	 */
	public boolean getElementOnIndex(int index) {
		return ((values[index/64] >> (index % 64)) & 1) != 0 ? true : false;
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
		
		return ((values[tripleToIndex(triple)/64] >> (tripleToIndex(triple) % 64)) & 1) != 0 ? true : false;
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
	 * Sets a certain element from the vector, based on its index in the array.
	 * 
	 * <p>This is equivalent with using
	 * <pre>
	 * setElement(indexToTriple(index), value)
	 * </pre>
	 * but it is more efficient.
	 * </p>
	 * 
	 * <p>This method is mainly used for creating visualisations of the data.</p>
	 * 
	 * @param index The index the element to retrieve resides on.
	 * @param value The new value.
	 */
	public void setElementOnIndex(int index, boolean value) {
		if (value) {
			values[index/64] |= (1L << (index % 64));
		} else {
			values[index/64] &= (-1L) ^ (1L << (index % 64));
		}
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
		
		if (value) {
			values[tripleToIndex(triple)/64] |= (1L << (tripleToIndex(triple) % 64));
		} else {
			values[tripleToIndex(triple)/64] &= (-1L) ^ (1L << (tripleToIndex(triple) % 64));
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
	 * Adds another vector to this vector in-place.
	 * 
	 * @param v The vector to add to this vector. This vector should be over
	 * the same set of taxa as this vector, else the behaviour is unspecified.
	 * @param startIndex When reducing, it is most often not needed to add the whole
	 * row, since it is already known that parts of the row to add are already zero.
	 * Therefore this parameter can be used. For example, if <code>startIndex == 3</code>
	 * the first element added is on index 3; the elements on index 0, 1 and 2 will
	 * remain unchanged. Use <code>startIndex == 0</code> if you just want to add
	 * the whole row.
	 */
	public void addVector(DenseVector v, int startIndex) {
		assert values.length == v.values.length;
		
		for (int i = startIndex/64; i < values.length; i++) {
			values[i] ^= v.values[i];
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("[");
		
		for (int i = values.length-1; i >= 0; i--) {
			if (i < values.length-1) {
				for (int j = 0; j < Long.numberOfLeadingZeros(values[i]); j++) {
					sb.append('0');
				}
			} else {
				for (int j = 0; j < Long.numberOfLeadingZeros(values[i])-(64-((taxonCount-1)*(taxonCount-2)/2)%64); j++) {
					sb.append('0');
				}
			}
			sb.append(Long.toBinaryString(values[i]));
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	/**
	 * Returns whether this vector contains only zeroes.
	 * 
	 * @return <code>true</code> if this vector contains only zeroes, <code>false</code>
	 * otherwise.
	 */
	public boolean isZeroVector() {
		
		for (long b : values) {
			if (b != 0) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Creates a cyclic order <code>f</code> based on this {@link DenseVector}
	 * <code>x<sup>f</sup></code>.
	 * 
	 * @throws NotCyclicException When this vector was not cyclic.
	 */
	public CyclicOrder determineOrder() throws NotCyclicException {

		CyclicOrder result = new CyclicOrder(0);
		
		for (int i = 0; i < taxonCount; i++) {
			boolean notCyclic = result.addTaxonBasedOnVector(i, this);
			if(notCyclic) {
				return null;
			}
		}
		
		// Second step: it may be that the result isn't correct.
		// This happens when the vector wasn't cyclic.
		Triple t = result.consistentWithVector(this);
		
		if (t != null) {
			throw new NotCyclicException();
		}
		
		return result;
	}
	
	/**
	 * Multiplies another vector with this vector and returns the result.
	 * 
	 * <p>This vector and <code>v</code> should have the same taxon count.</p>
	 * 
	 * @param v The other vector.
	 * @return The result (interpret as 0 or 1).
	 */
	public boolean multiplyWithVector(DenseVector v) {
		
		assert taxonCount == v.taxonCount;
		
		int result = 0;
		
		for (int i = 0; i < values.length; i++) {
			result += Long.bitCount(values[i] & v.values[i]);
		}
		
		return ((result % 2) == 1);
	}

	public DenseVector select(ArrayList<Integer> taxa) {
		DenseVector vSmall = new DenseVector(taxa.size());
		for (int i = 1; i < taxa.size() - 1; i++) {
			for (int j = i+1; j < taxa.size(); j++) {
				vSmall.setElement(new Triple(0, i, j), getElement(new Triple(taxa.get(0), taxa.get(i), taxa.get(j))));
			}
		}
		return vSmall;
	}
}
