package bep.fylogenetica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.model.Inference;

public class InferencePanel extends JPanel {

	Fylogenetica f;
	
	/**
	 * The list model.
	 */
	private DefaultListModel<Inference> listModel;
	
	/**
	 * The actual list.
	 */
	private JList<Inference> list;
	
	public InferencePanel(Fylogenetica f) {
		super(new BorderLayout(5, 5));
		
		this.f = f;
		
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		add(new JScrollPane(list));
		
		updateList();
		
		setPreferredSize(new Dimension(200, 200));
	}
	
	/**
	 * Updates the inference list.
	 */
	public void updateList() {
		
		if (f.model.inferences.isEmpty()) {
			setBorder(BorderFactory.createTitledBorder("Inference rules"));
		} else {
			setBorder(BorderFactory.createTitledBorder("Inference rules (count: " + f.model.inferences.size() + ")"));
		}
		
		listModel.clear();
		
		for (Inference inf : f.model.inferences) {
			listModel.addElement(inf);
		}
	}
	
	/**
	 * Yields the currently selected inference rule, or <code>null</code>.
	 */
	public Inference getSelectedInference() {
		return list.getSelectedValue();
	}
}
