package bep.fylogenetica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Quartet;

public class QuartetPanel extends JPanel {

	Fylogenetica f;
	
	/**
	 * The list model.
	 */
	private DefaultListModel<Quartet> listModel;
	
	/**
	 * The actual list.
	 */
	private JList<Quartet> list;
	
	public QuartetPanel(Fylogenetica f) {
		super(new BorderLayout(5, 5));
		
		this.f = f;
		
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		add(new JScrollPane(list));
		
		updateList();
		
		setPreferredSize(new Dimension(200, 200));
	}
	
	/**
	 * Updates the quartet list.
	 */
	public void updateList() {
		if (f.model.quartets.isEmpty()) {
			setBorder(BorderFactory.createTitledBorder("Quartets"));
		} else {
			setBorder(BorderFactory.createTitledBorder("Quartets (count: " + f.model.quartets.size() + ")"));
		}
		listModel.clear();
		for (Quartet q : f.model.quartets) {
			listModel.addElement(q);
		}
	}
	
	/**
	 * Yields the currently selected quartet, or <code>null</code>.
	 */
	public Quartet getSelectedQuartet() {
		return list.getSelectedValue();
	}
}
