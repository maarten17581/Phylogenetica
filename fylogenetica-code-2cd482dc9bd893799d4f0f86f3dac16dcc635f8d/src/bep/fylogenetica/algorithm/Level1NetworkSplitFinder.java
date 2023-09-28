package bep.fylogenetica.algorithm;

import java.util.ArrayList;
import java.util.Arrays;

import bep.fylogenetica.gui.NetworkPanel;
import bep.fylogenetica.model.Level1Network;
import bep.fylogenetica.model.Level1Network.Level1NetworkType;
import bep.fylogenetica.model.Network;

/**
 * This class contains the static method {@link #findSplits(CyclicOrder, GF2Matrix)},
 * that returns a {@link Level1Network} object based on the results of the previous steps
 * of the algorithm.
 */
public class Level1NetworkSplitFinder {
	
	/**
	 * Finds all non-trivial splits in the given data, even crossing ones, that do not
	 * contain the last taxon on <code>f</code> (that is, <code>f.taxa.get(f.taxa.size() - 1</code>).
	 * This will be a lateral collection.
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>. 
	 * @return A set of splits.
	 */
	public static ArrayList<int[]> findListOfSplits(CyclicOrder f, DenseVector xf, GF2Matrix m) {
		
		ArrayList<int[]> result = new ArrayList<>();
		
		for (int start = 0; start < f.taxa.size(); start++) {
			for (int end = start + 1; end < f.taxa.size(); end++) {
				if (isSplit(f, xf, m, start, end) && !isTrivialSplit(f.taxa.size(), start, end)) {
					result.add(new int[]{start, end});
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Returns whether a split is considered to be trivial.
	 * 
	 * <p>A split is trivial if one of its sides have 0 or 1 elements.</p>
	 * 
	 * @param m The number of taxa.
	 * @param startIndex The begin index (inclusive).
	 * @param endIndex The end index (inclusive).
	 * @return Whether the split is trivial.
	 */
	private static boolean isTrivialSplit(int m, int start, int end) {
		
		if (((start + m - 1) % m) == (end % m) || (start % m) == (end % m) || (start % m) == ((end + m - 1) % m)) {
			return true;
		}
		
		return false;
	}

	/**
	 * Finds splits in the given data and reconstructs the level-1-network corresponding
	 * with it.
	 * 
	 * <h3>Implementation details</h3>
	 * <p>The algorithm as described in the paper is not exactly followed here. Instead
	 * of first building the rooted tree-representation of the lateral set of splits
	 * (not containing taxon 0), we directly manipulate the resulting network.</p>
	 * 
	 * <p>So, instead of starting with a star tree, we start with the corresponding
	 * network, which is a simple level-1-network. Then, for every split, we update
	 * the network like described in Algorithm 5.8, however, we do these steps in
	 * the network immediately.</p>
	 * 
	 * <p>An advantage of this strategy is that the order of taxa can be maintained.
	 * Whereas in a rooted tree-representation no order of taxa is present, this
	 * certainly is present in the resulting network. In the paper, this is
	 * compensated for in Algorithm 5.10.</p>
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>.
	 * @param xf The {@link DenseVector} corresponding to <code>f</code>
	 * @return The reconstructed level-1-network.
	 * @throws NotCyclicException If <code>m</code> represents a non-cyclic
	 * subspace. Note that this may or may not be detected; thus, an exception will be
	 * thrown on an best-effort basis.
	 */
	public static Level1Network reconstructNetwork(CyclicOrder f, DenseVector xf, GF2Matrix m) throws NotCyclicException {
		
		// initialization
		Level1Network[] n = new Level1Network[f.taxa.size()];
		
		for (int i = 0; i < f.taxa.size(); i++) {
			n[i] = new Level1Network(f.taxa.get(i));
		}
		
		Level1Network network = new Level1Network(true, n);
		
		// get the splits
		ArrayList<int[]> splits = findListOfSplits(f, xf, m);
		
		for (int i = 0; i < splits.size(); i++) {
			int[] split = splits.get(i);
			
			Level1Network[] anchestor = searchCommonAnchestor(network, split, f);
			
			// this should not happen by Lemma 5.9 c)
			if (anchestor.length <= 2) {
				throw new RuntimeException("Unexpected situation occured during the network reconstruction");
			}
			
			if (anchestor[0].type != Level1NetworkType.CIRCLE) {
				throw new NotCyclicException();
			}
			
			// replace the first subnetwork by a network consisting of the whole split...
			Level1Network replacement;
			if (anchestor.length == 3) {
				replacement = new Level1Network(anchestor[1], anchestor[2]);
			} else {
				replacement = new Level1Network(true, Arrays.copyOfRange(anchestor, 1, anchestor.length)); // FIXME should be false
			}
			anchestor[0].replace(anchestor[1], replacement);
			
			// ... and remove all others
			for (int j = 2; j < anchestor.length; j++) {
				anchestor[0].remove(anchestor[j]);
			}
		}
		
		network.removeTrivialCircles();
		
		return network;
	}

	/**
	 * Searches for the common anchestor of the elements in the split.
	 * 
	 * <p>Actually not only the common anchestor is returned, but also the subnetworks
	 * of the common anchestor that define the split. In more detail: an array is returned
	 * of which the first element (index 0) is the common anchestor networks; the further
	 * <code>&gt;= 0</code> elements are the subnetworks of the common anchestor that
	 * contain exactly the taxa of the split.</p>
	 * 
	 * @param n The network to search in.
	 * @param split The split to search the common anchestor for.
	 * @return An array of level-1-networks as described above, or <code>null</code> if the
	 * level-1-network doesn't contain any of the elements in the split.
	 */
	private static Level1Network[] searchCommonAnchestor(Level1Network n, int[] split, CyclicOrder f) {
		
		if (n.type == Level1NetworkType.SINGLETON) {
			if (containedInSplit(n.taxon, split, f)) {
				return new Level1Network[] {n};
			} else {
				return null;
			}
		}
		
		if (n.type == Level1NetworkType.EDGE) {
			Level1Network[] sub1 = searchCommonAnchestor(n.subNetwork1, split, f);
			Level1Network[] sub2 = searchCommonAnchestor(n.subNetwork2, split, f);
			if (sub1 == null && sub2 == null) {
				return null;
			} else if (sub1 == null) {
				return sub2;
			} else if (sub2 == null) {
				return sub1;
			} else {
				return new Level1Network[] {n, n.subNetwork1, n.subNetwork2};
			}
		}
		
		if (n.type == Level1NetworkType.BLOB || n.type == Level1NetworkType.CIRCLE) {
			
			ArrayList<Level1Network> containingChildren = new ArrayList<Level1Network>();
			
			for (Level1Network child : n.connectedNetworks) {
				Level1Network[] childAnchestor = searchCommonAnchestor(child, split, f);
				if (childAnchestor != null) {
					containingChildren.add(child);
				}
			}
			
			if (containingChildren.isEmpty()) {
				return null;
			}
			
			if (containingChildren.size() == 1) {
				return searchCommonAnchestor(containingChildren.get(0), split, f);
			}
			
			Level1Network[] result = new Level1Network[containingChildren.size() + 1];
			result[0] = n;
			for (int i = 0; i < containingChildren.size(); i++) {
				result[i + 1] = containingChildren.get(i);
			}
			return result;
		}
		
		return null;
	}
	
	private static boolean containedInSplit(int taxon, int[] split, CyclicOrder f) {
		
		for (int i = split[0]; i < split[1]; i++) {
			if (f.taxa.get(i) == taxon) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Determines whether the taxa on a given part of a {@link CyclicOrder} form
	 * a split.
	 * 
	 * <p>For this method, <code>startIndex</code> may be larger than <code>endIndex</code>.</p>
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>. 
	 * @param startIndex The begin index (inclusive).
	 * @param endIndex The end index (exclusive).
	 * @return Whether the taxa are a split.
	 */
	private static boolean isSplit(CyclicOrder f, DenseVector xf, GF2Matrix m, int startIndex, int endIndex) {
		
		// in this case, the split "crosses" 0, but if one side is a split, the other is too
		if (startIndex > endIndex) {
			int h = endIndex;
			endIndex = startIndex;
			startIndex = h;
		}
		
		CyclicOrder g = new CyclicOrder(f);
		g.reverse(startIndex, endIndex);
		DenseVector xg = g.determineVector();
		
		return m.conformsToMatrix(xg);
	}
}
