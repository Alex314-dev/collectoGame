package ss.project.players;

import java.util.ArrayList;
import java.util.List;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;

/**
 * 
 * An abstract Player class for reusability. A model class as it
 * models all players.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public abstract class Player {
	// The name of the player.
	private String name;
		
	// The total acquired balls after every move.
	private List<Ball> acquiredBalls;

	/**
	 * Setting up the name and the acquired balls list.
	 * @requires name != null
	 * @param name The name of the player.
	 */
	public Player(String name) {
		this.name = name;
		acquiredBalls = new ArrayList<>();
	}
	
	// Getter for the name
	public String getName() {
		return this.name;
	}
	
	/**
	 * Add all of the given balls to the acquiredBalls.
	 * @param add The balls to be added to the acquiredBalls
	 */
	public void addBalls(List<Ball> add) {	
		this.acquiredBalls.addAll(add);
	}
	
	// Getter for the balls.
	public List<Ball> getBalls() {
		return this.acquiredBalls;
	}
	
	/**
	 * The method for determining the move of the player.
	 * @requires board != null
	 * @ensures \result[0] >= 0 && 27 >= \result[0] && \result[1] >= -1 && 27 >= \result[1]
	 * @param board The board onto which the move will be determined.
	 * @return An integer array where the first value is the first move to make,
	 * 			and the second value (if not -1) is the second move to make.
	 */
	public abstract int[] determineMove(Board board);
	
	/**
	 * A method for converting the protocol appropriate move number
	 * to an actual move for the given board.
	 * @requires board != null
	 * @param move The protocol appropriate move number
	 * @param board The board onto which the move will be played
	 */
	public void makeSingleMove(int move, Board board) {
		if (move >= 0 && move <= 6) {
			board.moveLeft(move, board.getFields());
		} else if (move >= 7 && move <= 13) {
			board.moveRight(move - 7, board.getFields());
		} else if (move >= 14 && move <= 20) {
			board.moveUp(move - 14, board.getFields());
		} else {
			board.moveDown(move - 21, board.getFields());
		}
		
		
		this.addBalls(board.handleAdjacency(board.getFields()));
	}
	
	/**
	 * A method for converting the protocol appropriate move numbers
	 * to an actual double for the fiven board.
	 * @requires board != null
	 * @param move1 The first move
	 * @param move2 The second move
	 * @param board The board onto which the move will be played
	 */
	public void makeDoubleMove(int move1, int move2, Board board) {
		if (move1 >= 0 && move1 <= 6) {
			board.moveLeft(move1, board.getFields());
		} else if (move1 >= 7 && move1 <= 13) {
			board.moveRight(move1 - 7, board.getFields());
		} else if (move1 >= 14 && move1 <= 20) {
			board.moveUp(move1 - 14, board.getFields());
		} else {
			board.moveDown(move1 - 21, board.getFields());
		}
		
		if (move2 >= 0 && move2 <= 6) {
			board.moveLeft(move2, board.getFields());
		} else if (move2 >= 7 && move2 <= 13) {
			board.moveRight(move2 - 7, board.getFields());
		} else if (move2 >= 14 && move2 <= 20) {
			board.moveUp(move2 - 14, board.getFields());
		} else {
			board.moveDown(move2 - 21, board.getFields());
		}
		
		this.addBalls(board.handleAdjacency(board.getFields()));
	}
	
	/**
	 * The method for making the move. 
	 * (Only for testing, used in the console version of the game!)
	 * @requires board != null
	 * @param board The board onto which the move will be played.
	 */
	public void makeMove(Board board) {
		int[] move = determineMove(board);
		
		if (move[1] == -1) { 
			makeSingleMove(move[0], board);
		} else {
			makeDoubleMove(move[0], move[1], board);
		}
	}
	
	/**
	 * A method for evaluating the points of the player according to the Collecto rules.
	 * @return The points.
	 */
	public int evaluatePoints() {
		int blueCount = 0;
		int yellowCount = 0;
		int redCount = 0;
		int orangeCount = 0;
		int purpleCount = 0;
		int greenCount = 0;
		
		for (Ball ball : acquiredBalls) {
			if (ball == Ball.BLUE) {
				blueCount++;
			} else if (ball == Ball.YELLOW) {
				yellowCount++;
			} else if (ball == Ball.RED) {
				redCount++;
			} else if (ball == Ball.ORANGE) {
				orangeCount++;
			} else if (ball == Ball.PURPLE) {
				purpleCount++;
			} else {
				greenCount++;
			}
		}
		
		return (blueCount / 3) +
			   (yellowCount / 3) +
			   (redCount / 3) +
			   (orangeCount / 3) +
			   (purpleCount / 3) +
			   (greenCount / 3);
	}
	
	/**
	 * A method for returning the size of the acquired balls.
	 * @return acquiredBalls.size()
	 */
	public int numOfBalls() {
		return this.acquiredBalls.size();
	}
}
