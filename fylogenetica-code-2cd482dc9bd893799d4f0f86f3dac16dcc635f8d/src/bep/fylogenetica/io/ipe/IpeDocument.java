package bep.fylogenetica.io.ipe;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * This class represents an Ipe 7 document.
 * 
 * <p>There are a lot of commands to be able to draw shapes easily, hiding the Ipe internal
 * format for such drawings.</p>
 * 
 * <p>When done, you can write the file to the Ipe format using the {@link #writeToFile(File)}
 * method.</p>
 */
public class IpeDocument {
	
	/**
	 * Possible horizontal alignments for text.
	 */
	public enum TextHorizontalAlignment {
		LEFT("left"),
		CENTER("center"),
		RIGHT("right");
		
		private final String ipeString;
		
		private TextHorizontalAlignment(String ipeString) {
			this.ipeString = ipeString;
		}
	}
	
	/**
	 * Possible vertical alignments for text.
	 */
	public enum TextVerticalAlignment {
		TOP("top"),
		CENTER("center"),
		BOTTOM("bottom"),
		BASELINE("baseline");
		
		private final String ipeString;
		
		private TextVerticalAlignment(String ipeString) {
			this.ipeString = ipeString;
		}
	}

	/**
	 * The preamble text.
	 */
	private StringBuilder preambleText = new StringBuilder();
	
	/**
	 * The "page" environment containing the drawing commands.
	 */
	private StringBuilder pageText = new StringBuilder();
	
	/**
	 * Creates a new Ipe document with a default preamble.
	 */
	public IpeDocument() {
		addToPreamble("\\usepackage{mathpazo}");
		addToPreamble("\\usepackage[euler-digits]{eulervm}");
	}
	
	/**
	 * Adds the given text to the LaTeX preamble used to compile text in this drawing.
	 * Ipe automatically inserts the preamble before compiling.
	 * 
	 * <p>For example, you could use
	 * <pre>addToPreamble("\\usepackage{amsmath}");</pre>
	 * to be able to use AMS math commands in texts.</p>
	 * 
	 * @param text The text to add.
	 * @see #writeText(int, int, String)
	 */
	public void addToPreamble(String text) {
		preambleText.append(text + "\n");
	}
	
	/**
	 * Draws a line onto the drawing.
	 * 
	 * @param p1 The coordinate of the start point of the line. (in Ipe coordinates).
	 * @param p2 The coordinate of the end point of the line. (in Ipe coordinates).
	 * @param lineColor The color to use for the line.
	 */
	public void drawLine(Point2D p1, Point2D p2, Color lineColor) {
		pageText.append("<path " +
				"stroke=\"" + toIpeColor(lineColor) + "\">\n" +
				toIpePoint(p1) + " m\n" + 
				toIpePoint(p2) + " l\n" + 
				"</path>\n");
	}
	
	/**
	 * Draws a polyline or polygon onto the drawing.
	 * 
	 * @param lineColor The color to use for the line.
	 * @param closed Whether the element should be closed. If <code>true</code>, the
	 * drawing thus becomes a polygon, else it becomes a polyline.
	 * @param points The points of the polyline.
	 */
	public void drawPolygon(Color lineColor, boolean closed, Point2D... points) {
		pageText.append("<path stroke=\"" + toIpeColor(lineColor) + "\">\n");
		
		for (int i = 0; i < points.length; i++) {
			Point2D p = points[i];
			if (i == 0) {
				pageText.append(toIpePoint(p) + " m\n");
			} else {
				pageText.append(toIpePoint(p) + " l\n");
			}
		}
		
		if (closed) {
			pageText.append("h\n");
		}
		pageText.append("</path>\n");
	}
	
	/**
	 * Writes text to a given position in the drawing.
	 * 
	 * @param p The position of the text (in Ipe coordinates).
	 * @param text The text to draw. This can be everything Ipe accepts, such as LaTeX
	 * (for example <code>$y = x^2$</code>).
	 * @param textColor The color to use for the text.
	 */
	public void drawText(Point2D p, String text, Color textColor,
						TextHorizontalAlignment hAlign, TextVerticalAlignment vAlign) {
		pageText.append("<text pos=\"" + p.x + " " + p.y + "\" " +
				"type=\"label\" " +
				"stroke=\"" + toIpeColor(textColor) + "\" " +
				"halign=\"" + hAlign.ipeString + "\" " +
				"valign=\"" + vAlign.ipeString + "\">" +
				text +
				"</text>\n");
	}
	
	/**
	 * Draws an {@link IpeObject}.
	 * 
	 * @param p The coordinate of the lower-left corner of the object.
	 * @param size The size of the object.
	 * @param object The object to draw.
	 */
	public void drawObject(Point2D p, Point2D size, IpeObject object) {
		object.draw(p, size, this);
	}
	
	/**
	 * Converts the given {@link Color} to an Ipe color.
	 * 
	 * @param c The color to convert.
	 * @return The Ipe-formatted color.
	 */
	private String toIpeColor(Color c) {
		return c.getRed() / 255.0 + " " + c.getGreen() / 255.0 + " " + c.getBlue() / 255.0;
	}
	
	/**
	 * Converts the given {@link Point2D} to an Ipe point.
	 * 
	 * @param c The point to convert.
	 * @return The Ipe-formatted point.
	 */
	private String toIpePoint(Point2D p) {
		return p.x + " " + p.y;
	}

	/**
	 * Writes this Ipe drawing to a file.
	 * 
	 * @param f The file to write to.
	 * @throws FileNotFoundException If the file couldn't be written to.
	 */
	public void writeToFile(File f) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(f);
		
		out.write("<?xml version=\"1.0\"?>\n");
		out.write("<!DOCTYPE ipe SYSTEM \"ipe.dtd\">\n");
		out.write("<ipe version=\"70000\">\n");
		out.write("<preamble>\n");
		out.write(preambleText.toString());
		out.write("</preamble>\n");
        out.write("<page>\n");
		out.write(pageText.toString());
        out.write("</page>\n");
        out.write("</ipe>\n");
        
        out.close();
	}
}
