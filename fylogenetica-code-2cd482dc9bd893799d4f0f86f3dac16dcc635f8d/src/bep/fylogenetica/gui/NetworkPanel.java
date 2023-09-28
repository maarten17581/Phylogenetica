package bep.fylogenetica.gui;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.model.Network;
import bep.fylogenetica.model.Network.Vertex;
import bep.fylogenetica.model.Network.VertexType;

public class NetworkPanel extends JTextArea implements MouseListener, MouseMotionListener {
	
	Fylogenetica f;
	
	// the hovered vertex
	Vertex hovered;
	
	// the dragged vertex
	Vertex dragged;
	
	boolean isLayouting = false;
	
	// the thread that manages the layout
	Runnable layoutRunnable = new Runnable() {
		
		@Override
		public void run() {
			while (true) {
				synchronized (this) {
					while (!isLayouting) {
						try {
							wait();
						} catch (InterruptedException e) {
							// should not happen: we don't interrupt this thread
							e.printStackTrace();
						}
					}
				}
				double totalKineticEnergy = f.model.getNetwork().doLayoutStep(1000, dragged);
				if (totalKineticEnergy < 0.01f) {
					isLayouting = false;
				}
				repaint(); // note: repaint() is thread-safe, so this is allowed
			}
		}
	};
	
	public NetworkPanel(Fylogenetica f) {
		this.f = f;
		
		setOpaque(false);
		setEditable(false);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		
		new Thread(layoutRunnable, "Layout graph").start();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
        
		if (f.model.getNetwork() == null) {
	        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
	        
			g2.setFont(g2.getFont().deriveFont(48f));
			FontMetrics fm = g2.getFontMetrics();
	        g2.setColor(new Color(0, 0, 0, 64));
	        String text = "No network yet...";
        	g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, (getHeight() + fm.getAscent() / 2) / 2);
        	return;
        }
        
        drawGraph(g2, f.model.getNetwork(), hovered);
	}
	
	/**
	 * Draws the given graph to an image that is saved.
	 * 
	 * @param filename The file name of the image to write to.
	 * @param g The graph to draw.
	 */
	public static void toImage(String filename, Network g) {
		BufferedImage i = new BufferedImage(800, 600, BufferedImage.TYPE_4BYTE_ABGR);
		
		Graphics2D g2 = (Graphics2D) i.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, 800, 600);
		drawGraph(g2, g, null);
		
		File file = new File(filename);
		
		try {
			ImageIO.write(i, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Draws the given graph onto a graphics object.
	 * 
	 * @param g2 The graphics object to draw with. This may, for example, be a graphics object
	 * of a GUI container, or a graphics object from an image.
	 * @param g The graph to draw.
	 */
	private static void drawGraph(Graphics2D g2, Network g, Vertex hovered) {

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		synchronized (g) {
			
			// draw the "glow" around the hovered node
			if (hovered != null) {
	            g2.setColor(new Color(100, 140, 255));
				g2.draw(new Ellipse2D.Float(hovered.pos.x - 5, hovered.pos.y - 5, 10, 10)); 
			}
			
			// draw the labels and edges
            g2.setColor(Color.BLACK);
            
	        for (Vertex v : g.vertices) {
	        	
	        	if (!v.label.equals("")) {
	        		
	        		boolean isBold = false;
	        		
	        		int dx = 5;
	        		
	        		if (v.neighbours.size() > 0 && v.neighbours.get(0).pos.x > v.pos.x) {
	        			dx = -5 - g2.getFontMetrics().stringWidth(v.label);
	        		}
	        		
	        		g2.drawString(v.label, v.pos.x + dx, v.pos.y + 3);
	        	}
	        	
	        	for (Vertex v2 : v.neighbours) {
	        		if (v.pos.x >= v2.pos.x) {
	        			g2.draw(new Line2D.Float(v.pos.x, v.pos.y, v2.pos.x, v2.pos.y));
	        		}
	        	}
	        }
	        
	        // draw the vertices
	        for (Vertex v : g.vertices) {
	        	
	        	int size = v.type == VertexType.QUESTION_MARK_VERTEX ? 12 : 6;
	        	
	            g2.setColor(Color.BLACK);
	        	g2.fill(new Ellipse2D.Float(v.pos.x - size / 2, v.pos.y - size / 2, size, size));
	        	g2.draw(new Ellipse2D.Float(v.pos.x - size / 2, v.pos.y - size / 2, size, size));
	        	
	        	if (v.type == VertexType.QUESTION_MARK_VERTEX) {
	        		g2.setColor(Color.WHITE);
	        		g2.draw(new Line2D.Float(v.pos.x - 2.5f, v.pos.y - 2.5f, v.pos.x + 2.5f, v.pos.y + 2.5f));
	        		g2.draw(new Line2D.Float(v.pos.x + 2.5f, v.pos.y - 2.5f, v.pos.x - 2.5f, v.pos.y + 2.5f));
	        	}
	        }
		}

        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
	}
	
	/**
	 * Starts the layout procedure if it is not already running. The layout
	 * automatically stops when the algorithm is finished. This algorithm should
	 * be called when the network to be displayed is changed.
	 */
	public void triggerLayout() {
		isLayouting = true;
		
		synchronized (layoutRunnable) {
			layoutRunnable.notify();
		}
	}
	
	// methods for the mouse handlers
	
	private Vertex getVertexOnPoint(int x, int y) {
		
		if (f.model.getNetwork() == null) {
			return null;
		}
		
		for (Vertex v : f.model.getNetwork().vertices) {
			if (new Rectangle((int) v.pos.x - 8, (int) v.pos.y - 8, 16, 16).contains(x, y)) {
				return v;
			}
		}
		
		return null;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		hovered = getVertexOnPoint(e.getX(), e.getY());
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		
		if (dragged != null) {
			dragged.pos.x = e.getX();
			dragged.pos.y = e.getY();
		}
		
		triggerLayout();
		
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragged = getVertexOnPoint(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragged = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
