package bep.fylogenetica.model;

import java.util.ArrayList;

import bep.fylogenetica.model.Network.Vertex;
import bep.fylogenetica.model.Network.VertexType;

/**
 * A recursive data structure for a level-1 network.
 * 
 * <p>A level-1-network can either be:
 * <ul>
 * <li>a singleton taxa;</li>
 * <li>an edge connecting two level-1-networks;</li>
 * <li>an ordered list of (at least 4) level-1-networks in a circle formation;</li>
 * <li>an unordered list of level-1-networks in a blob (of which we don't know more).</li>
 * </ul>
 * <p>
 */
public class Level1Network {
	
	/**
	 * Types of a level-1-network.
	 */
	public enum Level1NetworkType {
		
		/**
		 * A singleton. {@link Level1Network#taxon} indicates which taxon this is.
		 */
		SINGLETON,
		
		/**
		 * An edge with two level-1-networks connected to it. {@link Level1Network#subTree1}
		 * and {@link Level1Network#subTree2} indicate the two level-1-networks.
		 */
		EDGE,
		
		/**
		 * A circle with level-1-networks connected to it. {@link Level1Network#connectedNetworks}
		 * is an ordered list of connected networks.
		 */
		CIRCLE,
		
		/**
		 * A blob with level-1-networks connected to it. {@link Level1Network#connectedNetworks}
		 * is a list of connected networks.
		 */
		BLOB
	}
	
	/**
	 * The type of this network.
	 */
	public Level1NetworkType type;

	/**
	 * The singleton taxon (if <code>type == SINGLETON</code>).
	 */
	public int taxon;
	
	/**
	 * The first subtree (if <code>type == EDGE</code>).
	 */
	public Level1Network subNetwork1;
	
	/**
	 * The second subtree (if <code>type == EDGE</code>).
	 */
	public Level1Network subNetwork2;
	
	/**
	 * The connected networks (if <code>type == CIRCLE</code> or <code>type == BLOB</code>).
	 */
	public ArrayList<Level1Network> connectedNetworks;
	
	/**
	 * Creates a new singleton network.
	 * @param taxon The taxon this singleton network should consist of.
	 */
	public Level1Network(int taxon) {
		type = Level1NetworkType.SINGLETON;
		this.taxon = taxon;
	}
	
	/**
	 * Creates a new network that is an edge.
	 * 
	 * @param subNetwork1 The first subnetwork.
	 * @param subNetwork2 The second subnetwork.
	 */
	public Level1Network(Level1Network subNetwork1, Level1Network subNetwork2) {
		type = Level1NetworkType.EDGE;
		this.subNetwork1 = subNetwork1;
		this.subNetwork2 = subNetwork2;
	}
	
	/**
	 * Creates a new empty circle or blob.
	 * 
	 * @param isCircle If true, a circle is created, else a blob is created.
	 * @param networks The connected networks.
	 */
	public Level1Network(boolean isCircle, Level1Network... networks) {
		
		if (isCircle) {
			type = Level1NetworkType.CIRCLE;
		} else {
			type = Level1NetworkType.BLOB;
		}
		
		connectedNetworks = new ArrayList<>();
		
		for (Level1Network n : networks) {
			connectedNetworks.add(n);
		}
	}
	
	/**
	 * Replaces a certain subnetwork by another one.
	 * 
	 * @param original The network to replace.
	 * @param replacement The network that should be put in place of the old element.
	 * @throws IllegalStateException If <code>type == SINGLETON</code> or
	 * <code>type == EDGE</code>.
	 */
	public void replace(Level1Network original, Level1Network replacement) {
		if (type == Level1NetworkType.SINGLETON || type == Level1NetworkType.EDGE) {
			throw new IllegalStateException("Can't replace taxa of a singleton or edge");
		}
		
		connectedNetworks.set(connectedNetworks.indexOf(original), replacement);
	}
	
	/**
	 * Removes a certain subnetwork.
	 * 
	 * @param toRemove The network to remove.
	 * @throws IllegalStateException If <code>type == SINGLETON</code> or
	 * <code>type == EDGE</code>.
	 */
	public void remove(Level1Network toRemove) {
		if (type == Level1NetworkType.SINGLETON || type == Level1NetworkType.EDGE) {
			throw new IllegalStateException("Can't replace taxa of a singleton or edge");
		}
		
		connectedNetworks.remove(connectedNetworks.indexOf(toRemove));
	}

	/**
	 * Generates a random tree on the given amount of taxa. That is, a
	 * level-1-network is generated that is actually a tree (i.e. without
	 * circles or blobs).
	 * 
	 * @param taxonCount The number of taxa in the tree.
	 * @return The created tree.
	 */
	public static Level1Network generateRandomTree(int taxonCount) {
		
		ArrayList<Integer> taxa = new ArrayList<Integer>();
		
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
	public static Level1Network generateRandomTree(ArrayList<Integer> taxa) {
		
		if (taxa.size() == 1) {
			return new Level1Network(taxa.get(0));
		}
		
		ArrayList<Integer> subTaxa1;
		ArrayList<Integer> subTaxa2;
		
		do {
			subTaxa1 = new ArrayList<Integer>();
			subTaxa2 = new ArrayList<Integer>();
			
			for (Integer i : taxa) {
				if (Math.random() < 0.5) {
					subTaxa1.add(i);
				} else {
					subTaxa2.add(i);
				}
			}
		} while (subTaxa1.size() == 0 || subTaxa2.size() == 0); // dit is best lelijk
		
		return new Level1Network(generateRandomTree(subTaxa1), generateRandomTree(subTaxa2));
	}
	
/*	@Override
	public String toString() {
		if (type == Level1NetworkType.SINGLETON) {
			return "" + taxon;
		}

		if (type == Level1NetworkType.EDGE) {
			return "{" + subNetwork1 + ", " + subNetwork2 + "}";
		}
	}*/
	
	/**
	 * Creates a {@link Network} of this tree.
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
	 * @param g The network to add the vertices and edges to.
	 * @param parent The vertex to connect the network to. If this is <code>null</code>,
	 * the tree is assumed not to have a parent. This means that there are no edges connected
	 * to it if the tree is a singleton, or that only the two subtrees are connected to each
	 * other if the tree is a compound tree.
	 * @return The root vertex in the resulting network. This is the vertex a parent network
	 * has to be attached to.
	 */
	Vertex addToNetwork(Network g, Vertex parent) {
		
		Vertex root = null;
		
		switch (type) {
		
		case SINGLETON:
			// the singleton vertex
			root = new Vertex(String.valueOf(taxon));
			g.addVertex(root);
			if (parent != null) {
				g.addEdge(parent, root);
			}
			
			break;
			
		case EDGE:
			// the root
			root = new Vertex("");
			g.addVertex(root);
			Vertex v1 = subNetwork1.addToNetwork(g, root);
			Vertex v2 = subNetwork2.addToNetwork(g, root);
			
			// special for edges: we don't want a spurious node in the middle
			if (parent != null) {
				// the connecting edge to the parent
				g.addEdge(parent, root);
			} else {
				g.addEdge(v1, v2);
				g.removeVertex(root);
			}
			
			break;
		
		case BLOB:
			// the blob vertex
			root = new Vertex("");
			root.type = VertexType.QUESTION_MARK_VERTEX;
			g.addVertex(root);
			
			if (parent != null) {
				g.addEdge(parent, root);
			}
			
			for (Level1Network n : connectedNetworks) {
				n.addToNetwork(g, root);
			}
			
			break;
			
		case CIRCLE:
			// the root
			root = new Vertex("");
			g.addVertex(root);
			Vertex previous = root;
			Vertex first = null;
			
			for (Level1Network n : connectedNetworks) {
				Vertex networkRoot = new Vertex("");
				g.addVertex(networkRoot);
				if (first == null) {
					first = networkRoot;
				}
				n.addToNetwork(g, networkRoot);
				g.addEdge(previous, networkRoot);
				previous = networkRoot;
			}
			
			g.addEdge(previous, root);
			
			// special for edges: we don't want a spurious node in the middle
			if (parent != null) {
				// the connecting edge to the parent
				g.addEdge(parent, root);
			} else {
				g.addEdge(first, previous);
				g.removeVertex(root);
			}
			
			break;
		}
		
		return root;
	}
	
	/**
	 * Removes all circles in the network containing only three elements.
	 */
	public void removeTrivialCircles() {
		
		switch (type) {
		
		case SINGLETON:
			break;
			
		case EDGE:
			subNetwork1.removeTrivialCirclesChildren();
			subNetwork2.removeTrivialCirclesChildren();
			break;
			
		case BLOB:
			for (Level1Network n : connectedNetworks) {
				n.removeTrivialCirclesChildren();
			}
			break;
			
		case CIRCLE:
			for (Level1Network n : connectedNetworks) {
				n.removeTrivialCirclesChildren();
			}
			
			if (connectedNetworks.size() == 3) {
				type = Level1NetworkType.EDGE;
				subNetwork1 = connectedNetworks.get(0);
				subNetwork2 = new Level1Network(connectedNetworks.get(1), connectedNetworks.get(2));
			}
		}
	}
	
	/**
	 * A helper method for {@link #removeTrivialCircles()}. This method actually
	 * does the same, but assumes that the network has a parent. Thus, circles with
	 * three child elements are not removed (after all, the parent element is the
	 * fourth edge attached to the circle). 
	 */
	private void removeTrivialCirclesChildren() {
		
		switch (type) {
		
		case SINGLETON:
			break;
			
		case EDGE:
			subNetwork1.removeTrivialCirclesChildren();
			subNetwork2.removeTrivialCirclesChildren();
			break;
			
		case BLOB:
			for (Level1Network n : connectedNetworks) {
				n.removeTrivialCirclesChildren();
			}
			break;
			
		case CIRCLE:
			for (Level1Network n : connectedNetworks) {
				n.removeTrivialCirclesChildren();
			}
			
			if (connectedNetworks.size() == 2) {
				type = Level1NetworkType.EDGE;
				subNetwork1 = connectedNetworks.get(0);
				subNetwork2 = connectedNetworks.get(1);
			}
		}
	}
}
