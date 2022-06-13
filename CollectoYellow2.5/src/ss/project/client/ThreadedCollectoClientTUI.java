package ss.project.client;

import java.io.*;
import java.net.InetAddress;
import java.rmi.UnknownHostException;

import ss.project.exceptions.*;

import ss.project.protocol.ProtocolMessages;

/**
 * 
 * The client TUI for Collecto.
 * A view and a controller as it presents to the user, and also interprets
 * his/her commands.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 * 
 */
public class ThreadedCollectoClientTUI implements Runnable {
	
	// The client which is using this view.
	private ThreadedCollectoClient client;
	
	// A buffered reader to read input from the console.
	private BufferedReader in;
	
	/**
	 * Constructing the client and the buffered reader.
	 * @param collectoCLient The client which is using this view.
	 */
	public ThreadedCollectoClientTUI(ThreadedCollectoClient collectoClient) {
		this.client = collectoClient;
		in = new BufferedReader(new InputStreamReader(System.in));
	}
	
	/**
	 * A method which continuously reads from console, and handles them accordingly.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	@Override
	public void run() {
		while (true) {
			try {
				String userIn = in.readLine();
				handleUserInput(userIn);
			} catch (IOException e) {
				client.closeConnection();
				break;
			} catch (ExitProgram e) {
				client.closeConnection();
				break;
			} catch (ServerUnavailableException e) {
				client.closeConnection();
				break;
			}
		}
	}
	
	/**
	 * To process the inputs from the client.
	 * @requires userIn != null
	 * @param userIn The user input.
	 * @throws ServerUnavailableException In case the server is not available.
	 * @throws ExitProgram In case client types the command EXIT.
	 */
	private void handleUserInput(String userIn) throws ServerUnavailableException, ExitProgram {
		String[] splitUserIn = userIn.split("~");
		if (splitUserIn.length == 0) {
			System.out.println("Invalid command!");
			return;
		}
		try {
			switch (splitUserIn[0]) {
				case ProtocolMessages.HELLO:
					if (splitUserIn.length == 2) {
						this.client.sendHello(splitUserIn[1]);
					} else {
						System.out.println("Invalid command. "
					            + "HELLO~<client description> expected.");
					}
					break;
				case ProtocolMessages.LOGIN: 
					if (this.client.isRegisteredUsername()) {
						System.out.println("You already logged in.");
						break;
					}
					if (splitUserIn.length == 2) {
						this.client.sendLogIn(splitUserIn[1]);
					} else {
						System.out.println("Invalid command. "
					            + "LOGIN~<username> expected.");
					}		
					break;
				case ProtocolMessages.LIST:
					if (splitUserIn.length == 1) {
						this.client.sendList();
					} else {
						System.out.println("Invalid command. "
					            + "LIST expected.");
					}
					break;
				case ProtocolMessages.QUEUE:
					if (splitUserIn.length == 1) {
						this.client.sendQueue();
					} else {
						System.out.println("Invalid command. "
					            + "QUEUE expected.");
					}
					break;
				case ProtocolMessages.MOVE:
					if (splitUserIn.length == 2) {
						this.client.sendMove(Integer.parseInt(splitUserIn[1]),
															  -1);
					} else if (splitUserIn.length == 3) {
						this.client.sendMove(Integer.parseInt(splitUserIn[1]),
											 Integer.parseInt(splitUserIn[2]));
					} else {
						System.out.println("Invalid command. "
					            + "MOVE~<first push>[~second push] expected.");					
					}
					break;
				case "HINT":
					this.client.doHint();
					break;
				case "EXIT":
					throw new ExitProgram("");
				default:
					System.out.println("Unknown command!");
					break;
			}
		} catch (NumberFormatException e) {
			System.out.println("Only move with numbers!");
		}
	}
	
	/**
	 * A method to specifically ask a yes or no question to the client.
	 * @requires question != null
	 * @param question The yes or no question to be asked.
	 * @return true if the client responded with yes, false if the client responded with no.
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
	
	/**
	 * A method which simply presents the given string to the console.
	 * @requires string != null
	 * @param string The string to be shown to the client.
	 */
	public void showMessage(String string) {
		System.out.println(string);
	}
	
	/**
	 * A method to specifically ask for a valid IP address.
	 * @return An InetAddress representing the IP address given by the user.
	 */
	public InetAddress getIp() {
		while (true) {
			try {
				System.out.print("Please input a valid IP: ");
				String addr = in.readLine();
				System.out.println();
				InetAddress address = InetAddress.getByName(addr);
				return address;
			} catch (UnknownHostException e) {
				System.out.println("Unknown address!");
			} catch (IOException e) {
				System.out.println("Unknown address!");
			}
		}
	}
	
	/**
	 * A method to specifically ask for a valid port number.
	 * @return The given valid port number.
	 */
	public int getPort() {
		while (true) {
			try {
				System.out.print("Please input a valid port: ");
				String port = in.readLine();
				System.out.println();
				return Integer.parseInt(port);
			} catch (IOException e) {
				System.out.println("Please input a valid port numbers: ");
			} catch (NumberFormatException e) {
				System.out.println("Please input a valid port numbers: ");
			}
		}
	}
}
