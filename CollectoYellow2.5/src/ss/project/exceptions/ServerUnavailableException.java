package ss.project.exceptions;

/**
 * 
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class ServerUnavailableException extends Exception {
	// Default serial number.
	private static final long serialVersionUID = 1L;

	public ServerUnavailableException(String msg) {
		super(msg);
	}
}
