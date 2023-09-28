package bep.fylogenetica.gui;

/**
 * Objects of this class are used to get progress updates from the worker threads
 * to the GUI.
 */
public class ProgressUpdate {
	
	/**
	 * The time this status started.
	 */
	public long time;
	
	/**
	 * The new status.
	 */
	public String status;
	
	/**
	 * Constructor for {@link ProgressUpdate}.
	 * 
	 * @param time The time this status started.
	 * @param status The new status.
	 */
	public ProgressUpdate(long time, String status) {
		this.time = time;
		this.status = status;
	}
}
