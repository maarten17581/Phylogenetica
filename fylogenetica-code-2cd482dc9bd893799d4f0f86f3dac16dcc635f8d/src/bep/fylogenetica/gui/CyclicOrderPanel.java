package bep.fylogenetica.gui;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.JPanel;

import bep.fylogenetica.algorithm.CyclicOrder;

/**
 * A JPanel that shows an image of a cyclic order.
 * 
 * <p>Note: this is not being used at the moment in the interface, but it could
 * be useful for debugging, to show a {@link CyclicOrder} object graphically.</p>
 */
public class CyclicOrderPanel extends JPanel {
	
	/**
	 * The cyclic order to draw.
	 */
	private CyclicOrder c;
	
	/**
	 * Creates a new {@link CyclicOrderPanel} for the given order.
	 * @param c The cyclic order to paint.
	 */
	public CyclicOrderPanel(CyclicOrder c) {
		this.c = c;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform formerTransform = g2.getTransform();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        g2.setFont(getFont());
        
		int width = getWidth();
		int height = getHeight();
		
		int size = Math.min(width, height);
		
		if (width >= height) {
			g2.translate((width - height) / 2.0, 0);
		} else {
			g2.translate(0, (height - width) / 2.0);
		}
		
		// draw the circle
		g2.draw(new Ellipse2D.Double(0.15 * size, 0.15 * size, 0.7 * size, 0.7 * size));
		
		// draw the elements
		for (int i = 0; i < c.taxa.size(); i++) {
		
			int t = c.taxa.get(i);
			
			double rotation = i * 2 * Math.PI / c.taxa.size();
			
			// draw the line, and two dots
			Point2D p1 = new Point2D.Double(0.5 * size + 0.35 * size * Math.cos(rotation), 0.5 * size + 0.35 * size * Math.sin(rotation));
			Point2D p2 = new Point2D.Double(0.5 * size + (0.35 * size + 15) * Math.cos(rotation), 0.5 * size + (0.35 * size + 15) * Math.sin(rotation));
			g2.draw(new Line2D.Double(p1, p2));
			g2.fill(new Ellipse2D.Double(p1.getX() - 3, p1.getY() - 3, 6, 6));
			g2.fill(new Ellipse2D.Double(p2.getX() - 3, p2.getY() - 3, 6, 6));
			
			// draw the taxon's name
			Point2D p3 = new Point2D.Double(0.5 * size + (0.35 * size + 25) * Math.cos(rotation), 0.5 * size + (0.35 * size + 25) * Math.sin(rotation));
			
			String name = String.valueOf(t);
			FontMetrics fm = g2.getFontMetrics();
			int xCorr = fm.stringWidth(name) / 2;
			int yCorr = (fm.getAscent() - fm.getDescent()) / 2;
			g2.drawString(name, (int) p3.getX() - xCorr, (int) p3.getY() + yCorr);
		}
		
		g2.setTransform(formerTransform);
	}
	
	@Override
	public Dimension getPreferredSize() {
		int size = Math.max(8 * c.taxa.size(), 200);
		return new Dimension(size, size);
	}
}
