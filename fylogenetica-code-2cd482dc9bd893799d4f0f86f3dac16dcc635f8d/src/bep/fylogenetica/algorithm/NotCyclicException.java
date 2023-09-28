package bep.fylogenetica.algorithm;

import java.util.ArrayList;

/**
 * This exception is thrown when the resulting space wasn't cyclic.
 */
public class NotCyclicException extends Exception {
	
	/**
	 * Creates a new {@link NotCyclicException} without a message.
	 */
	public NotCyclicException() {
		this("");
	}

	/**
	 * Creates a new {@link NotCyclicException} with a message
	 * @param message The message.
	 */
	public NotCyclicException(String message) {
		super(message);
	}
}
