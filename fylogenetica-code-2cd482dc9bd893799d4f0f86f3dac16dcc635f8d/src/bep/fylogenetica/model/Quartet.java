package bep.fylogenetica.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a quartet.
 * 
 * <p>A quartet doesn't contain the actual {@link Taxon} objects, but for speed, instead
 * just the taxon IDs are maintained. To get the {@link Taxon}s back, note that the
 * ID of a taxon should be the same as its place in the taxon {@link ArrayList}.</p>
 */
public class Quartet {
	
	/** First taxon on the left side of the quartet. */
	public int left1;
	
	/** Second taxon on the left side of the quartet. */
	public int left2;
	
	/** First taxon on the right side of the quartet. */
	public int right1;
	
	/** Second taxon on the right side of the quartet. */
	public int right2;

	/** Used for inference rules as a sorting measure */
	public int totalOverlap;
	
	/**
	 * Constructs a quartet of the form "left1 left2 | right1 right2".
	 * 
	 * @param left1 First taxon on the left.
	 * @param left2 Second taxon on the left.
	 * @param right1 First taxon on the right.
	 * @param right2 Second taxon on the right.
	 */
	public Quartet(int left1, int left2, int right1, int right2) {
		this.left1 = left1;
		this.left2 = left2;
		this.right1 = right1;
		this.right2 = right2;
		totalOverlap = 0;
	}
	
	@Override
	public boolean equals(Object other) {
		
		if (other instanceof Quartet) {
			Quartet q = (Quartet) other;
			if (sameTaxa(left1, left2, q.left1, q.left2) && sameTaxa(right1, right2, q.right1, q.right2)) {
				return true;
			}
			if (sameTaxa(left1, left2, q.right1, q.right2) && sameTaxa(right1, right2, q.left1, q.left2)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public int hashCode() {
		return (left1 << 24) ^ (left2 << 16) ^ (right1 << 8) ^ right2;
	}
	
	/**
	 * Returns whether a1 and a2 are the same taxa as b1 and b2.
	 * @param a1 A number.
	 * @param a2 A number.
	 * @param b1 A number.
	 * @param b2 A number.
	 * @return The result, as a boolean.
	 */
	private static boolean sameTaxa(int a1, int a2, int b1, int b2) {
		return (a1 == b1 && a2 == b2) || (a1 == b2 && b1 == a2);
	}
	
	/**
	 * Returns whether there are no two the same taxa in this quartet.
	 * @return Whether the quartet is valid.
	 */
	public boolean isValid() {
		if (left1 == left2 || left1 == right1 || left1 == right2
				|| left2 == right1 || left2 == right2 || right1 == right2) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Puts this quartet in canonical form. That means:
	 * <ul>
	 * <li>both pairs are sorted, so for example <code>2 4 | 3 5</code> and not
	 * <code>2 4 | 5 3</code> or <code>4 2 | 3 5</code>;</li>
	 * <li>the pair with the lowest element is listed first, so for example
	 * <code>2 4 | 3 5</code> and not <code>3 5 | 2 4</code>.</li>
	 * </ul>
	 */
	public void toCanonicalForm() {
		
		int h;
		
		if (left2 < left1) {
			h = left1;
			left1 = left2;
			left2 = h;
		}
		
		if (right2 < right1) {
			h = right1;
			right1 = right2;
			right2 = h;
		}
		
		if (right1 < left1) {
			h = right1;
			right1 = left1;
			left1 = h;
			
			h = right2;
			right2 = left2;
			left2 = h;
		}
	}

	public boolean contains(int taxa) {
		return (left1 == taxa) || (left2 == taxa) || (right1 == taxa) || (right2 == taxa);
	}

	/**
	 * Calculates the amount of overlapping taxa in 2 quartets
	 * @param q The quartet to compare overlap with
	 * @return The amount of overlapping quartets
	 */
	public int overlap(Quartet q) {
		int overlap = 0;
		int x = 0;
		int y = 0;
		int[] taxa1 = new int[]{left1, left2, right1, right2};
		int[] taxa2 = new int[]{q.left1, q.left2, q.right1, q.right2};
		Arrays.sort(taxa1);
		Arrays.sort(taxa2);
		while (x < 4 && y < 4) {
			if (taxa1[x] == taxa2[y]) {
				overlap++;
				x++;
				y++;
			} else if (taxa1[x] < taxa2[y]) {
				x++;
			} else if (taxa1[x] > taxa2[y]) {
				y++;
			}
		}
		return overlap;
	}
	
	@Override
	public String toString() {
		return "(" + left1 + " " + left2 + " | " + right1 + " " + right2 + ")";
	}
}
