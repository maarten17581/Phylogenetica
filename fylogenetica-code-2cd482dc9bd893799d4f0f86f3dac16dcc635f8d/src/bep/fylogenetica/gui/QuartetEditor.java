package bep.fylogenetica.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Quartet;

public class QuartetEditor extends JDialog {
	
	Fylogenetica f;
	
	/**
	 * The quartet to edit.
	 */
	private Quartet q;

	private JPanel buttonPanel;

	public JButton button;
	
	public QuartetEditor(Fylogenetica f, Quartet q, boolean add) {
		super(f.gui, "Edit quartet", true);
		
		this.f = f;
		this.q = q;
		
		setLayout(new BorderLayout(5, 5));
		
		QuartetEditPanel editPanel = new QuartetEditPanel(f, q, this);
		add(editPanel, BorderLayout.CENTER);
		
		buttonPanel = new JPanel();

		button = new JButton("Save and close") {{
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(add) {
						f.model.quartets.add(q);
						f.gui.qp.updateList();
					}
					dispose();
				}
			});
		}};
		
		buttonPanel.add(button);
		button.setEnabled(false);
		
		add(buttonPanel, BorderLayout.PAGE_END);
		
		pack();
		setResizable(false);
		setVisible(true);
	}
}
