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
 * This action gives the user a way to edit a quartet.
 */
public class EditQuartetAction extends AbstractAction {
	
	Fylogenetica f;
	
	public EditQuartetAction(Fylogenetica f) {
		super("Edit quartet...");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Edits the currently selected quartet.");
        putValue(SMALL_ICON, IconHandler.getIcon("edit-rename-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("edit-rename-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Quartet q = f.gui.qp.getSelectedQuartet();
		if (q != null) {
			new QuartetEditor(f, q, false);
		} else {
			JOptionPane.showMessageDialog(f.gui, "No quartet selected...");
		}
		f.gui.qp.updateList();
	}
}
