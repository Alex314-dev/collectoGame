package ss.project.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;
import ss.project.protocol.ProtocolMessages;

class BoardTest {
	private Board board;
	
	@BeforeEach
	void setUp() {
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
					  4, 6, 3, 4, 3, 1, 2,
			 		  5, 3, 2, 1, 2, 6, 5,
					  4, 1, 4, 0, 4, 1, 4,
					  5, 6, 2, 1, 5, 6, 2,
					  3, 1, 5, 4, 6, 5, 3,
					  6, 3, 6, 2, 1, 2, 1};
		board = new Board(init);
	}
	
	@Test
	void clearingTheFieldsTest() {
		board.clearFields();
		
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				assertEquals(Ball.EMPTY, board.getBall(row, col));
				assertTrue(board.checkFieldEmpty(board.getFields(), row, col));
			}
		}
		
		assertTrue(board.checkBoardEmpty());
	}
	
	@Test
	void deepCopyTest() {
		Board copy = new Board(this.board.fieldDeepCopy(this.board.getFields()));
		
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				assertEquals(this.board.getBall(row, col), copy.getBall(row, col));
			}
		}
		
		assertFalse(this.board == copy);
	}
	
	@Test
	void getAdjacentTest() {
		assertTrue(this.board.getAdjacent(this.board.getFields()).size() == 0);
		
		this.board.moveUp(3, this.board.getFields());
		
		List<int[]> adjacent = new ArrayList<>(this.board.getAdjacent(this.board.getFields()));
		// 2 3, 3 3 have adjaceny
		assertEquals(2, adjacent.get(0)[0]);
		assertEquals(3, adjacent.get(0)[1]);
		assertEquals(3, adjacent.get(1)[0]);
		assertEquals(3, adjacent.get(1)[1]);
		
	}

	@Test
	void handleAdjacencyTest() {
		this.board.moveUp(3, this.board.getFields());
		List<Ball> balls = this.board.handleAdjacency(this.board.getFields());
		
		assertEquals(Ball.EMPTY, this.board.getBall(2, 3));
		assertEquals(Ball.EMPTY, this.board.getBall(3, 3));
		
		assertEquals(Ball.BLUE, balls.get(0));
		assertEquals(Ball.BLUE, balls.get(1));
	}
	
	@Test
	void possibleSingleMoveTest() {
		// move column 3 down
		assertTrue(this.board.possibleSingleMove(this.board.getFields()));
		
		// changed the init board so that no single move is possible 
		// (look at row:3, col:2 and row:4, col:3)
		int[] init = {5, 3, 4, 2, 5, 3, 6, 
					  4, 6, 3, 4, 3, 1, 2,
			 		  5, 3, 2, 1, 2, 6, 5,
					  4, 1, 3, 0, 4, 1, 4,
					  5, 6, 2, 6, 5, 6, 2,
					  3, 1, 5, 4, 6, 5, 3,
					  6, 3, 6, 2, 1, 2, 1};
		this.board = new Board(init);
		
		assertFalse(this.board.possibleSingleMove(this.board.getFields()));
	}
	
	@Test
	void possibleDoubleMoveTest() {
		// move column 3 down
		// and then move row 3 right
		assertTrue(this.board.possibleDoubleMove(this.board.getFields()));
		
		// changed the init board so that no double move is possible 
		int[] init = {1, 4, 0, 0, 0, 0, 0, 
					  0, 0, 0, 0, 0, 0, 0,
			 		  0, 0, 0, 0, 0, 0, 0,
					  2, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 5,
					  6, 3, 0, 0, 0, 0, 0};
		this.board = new Board(init);
		
		assertFalse(this.board.possibleDoubleMove(this.board.getFields()));
	}
	
	@Test
	void moveUpTest() {
		int[] init = {0, 0, 0, 0, 0, 0, 0, 
					  0, 0, 0, 0, 0, 0, 0,
			 		  1, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  2, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  3, 0, 0, 0, 0, 0, 0};
		this.board = new Board(init);
		
		this.board.moveUp(0, board.getFields());
		assertEquals(Ball.BLUE, this.board.getBall(0, 0));
		assertEquals(Ball.YELLOW, this.board.getBall(1, 0));
		assertEquals(Ball.RED, this.board.getBall(2, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(3, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(4, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(5, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(6, 0));
	}
	
	@Test
	void moveDownTest() {
		int[] init = {0, 0, 0, 0, 0, 0, 0, 
					  0, 0, 0, 0, 0, 0, 0,
			 		  1, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  2, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  3, 0, 0, 0, 0, 0, 0};
		this.board = new Board(init);
		
		this.board.moveDown(0, board.getFields());
		assertEquals(Ball.EMPTY, this.board.getBall(0, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(1, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(2, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(3, 0));
		assertEquals(Ball.BLUE, this.board.getBall(4, 0));
		assertEquals(Ball.YELLOW, this.board.getBall(5, 0));
		assertEquals(Ball.RED, this.board.getBall(6, 0));
	}
	
	@Test
	void moveRightTest() {
		int[] init = {0, 0, 1, 0, 2, 0, 3, 
					  0, 0, 0, 0, 0, 0, 0,
			 		  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0};
		this.board = new Board(init);
		
		this.board.moveRight(0, board.getFields());
		assertEquals(Ball.EMPTY, this.board.getBall(0, 0));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 1));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 2));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 3));
		assertEquals(Ball.BLUE, this.board.getBall(0, 4));
		assertEquals(Ball.YELLOW, this.board.getBall(0, 5));
		assertEquals(Ball.RED, this.board.getBall(0, 6));
	}
	
	@Test
	void moveLeftTest() {
		int[] init = {0, 0, 1, 0, 2, 0, 3, 
					  0, 0, 0, 0, 0, 0, 0,
			 		  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0,
					  0, 0, 0, 0, 0, 0, 0};
		this.board = new Board(init);
		
		this.board.moveLeft(0, board.getFields());
		assertEquals(Ball.BLUE, this.board.getBall(0, 0));
		assertEquals(Ball.YELLOW, this.board.getBall(0, 1));
		assertEquals(Ball.RED, this.board.getBall(0, 2));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 3));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 4));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 5));
		assertEquals(Ball.EMPTY, this.board.getBall(0, 6));
	}
	
	@Test
	void randomBoardTest() {
		this.board = new Board();
		assertEquals(Ball.EMPTY, this.board.getBall(3, 3));
		assertTrue(this.board.possibleSingleMove(this.board.getFields()));
		assertTrue(this.board.getAdjacent(this.board.getFields()).size() == 0);
		
		int blueCount = 0;
		int yellowCount = 0;
		int redCount = 0;
		int orangeCount = 0;
		int purpleCount = 0;
		int greenCount = 0;
		
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				switch (this.board.getBall(row, col)) {
					case BLUE:
						blueCount++;
						break;
					case YELLOW:
						yellowCount++;
						break;
					case RED:
						redCount++;
						break;
					case ORANGE:
						orangeCount++;
						break;
					case PURPLE:
						purpleCount++;
						break;
					case GREEN:
						greenCount++;
						break;
					default:
						break;
				}
			}
		}
		
		assertEquals(8, blueCount);
		assertEquals(8, yellowCount);
		assertEquals(8, redCount);
		assertEquals(8, orangeCount);
		assertEquals(8, purpleCount);
		assertEquals(8, greenCount);
	}
	
	@Test
	void gameOverTest() {
		assertFalse(this.board.gameOver());
		
		int[] init1 = {0, 0, 0, 0, 0, 0, 0, 
					   0, 0, 0, 0, 0, 0, 0,
			 		   0, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 0};
		this.board = new Board(init1);
		
		assertTrue(this.board.gameOver());
		
		int[] init2 = {1, 4, 0, 0, 0, 0, 0, 
					   0, 0, 0, 0, 0, 0, 0,
			 		   0, 0, 0, 0, 0, 0, 0,
					   2, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 0,
					   0, 0, 0, 0, 0, 0, 5,
					   6, 3, 0, 0, 0, 0, 0};
		this.board = new Board(init2);
		
		assertTrue(this.board.gameOver());
	}
	
	@Test
	void toStringTest() {
		assertTrue(this.board.toString().contains("+---+---+---+---+---+---+---+"));
		assertTrue(this.board.toString().contains(" | "));
	}
	
	@Test
	void convertIntToBallTest() {
		assertEquals(board.convertIntToBall(0), Ball.EMPTY);
		assertEquals(board.convertIntToBall(1), Ball.BLUE);
		assertEquals(board.convertIntToBall(2), Ball.YELLOW);
		assertEquals(board.convertIntToBall(3), Ball.RED);
		assertEquals(board.convertIntToBall(4), Ball.ORANGE);
		assertEquals(board.convertIntToBall(5), Ball.PURPLE);
		assertEquals(board.convertIntToBall(6), Ball.GREEN);
		assertEquals(board.convertIntToBall(99), null);
	}
	
	@Test
	void convertBallToIntTest() {
		assertEquals(board.convertBallToInt(Ball.EMPTY), 0);
		assertEquals(board.convertBallToInt(Ball.BLUE), 1);
		assertEquals(board.convertBallToInt(Ball.YELLOW), 2);
		assertEquals(board.convertBallToInt(Ball.RED), 3);
		assertEquals(board.convertBallToInt(Ball.ORANGE), 4);
		assertEquals(board.convertBallToInt(Ball.PURPLE), 5);
		assertEquals(board.convertBallToInt(Ball.GREEN), 6);
	}
	
	@Test
	void newGameStringTest() {
		String newGame = board.newGameString();
		assertTrue(newGame.contains(ProtocolMessages.DELIMITER));
		String[] splitNewGame = newGame.split(ProtocolMessages.DELIMITER);
		assertEquals(50, splitNewGame.length);
	}
}
