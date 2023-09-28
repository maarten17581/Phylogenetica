package bep.fylogenetica.algorithm;

/**
 * This interface is used to report progress of algorithms to the GUI.
 */
public interface ProgressListener {
	
	/**
	 * Called by the algorithm when a new progress value is available.
	 * @param value The progress value, this should be in the range 0-100.
	 */
	public void newProgressValue(int value);
}
