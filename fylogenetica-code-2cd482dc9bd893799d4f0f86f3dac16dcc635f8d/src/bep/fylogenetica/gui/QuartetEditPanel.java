package bep.fylogenetica.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Quartet;

public class QuartetEditPanel extends JPanel {
	
	/**
	 * The quartet to edit.
	 */
	private Quartet q;
	
	Fylogenetica f;
	
	private JSpinner left1box, left2box, right1box, right2box;
	private JLabel errorBar;
	
	public QuartetEditPanel(Fylogenetica f, Quartet q, QuartetEditor editor) {
		setLayout(null);
		
		this.f = f;
		this.q = q;
		
		SpinnerModel left1model = new SpinnerNumberModel(q.left1, 0, f.model.taxonCount - 1, 1);
		left1box = new JSpinner(left1model);
		
		SpinnerModel left2model = new SpinnerNumberModel(q.left2, 0, f.model.taxonCount - 1, 1);
		left2box = new JSpinner(left2model);
		
		SpinnerModel right1model = new SpinnerNumberModel(q.right1, 0, f.model.taxonCount - 1, 1);
		right1box = new JSpinner(right1model);
		
		SpinnerModel right2model = new SpinnerNumberModel(q.right2, 0, f.model.taxonCount - 1, 1);
		right2box = new JSpinner(right2model);
		
		ChangeListener changeListener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				QuartetEditPanel.this.q.left1 = (int) left1box.getValue();
				QuartetEditPanel.this.q.left2 = (int) left2box.getValue();
				QuartetEditPanel.this.q.right1 = (int) right1box.getValue();
				QuartetEditPanel.this.q.right2 = (int) right2box.getValue();
				errorBar.setVisible(!QuartetEditPanel.this.q.isValid());
				editor.button.setEnabled(QuartetEditPanel.this.q.isValid());
			}
		};
		
		left1box.addChangeListener(changeListener);
		left2box.addChangeListener(changeListener);
		right1box.addChangeListener(changeListener);
		right2box.addChangeListener(changeListener);
		
		add(left1box);
		add(left2box);
		add(right1box);
		add(right2box);
		
		errorBar = new JLabel("This quartet is not valid; all taxa must be different");
		errorBar.putClientProperty("joxy.isErrorBar", "error");
		errorBar.setVisible(!q.isValid());
		add(errorBar);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		g2.drawLine(210, 83, 330, 83);
		g2.drawLine(210, 83, 160, 33);
		g2.drawLine(210, 83, 160, 133);
		g2.drawLine(330, 83, 380, 33);
		g2.drawLine(330, 83, 380, 133);
		
		drawDot(g2, 210, 83);
		drawDot(g2, 160, 33);
		drawDot(g2, 160, 133);
		drawDot(g2, 330, 83);
		drawDot(g2, 380, 33);
		drawDot(g2, 380, 133);
		
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
	}
	
	private void drawDot(Graphics2D g2, int x, int y) {
		g2.fillOval(x - 3, y - 3, 6, 6);
	}

	@Override
	public void doLayout() {
		left1box.setBounds(80, 20, 60, 26);
		left2box.setBounds(80, 120, 60, 26);
		right1box.setBounds(400, 20, 60, 26);
		right2box.setBounds(400, 120, 60, 26);
		errorBar.setBounds(0, 155, 540, 25);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(540, 180);
	}
}
