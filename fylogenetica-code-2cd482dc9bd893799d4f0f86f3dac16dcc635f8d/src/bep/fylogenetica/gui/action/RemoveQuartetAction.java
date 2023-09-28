package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.QuartetEditor;
import bep.fylogenetica.model.Quartet;

/**
 * This action gives the user a way to remove a quartet.
 */
public class RemoveQuartetAction extends AbstractAction {
	
	Fylogenetica f;
	
	public RemoveQuartetAction(Fylogenetica f) {
		super("Remove quartet");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Removes the currently selected quartet.");
        putValue(SMALL_ICON, IconHandler.getIcon("list-remove-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("list-remove-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Quartet q = f.gui.qp.getSelectedQuartet();
		f.model.quartets.remove(q);
		f.gui.qp.updateList();
	}
}
