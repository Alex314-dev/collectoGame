package ss.project.strategy;

import java.util.ArrayList;
import java.util.List;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;

/**
 * 
 * A strategy interface for reusability and for defining the core methods for a strategy.
 * A model class as it implements the AI strategy.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public interface Strategy {
	
	// A getter for the strategy name.
	String getName();
	
	/**
	 * The method for determining the move of the AI.
	 * @requires board != null
	 * @ensures \result[0] &gt;= 0 &amp;&amp;
	 *  27 &gt;= \result[0] &amp;&amp; \result[1] &gt;= -1 &amp;&amp; 27 &gt;= \result[1]
	 * @param board The board onto which the move will be determined.
	 * @return An integer array where the first value is the first move to make,
	 * 			and the second value (if not -1) is the second move to make.
	 */
	int[] determineMove(Board board);
	
	/**
	 * A method to do the move onto the given field.
	 * @requires move &gt;= 0 &amp;&amp; 27 &gt;= move 
	 * &amp;&amp; field != null &amp;&amp; board != null
	 * @param move The move to be made.
	 * @param field The field onto which the move will be made
	 * @param board A board object to access the move methods.
	 */
	default void doMove(int move, Ball[][] field, Board board) {
		if (move >= 0 && move <= 6) {
			board.moveLeft(move, field);
		} else if (move >= 7 && move <= 13) {
			board.moveRight(move - 7, field);
		} else if (move >= 14 && move <= 20) {
			board.moveUp(move - 14, field);
		} else if (move >= 21 && move <= 27) {
			board.moveDown(move - 21, field);
		}
	}
	
	/**
	 * Returns all legal single moves for the given field.
	 * @requires current != null &amp;&amp; board != null
	 * @ensures All elements of the resulting list contain legal moves.
	 * @param current The given ball field.
	 * @param board A board class to access important board methods.
	 * @return A list of all possible single moves.
	 */
	default public List<Integer> allPossibleSingleMoves(Ball[][] current, Board board) {
		Ball[][] copy;
		List<Integer> result = new ArrayList<>();
		
		for (int row = 0; row < 7; row++) {
			copy = board.fieldDeepCopy(current);
			board.moveRight(row, copy);
			if (!board.getAdjacent(copy).isEmpty()) {
				result.add(row + 7);
			}
			
			copy = board.fieldDeepCopy(current);
			board.moveLeft(row, copy);
			if (!board.getAdjacent(copy).isEmpty()) {
				result.add(row);
			}
		}
		
		for (int col = 0; col < 7; col++) {
			copy = board.fieldDeepCopy(current);
			board.moveUp(col, copy);
			if (!board.getAdjacent(copy).isEmpty()) {
				result.add(col + 14);
			}
			
			copy = board.fieldDeepCopy(current);
			board.moveDown(col, copy);
			if (!board.getAdjacent(copy).isEmpty()) {
				result.add(col + 21);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns all legal double moves for the given field.
	 * @requires current != null &amp;&amp; board != null
	 * @ensures All elements of the resulting list contain legal moves.
	 * @param current The given ball field.
	 * @param board A board class to access important board methods.
	 * @return A list of all possible double moves.
	 */
	default public List<int[]> allPossibleDoubleMoves(Ball[][] current, Board board) {
		Ball[][] copy;
		List<int[]> result = new ArrayList<>();
		
		if (!allPossibleSingleMoves(current, board).isEmpty()) {
			return result;
		}
		
		List<Integer> singleMoves = new ArrayList<>();
		for (int row = 0; row < 7; row++) {
			copy = board.fieldDeepCopy(current);
			board.moveRight(row, copy);
			singleMoves = allPossibleSingleMoves(copy, board);
			if (!singleMoves.isEmpty()) {
				for (int sm : singleMoves) {
					int[] temp = {row + 7, sm};
					result.add(temp);
				}
			}
			
			copy = board.fieldDeepCopy(current);
			board.moveLeft(row, copy);
			singleMoves = allPossibleSingleMoves(copy, board);
			if (!singleMoves.isEmpty()) {
				for (int sm : singleMoves) {
					int[] temp = {row, sm};
					result.add(temp);
				}
			}
		}
		
		for (int col = 0; col < 7; col++) {
			copy = board.fieldDeepCopy(current);
			board.moveUp(col, copy);
			singleMoves = allPossibleSingleMoves(copy, board);
			if (!singleMoves.isEmpty()) {
				for (int sm : singleMoves) {
					int[] temp = {col + 14, sm};
					result.add(temp);
				}
			}
			
			copy = board.fieldDeepCopy(current);
			board.moveDown(col, copy);
			singleMoves = allPossibleSingleMoves(copy, board);
			if (!singleMoves.isEmpty()) {
				for (int sm : singleMoves) {
					int[] temp = {col + 21, sm};
					result.add(temp);
				}
			}
		}
		
		return result;
	}
}
