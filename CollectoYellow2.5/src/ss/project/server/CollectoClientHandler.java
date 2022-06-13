package ss.project.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import ss.project.protocol.ProtocolMessages;

/**
 * 
 * A client handler to communicate with the client, also stores its own
 * BoardGame. A model class as it stores information about the client
 * and its current game situation. It can be argued that the client handler
 * is also doing a partial controller job, as it interprets commands coming
 * from the user.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class CollectoClientHandler implements Runnable {
	/** The socket and In- and OutputStreams. */
	private BufferedReader in;
	private PrintWriter out;
	private Socket sock;
	
	/** The connected CollectoServer. */
	private CollectoServer srv;
	
	/** The view for the server. */
	private CollectoServerTUI view;

	/** Name of this ClientHandler. */
	private String name;
	
	/** Flags. */
	private boolean initialized;
	private boolean loggedIn;
	private boolean inGame;
	private boolean turnToPlay;
	private BoardGame myGame;

	/**
	 * Constructs a new CollectoClientHandler. Opens the In- and OutputStreams.
	 * 
	 * @param sock The client socket
	 * @param srv  The connected server
	 * @param name The name of this ClientHandler
	 */
	public CollectoClientHandler(Socket sock, CollectoServer srv, String name,
			CollectoServerTUI view) {
		try {
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(
					new OutputStreamWriter(sock.getOutputStream()));
			this.sock = sock;
			this.srv = srv;
			this.name = name;
			this.view = view;
		} catch (IOException e) {
			shutdown();
		}
	}
	
	/**
	 * This is only used in the server test, JUnit test case. Not to be used
	 * anywhere else!
	 * @param name
	 */
	public CollectoClientHandler(String name) {
		this.name = name;
		this.out = new PrintWriter(System.out, true);
	}

	/**
	 * Continuously listens to client input and forwards the input to the
	 * {@link #handleCommand(String)} method.
	 */
	public void run() {
		String msg;
		try {
			msg = in.readLine();
			while (msg != null) {
				view.showMessage("> [" + this.name + "] Incoming: " + msg);
				handleCommand(msg);
				msg = in.readLine();
			}
			shutdown();
		} catch (IOException e) {
			shutdown();
		}
	}
	
	/**
	 * A method to send the MOVE messages to the clients.
	 * (sometimes used to send ERROR messages as well)
	 * @requires newMove != null
	 * @param newMove The move message
	 */
	public void sendMessage(String str) {
		out.println(str);
		out.flush();
		view.showMessage("> [" + this.name + "] Outgoing: " + str);
	}
	
	/**
	 * To process and handle the commands from the client.
	 * @requires msg != null
	 * @param msg The client message.
	 */
	private void handleCommand(String msg) {
		try (Scanner sc = new Scanner(msg)) {
			sc.useDelimiter(ProtocolMessages.DELIMITER);
			switch (sc.next()) {
				case ProtocolMessages.HELLO:
					if (!this.initialized) {
						out.println(srv.doHello(sc.next()));
						out.flush();
						this.initialized = true;
					} else {
						out.println("ERROR~Untimely hello!");
						out.flush();
					}
					break;
				case ProtocolMessages.LOGIN:
					if (this.initialized && !this.loggedIn) {
						out.println(srv.doLogIn(sc.next(), this));
						out.flush();
					} else {
						out.println("ERROR~Untimely login!");
						out.flush();
					}
					break;
				case ProtocolMessages.QUEUE:
					if (!this.inGame && this.initialized && this.loggedIn) {
						srv.doQueue(this);
					} else {
						out.println("ERROR~Untimely queue!");
						out.flush();
					}
					break;
				case ProtocolMessages.LIST:
					if (this.initialized && this.loggedIn) {
						out.println(srv.doList());
						out.flush();
					} else {
						out.println("ERROR~Untimely list!");
						out.flush();
					}
					break;
				case ProtocolMessages.MOVE:
					int move1 = Integer.parseInt(sc.next());
					int move2 = -1;
					if (sc.hasNext()) {
						move2 = Integer.parseInt(sc.next());
					}
					doMove(move1, move2);
					break;
				default:
					out.println("ERROR~Unknown command!");
					out.flush();
					break;
			}
		} catch (NoSuchElementException e) {
			out.println("ERROR");
			out.flush();
		} catch (NumberFormatException e) {
			out.println("ERROR");
			out.flush();
		}
	}

	/**
	 * The method to process the moves sent by the client.
	 * Synchronized over the game that is played.
	 * The moves are always checked for legality.
	 * @requires move1 >= 0 && 27 >= move1 && move2 >= -1 && 27 >= move2
	 * @param move1 The first move
	 * @param move2 The second move
	 */
	public void doMove(int move1, int move2) {
		synchronized (this.myGame) {
			if (!(this.inGame && this.turnToPlay)) {
				this.sendMessage("ERROR");
				return;
			}
			
			if (getBoardGame().getTheBoard()
							  .possibleSingleMove(getBoardGame().getTheBoard().getFields())
				&& move2 != -1) {
				this.sendMessage("ERROR~Single move expected!");
				return;
			}
			
			if (!getBoardGame().getTheBoard()
							   .possibleSingleMove(getBoardGame().getTheBoard().getFields()) 
				&& getBoardGame().getTheBoard()
								 .possibleDoubleMove(getBoardGame().getTheBoard().getFields()) 
				&& move2 == -1) {
				this.sendMessage("ERROR~Double move expected");
				return;
			}
			
			boolean singleMove = move2 == -1;
			CollectoClientHandler opponent;
			if (this.getName().equals(getBoardGame().getPlayer1().getName())) {
				if (singleMove && 
					getBoardGame().getPlayer1()
								  .checkSingleMoveLegality(move1,
										  				   getBoardGame().getTheBoard())) {
					getBoardGame().getPlayer1()
								  .makeSingleMove(move1, getBoardGame().getTheBoard());
				} else if (!singleMove &&
							getBoardGame().getPlayer1()
										  .checkDoubleMoveLegality(move1, move2, 
								   								   getBoardGame().getTheBoard())) {
					getBoardGame().getPlayer1()
								  .makeDoubleMove(move1, move2, getBoardGame().getTheBoard());
				} else {
					this.sendMessage("ERROR~Illegal move!");
					return;
				}
				opponent = getBoardGame().getCch2();
			} else {
				if (singleMove && 
					getBoardGame().getPlayer2()
								  .checkSingleMoveLegality(move1, 
										  				   getBoardGame().getTheBoard())) {
					getBoardGame().getPlayer2()
								  .makeSingleMove(move1, getBoardGame().getTheBoard());
				} else if (!singleMove &&
							getBoardGame().getPlayer2()
										  .checkDoubleMoveLegality(move1, move2, 
								   								   getBoardGame().getTheBoard())) {
					getBoardGame().getPlayer2()
								  .makeDoubleMove(move1, move2, getBoardGame().getTheBoard());
				} else {
					this.sendMessage("ERROR~Illegal move!");
					return;
				}
				opponent = getBoardGame().getCch1();
			}
			this.setTurnToPlay(false);
			opponent.setTurnToPlay(true);
			
			String movePlayed = singleMove ? (ProtocolMessages.MOVE 
											  + ProtocolMessages.DELIMITER + move1)
										   : (ProtocolMessages.MOVE 
											  + ProtocolMessages.DELIMITER + move1
											  + ProtocolMessages.DELIMITER + move2);
			this.sendMessage(movePlayed);
			opponent.sendMessage(movePlayed);
			
			doGameOver();
		}
	}
	
	/**
	 * A method to check if the game is over.
	 * If the game is over, the client handlers' inGame field will be set to false,
	 * and a game over message will be sent.
	 */
	public void doGameOver() {
		synchronized (this.myGame) {
			if (this.inGame && getBoardGame().getTheBoard().gameOver()) {
				String result = getBoardGame().printResult();
				getBoardGame().getCch1().setInGame(false);
				getBoardGame().getCch1().setTurnToPlay(false);
				getBoardGame().getCch2().setInGame(false);
				getBoardGame().getCch2().setTurnToPlay(false);
				getBoardGame().getCch1().sendMessage(result);
				getBoardGame().getCch2().sendMessage(result);
			}
		}
	}
	
	/**
	 * A method to send the game over message in case of a disconnection.
	 */
	public void doGameOverDisconnect() {
		synchronized (this.myGame) {
			if (this.inGame) {
				getBoardGame().getCch1().setInGame(false);
				getBoardGame().getCch1().setTurnToPlay(false);
				getBoardGame().getCch2().setInGame(false);
				getBoardGame().getCch2().setTurnToPlay(false);
				if (this.getName().equals(getBoardGame().getCch1().getName())) {
					String result = ProtocolMessages.GAMEOVER 
								  + ProtocolMessages.DELIMITER + "DISCONNECT" 
							      + ProtocolMessages.DELIMITER + getBoardGame().getCch2().getName();
					getBoardGame().getCch2().sendMessage(result);
				} else {
					String result = ProtocolMessages.GAMEOVER 
							      + ProtocolMessages.DELIMITER + "DISCONNECT" 
							      + ProtocolMessages.DELIMITER + getBoardGame().getCch1().getName();
					getBoardGame().getCch1().sendMessage(result);
				}
			}
		}
	}
	
	// Getter and setter for the name.
	public void setName(String newName) {
		this.name = newName;
	}
	
	public String getName() {
		return this.name;
	}

	// Setter for the flag loggedIn.
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	// Setter for the flag inGame.
	public void setInGame(boolean inGame) {
		this.inGame = inGame;
	}
	
	// Setter for the flag turnToPlay.
	public void setTurnToPlay(boolean turnToPlay) {
		this.turnToPlay = turnToPlay;
	}
	
	// Getter and setter for the boardGame.
	public BoardGame getBoardGame() {
		return this.myGame;
	}
	
	public void setBoardGame(BoardGame boardGame) {
		this.myGame = boardGame;
	}
	
	/**
	 * A method which is invoked when the client shuts down the connection.
	 * The communication channels, and the socket is closed.
	 * The client handler is removed from the server client handler list.
	 * The client handler is removed from the queue.
	 * The client handler's name is removed from the logged in list.
	 * If the client handler was in a game, doGameOverDisconnect() is invoked.
	 */
	private void shutdown() {
		System.out.println("> [" + this.name + "] Shutting down.");
		try {
			in.close();
			out.close();
			sock.close();
		} catch (IOException ignored) {

		}
		srv.removeClient(this);
		srv.removeFromQueue(this);
		srv.removeFromLoggedInList(this);
		if (this.inGame) {
			doGameOverDisconnect();
		}
	}
}
