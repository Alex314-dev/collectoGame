package ss.project.server;

import ss.project.gamelogic.Board;
import ss.project.players.HumanPlayer;
import ss.project.protocol.*;

/**
 * 
 * An encapsulation and abstraction of a game environment.
 * Thus, also a model class.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class BoardGame {
	// The board for the main game logic.
	private Board theBoard;
	
	// The first player and its associated client handler.
	private HumanPlayer player1;
	private CollectoClientHandler cch1;
	
	// The second player and its associated client handler.
	private HumanPlayer player2;
	private CollectoClientHandler cch2;
	
	/**
	 * Setting up the board, players, and the client handlers.
	 * @param cch1 The client handler
	 * @param cch2 The other client handler
	 */
	public BoardGame(CollectoClientHandler cch1, CollectoClientHandler cch2) {
		this.theBoard = new Board();
		this.player1 = new HumanPlayer(cch1.getName());
		this.player2 = new HumanPlayer(cch2.getName());
		this.cch1 = cch1;
		this.cch2 = cch2;
	}
	
	/**
	 * A method to return the game over message.
	 * @return A protocol appropriate game over message
	 */
	public String printResult() {
    	if (getPlayer1().evaluatePoints() > getPlayer2().evaluatePoints()) {
    		return ProtocolMessages.GAMEOVER + ProtocolMessages.DELIMITER + "VICTORY" 
    			 + ProtocolMessages.DELIMITER + getPlayer1().getName();
    	} else if (getPlayer2().evaluatePoints() > getPlayer1().evaluatePoints()) {
    		return ProtocolMessages.GAMEOVER + ProtocolMessages.DELIMITER + "VICTORY" 
       			 + ProtocolMessages.DELIMITER + getPlayer2().getName();
    	} else if (getPlayer1().numOfBalls() > getPlayer2().numOfBalls()) {
    		return ProtocolMessages.GAMEOVER + ProtocolMessages.DELIMITER + "VICTORY" 
       			 + ProtocolMessages.DELIMITER + getPlayer1().getName();
    	} else if (getPlayer2().numOfBalls() > getPlayer1().numOfBalls()) {
    		return ProtocolMessages.GAMEOVER + ProtocolMessages.DELIMITER + "VICTORY" 
       			 + ProtocolMessages.DELIMITER + getPlayer2().getName();
    	} else {
    		return ProtocolMessages.GAMEOVER + ProtocolMessages.DELIMITER + "DRAW";
    	}
    }
	
	// Getter and setter for the board.
	
	public Board getTheBoard() {
		return theBoard;
	}
	
	public void setTheBoard(Board theBoard) {
		this.theBoard = theBoard;
	}
	
	// Getter and setter for player1.
	
	public HumanPlayer getPlayer1() {
		return player1;
	}
	
	public void setPlayer1(HumanPlayer player1) {
		this.player1 = player1;
	}
	
	// Getter and setter for player2.
	
	public HumanPlayer getPlayer2() {
		return player2;
	}
	
	public void setPlayer2(HumanPlayer player2) {
		this.player2 = player2;
	}
	
	// Getters and setters for the client handlers.
	
	public CollectoClientHandler getCch1() {
		return cch1;
	}

	public void setCch1(CollectoClientHandler cch1) {
		this.cch1 = cch1;
	}

	public CollectoClientHandler getCch2() {
		return cch2;
	}

	public void setCch2(CollectoClientHandler cch2) {
		this.cch2 = cch2;
	}
	
	// A method to return the appropriate integer representation of the board.
	
	public String newGameString() {
		return theBoard.newGameString();
	}
}
