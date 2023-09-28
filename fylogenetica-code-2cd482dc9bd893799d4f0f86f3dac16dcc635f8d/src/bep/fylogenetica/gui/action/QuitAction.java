package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;

/**
 * This action quits the application.
 */
public class QuitAction extends AbstractAction {
	
	Fylogenetica f;
	
	public QuitAction(Fylogenetica f) {
		super("Quit");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Quits the application.");
        putValue(SMALL_ICON, IconHandler.getIcon("application-exit-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("application-exit-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.exit(0);
	}
}
