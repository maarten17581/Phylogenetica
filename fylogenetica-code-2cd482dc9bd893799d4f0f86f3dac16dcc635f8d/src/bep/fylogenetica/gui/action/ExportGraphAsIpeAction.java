package bep.fylogenetica.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.io.ipe.IpeDocument;
import bep.fylogenetica.io.ipe.IpeGraph;
import bep.fylogenetica.io.ipe.Point2D;

/**
 * This action allows the user to pick a name and export the visible graph to an Ipe file.
 */
public class ExportGraphAsIpeAction extends AbstractAction {
	
	Fylogenetica f;
	
	public ExportGraphAsIpeAction(Fylogenetica f) {
		super("Export graph to Ipe...");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Exports the graph to an Ipe vector file");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);

		JPanel accessory = new JPanel();
		
		JTextField widthField = new JTextField("256");
		accessory.add(widthField);
		JTextField heightField = new JTextField("192");
		accessory.add(heightField);
		
		fc.setAccessory(accessory);
		
		int value = fc.showSaveDialog(f.gui);
		
		if (value != JFileChooser.APPROVE_OPTION) {
			return;
		}
		
		File file = fc.getSelectedFile();
		if (!file.getName().endsWith(".ipe")) {
			file = new File(file.getPath() + ".ipe");
		}
		
		int width = Integer.valueOf(widthField.getText());
		int height = Integer.valueOf(heightField.getText());
		
		IpeDocument ipe = new IpeDocument();
		ipe.addToPreamble("\\usepackage{mathpazo}");
		ipe.addToPreamble("\\usepackage[euler-digits]{eulervm}");
		ipe.drawObject(new Point2D(64, 64), new Point2D(width, height), new IpeGraph(f.model.getNetwork()));

		try {
			ipe.writeToFile(fc.getSelectedFile());
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
