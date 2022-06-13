package ss.project.strategy;

import java.util.List;

import ss.project.gamelogic.Board;

/**
 * 
 * A naive strategy implementing strategy interface. Simply used for determining
 * the first legal move it can find.
 * A model class as it implements the AI strategy.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class NaiveStrategy implements Strategy {
	
	// A getter for the name.
	@Override
	public String getName() {
		return "Naive";
	}
	
	/**
	 * The method for determining the move of the AI.
	 * This method will find the first legal move it can make and return it.
	 * @requires board != null
	 * @ensures \result[0] >= 0 && 27 >= \result[0] && \result[1] >= -1 && 27 >= \result[1]
	 * @param board The board onto which the move will be determined.
	 * @return An integer array where the first value is the first move to make,
	 * 			and the second value (if not -1) is the second move to make.
	 */
	@Override
	public int[] determineMove(Board board) {
		int[] result = new int[2];
		
		if (board.possibleSingleMove(board.getFields())) {
			List<Integer> singleMoves = allPossibleSingleMoves(board.getFields(), board);
			int randomIndex = (int) (Math.random() * singleMoves.size());
			result[0] = singleMoves.get(randomIndex);
			result[1] = -1;
		} else {
			List<int[]> doubleMoves = allPossibleDoubleMoves(board.getFields(), board);
			int randomIndex = (int) (Math.random() * doubleMoves.size());
			int[] doubleMove = doubleMoves.get(randomIndex);
			result = doubleMove;
		}
		return result;
	}
}
