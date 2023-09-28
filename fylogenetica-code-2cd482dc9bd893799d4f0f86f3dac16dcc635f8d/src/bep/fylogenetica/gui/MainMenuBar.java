package bep.fylogenetica.gui;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.action.Actions;

/**
 * The main menu bar of the application.
 * 
 * <p>The menu bar uses the {@link Action} objects from the {@link Actions} class.</p>
 */
public class MainMenuBar extends JMenuBar {
	
	/**
	 * The {@link Fylogenetica} object.
	 */
	Fylogenetica f;
	
	/**
	 * Creates the menu bar.
	 * 
	 * @param f The {@link Fylogenetica} application.
	 * @param actions The {@link Actions} element to use for this menu bar. Note: this
	 * is actually a hack, since this would just be <code>f.gui.actions</code>. However
	 * since this constructor is called during the construction of <code>f.gui</code>,
	 * this reference is still <code>null</code>. Therefore the <code>actions</code> is
	 * given seperately.
	 */
	public MainMenuBar(Fylogenetica f, Actions actions) {
		
		this.f = f;
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(actions.open));
		fileMenu.add(new JMenuItem(actions.save));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(actions.exportGraphAsIpe));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(actions.quit));
		add(fileMenu);
		
		JMenu quartetMenu = new JMenu("Quartets");
		quartetMenu.add(new JMenuItem(actions.addQuartet));
		quartetMenu.add(new JMenuItem(actions.editQuartet));
		quartetMenu.add(new JMenuItem(actions.removeQuartet));
		quartetMenu.addSeparator();
		quartetMenu.add(new JMenuItem(actions.addRandomQuartets));
		quartetMenu.add(new JMenuItem(actions.removeRandomQuartets));
		quartetMenu.add(new JMenuItem(actions.shuffleQuartets));
		add(quartetMenu);
		
		JMenu algoMenu = new JMenu("Algorithms");
		algoMenu.add(new JMenuItem(actions.reconstructNetwork));
		algoMenu.add(new JMenuItem(actions.reconstructTree));
		algoMenu.addSeparator();
		JMenu separateStepsMenu = new JMenu("Separate steps");
		separateStepsMenu.add(new JMenuItem(actions.algoStep1));
		separateStepsMenu.add(new JMenuItem(actions.algoStep2));
		separateStepsMenu.add(new JMenuItem(actions.algoStep3));
		separateStepsMenu.add(new JMenuItem(actions.algoStep4));
		separateStepsMenu.add(new JMenuItem(actions.algoStep5));
		algoMenu.add(separateStepsMenu);
		add(algoMenu);
		
		JMenu networkMenu = new JMenu("Network");
		networkMenu.add(new JMenuItem(actions.randomTree));
		networkMenu.add(new JMenuItem(actions.layoutNetwork));
		add(networkMenu);
		
		JMenu settingsMenu = new JMenu("Settings");
		settingsMenu.add(new JMenuItem(actions.editTaxonCount));
		add(settingsMenu);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new JMenuItem(actions.about));
		add(helpMenu);
	}
}
