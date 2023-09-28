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
 * This action gives the user a way to add a quartet.
 */
public class AddQuartetAction extends AbstractAction {
	
	Fylogenetica f;
	
	public AddQuartetAction(Fylogenetica f) {
		super("Add quartet...");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Adds a quartet to the quartet list.");
        putValue(SMALL_ICON, IconHandler.getIcon("list-add-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("list-add-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Quartet q = new Quartet(0, 0, 0, 0);
		
		new QuartetEditor(f, q, true);
	}
}
