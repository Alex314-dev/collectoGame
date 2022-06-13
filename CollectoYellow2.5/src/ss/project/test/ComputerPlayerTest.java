package ss.project.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ss.project.gamelogic.Board;
import ss.project.players.ComputerPlayer;
import ss.project.strategy.NaiveStrategy;
import ss.project.strategy.SmartStrategy;

class ComputerPlayerTest {
	ComputerPlayer test;
	Board board;
	
	@Test
	void getStrategyTest() {
		test = new ComputerPlayer(new NaiveStrategy());
		assertTrue(test.getStrategy() instanceof NaiveStrategy);
		
		test = new ComputerPlayer(new SmartStrategy());
		assertTrue(test.getStrategy() instanceof SmartStrategy);
	}
	
	@Test
	void setStrategyTest() {
		test = new ComputerPlayer(new NaiveStrategy());
		test.setStrategy(new SmartStrategy());
		assertTrue(test.getStrategy() instanceof SmartStrategy);
		
		test = new ComputerPlayer(new SmartStrategy());
		test.setStrategy(new NaiveStrategy());
		assertTrue(test.getStrategy() instanceof NaiveStrategy);
	}
	
	@Test
	void naiveSingleMoveTest() {
		test = new ComputerPlayer(new NaiveStrategy());
		
		int[] init = {1, 0, 0, 0, 0, 0, 1, 
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0};
		board = new Board(init);
		
		int[] determinedMove = test.determineMove(board);
		test.makeSingleMove(determinedMove[0], board);
		assertTrue(board.checkBoardEmpty());
	}
	
	@Test
	void naiveDoubleMoveTest() {
		test = new ComputerPlayer(new NaiveStrategy());
		
		int[] init = {1, 0, 0, 0, 0, 0, 0, 
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 1,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0,
				  	  0, 0, 0, 0, 0, 0, 0};
		board = new Board(init);
		
		int[] determinedMove = test.determineMove(board);
		test.makeDoubleMove(determinedMove[0], determinedMove[1], board);
		assertTrue(board.checkBoardEmpty());
	}
	
	@Test
	void smartSingleMoveTest() {
		test = new ComputerPlayer(new SmartStrategy());
		
		int[] init = {1, 0, 0, 0, 0, 0, 1, 
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  2, 0, 0, 2, 0, 0, 2,
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  0, 0, 0, 0, 0, 0, 0};
		board = new Board(init);
		
		int[] determinedMove = test.determineMove(board);
		test.makeSingleMove(determinedMove[0], board);
		assertEquals(0, board.convertBallToInt(board.getBall(2, 0)));
		assertEquals(1, board.convertBallToInt(board.getBall(0, 0)));
	}

	@Test
	void smartDoubleMoveTest() {
		test = new ComputerPlayer(new SmartStrategy());
		
		int[] init = {0, 0, 0, 0, 0, 0, 0, 
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  0, 0, 0, 0, 0, 0, 0,
			  	  	  1, 0, 2, 0, 0, 0, 0,
			  	  	  0, 3, 0, 0, 0, 0, 0,
			  	  	  2, 0, 0, 0, 0, 0, 0};
		board = new Board(init);
		
		int[] determinedMove = test.determineMove(board);
		test.makeDoubleMove(determinedMove[0], determinedMove[1], board);
		assertEquals(1, board.convertBallToInt(board.getBall(4, 0)));
		assertEquals(3, board.convertBallToInt(board.getBall(5, 1)));
		assertEquals(0, board.convertBallToInt(board.getBall(6, 0)));
		
		int[] init2 = {0, 0, 0, 0, 0, 0, 0, 
		  	  	  	   0, 0, 0, 0, 0, 0, 0,
		  	  	  	   0, 0, 0, 0, 0, 0, 0,
		  	  	  	   0, 0, 2, 0, 0, 0, 0,
		  	  	  	   4, 0, 3, 0, 0, 0, 0,
		  	  	  	   2, 1, 0, 0, 0, 0, 0,
		  	  	  	   3, 0, 0, 0, 0, 0, 0};
		board = new Board(init2);
	
		int[] determinedMove2 = test.determineMove(board);
		test.makeDoubleMove(determinedMove2[0], determinedMove2[1], board);
		assertEquals(0, board.convertBallToInt(board.getBall(6, 0)));
		assertEquals(4, board.convertBallToInt(board.getBall(4, 0)));
		assertEquals(2, board.convertBallToInt(board.getBall(5, 0)));
	}
}
