package bep.fylogenetica.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.IconHandler;
import bep.fylogenetica.gui.MainMenuBar;

/**
 * This action shows some information about the application.
 */
public class AboutAction extends AbstractAction {
	
	Fylogenetica f;
	
	public AboutAction(Fylogenetica f) {
		super("About");
		this.f = f;
		
        putValue(SHORT_DESCRIPTION, "Shows information about this application.");
        putValue(SMALL_ICON, IconHandler.getIcon("help-about-16"));
        putValue(LARGE_ICON_KEY, IconHandler.getIcon("help-about-22"));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(f.gui, "Fylogenetica 1.0\n" +
				"(c) 2012-2013 Willem Sonke\n" +
				"Distributed under the GNU General Public License 3.0\n\n" +
				"This application implements an algorithm by Keijsper and Pendavingh to construct networks from quartet data.\n" +
				"See:\n" +
				"\t \u2022 Judith Keijsper and Rudi Pendavingh. Reconstructing a phylogenetic level-1 network from quartets. 2013.\n" +
				"\t \u2022 Willem Sonke. Reconstructing a level-1-network from quartets. 2013.\n\n" +
				"This application uses icons from the Oxygen Project, under the GNU Lesser General Public License.", "About",
				JOptionPane.INFORMATION_MESSAGE);
	}
}
