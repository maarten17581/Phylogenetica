package bep.fylogenetica.algorithm;

import java.lang.Enum.EnumDesc;
import java.util.ArrayList;
import java.util.Arrays;

import bep.fylogenetica.model.Tree;

/**
 * This class contains the static method {@link #findSplits(CyclicOrder, GF2Matrix)},
 * that returns a {@link Tree} object based on the results of the previous steps
 * of the algorithm.
 * 
 * <p>This split finder obviously doesn't work if the input was a level-1-network.</p>
 */
public class TreeSplitFinder {
	
	/**
	 * Finds splits in the given data.
	 * 
	 * <p>This method assumes that the splits don't cross (i.e. there are only cyclic
	 * vectors).</p>
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>. 
	 * @return A set of splits.
	 */
	public static Tree findSplits(CyclicOrder f, DenseVector xf, GF2Matrix m) {
		
		ArrayList<int[]> splits = findListOfSplits(f, xf, m);
		if (splits == null || splits.size() != f.taxa.size()-3) {
			return null;
		}
		Tree result = findSplitsRecursive(f, xf, m, 0, f.taxa.size());
		
		return result;
	}

	/**
	 * Creates the tree corresponding to the set of all splits, given that no splits are overlapping
	 * @param f
	 * @param splits
	 * @param used
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	private static Tree createTreeFromSplitsRecursive(CyclicOrder f, ArrayList<int[]> splits, boolean[] used, int startIndex, int endIndex, int taxonCount) {
		// base case: only one taxon
		if (startIndex == endIndex) {
			return new Tree(f.taxa.get(startIndex));
		}
		
		// base case: only two taxa
		if ((startIndex + 1)%taxonCount == endIndex) {
			return new Tree(new Tree(f.taxa.get(startIndex)), new Tree(f.taxa.get(endIndex)));
		}
		
		int[] maxSplit = new int[2];
		int index = -1;
		for (int i = 0; i < splits.size(); i++) {
			int[] split = splits.get(i);
			if (!used[i] && (split[0]-startIndex+taxonCount)%taxonCount <= (endIndex-startIndex+taxonCount)%taxonCount
					&& (split[1]-startIndex+taxonCount)%taxonCount <= (endIndex-startIndex+taxonCount)%taxonCount
					&& (split[1]-split[0]+taxonCount)%taxonCount > (maxSplit[1]-maxSplit[0]+taxonCount)%taxonCount) {
				maxSplit = split;
				index = i;
			}
		}

		if(index == -1) {
			System.out.println(startIndex + " " + endIndex);
		}
		used[index] = true;
		int[] otherSplit = new int[2];
		if (maxSplit[0] == startIndex) {
			otherSplit[0] = maxSplit[1]+1;
			otherSplit[1] = endIndex;
		} else if (maxSplit[1] == endIndex) {
			otherSplit[0] = startIndex;
			otherSplit[1] = maxSplit[0]-1;
		} else {
			otherSplit[0] = (maxSplit[1]+1)%taxonCount;
			otherSplit[1] = (maxSplit[0]-1+taxonCount)%taxonCount;
		}

		for (int i = 0; i < splits.size(); i++) {
			int[] split = splits.get(i);
			if (split[0] == otherSplit[0] && split[1] == otherSplit[1]) {
				used[i] = true;
				break;
			}
		}

		Tree subTree1 = createTreeFromSplitsRecursive(f, splits, used, maxSplit[0], maxSplit[1], taxonCount);
		Tree subTree2 = createTreeFromSplitsRecursive(f, splits, used, otherSplit[0], otherSplit[1], taxonCount);
		return new Tree(subTree1, subTree2);
	}

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
				if (!isTrivialSplit(f.taxa.size(), start, end) && isSplit(f, xf, m, start, end)) {
					int[] newSplit = new int[]{start, end};
					for (int[] split : result) {
						if (overlap(split, newSplit)) {
							//System.out.println(Arrays.toString(split) + " " + Arrays.toString(newSplit) + " splits with overlap");
							return null;
						}
					}
					result.add(newSplit);
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
	 * @param endIndex The end index (exclusive).
	 * @return Whether the split is trivial.
	 */
	private static boolean isTrivialSplit(int m, int start, int end) {
		
		if (((start + m - 1) % m) == (end % m) || (start % m) == (end % m) || (start % m) == ((end + m - 1) % m)) {
			return true;
		}
		
		return false;
	}

	private static boolean overlap(int[] split1, int[] split2) {
		return (split1[0] < split2[0] && split2[0] < split1[1] && split1[1] < split2[1]) ||
				(split2[0] < split1[0] && split1[0] < split2[1] && split2[1] < split1[1]);
	}

	/**
	 * Finds splits in the [<code>beginIndex</code>, <code>endIndex</code>] items
	 * on the {@link CyclicOrder}.
	 * Does not look out for non cyclicity of the dense vector!!!
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>. 
	 * @param startIndex The begin index (inclusive).
	 * @param endIndex The end index (inclusive).
	 * @return A tree with the splits. (TODO a tree of a list of splits needed?)
	 */
	private static Tree findSplitsRecursive(CyclicOrder f, DenseVector xf, GF2Matrix m, int startIndex, int endIndex) {
		
		assert endIndex - startIndex >= 0;
		// base case: only one taxon
		if (startIndex == endIndex - 1) {
			return new Tree(f.taxa.get(startIndex));
		}
		
		// base case: only two taxa
		if (startIndex == endIndex - 2) {
			return new Tree(new Tree(f.taxa.get(startIndex)), new Tree(f.taxa.get(endIndex-1)));
		}

		//System.out.println();
		//System.out.println("finding splits in [" + startIndex + "-" + endIndex + "]:");
		
		for (int i = startIndex+1; i < endIndex; i++) {
			
			//System.out.println(" * trying [" + startIndex + "-" + i + "]:");
			
			if (isSplit(f, xf, m, startIndex, i)) {
				
				//System.out.println("   this is a split!");

				//System.out.println("   verifying that [" + (i + 1) + "-" + endIndex + "] is also a split:");
				if (!isSplit(f, xf, m, i, endIndex)) {
					//System.out.println("   it isn't, so this is not an acceptable split");
					continue;
				}
				//System.out.println("   fine, it is");

				Tree subTree1 = findSplitsRecursive(f, xf, m, startIndex, i);
				Tree subTree2 = findSplitsRecursive(f, xf, m, i, endIndex);
				
				return new Tree(subTree1, subTree2);
			}
			
			//System.out.println("   no split");
		}
		
		System.out.println(f);
		throw new RuntimeException("There was no split found in a part of the cyclic ordering!\n" +
				"This happened in part [" + startIndex + ", " + endIndex + "] of the cyclic ordering (both inclusive).\n" +
				"Now I don't know what to do anymore.");
	}
	
	/**
	 * Determines whether the taxa on a given part of a {@link CyclicOrder} form
	 * a split.
	 * 
	 * @param m A {@link GF2Matrix} representing the affine subspace containing all
	 * cyclic vectors that satisfy the conditions.
	 * @param f An arbitrary {@link CyclicOrder} derived from <code>m</code>. 
	 * @param startIndex The begin index (inclusive).
	 * @param endIndex The end index (exclusive).
	 * @return Whether the taxa are a split.
	 */
	private static boolean isSplit(CyclicOrder f, DenseVector xf, GF2Matrix m, int startIndex, int endIndex) {
		
		//System.out.println("    - circular ordering was " + f.toString());
		if(startIndex > endIndex) {
			int temp = startIndex;
			startIndex = endIndex;
			endIndex = temp;
		}
		CyclicOrder g = new CyclicOrder(f);
		g.reverse(startIndex, endIndex);
		//System.out.println("    - and after reversing " + g.toString());
		DenseVector xg = g.determineVector();
		
		return m.conformsToMatrix(xg);
	}
}
