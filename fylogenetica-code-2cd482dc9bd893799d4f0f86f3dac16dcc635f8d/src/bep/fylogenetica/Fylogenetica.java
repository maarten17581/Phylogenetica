package bep.fylogenetica;

import javax.swing.SwingUtilities;

import bep.fylogenetica.analysis.AnalysisRunner;
import bep.fylogenetica.gui.MainWindow;
import bep.fylogenetica.model.MainModel;

/**
 * The main class for the Fylogenetica application.
 */
public class Fylogenetica {
	
	/**
	 * The main method for Fylogenetica.
	 * @param args The command line arguments. Use the <code>-h</code> switch to get help
	 * about the options.
	 */
	public static void main(String[] args) {
		
		if (args.length == 0) {
			new Fylogenetica(false);
			return;
		}
		
		if (args[0].equals("--gui")) {
			new Fylogenetica(true);
			return;
		}
		
		if (args[0].equals("--analysis")) {
			new Fylogenetica(false);
			return;
		}
		
		printHelp();
	}
	
	/**
	 * Prints a help message to the standard output.
	 */
	private static void printHelp() {
		System.out.println("Fylogenetica - an application that implements a new algorithm for\n" +
				"reconstructing fylogenetic trees and networks from quartets\n" +
				"\n" +
				"Usage: java -jar Fylogenetica.jar [option]\n" +
				"\n" +
				"Possible options:\n" +
				"    --gui         Use the interactive GUI (default)\n" +
				"    --analysis    Run the analysis scripts\n" +
				"    --help        Show this help message\n" +
				"\n" +
				"If no options are given, the interactive GUI is shown.");
	}

	/**
	 * The GUI for the application.
	 */
	public MainWindow gui;
	
	/**
	 * The model for the application.
	 */
	public MainModel model;
	
	/**
	 * The actual starting point for the application. Starts the application and creates
	 * the GUI and the data model.
	 * 
	 * @param useGUI Whether to use the interactive GUI (if <code>false</code>, use the
	 * analysis scripts).
	 */
	public Fylogenetica(boolean useGUI) {
		
		if (useGUI) {
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					model = new MainModel(Fylogenetica.this);
					
					gui = new MainWindow(Fylogenetica.this);
				}
			});
		} else {
			model = new MainModel(Fylogenetica.this);
			
			AnalysisRunner.doAnalysis(this);
		}
	}
}
