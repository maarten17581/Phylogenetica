package bep.fylogenetica.gui.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.algorithm.ProgressListener;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.MainMenuBar;
import bep.fylogenetica.gui.ProgressUpdate;

/**
 * This action layouts the graph.
 */
public class LayoutNetworkAction extends AbstractAction {
	
	Fylogenetica f;
	
	public LayoutNetworkAction(Fylogenetica f) {
		super("Re-layout network");
		this.f = f;
		
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        putValue(SHORT_DESCRIPTION, "Automatically layouts the network to let it look nicer.");
        putValue(SMALL_ICON, IconHandler.getIcon("games-solve-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("games-solve-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (f.model.getNetwork() == null) {
			JOptionPane.showMessageDialog(f.gui, "<html><body style='width: 350px;'>" +
					"<big>There is no network yet</big><br>" +
					"<p>Create a network first by executing an algorithm from the Algorithms menu.</p>",
					"Layout failed", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		f.model.getNetwork().randomizePositions(f.gui.np.getWidth(), f.gui.np.getHeight());
		f.gui.np.triggerLayout();
	}
}
