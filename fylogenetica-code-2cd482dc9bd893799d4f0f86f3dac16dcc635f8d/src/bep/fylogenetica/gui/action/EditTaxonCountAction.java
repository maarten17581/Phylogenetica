package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import bep.fylogenetica.Fylogenetica;

/**
 * This action gives the user a way to edit the taxon count in use.
 */
public class EditTaxonCountAction extends AbstractAction {
	
	Fylogenetica f;
	
	public EditTaxonCountAction(Fylogenetica f) {
		super("Edit taxon count...");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Edit the amount of taxa in use.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		String countS = JOptionPane.showInputDialog(f.gui, "<html>What is the taxon count?" +
				"<p>Note: it does not work yet to decrease the taxon count if there are quartets with the removed taxa...</p>", f.model.taxonCount);
		
		if (countS == null) {
			return;
		}
		
		int count;
		
		try {
			count = Integer.valueOf(countS);
		} catch (Exception e2) {
			JOptionPane.showMessageDialog(f.gui, "This is not a valid integer...");
			return;
		}
		
		f.model.taxonCount = count;
	}
}
