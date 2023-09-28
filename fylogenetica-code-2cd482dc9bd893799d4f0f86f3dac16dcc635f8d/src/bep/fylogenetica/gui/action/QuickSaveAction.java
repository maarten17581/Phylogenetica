package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.io.FyloFile;

/**
 * This action allows to save the quartets to a file.
 * 
 * <p>This is for debugging.</p>
 */
public class QuickSaveAction extends AbstractAction {
	
	Fylogenetica f;
	
	public QuickSaveAction(Fylogenetica f) {
		super("Quick save");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Saves a Fylogenetica file to /home/willem/Fylogenetica-quick.fyl");
        putValue(SMALL_ICON, IconHandler.getIcon("document-save-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("document-save-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		File file = new File("/home/willem/Fylogenetica-quick.fyl");
		
		try {
			FyloFile.write(f, file);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
					"<big>Could not create file</big><br>" +
					"<p>The file '" + file.getPath() + "' could not be created, " +
					"or it existed already and could not be written to.</p>",
					"Save failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
}
