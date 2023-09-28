package bep.fylogenetica.io.ipe;

import java.awt.Color;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;

import bep.fylogenetica.io.ipe.IpeDocument.TextHorizontalAlignment;
import bep.fylogenetica.io.ipe.IpeDocument.TextVerticalAlignment;

/**
 * A 2-dimensional line graph.
 */
public class IpeLineGraph implements IpeObject {
	
	// View parameters
	
	public double xMin, xMax, xStep;
	public double yMin, yMax, yStep;
	
	/**
	 * The data points.
	 */
	private ArrayList<Point2D> data;
	
	/**
	 * Produces a new line graph using the given data. The view parameters are automatically
	 * adjusted to the data.
	 * 
	 * @param data The data points, represented as {@link Point2D}s.
	 */
	public IpeLineGraph(ArrayList<Point2D> data) {
		this.data = data;
		
		adjustViewParameters();
	}
	
	/**
	 * Adjusts the view parameters to the current data.
	 */
	protected void adjustViewParameters() {
		xMin = 0;
		xMax = 55;
		xStep = 10;
		yMin = 0;
		yMax = 1;
		yStep = 1;
	}

	@Override
	public void draw(Point2D p, Point2D size, IpeDocument ipe) {
		drawAxes(p, size, ipe);
		
		Point2D[] points = new Point2D[data.size()];
		
		for (int i = 0; i < points.length; i++) {
			points[i] = toIpeCoordinates(p, size, data.get(i));
		}
		
		ipe.drawPolygon(Color.BLACK, false, points);
	}
	
	/**
	 * Draws the axes of this graph.
	 * 
	 * @param p The coordinate of the lower-left corner of the graph.
	 * @param size The size of the graph.
	 * @param ipe The document to draw on.
	 */
	protected void drawAxes(Point2D p, Point2D size, IpeDocument ipe) {
		ipe.drawLine(toIpeCoordinates(p, size, new Point2D(xMin, 0)),
				toIpeCoordinates(p, size, new Point2D(xMax, 0)),
				Color.BLACK);
		
		ipe.drawLine(toIpeCoordinates(p, size, new Point2D(0, yMin)),
				toIpeCoordinates(p, size, new Point2D(0, yMax)),
				Color.BLACK);
		
		// label on origin
		ipe.drawText(toIpeCoordinates(p, size, new Point2D(0, 0)).add(new Point2D(-2, -2)),
				"$0$", Color.BLACK, TextHorizontalAlignment.RIGHT, TextVerticalAlignment.TOP);
		
		// labels on positive horizontal axis
		for (double x = xStep; x <= xMax; x += xStep) {
			ipe.drawText(toIpeCoordinates(p, size, new Point2D(x, 0)).add(new Point2D(0, -2)),
					"$" + formatNumber(x) + "$", Color.BLACK,
					TextHorizontalAlignment.CENTER, TextVerticalAlignment.TOP);
		}
		
		// labels on negative horizontal axis
		for (double x = -xStep; x >= xMin; x -= xStep) {
			ipe.drawText(toIpeCoordinates(p, size, new Point2D(x, 0)).add(new Point2D(0, -2)),
					"$" + formatNumber(x) + "$", Color.BLACK,
					TextHorizontalAlignment.CENTER, TextVerticalAlignment.TOP);
		}
		
		// labels on positive vertical axis
		for (double y = yStep; y <= yMax; y += yStep) {
			ipe.drawText(toIpeCoordinates(p, size, new Point2D(0, y)).add(new Point2D(-2, 0)),
					"$" + formatNumber(y) + "$", Color.BLACK,
					TextHorizontalAlignment.RIGHT, TextVerticalAlignment.CENTER);
		}
		
		// labels on negative vertical axis
		for (double y = -yStep; y >= yMin; y -= yStep) {
			ipe.drawText(toIpeCoordinates(p, size, new Point2D(0, y)).add(new Point2D(-2, 0)),
					"$" + formatNumber(y) + "$", Color.BLACK,
					TextHorizontalAlignment.RIGHT, TextVerticalAlignment.CENTER);
		}
	}
	
	/**
	 * Formats a number nicely.
	 * 
	 * @param x The number to format.
	 * @return The resulting String.
	 */
	protected String formatNumber(double x) {
        DecimalFormat df = new DecimalFormat("#.#####");
        return df.format(x);
	}

	/**
	 * Converts the given graph coordinates to Ipe coordinates in the graph.
	 * 
	 * @param p The coordinate of the lower-left corner of the graph.
	 * @param size The size of the graph.
	 * @param graphCoordinates The graph coordinates to convert.
	 * @return The graph coordinates converted to Ipe coordinates.
	 */
	protected Point2D toIpeCoordinates(Point2D p, Point2D size, Point2D graphCoordinates) {
		return new Point2D(p.x + ((graphCoordinates.x - xMin) * size.x / (xMax - xMin)),
				p.y + ((graphCoordinates.y - yMin) * size.y / (yMax - yMin)));
	}
}
