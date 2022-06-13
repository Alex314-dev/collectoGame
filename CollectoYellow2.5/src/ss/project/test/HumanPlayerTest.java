package ss.project.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;
import ss.project.players.HumanPlayer;

class HumanPlayerTest {
	private HumanPlayer test;
	private final String name = "Foo";
	private Board board;
	
	@BeforeEach
	void setUp() throws Exception {
		test = new HumanPlayer(name);
	}

	@Test
	void nameTest() {
		assertTrue(test.getName().equals(name));
	}
	
	@Test
	void addAndGetBallsTest() {
		List<Ball> balls = new ArrayList<>(List.of(Ball.BLUE, Ball.YELLOW));
		test.addBalls(balls);
		assertEquals(balls, test.getBalls());
	}
	
	@Test
	void makeSingleMoveTest() {
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
					  4, 6, 3, 4, 3, 1, 2,
					  5, 3, 2, 1, 2, 6, 5,
					  4, 1, 4, 0, 4, 1, 4,
					  5, 6, 2, 1, 5, 6, 2,
					  3, 1, 5, 4, 6, 5, 3,
					  6, 3, 6, 2, 1, 2, 1};
		board = new Board(init);
		
		test.makeSingleMove(3, board);
//		      afterMove = {5, 3, 4, 2, 5, 3, 6, 
//				  	  	   4, 6, 3, 4, 3, 1, 2,
//				  	  	   5, 3, 2, 1, 2, 6, 5,
//				  	  	   4, 1, 0, 0, 1, 4, 0,
//				  	  	   5, 6, 2, 1, 5, 6, 2,
//				  	  	   3, 1, 5, 4, 6, 5, 3,
//				  	  	   6, 3, 6, 2, 1, 2, 1};
		
		assertEquals(board.convertBallToInt(board.getBall(3, 2)), 0);
		assertEquals(board.convertBallToInt(board.getBall(3, 3)), 0);
		
		List<Ball> balls = new ArrayList<>(List.of(Ball.ORANGE, Ball.ORANGE));
		assertEquals(balls, test.getBalls());
		
		int[] newBoard = {5, 0, 4, 2, 5, 3, 6, 
		  		  		  4, 6, 3, 4, 3, 1, 2,
		  		  		  5, 3, 2, 1, 2, 6, 5,
		  		  		  4, 1, 4, 0, 4, 1, 4,
		  		  		  5, 6, 2, 1, 5, 6, 2,
		  		  		  3, 1, 5, 4, 6, 5, 3,
		  		  		  6, 3, 6, 2, 1, 2, 1};
		board = new Board(newBoard);

		test.makeSingleMove(15, board);
		assertEquals(board.convertBallToInt(board.getBall(6, 1)), 0);
		assertEquals(board.convertBallToInt(board.getBall(5, 1)), 0);
		assertEquals(board.convertBallToInt(board.getBall(3, 1)), 6);
	
		test.makeSingleMove(22, board);
		assertEquals(board.convertBallToInt(board.getBall(0, 1)), 0);
		assertEquals(board.convertBallToInt(board.getBall(1, 1)), 0);
		assertEquals(board.convertBallToInt(board.getBall(2, 1)), 0);
	}
	
	@Test
	void numOfBallsTest() {
		List<Ball> balls = new ArrayList<>(List.of(Ball.BLUE, Ball.YELLOW));
		test.addBalls(balls);
		assertEquals(2, test.numOfBalls());
	}
	
	@Test
	void evaluatePointsTest() {
		List<Ball> balls = new ArrayList<>(List.of(Ball.BLUE, Ball.BLUE, Ball.BLUE, Ball.BLUE,
				Ball.YELLOW, Ball.YELLOW, Ball.YELLOW, Ball.RED, Ball.RED, Ball.RED, Ball.RED,
				Ball.ORANGE, Ball.ORANGE, Ball.ORANGE, Ball.GREEN, Ball.GREEN, Ball.GREEN,
				Ball.PURPLE, Ball.PURPLE, Ball.PURPLE));
		test.addBalls(balls);
		assertEquals(6, test.evaluatePoints());
	}
	
	@Test
	void doMoveTest() {
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
					  4, 6, 3, 4, 3, 1, 2,
					  5, 3, 2, 1, 2, 6, 5,
					  4, 1, 4, 0, 4, 1, 4,
					  5, 6, 2, 1, 5, 6, 2,
					  3, 1, 5, 4, 6, 5, 3,
					  6, 3, 6, 2, 1, 2, 1};
		board = new Board(init);
		
		test.doMove(3, board.getFields(), board);
//	      afterMove = {5, 3, 4, 2, 5, 3, 6, 
//	  	   			   4, 6, 3, 4, 3, 1, 2,
//	  	 			   5, 3, 2, 1, 2, 6, 5,
//	  	   			   4, 1, 4, 4, 1, 4, 0,
//	  	   			   5, 6, 2, 1, 5, 6, 2,
//	  	   			   3, 1, 5, 4, 6, 5, 3,
//	  	  			   6, 3, 6, 2, 1, 2, 1};
		assertEquals(board.convertBallToInt(board.getBall(3, 2)), 4);
		assertEquals(board.convertBallToInt(board.getBall(3, 3)), 4);
		assertEquals(board.convertBallToInt(board.getBall(3, 4)), 1);
		
		test.doMove(10, board.getFields(), board);
//	      afterMove = {5, 3, 4, 2, 5, 3, 6, 
//			           4, 6, 3, 4, 3, 1, 2,
//		               5, 3, 2, 1, 2, 6, 5,
//			           0, 4, 1, 4, 4, 1, 4,
//			           5, 6, 2, 1, 5, 6, 2,
//			           3, 1, 5, 4, 6, 5, 3,
//			           6, 3, 6, 2, 1, 2, 1};
		assertEquals(board.convertBallToInt(board.getBall(3, 0)), 0);
		assertEquals(board.convertBallToInt(board.getBall(3, 1)), 4);
		assertEquals(board.convertBallToInt(board.getBall(3, 2)), 1);
		
		test.doMove(14, board.getFields(), board);
//	      afterMove = {5, 3, 4, 2, 5, 3, 6, 
//        			   4, 6, 3, 4, 3, 1, 2,
//       		       5, 3, 2, 1, 2, 6, 5,
//        			   5, 4, 1, 4, 4, 1, 4,
//        			   3, 6, 2, 1, 5, 6, 2,
//        			   6, 1, 5, 4, 6, 5, 3,
//        			   0, 3, 6, 2, 1, 2, 1};
		assertEquals(board.convertBallToInt(board.getBall(6, 0)), 0);
		assertEquals(board.convertBallToInt(board.getBall(5, 0)), 6);
		assertEquals(board.convertBallToInt(board.getBall(4, 0)), 3);
		
		test.doMove(21, board.getFields(), board);
//	      afterMove = {0, 3, 4, 2, 5, 3, 6, 
//		   			   5, 6, 3, 4, 3, 1, 2,
//       			   4, 3, 2, 1, 2, 6, 5,
//		   			   5, 4, 1, 4, 4, 1, 4,
//		   			   5, 6, 2, 1, 5, 6, 2,
//		   			   3, 1, 5, 4, 6, 5, 3,
//		   			   6, 3, 6, 2, 1, 2, 1};
		assertEquals(board.convertBallToInt(board.getBall(0, 0)), 0);
		assertEquals(board.convertBallToInt(board.getBall(1, 0)), 5);
		assertEquals(board.convertBallToInt(board.getBall(2, 0)), 4);
	}
	
	@Test
	void singleMoveLegalityTest() {
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
				  	  4, 6, 3, 4, 3, 1, 2,
				  	  5, 3, 2, 1, 2, 6, 5,
				  	  4, 1, 4, 0, 4, 1, 4,
				  	  5, 6, 2, 1, 5, 6, 2,
				  	  3, 1, 5, 4, 6, 5, 3,
				  	  6, 3, 6, 2, 1, 2, 1};
		board = new Board(init);
		
		assertTrue(test.checkSingleMoveLegality(3, board));
		assertFalse(test.checkSingleMoveLegality(34, board));
		assertFalse(test.checkSingleMoveLegality(6, board));
	}
	
	@Test
	void doubleMoveLegalityTest() {
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
			  	  	  4, 6, 3, 4, 3, 1, 2,
			  	  	  5, 3, 2, 1, 2, 6, 5,
			  	  	  4, 1, 4, 0, 4, 1, 4,
			  	  	  5, 6, 2, 1, 5, 6, 2,
			  	  	  3, 1, 5, 4, 6, 5, 3,
			  	  	  6, 3, 6, 2, 1, 2, 1};
		board = new Board(init);
		
		assertFalse(test.checkDoubleMoveLegality(37, 3, board));
		assertFalse(test.checkDoubleMoveLegality(3, 37, board));
		assertTrue(test.checkDoubleMoveLegality(10, 21, board));
	}
}
