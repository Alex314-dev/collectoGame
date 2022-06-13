package ss.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * 
 * The TUI for the server. A view class as its core functionality is simply
 * to present server logs.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class CollectoServerTUI {
	/** PrintWriter to write messages to the user. */ 
	private PrintWriter console;
	
	/** BufferedReader to read messages with. */
	private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * Constructs a new CollectoServerTUI. Initializes the console.
	 */
	public CollectoServerTUI() {
		console = new PrintWriter(System.out, true);
	}
	
	/**
	 * A method for requesting a valid port from the user.
	 * @ensures Valid port will be returned
	 * @return The port provided, if it is a valid one
	 */
	public int getPort() {
		while (true) {
			try {
				System.out.print("Please input a valid port: ");
				String port = in.readLine();
				System.out.println();
				return Integer.parseInt(port);
			} catch (IOException e) {
				System.out.println("Not a valid port!");
			} catch (NumberFormatException e) {
				System.out.println("Not a valid port!");
			}
		}
	}

	/**
	 * A method for printing messages to the console.
	 * @param The message that has to be printed
	 */
	public void showMessage(String message) {
		console.println(message);
	}
	
	/**
	 * A method for asking a question to the user and returning the answer.
	 * @param The question to be asked
	 * @return The answer
	 */
	public String getString(String question) {
		try {
			System.out.print(question);
			String answer = in.readLine();
			System.out.println();
			return answer;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * A method for asking a yes/no question to the user and returning the answer.
	 * @ensures \result == true || \result == false
	 * @param The question to be asked
	 * @return The answer
	 */
	public boolean getBoolean(String question) {
		while (true) {
			try {
				System.out.print(question);
				String answer = in.readLine();
				System.out.println();
				if (answer.equals("yes")) {
					return true;
				} else if (answer.equals("no")) {
					return false;
				} else {
					System.out.println("Please only answer with "
							+ "\"yes\" or \"no\" (case sensitive).");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
