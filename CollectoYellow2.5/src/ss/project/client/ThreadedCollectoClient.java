package ss.project.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ss.project.exceptions.ExitProgram;
import ss.project.exceptions.ProtocolException;
import ss.project.exceptions.ServerUnavailableException;
import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;
import ss.project.players.*;
import ss.project.protocol.ProtocolMessages;
import ss.project.strategy.*;

/**
 * 
 * The client for Collecto.
 * A model class as it is a part of the network communication.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 * 
 */
public class ThreadedCollectoClient implements Runnable {
	
	/** Game logic. */
	private Player opponentPlayer;
	private volatile Player thisPlayer;
	private volatile Board theBoard;
	
	/** Client's name. */
	private volatile String userName;
	
	/** Flags. */
	private volatile boolean connectionMade;
	private volatile boolean initialized;
	private volatile boolean loggedIn;
	private volatile boolean inGame;
	private volatile boolean turnToPlay;
	private volatile boolean registeredUsername;
	private boolean queued;
	private volatile String lastPlayedMove;
	private volatile boolean testing;
	
	/** Server socket, and communication channels. */
	private Socket serverSock;
	private BufferedReader in;
	private BufferedWriter out;
	
	/** The client's view. */
	private ThreadedCollectoClientTUI view;
	
	/**
	 * Constructs a new ThreadedCollectoClient. Initializes the view.
	 */
	public ThreadedCollectoClient() {
		view = new ThreadedCollectoClientTUI(this);
		this.loggedIn = false;
		this.userName = null;
		this.turnToPlay = false;
		this.initialized = false;
		this.inGame = false;
		this.connectionMade = false;
		this.registeredUsername = false;
		this.queued = false;
	}
	
	/**
	 * Constructor only for testing!
	 */
	public ThreadedCollectoClient(boolean testing) {
		view = new ThreadedCollectoClientTUI(this);
		this.testing = true;
		this.loggedIn = testing;
		this.userName = null;
		this.turnToPlay = false;
		this.initialized = false;
		this.inGame = false;
		this.connectionMade = false;
		this.registeredUsername = false;
		this.queued = false;
		this.out = new BufferedWriter(new OutputStreamWriter(System.out));
	}
	
	/**
	 * Creating a connection and starting the view for user input.
	 */
	public void start() {
		try {
			createConnection();
			Thread tui = new Thread(view);
			tui.setDaemon(true);
			tui.start();
		} catch (ExitProgram e) {
			return;
		}
	}
	
	/**
	 * Connecting to the server and setting the connectionMade flag, 
	 * if the connection is established.
	 * @throws ExitProgram In case the client doesn't want to reconnect to the server.
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
				this.connectionMade = true;
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on " 
					+ addr + " and port " + port + ".");

				//Do you want to try again?
				boolean again = view.getBoolean("Would you like to make a connection? (yes/no): ");
				if (!again) {
					this.connectionMade = true;
					// this flag is set to true here 
					// to make the client thread escape the while loop in line 229
					// thus the in.readLine() will throw a NullPointerException there, 
					// closing the connection,
					// and terminating the client.
					throw new ExitProgram("User indicated to exit.");
				}
			}
		}
	}
	
	/**
	 * Clears the sockets and communication channels.
	 * @ensures this.serverSock == null && this.in == null && this.out == null
	 */
	public void clearConnection() {
		serverSock = null;
		in = null;
		out = null;
	}
	
	/**
	 * Closes the sockets and communication channels. Resets the client.
	 */
	public synchronized void closeConnection() {
		try {
			// Server socket is closed first because otherwise in.close() will block
			// as we will try to close it, while also we are still trying to read from it.
			serverSock.close();
			in.close();
			out.close();
			reset();
		} catch (IOException ignored) {
			
		} catch (NullPointerException ignored) {
			// this will be invoked after the run method gets a NullPointerException
			// from the in.readLine(), meaning that no connection was ever made.
			// thus to simply terminate the client we just ignore the exception.
		}
	}
	
	/**
	 * Resets all of the flags back to their default value.
	 */
	public void reset() {
		this.inGame = false;
		this.initialized = false;
		this.loggedIn = false;
		this.turnToPlay = false;
		this.userName = null;
		this.connectionMade = false;
		this.registeredUsername = false;
		this.queued = false;
	}
	

	/**
	 * Sends the given message to the server.
	 * @param msg The message to be sent to the server.
	 * @throws ServerUnavailableException In case something went wrong with writing to the server.
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
	
	/**
	 * Returns a split version of the given response.
	 * @param response To be split from the ~ character.
	 * @return An array of words which were split by ~.
	 */
	public String[] split(String response) {
		return response.split(ProtocolMessages.DELIMITER);
	}
	
	/**
	 * Continuously reading the messages coming from the server and 
	 * sending them to a method which handles the messages.
	 */
	@Override
	public void run() {
		while (!this.connectionMade) {
			continue;
		}
		
		String msg;
		try {
			msg = in.readLine();
			while (msg != null) {
				handleCommand(msg);
				msg = in.readLine();
			}
			closeConnection();
		} catch (IOException e) {
			view.showMessage("Closing the connection...");
			closeConnection();
		} catch (ServerUnavailableException e) {
			closeConnection();
		} catch (NullPointerException e) {
			view.showMessage("Closing the connection...");
			closeConnection();
		}
	}
	
	/**
	 * Handles each command coming from the server.
	 * Sending them into the according handle* methods.
	 * @param msg The message from the server.
	 * @throws IOException In case handle methods couldn't write back to the server.
	 * @throws ServerUnavailableException  In case server is not available.
	 */
	public void handleCommand(String msg) throws IOException, ServerUnavailableException {
		try (Scanner sc = new Scanner(msg)) {
			sc.useDelimiter(ProtocolMessages.DELIMITER);
			switch (sc.next()) {
				case ProtocolMessages.HELLO:
					handleHello(msg);
					break;
				case ProtocolMessages.LOGIN:
					handleLogIn();
					break;
				case ProtocolMessages.ALREADYLOGGEDIN:
					handleAlreadyLoggedIn();
					break;
				case ProtocolMessages.LIST:
					handleList(msg);
					break;
				case ProtocolMessages.NEWGAME:
					handleNewGame(msg);
					break;
				case ProtocolMessages.MOVE:
					int move1 = Integer.parseInt(sc.next());
					int move2 = -1;
					if (sc.hasNext()) {
						move2 = Integer.parseInt(sc.next());
					}
					handleMove(move1, move2);
					break;
				case ProtocolMessages.GAMEOVER:
					handleGameOver(msg);
					break;
				case "ERROR":
					/*
					 * This case will be reached rarely. The reason
					 * is that the client makes sure that the commands
					 * are formatted correctly and are sent in a timely manner.
					 * The moves are also checked for legality.
					 * 
					 * This case, then is only reached in a very rare occurrence,
					 * where the client sends a move as a response to the opponent's move
					 * but while the move is being sent, the opponent quit, and thus
					 * there is actually no game to make moves on.
					 * 
					 * In this case, the server will send an ERROR (handled here),
					 * which means that we sent the move but the opponent has already quit.
					 * We can ignore this Error message safely and continue with other commands.
					 */
					break;
				default:
					sendMessage("ERROR~No such command!");
					break;
			}
		} catch (NoSuchElementException e) {
			sendMessage("ERROR");
		} catch (NumberFormatException e) {
			sendMessage("ERROR");
		} catch (ProtocolException e) {
			sendMessage("ERROR~Protocol violated!");
		}
	}
	
	/**
	 * To send hello to the server with the client description.
	 * @requires desc != null
	 * @param desc The client description.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendHello(String desc) throws ServerUnavailableException {
		if (!this.initialized) {
			sendMessage(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + desc);
		} else {
			view.showMessage("Already initialized!");
		}
	}
	
	/**
	 * To process the hello message coming from the server.
	 * @requires msg != null
	 * @ensures initialized == true, if the server does not violate the protocol.
	 * @param msg The message the server has sent.
	 * @throws ProtocolException In case the server violates the protocol.
	 * @throws ServerUnavailableException 
	 */
	public void handleHello(String msg) throws ProtocolException, ServerUnavailableException {
		if (this.initialized && !this.testing) {
			sendMessage("ERROR~Already initialized!");
			return;
		}
		
		String[] messageParts = split(msg);
		if (messageParts.length == 2) {
			if (messageParts[0].equals(ProtocolMessages.HELLO)) {
				view.showMessage("Initialization sequence with " 
							+ messageParts[1] + " completed");
				view.showMessage("You can now log in. If you wish to play with an AI you can add"
					       + " \"AI\" or \"AI+\" for better performance to your username.");
				view.showMessage("To login: LOGIN~<username>");
				this.initialized = true;
			} else {
				throw new ProtocolException("Invalid response from server."
			            + "HELLO~<server description>[~extension] expected.");
			}
		} else if (messageParts.length > 2) {  
			if (messageParts[0].equals(ProtocolMessages.HELLO)) {
				view.showMessage("Initialization sequence with " + messageParts[1]
								+ " completed! Server's extensions:");
				for (int i = 2; i < messageParts.length; i++) {
					view.showMessage(messageParts[i]);
				}
				view.showMessage("You can now log in. If you wish to play with an AI you can add"
					       + " \"AI\" or \"AI+\" for better performance to your username.");
				view.showMessage("To login: LOGIN~<username>");
				this.initialized = true;
			} else {
				throw new ProtocolException("Invalid response from server."
			            + "HELLO~<server description>[~extension] expected.");
			}
		} else {
			throw new ProtocolException("Invalid response from server."
					            + "HELLO~<server description>[~extension] expected.");
		}
	}
	
	/**
	 * To send a list request to the server.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendList() throws ServerUnavailableException {
		if (this.initialized && this.loggedIn) {
			sendMessage(ProtocolMessages.LIST);
		} else {
			if (!initialized) {
				view.showMessage("Waiting for initialization.");
				return;
			}
			view.showMessage("You are not logged in.");
		}
	}
	
	/**
	 * To process the list message from the server.
	 * @requires msg != null
	 * @param msg The message the server has sent.
	 * @throws ProtocolException In case the server violated the protocol.
	 * @throws ServerUnavailableException 
	 */
	public void handleList(String msg) throws ProtocolException, ServerUnavailableException {
		if ((!this.initialized || !this.loggedIn) && !this.testing) {
			sendMessage("ERROR~Untimely list command");
			return;
		}
		
		String[] listResponse = split(msg);
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
	}
	
	/**
	 * To send the LOGIN request to the server.
	 * @requires username != null
	 * @param username The client's possible username
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendLogIn(String username) throws ServerUnavailableException {
		if (this.initialized && !this.loggedIn) {
			sendMessage(ProtocolMessages.LOGIN + ProtocolMessages.DELIMITER + username);
			this.userName = username;
		} else {
			if (loggedIn) {
				view.showMessage("You already logged in.");
				return;
			}
			view.showMessage("Waiting for initialization.");
		}	
	}
	
	/**
	 * To handle log in responses coming from the server.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void handleLogIn() throws ServerUnavailableException {
		if (!(this.initialized && !this.loggedIn)) {
			sendMessage("ERROR~Untimley login!");
			return;
		}
		view.showMessage("Login is completed. "
				+ "You can now queue or request the list of connected clients."
				+ "\nHint: `QUEUE` or `LIST`");
		this.loggedIn = true;
		this.registeredUsername = true;
	}
	
	/**
	 * To process the ALREADYLOGGEDIN message from the server.
	 * @throws ServerUnavailableException 
	 */
	public void handleAlreadyLoggedIn() throws ServerUnavailableException {
		if (!this.initialized && !this.testing) {
			sendMessage("ERROR~Untimley alreadyloggedin!");
			return;
		}
		view.showMessage("Username taken. Try again.");
	}
	
	/**
	 * To send the QUEUE request to the server.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendQueue() throws ServerUnavailableException {
		if (this.initialized && this.loggedIn && !this.inGame) {
			sendMessage(ProtocolMessages.QUEUE);
			setQueued(!queued);
			if (isQueued()) {
				view.showMessage("Queue request sent.");
			} else {
				view.showMessage("Queue exit request sent.");
			}
		} else {
			if (!this.initialized) {
				view.showMessage("Please initialize first! Hint: HELLO~<client description>");
				return;
			}
			if (!this.loggedIn) {
				view.showMessage("Please log in first! Hint: LOGIN~<username>");
				return;
			}
			view.showMessage("Can't queue while in a game!");
		}
	}
	
	/**
	 * To process the NEWGAME message from the server.
	 * @requires msg != null
	 * @ensures theBoard will be created in accordance to the server's message.
	 * @ensures The players will be correctly assigned with the turns decided.
	 * @param msg The message from the server
	 * @throws ProtocolException In case the server violates the protocol.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void handleNewGame(String msg) throws ProtocolException, ServerUnavailableException {
		view.showMessage("Server responded with new game, pending...");
		String[] splitNewGame = split(msg);
		
		if (splitNewGame[0].equals(ProtocolMessages.NEWGAME) && splitNewGame.length == 52) {
			int[] init = new int[49];
			String user1 = splitNewGame[splitNewGame.length - 2];
			String user2 = splitNewGame[splitNewGame.length - 1];
			createField(init, splitNewGame);
			// Creation of the client board.
			this.theBoard = new Board(init);
			if ((this.theBoard.getBall(3, 3) != Ball.EMPTY 
					|| this.theBoard.handleAdjacency(this.theBoard.getFields()).size() != 0
					|| !this.theBoard.possibleSingleMove(this.theBoard.getFields()))
					&& !this.testing) {
				view.showMessage("The game is aborted due to illegal board.");
				sendMessage("ERROR~Illegal board!");
				return;
			}
			
			view.showMessage("Game started between " + user1 + " and " + user2);
			view.showMessage(this.theBoard.toString());
			playerCreation(user1, user2);
			this.inGame = true; 
			// The inGame is below player creation so that
			// if the client types a move while we are in the process of creating the players
			// (very very very rare)
			// he/she will get a "Not in a game!" error message as the game is not yet set.
		} else {
			throw new ProtocolException("Invalid response from server. "
					+ "NEWGAME~<cell value>^49~<player1 name>~<player2 name> expected");
		}
	}
	
	/**
	 * To create players in accordance with the NEWGAME message.
	 * @param user1 The first player's username.
	 * @param user2 The second player's username.
	 * @param level Client's AI level.
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void playerCreation(String user1, String user2) 
			throws ServerUnavailableException {
		if (user1.equals(this.userName)) {
			if (this.userName.contains("AI+")) {
				this.thisPlayer = new ComputerPlayer(new SmartStrategy());
				this.opponentPlayer = new HumanPlayer(user2);
				view.showMessage("Smarter AI deciding on move: ");
				sendAiMove();
			} else if (this.userName.contains("AI")) {
				this.thisPlayer = new ComputerPlayer(new NaiveStrategy());
				this.opponentPlayer = new HumanPlayer(user2);
				view.showMessage("Simple AI deciding on move: ");
				sendAiMove();
			} else {
				this.thisPlayer = new HumanPlayer(user1);
				this.opponentPlayer = new HumanPlayer(user2);
				view.showMessage("To make a move: MOVE~<first move>~[second move]");
				view.showMessage("If you want a hint: HINT");
				view.showMessage("Your move: ");
			}
			this.turnToPlay = true;
		} else {
			if (this.userName.contains("AI+")) {
				this.thisPlayer = new ComputerPlayer(new SmartStrategy());
				this.opponentPlayer = new HumanPlayer(user1);
				view.showMessage("Waiting for opponent's move: ");
			} else if (this.userName.contains("AI")) {
				this.thisPlayer = new ComputerPlayer(new NaiveStrategy());
				this.opponentPlayer = new HumanPlayer(user1);
				view.showMessage("Waiting for opponent's move: ");
			} else {
				this.thisPlayer = new HumanPlayer(user2);
				this.opponentPlayer = new HumanPlayer(user1);
				view.showMessage("Waiting for opponent's move: ");
			}
			this.turnToPlay = false;
		}
	}
	
	/**
	 * A helper method for the new game creation.
	 * Creating a integer array with the given integers by the server
	 * @requires init.lenght == 49 && splitNewGame.lenght == 49 && splitNewGame != null
	 * @param init The integer array to be filled
	 * @param splitNewGame The string array consisting of numbers sent by the server.
	 */
	private void createField(int[] init, String[] splitNewGame) {
		for (int i = 1; i < splitNewGame.length - 2; i++) {
			init[i - 1] = Integer.parseInt(splitNewGame[i]);
		}
	}
	
	/**
	 * To send the move to the server.
	 * @requires move1 >= 0 && 27 >= move1 && move2 >= -1 && 27 >= move2
	 * @param move1 The first move
	 * @param move2 The second move
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendMove(int move1, int move2) throws ServerUnavailableException {
		if (this.inGame && this.turnToPlay) {
			if (!validMoveType(move1, move2)) {
				return;
			}
			
			if (move2 == -1 && 
					((HumanPlayer) (this.thisPlayer)).checkSingleMoveLegality(move1, 
																			  this.theBoard)) {
				this.thisPlayer.makeSingleMove(move1, this.theBoard);
				view.showMessage("\nGame progress:\n" + this.theBoard.toString());
				view.showMessage("Waiting for opponent's move...");
				this.lastPlayedMove = ProtocolMessages.MOVE
									  + ProtocolMessages.DELIMITER
									  + move1;
				this.turnToPlay = false;
				sendMessage(this.lastPlayedMove);
				return;
			}
			
			if (move2 != -1 &&
					((HumanPlayer) (this.thisPlayer)).checkDoubleMoveLegality(move1, move2, 
																		  this.theBoard)) {
				this.thisPlayer.makeDoubleMove(move1, move2, this.theBoard);
				view.showMessage("\nGame progress:\n" + this.theBoard.toString());
				view.showMessage("Waiting for opponent's move...");
				this.lastPlayedMove = ProtocolMessages.MOVE
									  + ProtocolMessages.DELIMITER
									  + move1
									  + ProtocolMessages.DELIMITER
									  + move2;
				this.turnToPlay = false;
				sendMessage(this.lastPlayedMove);
				return;
			}
			
			view.showMessage("Not a legal move!");
		} else {
			if (this.inGame) {
				view.showMessage("Not your turn to play!");
				return;
			}
			view.showMessage("You are not in a game!");
		}
	}
	
	/**
	 * A helper method for sending a move that makes the first checks.
	 * @param move1 The first move
	 * @param move2 The second move
	 * @return true if the move passes method's checks, false otherwise.
	 */
	public boolean validMoveType(int move1, int move2) {
		if (this.theBoard.gameOver()) {
			view.showMessage("The game is over!");
			return false;
		}
		
		if (this.theBoard.possibleSingleMove(this.theBoard.getFields()) && move2 != -1) {
			view.showMessage("Please enter a single move!");
			return false;
		}
		if (!this.theBoard.possibleSingleMove(this.theBoard.getFields()) &&
				this.theBoard.possibleDoubleMove(this.theBoard.getFields()) && 
				move2 == -1) {
			view.showMessage("Please enter a double move!");
			return false;
		}
		
		return true;
	}
	
	/**
	 * To send an AI move automatically.
	 * Invoked after handleMove, and handleNewGame (if the AI is first to play).
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void sendAiMove() throws ServerUnavailableException {
		if (this.theBoard.gameOver()) {
			return;
		}
		
		int[] moves = ((ComputerPlayer) this.thisPlayer)
				.determineMove(this.theBoard);
		
		if (moves[1] == -1) { 					
			this.thisPlayer.makeSingleMove(moves[0], this.theBoard);
			this.turnToPlay = false;
			
			this.lastPlayedMove = ProtocolMessages.MOVE + ProtocolMessages.DELIMITER 
				       + moves[0];
			sendMessage(this.lastPlayedMove);	
		} else {					
			this.thisPlayer.makeDoubleMove(moves[0], moves[1], this.theBoard);
			this.turnToPlay = false;
			
			this.lastPlayedMove = ProtocolMessages.MOVE + ProtocolMessages.DELIMITER 
					+ moves[0] + ProtocolMessages.DELIMITER 
					+ moves[1];
			sendMessage(this.lastPlayedMove);			
		}
		
		if (moves[1] == -1) {
			view.showMessage("AI chose move: " + moves[0]);
		} else {
			view.showMessage("AI chose move: " + moves[0] + ", and then " + moves[1]);
		}
		
		view.showMessage("\nGame progress:\n" + this.theBoard.toString());
		view.showMessage("Waiting for opponent's move...");
	}
	
	/**
	 * To process move messages coming from the server.
	 * The client also checks the moves legality in this method,
	 * and sends appropriate error messages in case the server sent an illegal move.
	 * The method also handles confirmation moves through a flag called lastPlayedMove.
	 * @requires move1 >= 0 && 27 >= move1 && move2 >= -1 && 27 >= move2
	 * @param move1 The first move
	 * @param move2 The second move
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void handleMove(int move1, int move2) throws ServerUnavailableException {
		String protocolMove = (move2 == -1) ? 
							  ProtocolMessages.MOVE + ProtocolMessages.DELIMITER
							  + move1 : 
							  ProtocolMessages.MOVE + ProtocolMessages.DELIMITER
							  + move1 + ProtocolMessages.DELIMITER + move2;
		
		if (protocolMove.equals(this.lastPlayedMove)) {
			this.lastPlayedMove = "";
			return;
		}
		
		if (this.theBoard.gameOver()) {
			sendMessage("ERROR~The game is over!");
			return;
		}
		if (this.theBoard.possibleSingleMove(this.theBoard.getFields()) && move2 != -1) {
			sendMessage("ERROR~Please enter a single move!");
			return;
		}
		if (!this.theBoard.possibleSingleMove(this.theBoard.getFields()) &&
				this.theBoard.possibleDoubleMove(this.theBoard.getFields()) && 
				move2 == -1) {
			sendMessage("ERROR~Please enter a double move!");
			return;
		}
		
		if (move2 == -1 && 
				((HumanPlayer) (this.opponentPlayer)).checkSingleMoveLegality(move1, 
																		  this.theBoard)) {
			this.opponentPlayer.makeSingleMove(move1, this.theBoard);
			view.showMessage("Opponent move: " + move1);
			view.showMessage("\nGame progress:\n" + this.theBoard.toString());
			this.turnToPlay = true;
			
			if (this.thisPlayer instanceof ComputerPlayer) {
				view.showMessage("AI deciding on move: ");
				sendAiMove();
			} else {
				view.showMessage("To make a move: MOVE~<first move>~[second move]");
				view.showMessage("If you want a hint: HINT");
				view.showMessage("Your move: ");
			}
			return;
		}
			
		if (move2 != -1 &&
				((HumanPlayer) (this.opponentPlayer)).checkDoubleMoveLegality(move1, move2, 
																		  this.theBoard)) {
			this.opponentPlayer.makeDoubleMove(move1, move2, this.theBoard);
			view.showMessage("Opponent move: " + move1 + ", " + move2);
			view.showMessage("\nGame progress:\n" + this.theBoard.toString());
			this.turnToPlay = true;
			
			if (this.thisPlayer instanceof ComputerPlayer) {
				view.showMessage("AI deciding on move: ");
				sendAiMove();
			} else {
				view.showMessage("To make a move: MOVE~<first move>~[second move]");
				view.showMessage("If you want a hint: HINT");
				view.showMessage("Your move: ");
			}
			return;
		}
			
		sendMessage("ERROR~Not a legal move!");
	}
	
	/**
	 * To give a hint to the client who is struggling to find a legal move.
	 */
	public void doHint() {
		if (this.inGame && this.turnToPlay) {
			ComputerPlayer check = new ComputerPlayer(new NaiveStrategy());
			int[] moves = check.determineMove(this.theBoard);
			
			if (moves[1] == -1) {
				view.showMessage("Hint: " + moves[0]);
			} else {
				view.showMessage("Hint: " + moves[0] + ProtocolMessages.DELIMITER + moves[1]);
			}
		} else {
			if (!this.inGame) {
				view.showMessage("You are not in game.");
				return;
			}
			
			view.showMessage("Not your turn to play!");
		}
	}
	
	/**
	 * To process the game over message from the server.
	 * @requires msg != null
	 * @param msg The game over message
	 * @throws ServerUnavailableException In case the server is not available.
	 */
	public void handleGameOver(String msg) throws ServerUnavailableException {
		if (!msg.contains("DISCONNECT") && !this.theBoard.gameOver()) {
			sendMessage("ERROR~Game is not over!");
			view.showMessage("The server finished the game prematurally!");
			this.inGame = false;
			this.turnToPlay = false;
			setQueued(false);
			return;
		}
		
		String[] gameOver = msg.split(ProtocolMessages.DELIMITER);
		String winner = getWinner();
		
		try {
			if (gameOver.length == 3) {
				if (gameOver[2].equals(winner) || msg.contains("DISCONNECT")) {
					view.showMessage("Game over! Winner: " + gameOver[2] 
						+ ". Reason: " + gameOver[1] + ".");
				} else if (winner != null) {
					view.showMessage("The server evaluated the winner incorrectly.");
					view.showMessage("The actual result of the game: ");
					view.showMessage("Game over! Winner: " + winner 
							+ ". Reason: VICTORY.");
				} else {
					view.showMessage("The server evaluated the winner incorrectly.");
					view.showMessage("The actual result of the game: ");
					view.showMessage("Game Over! Reason: draw.");
				}
				presentPoints();
			} else {
				if (winner == null) {
					view.showMessage("Game Over! Reason: draw.");
				} else {
					view.showMessage("The server evaluated the winner incorrectly.");
					view.showMessage("The actual result of the game: ");
					view.showMessage("Game over! Winner: " + winner 
							+ ". Reason: VICTORY.");
					presentPoints();
				}
			}
			view.showMessage("To queue again, for a new game: QUEUE");
		} catch (IndexOutOfBoundsException e) {
			sendMessage("ERROR~Protocol violation in GAMEOVER message!");
		}
		
		this.inGame = false;
		this.turnToPlay = false;
		setQueued(false);
	}
	
	/**
	 * Used to decide the local winner of the game.
	 * @return the winner of the locally kept version of the game.
	 */
	public String getWinner() {
		if (this.thisPlayer.evaluatePoints() > this.opponentPlayer.evaluatePoints()) {
			return this.userName;
		} else if (this.thisPlayer.evaluatePoints() < this.opponentPlayer.evaluatePoints()) {
			return this.opponentPlayer.getName();
		} else {
			if (this.thisPlayer.numOfBalls() > this.opponentPlayer.numOfBalls()) {
				return this.userName;
			} else if (this.thisPlayer.numOfBalls() < this.opponentPlayer.numOfBalls()) {
				return this.opponentPlayer.getName();
			} else {
				return null;
			}
		}
	}
	
	/**
	 * A method to present the point and ball count of the two competitors.
	 */
	public void presentPoints() {
		view.showMessage(this.userName + " points: "
				+ this.thisPlayer.evaluatePoints()
				+ ", ball count: " + this.thisPlayer.numOfBalls());
		view.showMessage(this.opponentPlayer.getName() + " points: " 
				+ this.opponentPlayer.evaluatePoints()
				+  ", ball count: " + this.opponentPlayer.numOfBalls());
	}
	
	
	//  Getter and setter for the flag queued.
	public boolean isQueued() {
		return queued;
	}
	
	public synchronized void setQueued(boolean queued) {
		this.queued = queued;
	}
	
	// Getter and setter for the flag registeredUsername.
	public boolean isRegisteredUsername() {
		return registeredUsername;
	}

	public void setRegisteredUsername(boolean registeredUsername) {
		this.registeredUsername = registeredUsername;
	}
	
	//Getter for the board.
	public Board getBoard() {
		return this.theBoard;
	}
	
	//Getter for the players.
	public Player getThisPlayer() {
		return this.thisPlayer;
	}
	
	public Player getOpponentPlayer() {
		return this.opponentPlayer;
	}

	public static void main(String[] args) {
		ThreadedCollectoClient cc = new ThreadedCollectoClient();
		new Thread(cc).start();
		cc.start();
	}
}
