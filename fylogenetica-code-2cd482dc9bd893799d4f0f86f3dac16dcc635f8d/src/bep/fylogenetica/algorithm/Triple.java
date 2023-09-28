package bep.fylogenetica.algorithm;

/**
 * A triple, that is, an element of [T].
 */
public class Triple {
	
	public int i1;
	public int i2;
	public int i3;
	
	/**
	 * Constructs a new Triple.
	 * 
	 * @param i1 The first value.
	 * @param i2 The second value.
	 * @param i3 The third value.
	 */
	public Triple(int i1, int i2, int i3) {
		this.i1 = i1;
		this.i2 = i2;
		this.i3 = i3;
	}
	
	/**
	 * Sorts the elements in-place.
	 * 
	 * @return Whether the number of swaps needed was even (<code>false</code>)
	 * or odd (<code>true</code>).
	 */
	public boolean makeAscending() {
		
		boolean evenSwaps = false;
		int h;
		
		// using bubble sort
		
		if (i1 > i2) {
			h = i1;
			i1 = i2;
			i2 = h;
			evenSwaps = !evenSwaps;
		}
		
		if (i2 > i3) {
			h = i2;
			i2 = i3;
			i3 = h;
			evenSwaps = !evenSwaps;
		}
		
		if (i1 > i2) {
			h = i1;
			i1 = i2;
			i2 = h;
			evenSwaps = !evenSwaps;
		}
		
		return evenSwaps;
	}
	
	/**
	 * Sorts the second and third element in-place. This is an alternative for
	 * {@link #makeAscending()} if you already know that the first element will
	 * be the smallest (e.g. it is 0). It is of course a bit more efficient than
	 * a complete sort.
	 * 
	 * @return Whether the number of swaps needed was even (i.e. 0, <code>false</code>)
	 * or odd (i.e. 1, <code>true</code>).
	 */
	public boolean makeAscendingAlt() {
		
		if (i2 > i3) {
			int h = i2;
			i2 = i3;
			i3 = h;
			return true;
		}
		
		return false;
	}

	/**
	 * Gives an order on the triples, given that both are in ascending order
	 * @param t Triple to give the order
	 * @return true if this triple is less than t
	 */
	public boolean lessThan(Triple t) {
		if (i1 == t.i1) {
			if(i2 == t.i2) {
				return i3 < t.i3;
			}
			return i2 < t.i2;
		} else {
			return i1 < t.i1;
		}
	}

	/**
	 * Gives an order on the triples, given that both are in ascending order
	 * @param t Triple to give the order
	 * @return true if this triple is less or equal to t
	 */
	public boolean lessThanEqual(Triple t) {
		if (i1 == t.i1) {
			if(i2 == t.i2) {
				return i3 <= t.i3;
			}
			return i2 <= t.i2;
		} else {
			return i1 <= t.i1;
		}
	}
	
	@Override
	public String toString() {
		return "[" + i1 + " " + i2 + " " + i3 + "]";
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Triple)) {
			return false;
		}
		Triple t = (Triple) o;
		return (i1 == t.i1 && i2 == t.i2 && i3 == t.i3);
	}

	@Override
	public int hashCode() {
		return (i1 << 16) ^ (i2 << 8) ^ (i3);
	}
}
