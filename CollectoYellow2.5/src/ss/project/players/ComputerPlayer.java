package ss.project.players;

import ss.project.gamelogic.Board;
import ss.project.strategy.Strategy;

/**
 * 
 * A computer player which encapsulates a strategy and plays
 * according to the strategy. A model class as it models an AI player.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class ComputerPlayer extends Player {
	// The strategy to be used by the AI
	private Strategy strategy;
	
	/**
	 * Constructing the AI with a given strategy.
	 * @requires strategy != null
	 * @param strategy The strategy which will determine the moves.
	 */
	public ComputerPlayer(Strategy strategy) {
		super(strategy.getName());
		this.strategy = strategy;
	}
	
	/**
	 * A method to determine the move of the AI via the strategy.
	 * @requires board != null
	 * @ensures \result[0] >= 0 && 27 >= \result[0] && \result[1] >= -1 && 27 >= \result[1]
	 * @param board The board onto which the move will be determined.
	 */
	@Override
	public int[] determineMove(Board board) {
		return strategy.determineMove(board);
	}
	
	// Getter and setter for the strategy.
	
	public Strategy getStrategy() {
		return strategy;
	}
	
	public void setStrategy(Strategy strat) {
		this.strategy = strat;
	}
}
