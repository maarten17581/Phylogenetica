package bep.fylogenetica.model;

import java.util.ArrayList;

import bep.fylogenetica.model.Network.Vertex;

/**
 * A recursive data structure for a tree.
 */
public class Tree {
	
	/**
	 * If <code>true</code>, this {@link Tree} is a singleton. If <code>false</code>,
	 * this Tree is a pair of trees.
	 */
	public boolean isSingleton;
	
	/**
	 * The first subtree (if <code>isSingleton == false</code>).
	 */
	public Tree subTree1;
	
	/**
	 * The second subtree (if <code>isSingleton == false</code>).
	 */
	public Tree subTree2;
	
	/**
	 * The singleton taxon (if <code>isSingleton == true</code>).
	 */
	public int taxon;
	
	/**
	 * Creates a new singleton tree.
	 * @param taxon The taxon this singleton tree should consist of.
	 */
	public Tree(int taxon) {
		isSingleton = true;
		this.taxon = taxon;
	}
	
	/**
	 * Creates a new compound tree.
	 * 
	 * @param subTree1 The first subtree.
	 * @param subTree2 The second subtree.
	 */
	public Tree(Tree subTree1, Tree subTree2) {
		isSingleton = false;
		this.subTree1 = subTree1;
		this.subTree2 = subTree2;
	}

	/**
	 * Generates a random tree on the given amount of taxa.
	 * 
	 * @param taxonCount The number of taxa in the tree.
	 * @return The created tree.
	 */
	public static Tree generateRandomTree(int taxonCount) {
		
		ArrayList<Integer> taxa = new ArrayList<>();
		
		for (int i = 0; i < taxonCount; i++) {
			taxa.add(i);
		}
		
		return generateRandomTree(taxa);
	}
	
	/**
	 * Generates a random tree on the given taxa.
	 * 
	 * @param taxa The taxa in the tree.
	 * @return The created tree.
	 */
	public static Tree generateRandomTree(ArrayList<Integer> taxa) {
		
		if (taxa.size() == 1) {
			return new Tree(taxa.get(0));
		}
		
		ArrayList<Integer> subTaxa1;
		ArrayList<Integer> subTaxa2;
		
		do {
			subTaxa1 = new ArrayList<>();
			subTaxa2 = new ArrayList<>();
			
			for (Integer i : taxa) {
				if (Math.random() < 0.5) {
					subTaxa1.add(i);
				} else {
					subTaxa2.add(i);
				}
			}
		} while (subTaxa1.size() == 0 || subTaxa2.size() == 0); // dit is best lelijk
		
		return new Tree(generateRandomTree(subTaxa1), generateRandomTree(subTaxa2));
	}
	
	@Override
	public String toString() {
		if (isSingleton) {
			return "" + taxon;
		}
		
		return "{" + subTree1 + ", " + subTree2 + "}";
	}
	
	/**
	 * Checks whether this tree conforms to the given quartet.
	 * 
	 * @param q The quartet to check for.
	 * @return Whether the tree conforms to the quartet.
	 */
	public boolean conformsToQuartet(Quartet q) {
		// TODO implement
		assert false : "not implemented";
		return false;
	}
	
	/**
	 * Contracts the tree to the elements from the given quartet.
	 * 
	 * @param q The quartet to use the elements from.
	 * @return The new tree, or <code>null</code> if the tree is reduced to
	 * "nothing" (i.e. if none of the elements of the quartet were in the tree).
	 */
	public Tree contractTo(Quartet q) {
		
		if (isSingleton) {
			if (quartetContains(q, taxon)) {
				return new Tree(taxon);
			}
			
			return null;
		}
		
		// this tree is a compound tree
		Tree tree = new Tree(subTree1.contractTo(q), subTree2.contractTo(q));
		return tree;
	}

	/**
	 * Returns whether the given quartet contains the taxon.
	 * 
	 * TODO perhaps move to Quartet?
	 * 
	 * @param q The quartet.
	 * @param t The taxon to check for.
	 * @return Whether the quartet contains the taxon.
	 */
	private static boolean quartetContains(Quartet q, int t) {
		return t == q.left1 || t == q.left2 || t == q.right1 || t == q.right2;
	}
	
	/**
	 * Creates a network of this tree.
	 * @return The created network.
	 */
	public Network toNetwork() {
		Network g = new Network();
		addToNetwork(g, null);
		return g;
	}
	
	/**
	 * Adds this tree to the given network. This is being called from {@link #toNetwork()}.
	 * 
	 * @param n The network to add the vertices and edges to.
	 * @param parent The vertex to connect the network to. If this is <code>null</code>,
	 * the tree is assumed not to have a parent. This means that there are no edges connected
	 * to it if the tree is a singleton, or that only the two subtrees are connected to each
	 * other if the tree is a compound tree.
	 * @return The root vertex in the resulting network. This is the vertex a parent network
	 * has to be attached to.
	 */
	Vertex addToNetwork(Network n, Vertex parent) {
		
		Vertex root = null;
		
		if (isSingleton) {
			
			// the singleton vertex
			root = new Vertex(String.valueOf(taxon));
			n.addVertex(root);
			if (parent != null) {
				n.addEdge(parent, root);
			}
			
		} else {

			if (parent != null) {
				// the root
				root = new Vertex("");
				n.addVertex(root);
				subTree1.addToNetwork(n, root);
				subTree2.addToNetwork(n, root);
			
				// the connecting edge to the parent
				n.addEdge(parent, root);
			} else {
				root = new Vertex("");
				n.addVertex(root);
				Vertex v1 = subTree1.addToNetwork(n, root);
				Vertex v2 = subTree2.addToNetwork(n, root);
				n.addEdge(v1, v2);
				n.removeVertex(root);
			}
		}
		
		return root;
	}
	
	/**
	 * Returns a list of all quartets of this tree.
	 * 
	 * @param taxonCount The amount of taxa this tree has.
	 * @return An {@link ArrayList} containing the quartets.
	 */
	public ArrayList<Quartet> getQuartets(int taxonCount) {
		
		ArrayList<Quartet> quartets = new ArrayList<>();
		
		for (int[] i : getQuadruples(taxonCount)) {
			quartets.add(getQuartetOnQuadruple(i));
		}
		
		return quartets;
	}
	
	/**
	 * Enumerates all possible quadruples over the given amount of taxa.
	 * 
	 * @param taxonCount The amount of taxa.
	 * @return An {@link ArrayList} containing the quadruples. Every quadruple
	 * is represented by an <code>int</code> array, containing the four elements
	 * of the quadruple.
	 */
	private static ArrayList<int[]> getQuadruples(int taxonCount) {
		
		ArrayList<int[]> quadruples = new ArrayList<>();
		
		for (int i1 = 0; i1 < taxonCount - 3; i1++) {
			for (int i2 = i1 + 1; i2 < taxonCount - 2; i2++) {
				for (int i3 = i2 + 1; i3 < taxonCount - 1; i3++) {
					for (int i4 = i3 + 1; i4 < taxonCount; i4++) {
						quadruples.add(new int[]{i1, i2, i3, i4});
					}
				}
			}
		}
		
		return quadruples;
	}
	
	/**
	 * Given a quadruple, returns the quartet over these four taxa that this
	 * tree is compatible with.
	 * 
	 * <p>Since this is a tree, there will indeed only be one quartet on any
	 * quadruple of taxa.</p>
	 * 
	 * @param quadruple The quadruple to find the quartet on.
	 * @return The resulting quartet.
	 */
	private Quartet getQuartetOnQuadruple(int[] quadruple) {
		assert quadruple.length == 4;
		
		Tree restriction = getRestriction(quadruple);

		assert !restriction.isSingleton;
		
		int left1, left2, right1, right2;
		
		// there are two possibilities: either the special edge is the split edge,
		// or it is an edge to a singleton
		
		if (restriction.subTree1.isSingleton) { // an edge to a singleton
			left1 = restriction.subTree1.taxon;
			
			if (restriction.subTree2.subTree1.isSingleton) {
				left2 = restriction.subTree2.subTree1.taxon;
				right1 = restriction.subTree2.subTree2.subTree1.taxon;
				right2 = restriction.subTree2.subTree2.subTree2.taxon;
			} else {
				left2 = restriction.subTree2.subTree2.taxon;
				right1 = restriction.subTree2.subTree1.subTree1.taxon;
				right2 = restriction.subTree2.subTree1.subTree2.taxon;
			}
			
		} else if (restriction.subTree2.isSingleton) { // also an edge to a singleton
			left1 = restriction.subTree2.taxon;
			
			if (restriction.subTree1.subTree1.isSingleton) {
				left2 = restriction.subTree1.subTree1.taxon;
				right1 = restriction.subTree1.subTree2.subTree1.taxon;
				right2 = restriction.subTree1.subTree2.subTree2.taxon;
			} else {
				left2 = restriction.subTree1.subTree2.taxon;
				right1 = restriction.subTree1.subTree1.subTree1.taxon;
				right2 = restriction.subTree1.subTree1.subTree2.taxon;
			}
			
		} else { // the split-edge
			left1 = restriction.subTree1.subTree1.taxon;
			left2 = restriction.subTree1.subTree2.taxon;
			right1 = restriction.subTree2.subTree1.taxon;
			right2 = restriction.subTree2.subTree2.taxon;
		}
		
		Quartet q = new Quartet(left1, left2, right1, right2);
		
		return q;
	}
	
	// gets the restriction of this tree over the given 4 taxa
	/**
	 * Restricts the tree to only include the four taxa in the quadruple given.
	 * The result is returned as a separate tree, and the original tree is not
	 * modified.
	 * 
	 * @param quadruple The quadruple to restrict the tree on.
	 * @return The resulting {@link Tree}.
	 */
	public Tree getRestriction(int[] quadruple) {
		assert quadruple.length == 4;
		
		if (isSingleton) {
			if (inQuadruple(quadruple, taxon)) {
				return new Tree(taxon);
			}
			
			return null;
		}
		
		Tree restriction1 = subTree1.getRestriction(quadruple);
		Tree restriction2 = subTree2.getRestriction(quadruple);
		
		if (restriction1 == null && restriction2 == null) {
			return null;
		} else if (restriction1 == null) {
			return restriction2;
		} else if (restriction2 == null) {
			return restriction1;
		} else {
			return new Tree(restriction1, restriction2);
		}
	}
	
	/**
	 * Checks whether a given taxon is in a quadruple.
	 * 
	 * @param quadruple The quadruple to look in.
	 * @param taxon The taxon to search for.
	 * @return <code>true</code> if <code>taxon</code> is in <code>quadruple</code>,
	 * <code>false</code> otherwise.
	 */
	private static boolean inQuadruple(int[] quadruple, int taxon) {
		return taxon == quadruple[0] || taxon == quadruple[1] ||
				taxon == quadruple[2] || taxon == quadruple[3];
	}
}
