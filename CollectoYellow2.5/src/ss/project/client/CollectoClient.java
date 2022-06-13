package ss.project.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import ss.project.exceptions.*;
import ss.project.exceptions.ProtocolException;
import ss.project.gamelogic.Board;
import ss.project.players.ComputerPlayer;
import ss.project.players.HumanPlayer;
import ss.project.players.Player;
import ss.project.protocol.*;
import ss.project.strategy.NaiveStrategy;

/**
 * 
 * Aborted..!
 * 
 */
public class CollectoClient {
	
	private Player opponentPlayer;
	private Player thisPLayer; 
	private Board theBoard; 
	
	private String userName;	
	private boolean loggedIn;
	private boolean turnToPlay;
	private boolean initialized;
	private boolean doubleMove;
	private boolean inGame;
	
	private Socket serverSock;
	private BufferedReader in;
	private BufferedWriter out;
	
	private CollectoClientTUI view;

	

	/**
	 * Constructs a new HotelClient. Initialises the view.
	 */
	public CollectoClient() {
		view = new CollectoClientTUI(this);
		this.loggedIn = false;
		this.userName = null;
		this.turnToPlay = false;
		this.initialized = false;
		this.doubleMove = false;
		this.inGame = false;
	}

	/**
	 * Starts a new HotelClient by creating a connection, followed by the 
	 * HELLO handshake as defined in the protocol. After a successful 
	 * connection and handshake, the view is started. The view asks for 
	 * used input and handles all further calls to methods of this class. 
	 * 
	 * When errors occur, or when the user terminates a server connection, the
	 * user is asked whether a new connection should be made.
	 */
	public void start() {
		boolean run = true;
		while (run) {
			try {
				createConnection();
				view.start();
			
				run = view.getBoolean("Would you like to make a connection? (yes/no): ");
			} catch (ExitProgram e) {
				closeConnection();
				run = view.getBoolean("Would you like to make a connection? (yes/no): ");
			} catch (ServerUnavailableException e) {
				closeConnection();
				run = view.getBoolean("Would you like to make a connection? (yes/no): ");
			}
		}
		closeConnection();
		clearConnection();
	}
	
	

	/**
	 * Creates a connection to the server. Requests the IP and port to 
	 * connect to at the view (TUI).
	 * 
	 * The method continues to ask for an IP and port and attempts to connect 
	 * until a connection is established or until the user indicates to exit 
	 * the program.
	 * 
	 * @throws ExitProgram if a connection is not established and the user 
	 * 				       indicates to want to exit the program.
	 * @ensures serverSock contains a valid socket connection to a server
	 */
	public void createConnection() throws ExitProgram {
		clearConnection();
		while (serverSock == null) {
			InetAddress addr = view.getIp();
			int port = view.getPort();

			// try to open a Socket to the server
			try {

				view.showMessage("Attempting to connect to " + addr + ":" 
					+ port + "...");
				serverSock = new Socket(addr, port);
				in = new BufferedReader(new InputStreamReader(
						serverSock.getInputStream()));
				out = new BufferedWriter(new OutputStreamWriter(
						serverSock.getOutputStream()));
				view.showMessage("Connection established.");
				view.showMessage("Awaiting Initialization sequence. "
								+ "Hint: HELLO~<client description>");
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on " 
					+ addr + " and port " + port + ".");

				//Do you want to try again? (ask user, to be implemented)
				boolean again = view.getBoolean("Would you like to make a connection? (yes/no): ");
				if (!again) {
					throw new ExitProgram("User indicated to exit.");
				}
			}
		}
	}

	/**
	 * Resets the serverSocket and In- and OutputStreams to null.
	 * 
	 * Always make sure to close current connections via shutdown() 
	 * before calling this method!
	 */
	public void clearConnection() {
		serverSock = null;
		in = null;
		out = null;
	}
	
	/**
	 * Sends a message to the connected server, followed by a new line. 
	 * The stream is then flushed.
	 * 
	 * @param msg the message to write to the OutputStream.
	 * @throws ServerUnavailableException if IO errors occur.
	 */
	public synchronized void sendMessage(String msg) 
			throws ServerUnavailableException {
		if (out != null) {
			try {
				out.write(msg);
				out.newLine();
				out.flush();
			} catch (IOException e) {
				view.showMessage(e.getMessage());
				throw new ServerUnavailableException("Could not write "
						+ "to server.");
			}
		} else {
			throw new ServerUnavailableException("Could not write "
					+ "to server.");
		}
	}
	
	public String readLineFromServer() 
			throws ServerUnavailableException {
		if (in != null) {
			try {
				// Read and return answer from Server
				String answer = in.readLine();
				if (answer == null) {
					throw new ServerUnavailableException("Could not read "
							+ "from server.");
				}
				return answer;
			} catch (IOException e) {
				throw new ServerUnavailableException("Could not read "
						+ "from server.");
			}
		} else {
			throw new ServerUnavailableException("Could not read "
					+ "from server.");
		}
	}
	
	public void handleHello(String description)
			throws ServerUnavailableException, ProtocolException {
		if (!initialized) {
			sendMessage(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + description);
			String[] messageParts = split(readLineFromServer());
			if (messageParts.length == 2) {
				if (messageParts[0].equals(ProtocolMessages.HELLO)) {
					view.showMessage("Initialization sequence with " 
								+ messageParts[1] + " completed");
					view.showMessage("You can now log in.");
					this.initialized = true;
				} else {
					throw new ProtocolException("HELLO~<server description>");
				}
			} else if (messageParts.length > 2) {  
				if (messageParts[0].equals(ProtocolMessages.HELLO)) {
					view.showMessage("Initialization sequence with " + messageParts[1]
									+ " completed! Server's extensions:");
					for (int i = 2; i < messageParts.length; i++) {
						view.showMessage(messageParts[i]);
					}
					view.showMessage("You can now log in.");
					this.initialized = true;
				} else {
					throw new ProtocolException("Invalid response from server."
				            + "HELLO~<server description>[~extension] expected.");
				}
			} else {
				throw new ProtocolException("Invalid response from server."
						            + "HELLO~<server description>[~extension] expected.");
			}
		} else {
			view.showMessage("You already initialized. Please log in.");
		}
		
	}
	
	public void doLogIn(String username) throws ServerUnavailableException, ProtocolException {
		if (initialized && !loggedIn) {
			sendMessage(ProtocolMessages.LOGIN + ProtocolMessages.DELIMITER + username);
			String response = readLineFromServer();
			if (response.equals(ProtocolMessages.ALREADYLOGGEDIN)) {
				view.showMessage("Username taken or you are already logged in. "
						+ "Please enter a new one.");	
			} else if (response.equals(ProtocolMessages.LOGIN)) {
				view.showMessage("You logged in successfully.");
				view.showMessage("You can now request a game. Hint: QUEUE");
				loggedIn = true;
				setUsername(username);
			} else {
				view.showMessage(response);
				throw new ProtocolException("Invalid response from server." 
									+ ProtocolMessages.LOGIN + " or " 
						            + ProtocolMessages.ALREADYLOGGEDIN + " expected.");
			}
		} else {
			if (loggedIn) {
				view.showMessage("You already logged in.");
				return;
			}
			view.showMessage("Waiting for initialization.");
		}			
	}

	public void doList() throws ServerUnavailableException, ProtocolException {
		if (initialized && loggedIn) {	
			sendMessage(ProtocolMessages.LIST);
			String[] listResponse = split(readLineFromServer());
			try {
				if (listResponse[0].equals(ProtocolMessages.LIST)) {
					view.showMessage("Currently logged in:");
					for (int i = 1; i < listResponse.length; i++) {
						view.showMessage(listResponse[i]);
					}
				} else {
					throw new ProtocolException("Invalid response from server. "
							        + "	LIST[~username]* expected");
				}
			} catch (IndexOutOfBoundsException e) {
				throw new ProtocolException("Invalid response from server. "
				        + "	LIST[~username]* expected");
			}
		} else {
			if (!initialized) {
				view.showMessage("Waiting for initialization.");
				return;
			}
			view.showMessage("You are not logged in.");
		}
	}
	
	public void doQueue() throws ServerUnavailableException, ProtocolException {
		if (initialized && !inGame && loggedIn) {
			sendMessage(ProtocolMessages.QUEUE);
			view.showMessage("Waiting for game...");
			String gameCreation = readLineFromServer();
			view.showMessage("Server responded with new game, pending...");
			this.inGame = true;
			String[] splitNewGame = split(gameCreation);
			
			if (splitNewGame[0].equals(ProtocolMessages.NEWGAME) && splitNewGame.length == 52) {
				int[] init = new int[49];
				String user1 = splitNewGame[splitNewGame.length - 2];
				String user2 = splitNewGame[splitNewGame.length - 1];
				createField(init, splitNewGame);
				
				// Creation of the client board.
				this.theBoard = new Board(init);
				view.showMessage("Game started between " + user1 + " and " + user2);
				view.showMessage(this.theBoard.toString());
				
				if (user1.equals(getUserName())) {
					this.turnToPlay = true;
					if (getUserName().contains("AI")) {
						this.thisPLayer = new ComputerPlayer(new NaiveStrategy());
						this.opponentPlayer = new HumanPlayer(user2);
						
						view.showMessage("AI deciding on move: ");
						doMove("AI");
					} else {
						
						this.thisPLayer = new HumanPlayer(user1);
						this.opponentPlayer = new HumanPlayer(user2);
						
						view.showMessage("Please enter a move: ");
					}
				} else {
					this.turnToPlay = false;
					if (getUserName().contains("AI")) {
						this.thisPLayer = new ComputerPlayer(new NaiveStrategy());
						this.opponentPlayer = new HumanPlayer(user1);			
					} else {
						this.thisPLayer = new HumanPlayer(user2);
						this.opponentPlayer = new HumanPlayer(user1);

					}
					view.showMessage("Waiting for opponent's move...");
					String response = readLineFromServer();	
					if (response.contains(ProtocolMessages.GAMEOVER)) {
						doGameOver(response);
					} else {
						doOpponentMove(response);
					}
				}
			} else {
				throw new ProtocolException("Invalid response from server. "
						+ "NEWGAME~<cell value>^49~<player1 name>~<player2 name> expected");
			}	
		
		} else {
			if (!initialized) {
				view.showMessage("Waiting for initialization.");
				return;
			}
			if (!loggedIn) {
				view.showMessage("You are not logged in.");
				return;
			}
			view.showMessage("You are already in game.");
		}		
	}
	
	public void doHint() {
		if (inGame) {
			ComputerPlayer check = new ComputerPlayer(new NaiveStrategy());
			check.determineMove(this.theBoard);
		} else {
			view.showMessage("You are not in game.");
		}
	}
	
	public void doOpponentMove(String command) throws ProtocolException, 
	           										  ServerUnavailableException {
		String[] moveSplit = command.split(ProtocolMessages.DELIMITER);
		if (moveSplit[0].equals(ProtocolMessages.MOVE)) {
			if (moveSplit.length <= 3 && moveSplit.length > 1) {
				if (moveSplit.length == 2) {
					int move = Integer.parseInt(moveSplit[1]);
					
					if (((HumanPlayer) getOpponentPlayer()).
						checkSingleMoveLegality(move, this.theBoard)) {
						getOpponentPlayer().makeSingleMove(move, this.theBoard);
						view.showMessage("Your opponent played move: " + move);
						this.turnToPlay = true;
					} else {
						throw new ProtocolException("Invalid move from opponent");
					}
				} else {
					int move1 = Integer.parseInt(moveSplit[1]);
					int move2 = Integer.parseInt(moveSplit[2]);
					if (((HumanPlayer) getOpponentPlayer()).
						checkDoubleMoveLegality(move1, move2, this.theBoard)) {
						getOpponentPlayer().makeSingleMove(move1, this.theBoard);
						getOpponentPlayer().makeSingleMove(move2, this.theBoard);
						view.showMessage("Your opponent played moves: " + move1 + ", " + move2);
						this.turnToPlay = true;
					} else {
						throw new ProtocolException("Invalid move from opponent");
					}
				}
			} else {
				throw new ProtocolException("Invalid response from server. "
						+ "MOVE~<first push>[~second push] expected");
			}
		} else {
			throw new ProtocolException("Invalid response from server. "
					+ "MOVE~<first push>[~second push] expected");
		}
		//Show the board condition after the move
		
		view.showMessage("Game progress:\n" + this.theBoard.toString());
		if (this.theBoard.gameOver()) {
			doGameOver(readLineFromServer());
		} else {
			if (getThisPLayer() instanceof ComputerPlayer) {
				view.showMessage("AI deciding on move: ");
				doMove("AI");
			} else {
				if (this.theBoard.possibleSingleMove(this.theBoard.getFields())) {
					view.showMessage("Expecting your single move");
					this.doubleMove = false;
				} else if (this.theBoard.possibleDoubleMove(this.theBoard.getFields())) {
					view.showMessage("Expecting your double move");
					this.doubleMove = true;
				}	
			}
		}
	}
	
	public void doMove(String command) throws ProtocolException, ServerUnavailableException {
		if (initialized && loggedIn && inGame && this.turnToPlay) {
			if (command.equals("AI")) {
				int[] moves = ((ComputerPlayer) getThisPLayer())
								.determineMove(this.theBoard);			
				if (moves[1] == -1) { 					
					getThisPLayer().makeSingleMove(moves[0], this.theBoard);
					this.turnToPlay = false;
					
					sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER 
						       + moves[0]);															
				} else {					
					getThisPLayer().makeSingleMove(moves[0], this.theBoard);
					getThisPLayer().makeSingleMove(moves[1], this.theBoard);
					this.turnToPlay = false;
					
					sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER 
								+ moves[0] + ProtocolMessages.DELIMITER 
								+ moves[1]);			
				}
				readLineFromServer();
				view.showMessage("Game progress:\n" + this.theBoard.toString());
				view.showMessage("Waiting for opponent's move...");
				String response = readLineFromServer();	
				if (response.contains(ProtocolMessages.GAMEOVER)) {
					doGameOver(response);
				} else {
					doOpponentMove(response);
				}
			} else {
				String[] moveSplit = split(command);
				int move1 = Integer.parseInt(moveSplit[0]);
				int move2 = Integer.parseInt(moveSplit[1]);
				if (move2 == -1) {
					if (!doubleMove) {
						if (((HumanPlayer) getThisPLayer()).
								checkSingleMoveLegality(move1, this.theBoard)) {
							getThisPLayer().makeSingleMove(move1, this.theBoard);
							this.turnToPlay = false;
							
							sendMessage(ProtocolMessages.MOVE 
										+ ProtocolMessages.DELIMITER + move1);
							readLineFromServer(); //The server will return the 
												  //move we just did, we dont need it
							view.showMessage("Game progress:\n" + this.theBoard.toString());
							view.showMessage("Waiting for opponent's move...");
							String response = readLineFromServer();
							
							if (response.contains(ProtocolMessages.GAMEOVER)) {
								doGameOver(response);
							} else {
								doOpponentMove(response);
							}
						} else {
							view.showMessage("This move is not legal. Try again.");
						}
					} else {
						view.showMessage("Double move expected.");
					}
				} else {
					if (doubleMove) {
						if (((HumanPlayer) getThisPLayer()).
								checkDoubleMoveLegality(move1, move2, this.theBoard)) {
							getThisPLayer().makeSingleMove(move1, this.theBoard);
							getThisPLayer().makeSingleMove(move2, this.theBoard);
							this.turnToPlay = false;
							
							sendMessage(ProtocolMessages.MOVE + ProtocolMessages.DELIMITER 
										+ move1 + ProtocolMessages.DELIMITER + move2);
							readLineFromServer(); 
							view.showMessage("Game progress:\n" + this.theBoard.toString());
							view.showMessage("Waiting for opponent's move...");
							String response = readLineFromServer();	
							
							if (response.contains(ProtocolMessages.GAMEOVER)) {
								doGameOver(response);
							} else {
								doOpponentMove(response);
							}
						} else {
							view.showMessage("This move is not legal. Try again.");
						}
					} else {
						view.showMessage("There is a posible single move. "
								+ "Double move not allowed");
					}		
				}
			}
		} else {
			if (!initialized) {
				view.showMessage("Waiting for initialization.");
				return;
			}
			if (!loggedIn) {
				view.showMessage("You are not logged in.");
				return;
			}
			if (!inGame) {
				view.showMessage("You are not in a game.");
				return;
			}
			view.showMessage("Not your turn to play, please wait.");
			view.showMessage("Waiting for opponent's move...");
		}	
	}
	
	private void doGameOver(String response) throws ProtocolException {
		String[] splitResponse = response.split(ProtocolMessages.DELIMITER);
		if (splitResponse.length == 3) {
			view.showMessage("Game over! Winner: " + splitResponse[2] 
								+ ". Reason: " + splitResponse[1]);
			this.inGame = false;
		} else if (splitResponse.length == 2) {
			view.showMessage("Game over! Its a draw!");
			this.inGame = false;
		} else {
			throw new ProtocolException("Invalid response from server. "
						 			+ "GAMEOVER~<reason>[~winner] expected.");
		}
		
	}

	/**
	 * Closes the connection by closing the In- and OutputStreams, as 
	 * well as the serverSocket.
	 */
	public void closeConnection() {
		view.showMessage("Closing the connection...");
		try {
			in.close();
			out.close();
			serverSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void reset() {
		setDoubleMove(false);
		setInGame(false);
		setInitialized(false);
		setLogIn(false);
		setTurnToPLay(false);
		setUsername(null);
	}
	
	public String[] split(String response) {
		return response.split(ProtocolMessages.DELIMITER);
	}
	
	public void createField(int[] init, String[] splitNewGame) {
		for (int i = 1; i < splitNewGame.length - 2; i++) {
			init[i - 1] = Integer.parseInt(splitNewGame[i]);
		}
	}
	
	public void setUsername(String username) {
		this.userName = username;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public Player getOpponentPlayer() {
		return opponentPlayer;
	}
	
	public Player getThisPLayer() {
		return thisPLayer;
	}
	
	public void setLogIn(boolean set) {
		this.loggedIn = set;
	}
	
	public void setTurnToPLay(boolean set) {
		this.turnToPlay = set;
	}
	
	public void setInitialized(boolean set) {
		this.initialized = set;
	}
	
	public void setDoubleMove(boolean set) {
		this.doubleMove = set;
	}
	
	public void setInGame(boolean set) {
		this.inGame = set;
	}
	
	public static void main(String[] args) {
		(new CollectoClient()).start();
	}
}