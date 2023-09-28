package bep.fylogenetica.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import bep.fylogenetica.Fylogenetica;

/**
 * The status bar that resides in the bottom of the main window.
 */
public class StatusBar extends JPanel {
	
	Fylogenetica f;
	
	JLabel taskLabel;
	JProgressBar progressBar;
	
	public StatusBar(Fylogenetica f) {
		super(new BorderLayout());
		this.f = f;
		
		taskLabel = new JLabel();
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		
		add(taskLabel, BorderLayout.PAGE_START);
		add(progressBar, BorderLayout.CENTER);
		
		setVisible(false);
	}
	
	/**
	 * Makes the bar visible. The progress bar is set to determinate mode, with
	 * progress 0, automatically.
	 * 
	 * @param task The task text to put next to the bar.
	 */
	public void showBar(String task) {
		setVisible(true);
		
		taskLabel.setText(task);
		progressBar.setIndeterminate(false);
		progressBar.setValue(0);
	}
	
	/**
	 * Sets the task text.
	 * 
	 * @param task The new task text to put next to the bar.
	 */
	public void setTask(String task) {
		taskLabel.setText(task);
	}
	
	/**
	 * Sets the progress. This doesn't change the text.
	 * 
	 * @param progress The progress (0-100).
	 */
	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}
	
	/**
	 * Switches the progress bar to determinate or indeterminate state.
	 * 
	 * @param determinate If <code>true</code>, the progress bar will be put in
	 * determinate state (i.e. known progress); if <code>false</code> it will be
	 * put in indeterminate state (i.e. bouncing box).
	 */
	public void setDeterminate(boolean determinate) {
		progressBar.setIndeterminate(!determinate);
	}
	
	/**
	 * Hides the status bar again.
	 */
	public void hideBar() {
		setVisible(false);
	}
}
