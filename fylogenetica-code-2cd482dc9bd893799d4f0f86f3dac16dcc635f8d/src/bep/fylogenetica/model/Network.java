package bep.fylogenetica.model;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.gui.NetworkPanel;

/**
 * A representation of an undirected network (graph).
 * 
 * <p>This network is represented by a set of vertices, that each contain their neighbours.</p>
 */
public class Network {

	public ArrayList<Vertex> vertices;
	
	/**
	 * Creates a new network without vertices or edges.
	 */
	public Network() {
		this(0);
	}
	
	/**
	 * Creates a new network with a given number of vertices and no edges.
	 * @param i The vertex count.
	 */
	public Network(int vc) {
		vertices = new ArrayList<Vertex>();
		
		for (int i = 0; i < vc; i++) {
			addVertex(new Vertex("" + i));
		}
	}
	
	/**
	 * Adds a vertex to the network.
	 * @param v The vertex to add. This may not be <code>null</code>.
	 */
	public void addVertex(Vertex v) {
		assert v != null;
		vertices.add(v);
	}
	
	/**
	 * Removes a vertex from the network. All edges to this vertex are also removed.
	 * @param v The vertex to remove. This may not be <code>null</code>.
	 */
	public void removeVertex(Vertex v) {
		assert v != null;
		vertices.remove(v);
		for (Vertex v2 : v.neighbours) {
			v2.neighbours.remove(v);
		}
	}
	
	/**
	 * Adds an edge between two vertices.
	 * 
	 * @param v1 The first vertex.
	 * @param v2 The second vertex.
	 */
	public void addEdge(Vertex v1, Vertex v2) {
		v1.neighbours.add(v2);
		v2.neighbours.add(v1);
	}
	
	public void addEdge(int i1, int i2) {
		addEdge(vertices.get(i1), vertices.get(i2));
	}
	
	/**
	 * Layouts the network with a force-based layout algorithm. When this method returns,
	 * all vertices will have an appropriate x and y coordinate.
	 * 
	 * @param width The width of the containment.
	 * @param height The height of the containment.
	 * @param eps A small value that indicates the threshold to stop the algorithm.
	 */
	public void layout(float width, float height, float eps) {
		layout(width, height, eps, null, true);
	}

	/**
	 * Layouts the network with a force-based layout algorithm. When this method returns,
	 * all vertices will have an appropriate x and y coordinate.
	 * 
	 * @param width The width of the containment.
	 * @param height The height of the containment.
	 * @param eps A small value that indicates the threshold to stop the algorithm.
	 * @param progressListener You can give a {@link ProgressListener} here to
	 * receive progress updates. This may be <code>null</code>, then you won't get updates.
	 * @param initialize Whether the vertices' position should be randomized before starting.
	 */
	public void layout(float width, float height, float eps, ProgressListener progressListener, boolean initialize) {
		
		// initialize
		if (initialize) {
			randomizePositions(width, height);
		}
		
		// iteration count
		int i = 0;
		
		double totalKineticEnergy;
		
		do {
			totalKineticEnergy = doLayoutStep(1, null);
			
			if (i % 1000 == 0 && progressListener != null) {
				// estimated progress
				int progress = (int) (100 - (Math.log(totalKineticEnergy) - Math.log(eps)) * 5);
				if (progress < 0) {
					progress = 0;
				}
				progressListener.newProgressValue(progress);
			}
			
			i++;
			
		} while (totalKineticEnergy > eps);
	}
	
	/**
	 * Assigns every vertex a random position (uniformly distributed), inside
	 * the given bounds. This is meant to be used as an initial position for the
	 * layout algorithm.
	 * 
	 * @param width The maximum for the <i>x</i> value.
	 * @param height The maximum for the <i>y</i> value.
	 */
	public void randomizePositions(float width, float height) {
		for (Vertex v : vertices) {
			v.pos.x = (float) (Math.random() * width);
			v.pos.y = (float) (Math.random() * height);
			v.vel.x = 0;
			v.vel.y = 0;
		}
	}
	
	/**
	 * Executes a number of steps in the layout algorithm.
	 * 
	 * @param count The number of steps to execute.
	 * @param fixed If non-<code>null</code>, the given vertex is kept fixed. That
	 * is, the vertex is not being moved. This is useful, for example, while dragging
	 * a vertex; it is unwanted that this vertex is moving in the meantime.
	 * @return The "total kinetic energy", that indicates how fast the vertices
	 * in the graph are still moving.
	 */
	public synchronized double doLayoutStep(int count, Vertex fixed) {
		
		// damping constant
		final float damping = 0.998f;
		
		// time step
		final float dt = 0.004f;
		
		// note: this loop is unrolled one time, since we only need to maintain
		// the totalKineticEnergy in the last iteration
		for (int i = 0; i < count - 1; i++) {
			
			for (Vertex v : vertices) {
				if (v == fixed) {
					continue;
				}
				
				XYPair force = new XYPair(0, 0);
				
				for (Vertex v2 : vertices) {
					if (v != v2) {
						XYPair coulomb = coulombRepulsion(v, v2);
						force.x += coulomb.x;
						force.y += coulomb.y;
					}
				}
				
				for (Vertex v2 : v.neighbours) {
					XYPair hooke = hookeAttraction(v, v2);
					force.x += hooke.x;
					force.y += hooke.y;
				}
				
				v.vel.x = (v.vel.x + dt * force.x) * damping;
				v.vel.y = (v.vel.y + dt * force.y) * damping;
				
				v.pos.x += dt * v.vel.x;
				v.pos.y += dt * v.vel.y;
			}
		}
		
		double totalKineticEnergy = 0;
		
		for (Vertex v : vertices) {
			if (v == fixed) {
				continue;
			}
			
			XYPair force = new XYPair(0, 0);
			
			for (Vertex v2 : vertices) {
				if (v != v2) {
					XYPair coulomb = coulombRepulsion(v, v2);
					force.x += coulomb.x;
					force.y += coulomb.y;
				}
			}
			
			for (Vertex v2 : v.neighbours) {
				XYPair hooke = hookeAttraction(v, v2);
				force.x += hooke.x;
				force.y += hooke.y;
			}
			
			v.vel.x = (v.vel.x + dt * force.x) * damping;
			v.vel.y = (v.vel.y + dt * force.y) * damping;
			
			v.pos.x += dt * v.vel.x;
			v.pos.y += dt * v.vel.y;
			
			totalKineticEnergy += (v.vel.x * v.vel.x) + (v.vel.y * v.vel.y);
		}
		
		return totalKineticEnergy;
	}
	
	/**
	 * Returns the force that the first vertex experiences from the second vertex.
	 * 
	 * @param v The first vertex.
	 * @param v2 The second vertex.
	 * @return The force, as an {@link XYPair}.
	 */
	private final XYPair coulombRepulsion(Vertex v, Vertex v2) {
		
		// the "ideal" distance between vertices
		float k = 10f;
		
		float d = XYPair.distance(v.pos, v2.pos);
		assert d > 0;
		
		float f = -(k * k) / d;
		assert f < 0;
		
		float xForce = (f / d) * (v2.pos.x - v.pos.x);
		float yForce = (f / d) * (v2.pos.y - v.pos.y);
		
		return new XYPair(xForce, yForce);
	}
	
	/**
	 * Returns the force that the first vertex experiences from the second vertex.
	 * 
	 * @param v The first vertex.
	 * @param v2 The second vertex.
	 * @return The force, as an {@link XYPair}.
	 */
	private final XYPair hookeAttraction(Vertex v, Vertex v2) {
		
		// the "ideal" distance between vertices
		float k = 10f;
		
		float d = XYPair.distance(v.pos, v2.pos);
		assert d >= 0;
		
		float f = (d * d) / k;
		assert f >= 0;
		
		float xForce = (f / d) * (v2.pos.x - v.pos.x);
		float yForce = (f / d) * (v2.pos.y - v.pos.y);
		
		return new XYPair(xForce, yForce);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Network:\n");
		
		sb.append("  vertices:\n");
		
		for (Vertex v : vertices) {
			sb.append("    " + v.hashCode() + ": " + v.pos.x + ", " + v.pos.y + ", " + v.label + "\n");
		}
		
		return sb.toString();
	}

	/**
	 * A vertex in the network. This contains its label and a list
	 * of neighbours.
	 */
	public static class Vertex {
		
		/**
		 * The node's type.
		 */
		public VertexType type;
		
		/**
		 * The node's label.
		 */
		public String label;
		
		/**
		 * The neighbours of the node.
		 */
		public ArrayList<Vertex> neighbours;
		
		/**
		 * The x and y coordinates of the node, to use when the network is laid out.
		 */
		public XYPair pos = new XYPair(0, 0);
		
		/**
		 * The x and y components of the node's velocity, to use when the network is laid out.
		 */
		public XYPair vel = new XYPair(0, 0);
		
		/**
		 * Creates a new (normal) vertex with the given label.
		 * @param label The label.
		 */
		public Vertex(String label) {
			this.type = VertexType.NORMAL_VERTEX;
			this.label = label;
			neighbours = new ArrayList<>();
		}
	}

	/**
	 * Types of vertices.
	 */
	public enum VertexType {
		/**
		 * A normal vertex.
		 */
		NORMAL_VERTEX,
		
		/**
		 * A vertex that should be denoted with a question mark, since it is
		 * not known what happens there.
		 */
		QUESTION_MARK_VERTEX;
	}
	
	/**
	 * A pair of an x and an y coordinate. This can be used for positions, velocities,
	 * forces and the like.
	 */
	public static class XYPair {
		
		/**
		 * The x coordinate.
		 */
		public float x;
		
		/**
		 * The y coordinate.
		 */
		public float y;
		
		/**
		 * Creates a new {@link XYPair}.
		 * 
		 * @param x The x coordinate.
		 * @param y The y coordinate.
		 */
		public XYPair(float x, float y) {
			this.x = x;
			this.y = y;
		}
		
		/**
		 * Calculates the (Euclidean) distance between two {@link XYPair}s,
		 * that are assumed to represent positions in the plane.
		 * 
		 * @param p1 The first position.
		 * @param p2 The second position.
		 * @return The distance between the positions.
		 */
		public static float distance(XYPair p1, XYPair p2) {
			float dx = p1.x - p2.x;
			float dy = p1.y - p2.y;
			return (float) Math.sqrt((dx * dx) + (dy * dy));
		}
	}
}
