package bep.fylogenetica.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Random;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.model.Quartet;

/**
 * This action shuffles the quartets in the list.
 */
public class ShuffleQuartetsAction extends AbstractAction {
	
	Fylogenetica f;
	
	public ShuffleQuartetsAction(Fylogenetica f) {
		super("Shuffle quartets");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Shuffles the quartets in the quartet list.");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Collections.shuffle(f.model.quartets);
		f.gui.qp.updateList();
	}
}
