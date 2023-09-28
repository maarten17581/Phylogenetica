package bep.fylogenetica.io.ipe;

/**
 * Simple class that represents a point in 2D space. It is used to represent input data,
 * such as for 2D graphs, and to represent Ipe coordinates.
 */
public class Point2D {
	
	public double x;
	public double y;
	
	/**
	 * Creates a new point.
	 * 
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 */
	public Point2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Adds another point to this point and returns the result.
	 * 
	 * @param p The point to add.
	 * @return The new point.
	 */
	public Point2D add(Point2D p) {
		Point2D result = new Point2D(this.x, this.y);
		result.x += p.x;
		result.y += p.y;
		return result;
	}
}
