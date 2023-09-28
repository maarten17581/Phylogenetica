package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.io.FyloFile;

/**
 * This action allows the user to pick a name and save the quartets to a file.
 */
public class OpenAction extends AbstractAction {
	
	Fylogenetica f;
	
	public OpenAction(Fylogenetica f) {
		super("Open");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Opens the quartets and the taxon count from a file.");
        putValue(SMALL_ICON, IconHandler.getIcon("document-open-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("document-open-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setFileFilter(new FileNameExtensionFilter("Fylogenetica files", "fyl"));
		int value = fc.showOpenDialog(f.gui);
		
		if (value != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = fc.getSelectedFile();
		
		try {
			FyloFile.read(f, file);
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
					"<big>Could not find file</big><br>" +
					"<p>The file '" + file.getPath() + "' didn't exist, or it could not " +
					"be read.</p>",
					"Open failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		f.gui.setSaveName(file.getName());
		
		f.gui.qp.updateList();
	}
}
