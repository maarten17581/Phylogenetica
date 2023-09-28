package bep.fylogenetica.gui;

import java.awt.BorderLayout;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;
import bep.fylogenetica.gui.action.Actions;

public class MainWindow extends JFrame {
	
	Fylogenetica f;
	
	public Actions actions;
	
	public QuartetPanel qp;
	public InferencePanel ip;
	public NetworkPanel np;
	public TaskPanel tp;
	
	static {
		// Options for the Mac LAF: use the global menu bar, and set the application's name
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Fylogenetica");
	}
	
	/**
	 * Creates a new {@link MainWindow}.
	 * @param f The {@link Fylogenetica} application this is the main GUI for.
	 */
	public MainWindow(Fylogenetica f) {
		super("[unnamed] - Fylogenetica");
		this.f = f;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		actions = new Actions(f);
		
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		setJMenuBar(new MainMenuBar(f, actions));
		
		setLayout(new BorderLayout(5, 5));
		
		add(new MainToolBar(f, actions), BorderLayout.PAGE_START);
		
		qp = new QuartetPanel(f);
		ip = new InferencePanel(f);
		np = new NetworkPanel(f);
		tp = new TaskPanel(f);
		JSplitPane split3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, qp, ip);
		JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split3, np);
		JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split2, tp);
		split1.setResizeWeight(1);
		add(split1, BorderLayout.CENTER);
		
		setSize(1000, 600);
		setVisible(true);
	}
	
	/**
	 * Sets the save name.
	 * 
	 * @param name The file name.
	 */
	public void setSaveName(String name) {
		setTitle(name + " - Fylogenetica");
	}
}
