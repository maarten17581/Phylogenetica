package bep.fylogenetica.io.ipe;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import bep.fylogenetica.io.ipe.IpeDocument.TextHorizontalAlignment;
import bep.fylogenetica.io.ipe.IpeDocument.TextVerticalAlignment;
import bep.fylogenetica.model.Network;
import bep.fylogenetica.model.Network.Vertex;

/**
 * A graph (that is, the object with vertices and edges, not a {@link IpeLineGraph}).
 */
public class IpeGraph implements IpeObject {
	
	// View parameters
	
	public double xMin, xMax;
	public double yMin, yMax;
	
	/**
	 * The graph to draw.
	 */
	private Network graph;
	
	/**
	 * Produces a new Ipe graph.
	 * 
	 * @param graph The graph to draw.
	 */
	public IpeGraph(Network graph) {
		this.graph = graph;
		
		adjustViewParameters();
	}
	
	/**
	 * Adjusts the view parameters to the current data.
	 */
	protected void adjustViewParameters() {
		xMin = Integer.MAX_VALUE;
		xMax = Integer.MIN_VALUE;
		yMin = Integer.MAX_VALUE;
		yMax = Integer.MIN_VALUE;
		
		for (Vertex v : graph.vertices) {
			if (v.pos.x < xMin) {
				xMin = v.pos.x;
			}
			if (v.pos.x > xMax) {
				xMax = v.pos.x;
			}
			if (v.pos.y < yMin) {
				yMin = v.pos.y;
			}
			if (v.pos.y > yMax) {
				yMax = v.pos.y;
			}
		}
	}

	@Override
	public void draw(Point2D p, Point2D size, IpeDocument ipe) {
		
		for (Vertex v : graph.vertices) {
        	if (!v.label.equals("")) {
        		if (v.neighbours.size() > 0 && v.neighbours.get(0).pos.x > v.pos.x) {
        			ipe.drawText(toIpeCoordinates(p, size, new Point2D(v.pos.x - 4, v.pos.y)), v.label, Color.BLACK, TextHorizontalAlignment.RIGHT, TextVerticalAlignment.CENTER);
        		} else {
        			ipe.drawText(toIpeCoordinates(p, size, new Point2D(v.pos.x + 4, v.pos.y)), v.label, Color.BLACK, TextHorizontalAlignment.LEFT, TextVerticalAlignment.CENTER);
        		}
        	}
        	
        	for (Vertex v2 : v.neighbours) {
        		if (v.pos.x >= v2.pos.x) {
        			ipe.drawLine(toIpeCoordinates(p, size, new Point2D(v.pos.x, v.pos.y)), toIpeCoordinates(p, size, new Point2D(v2.pos.x, v2.pos.y)), Color.BLACK);
        		}
        	}
        	
			//ipe.drawSymbol(toIpeCoordinates(p, size, new Point2D(v.pos.x, v.pos.y)));
		}
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
