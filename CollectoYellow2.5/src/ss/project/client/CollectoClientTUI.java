package ss.project.client;

import java.io.*;
import java.net.InetAddress;
import java.rmi.UnknownHostException;

import ss.project.exceptions.*;

import ss.project.protocol.ProtocolMessages;

/**
 * 
 * Aborted..!
 * 
 */
public class CollectoClientTUI {

	private CollectoClient client;
	private BufferedReader in;
	
	public CollectoClientTUI(CollectoClient collectoCLient) {
		this.client = collectoCLient;
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public void start() throws ServerUnavailableException {
		while (true) {
			try {
				String userIn = in.readLine();
				handleUserInput(userIn);
			} catch (IOException e) {
				throw new ServerUnavailableException("Server unavailable!");
			} catch (ExitProgram e) {	
				client.reset();
				client.closeConnection();
				break;
			}
		}
	}

	private void handleUserInput(String userIn) throws  ServerUnavailableException, ExitProgram {
		String[] splitUserIn = userIn.split("~");
		try {
			switch (splitUserIn[0]) {
				case ProtocolMessages.HELLO:
					if (splitUserIn.length == 2) {
						this.client.handleHello(splitUserIn[1]);
					} else {
						System.out.println("Invalid command."
					            + "HELLO~<client description> expected.");
					}
				
					break;
				case ProtocolMessages.LOGIN: 
					if (splitUserIn.length == 2) {
						this.client.doLogIn(splitUserIn[1]);
					} else {
						System.out.println("Invalid command."
					            + "LOGIN~<username> expected.");
					}					
					break;
				case ProtocolMessages.LIST:
					this.client.doList();
					break;
				case ProtocolMessages.QUEUE:
					this.client.doQueue();
					break;
				case ProtocolMessages.MOVE:
					if (splitUserIn.length == 2) {
						this.client.doMove(splitUserIn[1] + ProtocolMessages.DELIMITER + "-1");
					} else if (splitUserIn.length == 3) {
						this.client.doMove(splitUserIn[1] 
											+ ProtocolMessages.DELIMITER 
											+ splitUserIn[2]);
					} else {
						System.out.println("Invalid command."
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
		} catch (ProtocolException e) {
			System.out.println(e.getMessage());
		}	
	}

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

	public void showMessage(String string) {
		System.out.println(string);
	}
	
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
