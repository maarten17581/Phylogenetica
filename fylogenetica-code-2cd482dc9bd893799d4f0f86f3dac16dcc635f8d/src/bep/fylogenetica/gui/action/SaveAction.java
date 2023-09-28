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
 * This action allows the user to pick a name and save the quartets to a file.
 */
public class SaveAction extends AbstractAction {
	
	Fylogenetica f;
	
	public SaveAction(Fylogenetica f) {
		super("Save");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Saves the quartets and the taxon count to a file.");
        putValue(SMALL_ICON, IconHandler.getIcon("document-save-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("document-save-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		int value = fc.showSaveDialog(f.gui);
		
		if (value != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = fc.getSelectedFile();
		if (!file.getName().endsWith(".fyl")) {
			file = new File(file.getPath() + ".fyl");
		}
		
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
		
		f.gui.setSaveName(file.getName());
	}
}
