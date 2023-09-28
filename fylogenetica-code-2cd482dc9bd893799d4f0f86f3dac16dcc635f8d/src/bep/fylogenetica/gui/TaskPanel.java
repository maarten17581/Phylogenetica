package bep.fylogenetica.gui;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;

import javax.swing.*;

import bep.fylogenetica.Fylogenetica;

/**
 * A panel in which events are logged.
 */
public class TaskPanel extends JPanel {
	
	/**
	 * The {@link Fylogenetica} this panel belongs to.
	 */
	Fylogenetica f;
	
	/**
	 * The JList containing the events.
	 */
	JList<String> l;
	
	/**
	 * The list model for {@link #l}.
	 */
	DefaultListModel<String> lm;
	
	/**
	 * The status bar element.
	 */
	public StatusBar sb;
	
	boolean hasPrevious;
	long previousTime;
	
	/**
	 * Creates a new task panel.
	 * @param f The {@link Fylogenetica} this panel belongs to.
	 */
	public TaskPanel(Fylogenetica f) {
		super(new BorderLayout(5, 5));
		
		this.f = f;
		
		setBorder(BorderFactory.createTitledBorder("Task log"));
		
		sb = new StatusBar(f);
		add(sb, BorderLayout.PAGE_END);
		
		lm = new DefaultListModel<>();
		l = new JList<>(lm);
		l.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				
				JLabel result = (JLabel) super.getListCellRendererComponent(list, value,
						index, isSelected, cellHasFocus);
				
				String text = (String) value;
				
				if (text.startsWith("Ready")) {
					result.setForeground(new Color(0, 128, 0));
				} else if (text.startsWith("Exception") || text.startsWith("Aborted")) {
					result.setForeground(new Color(255, 0, 0));
				} else if (text.startsWith("Task")) {
					result.setFont(result.getFont().deriveFont(Font.BOLD));
				} else {
					result.setForeground(new Color(0, 0, 0));
				}
				
				return result;
			}
		});
		add(new JScrollPane(l), BorderLayout.CENTER);
		
		l.addComponentListener(new ComponentAdapter() {
		    @Override
			public void componentResized(ComponentEvent e) {
		        l.scrollRectToVisible(new Rectangle(0, l.getHeight(), 0, 0));
		    }
		});
		
		setPreferredSize(new Dimension(300, 200));
	}
	
	/**
	 * Clears the event table and resets the counter.
	 * @param task The name of the task.
	 */
	public void startTask(String task) {
		
		hasPrevious = false;
		previousTime = 0;
		
		lm.addElement("Task: " + task);
		
		// show the progress bar
		sb.showBar(task);
	}

	/**
	 * Switches the progress bar to determinate or indeterminate state.
	 * This method delegates to {@link StatusBar}.
	 * 
	 * @param determinate If <code>true</code>, the progress bar will be put in
	 * determinate state (i.e. known progress); if <code>false</code> it will be
	 * put in indeterminate state (i.e. bouncing box).
	 */
	public void setDeterminate(boolean determinate) {
		sb.setDeterminate(determinate);
	}
	
	/**
	 * Puts the information of a progress update into the table, and updates the status bar.
	 * @param update The progress update to use.
	 */
	public void handleProgressUpdate(ProgressUpdate update) {
		sb.setTask(update.status);
		if (hasPrevious) {
			String s = lm.remove(lm.size() - 1);
			lm.addElement(s + " (" + timeToString(update.time - previousTime) + " s)");
		}
		hasPrevious = true;
		lm.addElement(update.status);
		previousTime = update.time;
	}
	
	/**
	 * Displays in the list that the algorithm has aborted, but without an exception.
	 * @param reason The reason for aborting.
	 */
	public void handleAbort(String reason) {
		lm.addElement("Aborted: " + reason);
		
		taskReady();
	}
	
	/**
	 * Displays an exception in the list.
	 * @param t The Throwable object to show.
	 */
	public void handleException(Throwable t) {
		lm.addElement("Exception during algorithm: " + t.getClass().getSimpleName() + " (see output for details)");
		t.printStackTrace();
		
		taskReady();
	}
	
	private static Object timeToString(long time) {
		DecimalFormat df = new DecimalFormat("####0.000");
		return df.format(time / 1000.0);
	}

	/**
	 * Signals that the currently running task is ready.
	 */
	public void taskReady() {
		sb.hideBar();
		hasPrevious = false;
	}
}
