package ss.project.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ss.project.exceptions.*;
import ss.project.protocol.ProtocolMessages;

/**
 * 
 * A server for Collecto. Accepting connections, creating games,
 * handling queues, and other non-game commands. A model class as
 * it stores the connected clients, logged in ones, and also queued ones.
 * And also because it is a part of the network communication.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class CollectoServer implements Runnable {
	//The server socket and description of the server.
	private ServerSocket ssock;
	private String description = "Alex and Kagan's Beatiful Server";

	/** List of connected CollectoClientHandlers. */
	private List<CollectoClientHandler> players;
	
	/** List of logged in CollectoClientHandlers (their names). */
	private List<String> loggedIn;
	
	/** List of queued CollectoClientHandlers. */
	private List<CollectoClientHandler> inQueue;
	
	/** Next client number, increasing for every new connection. */
	private int nextPlayerNo;
	
	/** The view of this CollectoServer. */
	private CollectoServerTUI view;
	
	/**
	 * Constructs a new server. Initializing the view.
	 */
	public CollectoServer() {
		this.players = new ArrayList<>();
		this.loggedIn = new ArrayList<>();
		this.inQueue = new ArrayList<>();
		this.view = new CollectoServerTUI();
		this.nextPlayerNo = 1;
	}
	
	/**
	 * Setting up the server socket. Continuously listens for new connections and creates
	 * new CollectoClientHandlers for the associated connections.
	 */
	@Override
	public void run() {
		boolean openNewSocket = true;
		while (openNewSocket) {
			try {
				// Sets up the server socket.
				setup();

				while (ssock != null) {
					Socket sock = ssock.accept();
					String name = "Client " 
							+ String.format("%02d", nextPlayerNo++);
					view.showMessage("New player [" + name + "] connected!");
					CollectoClientHandler handler = 
							new CollectoClientHandler(sock, this, name, view);
					new Thread(handler).start();
					players.add(handler);
				}
				openNewSocket = false;
			} catch (ExitProgram e1) {
				// If setup() throws an ExitProgram exception, 
				// stop the program.
				openNewSocket = false;
			} catch (IOException e) {
				System.out.println("A server IO error occurred: " 
						+ e.getMessage());

				if (!view.getBoolean("Do you want to open a new socket?")) {
					openNewSocket = false;
				}
			}
		}
		view.showMessage("See you later!");
	}
	
	/**
	 * A method for starting the server.
	 * @throws ExitProgram 
	 */
	public void setup() throws ExitProgram {
		ssock = null;
		while (ssock == null) {
			int port = view.getPort();
			// try to open a new ServerSocket
			try {
				view.showMessage("Attempting to open a socket on port " + port + "...");
				ssock = new ServerSocket(port);
				view.showMessage("Server started at port " + port);
			} catch (IOException e) {
				view.showMessage("ERROR: could not create a socket on port " + port + ".");

				if (!view.getBoolean("Do you want to try again? ")) {
					view.showMessage("User indicated that does not want to try again.");
					view.showMessage("Closing server.");
					break;
				}
			}
		}
	}
	
	/**
	 * Removes a client from the list of players.
	 * @ensures The client will be removed from the players list
	 * @requires client != null
	 * @param The client that needs to be removed
	 */
	public synchronized void removeClient(CollectoClientHandler client) {
		this.players.remove(client);
	}
	
	/**
	 * Handling the server side of the initialisation according to the protocol.
	 * @ensures Correct initialisation sequence will be done
	 * @param The description given by the client
	 * @return The correct protocol message for the initialisation
	 */
	public synchronized String doHello(String clientDescription) {
		this.view.showMessage("> Handshake with " + clientDescription);
		return ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + this.description;
	}
	
	/**
	 * Handling the server side of the log in according to the protocol.
	 * @requires cch != null && username != null
	 * @ensures Correct log in message will be returned
	 * @param The username provided by the new client
	 * @param A client handler that handles the new client
	 * @return The correct protocol message for the log in
	 */
	public synchronized String doLogIn(String username, CollectoClientHandler cch) {
		if (this.loggedIn.contains(username)) {
			return ProtocolMessages.ALREADYLOGGEDIN;
		} else {
			this.loggedIn.add(username);
			cch.setName(username);
			cch.setLoggedIn(true);
			return ProtocolMessages.LOGIN;
		}
	}
	
	/**
	 * Handling the server side of the list command according to the protocol.
	 * @ensures All usernames in the loggedIn list will be shown to the user
	 * @return The list of logged in users
	 */
	public synchronized String doList() {
		String result = "";
		for (String username : this.loggedIn) {
			result += ProtocolMessages.DELIMITER + username;
		}
		return ProtocolMessages.LIST + result;
	}
	
	/**
	 * Adds or removes a CollectoClientHandler from the queue depending on his state inQueue.
	 * @requires cch != null
	 * @ensures cch will be added or removed from the queue accordingly
	 * @param The CollectoClientHandler that needs to be queued or removed from the queue
	 * @return true if the client handler is not in a game, is initialised and is logged in
	 * 		   false otherwise
	 */
	public synchronized void doQueue(CollectoClientHandler cch) {
		if (this.inQueue.contains(cch)) {
			this.inQueue.remove(cch);
		} else {
			this.inQueue.add(cch);
			if (this.inQueue.size() == 2) {
				doGame(this.inQueue.get(0), this.inQueue.get(1));
				this.inQueue.remove(0);
				this.inQueue.remove(0);
			}
		}
	}
	
	/**
	 * Handling the creation of a new game and sending the corresponding 
	 * number field representation of the game with its players to 
	 * the appropriate clients that play the game.
	 * @requires cch1 != null && cch2 != null
	 * @ensures A new game will be constructed with two players and will be sent to the
	 * 			clients according to the protocol
	 * @param cch1, the CollectoClientHandler that corresponds to the first player of the new game
	 * @param cch2, the CollectoClientHandler that corresponds to the second player of the new game
	 */
	public synchronized void doGame(CollectoClientHandler cch1, CollectoClientHandler cch2) {
		BoardGame boardGame = new BoardGame(cch1, cch2);
		cch1.setBoardGame(boardGame);
		cch2.setBoardGame(boardGame);
		String newGame;
		if (new Random().nextBoolean()) {
			newGame = ProtocolMessages.NEWGAME + boardGame.newGameString()
				+ ProtocolMessages.DELIMITER
				+ boardGame.getPlayer1().getName()
				+ ProtocolMessages.DELIMITER + boardGame.getPlayer2().getName();
			cch1.setTurnToPlay(true);
			cch2.setTurnToPlay(false);
		} else {
			newGame = ProtocolMessages.NEWGAME + boardGame.newGameString()
				+ ProtocolMessages.DELIMITER
				+ boardGame.getPlayer2().getName()
				+ ProtocolMessages.DELIMITER + boardGame.getPlayer1().getName();
			cch1.setTurnToPlay(false);
			cch2.setTurnToPlay(true);
		}
		cch1.setInGame(true);
		cch2.setInGame(true);
		synchronized (boardGame) {
			cch1.sendMessage(newGame);
			cch2.sendMessage(newGame);
		}
	}
	
	// Getter for the queue.
	public synchronized List<CollectoClientHandler> getQueue() {
		return this.inQueue;
	}
	
	/**
	 * Used by the client handler to remove a 
	 * client handler from the queue in a thread safe way.
	 * @ensures this.inQueue.contains(cch) == false
	 * @param cch The client handler to be removed.
	 */
	public synchronized void removeFromQueue(CollectoClientHandler cch) {
		this.inQueue.remove(cch);
	}
	
	// Getter for the loggedIn list.
	public synchronized List<String> getLoggedInList() {
		return this.loggedIn;
	}
	
	/**
	 * Used by the client handler to remove a 
	 * client handler from the logged in list in a thread safe way.
	 * @ensures this.loggeIn.contains(cch) == false
	 * @param cch The client handler to be removed.
	 */
	public synchronized void removeFromLoggedInList(CollectoClientHandler cch) {
		this.loggedIn.remove(cch.getName());
	}
	
	// Getter and setter for the connected client handlers.
	public synchronized List<CollectoClientHandler> getPlayers() {
		return this.players;
	}
	
	public static void main(String[] args) {
		(new CollectoServer()).run();
	}
}
