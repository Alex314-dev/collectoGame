package ss.project.strategy;

import java.util.ArrayList;
import java.util.List;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;

/**
 * 
 * A smart strategy implementing strategy interface. Simply used for determining
 * the first legal move which results in the highest ball count.
 * A model class as it implements the AI strategy.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class SmartStrategy implements Strategy {
	
	// A getter for the name.
	@Override
	public String getName() {
		return "Smart";
	}
	
	/**
	 * The method for determining the move of the AI.
	 * This method will find the legal move which acquires the most amount of balls.
	 * @requires board != null
	 * @ensures \result[0] &gt;= 0 &amp;&amp; 27 &gt;= \result[0] 
	 * &amp;&amp; \result[1] &gt;= -1 &amp;&amp; 27 &gt;= \result[1]
	 * @param board The board onto which the move will be determined.
	 * @return An integer array where the first value is the first move to make,
	 * 			and the second value (if not -1) is the second move to make.
	 */
	@Override
	public int[] determineMove(Board board) {
		List<Integer> singleMoves = new ArrayList<>(
				allPossibleSingleMoves(board.getFields(), board));
		int ballCount = -10000;
		int bestMove = 0;
		int bestMove2 = 0;
		if (singleMoves.size() != 0) {
			for (Integer integer : singleMoves) {
				Ball[][] copy = board.fieldDeepCopy(board.getFields());
				doMove(integer, copy, board);
				int tempBallCount = board.handleAdjacency(copy).size();
				if (tempBallCount > ballCount) {
					ballCount = tempBallCount;
					bestMove = integer;
				}
			}
			int[] result = {bestMove, -1};
			return result;
		} else {
			List<int[]> doubleMoves = new ArrayList<>(
					allPossibleDoubleMoves(board.getFields(), board));
			for (int[] integer : doubleMoves) {
				Ball[][] copy = board.fieldDeepCopy(board.getFields());
				doMove(integer[0], copy, board);
				doMove(integer[1], copy, board);
				int tempBallCount = board.handleAdjacency(copy).size();
				if (tempBallCount > ballCount) {
					ballCount = tempBallCount;
					bestMove = integer[0];
					bestMove2 = integer[1];
				}
			}
			int[] result = {bestMove, bestMove2};
			return result;
		}
	}
}
