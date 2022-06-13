package ss.project.gamelogic;

import java.util.ArrayList;
import java.util.List;

import ss.project.protocol.ProtocolMessages;

/**
 * 
 * The main game logic class of the Collecto game.
 * A model class, as it stores board states and handles
 * rules and algorithms.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class Board {
	
	// Private class fields
	private Ball[][] fields = new Ball[7][7];
	
	// Constructor
	
	/**
	 * Constructing a board until a valid coloring and a valid initial move is satisfied.
	 */
	public Board() {
		while (!(initBoard() && possibleSingleMove(this.fields))) { 
			continue;
		}
	}
	
	/**
	 * For construction of board in the client based on data from server.
	 */
	public Board(int[] numbers) {
		for (int i = 0; i < numbers.length; i++) {
			this.fields[i / 7][i % 7] = convertIntToBall(numbers[i]);
		}
	}
	
	/**
	 * 
	 * For testing purposes.
	 */
	public Board(Ball[][] ballArray) {
		this.fields = ballArray;
	}
	
	/**
	 * A converter between integers and balls.
	 * @requires 0 <= number && number <= 6
	 * @param number To be converted into a ball.
	 * @return The ball associated with that number.
	 */
	public Ball convertIntToBall(int number) {
		switch (number) {
			case 0:
				return Ball.EMPTY;
			case 1:
				return Ball.BLUE;
			case 2:
				return Ball.YELLOW;
			case 3:
				return Ball.RED;
			case 4:
				return Ball.ORANGE;
			case 5:
				return Ball.PURPLE;
			case 6:
				return Ball.GREEN;
			default:
				return null;
		}
	}
	
	/**
	 * A converter between balls and integers.
	 * @requires ball != null
	 * @ensures 0 <= \result && \result <= 6
	 * @param ball
	 * @return Integer representation of the ball.
	 */
	public int convertBallToInt(Ball ball) {
		switch (ball) {
			case EMPTY:
				return 0;
			case BLUE:
				return 1;
			case YELLOW:
				return 2;
			case RED:
				return 3;
			case ORANGE:
				return 4;
			case PURPLE:
				return 5;
			case GREEN:
				return 6;
			default:
				return 0;
		}
	}
	
	/**
	 * Returns a protocol appropriate representation of the board.
	 */
	public String newGameString() {
		String result = "";
		for (int row = 0; row < 7; row++) {
			for (int col = 0; col < 7; col++) {
				result += ProtocolMessages.DELIMITER + convertBallToInt(fields[row][col]);
			}
		}
		return result;
	}
	
	// Field getter, clearer, copier, and empty checker.
	
	/**
	 * Get the ball in the specified row, column pair. (Mainly for testing purposes).
	 * @requires (row >= 0) && (row <= 6) && (col >= 0) && (col <= 6)
	 * @param row specifying the row number
	 * @param col specifying the column number
	 * @return ball located in the row, col pair
	 */
	public Ball getBall(int row, int col) {
		return fields[row][col];
	}
	
	/**
	 * Returns the fields of this board class.
	 */
	public Ball[][] getFields() {
		return this.fields;
	}
	
	/**
	 * Set every field to Ball.EMPTY.
	 * @ensures \all(getBall(row, col)) == Ball.EMPTY
	 */
	public void clearFields() {
		for (int row = 0; row < fields.length; row++) {
			for (int col = 0; col < fields[0].length; col++) {
				fields[row][col] = Ball.EMPTY;
			}
		}
	}
	
	/**
	 * Creates and returns a deep copy of the current board.
	 * @return a deep copy of the board
	 */
	public Ball[][] fieldDeepCopy(Ball[][] field) {
		Ball[][] deepCopy = new Ball[7][7];
		
		for (int row = 0; row < field.length; row++) {
			for (int col = 0; col < field[0].length; col++) {
				deepCopy[row][col] = field[row][col];
			}
		}
		
		return deepCopy;
	}
	
	/**
	 * Check if there are any non-empty balls on the board.
	 * @return false if there is at least one non-empty ball.
	 * 		   true otherwise.
	 */
	public boolean checkBoardEmpty() {
		for (int row = 0; row < fields.length; row++) {
			for (int col = 0; col < fields[0].length; col++) {
				if (checkFieldEmpty(fields, row, col) == false) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Check if, for the given field, the row, column pair is an empty field.
	 * @param field
	 * @param row
	 * @param column
	 * @return true if the specified field is empty, false otherwise.
	 */
	public boolean checkFieldEmpty(Ball[][] field, int row, int col) {
		if (field[row][col] == Ball.EMPTY) {
			return true;
		}
		return false;
	}
	
	// Board initialization
	
	/**
	 * Creating a list consisting of eight of every color.
	 * To be used in the board initialization.
	 * @return a list of all the balls required to play Collecto
	 */
	public List<Ball> getColors() {
		List<Ball> result = new ArrayList<Ball>(List.of(Ball.BLUE, Ball.BLUE, Ball.BLUE, Ball.BLUE,
				 Ball.BLUE, Ball.BLUE, Ball.BLUE, Ball.BLUE,
				 Ball.GREEN, Ball.GREEN, Ball.GREEN, Ball.GREEN,
				 Ball.GREEN, Ball.GREEN, Ball.GREEN, Ball.GREEN,
			     Ball.ORANGE, Ball.ORANGE, Ball.ORANGE, Ball.ORANGE,
				 Ball.ORANGE, Ball.ORANGE, Ball.ORANGE, Ball.ORANGE,
				 Ball.PURPLE, Ball.PURPLE, Ball.PURPLE, Ball.PURPLE,
				 Ball.PURPLE, Ball.PURPLE, Ball.PURPLE, Ball.PURPLE,
				 Ball.RED, Ball.RED, Ball.RED, Ball.RED,
				 Ball.RED, Ball.RED, Ball.RED, Ball.RED,
				 Ball.YELLOW, Ball.YELLOW, Ball.YELLOW, Ball.YELLOW,
				 Ball.YELLOW, Ball.YELLOW, Ball.YELLOW, Ball.YELLOW));
		
		return result;
	}
	
	/**
	 * A function which checks if in the initial board setup the color assignment ended up in an
	 * impossible situation where valid color can not be assigned.
	 * @param balls A list of remaining balls which are not assigned to any particular field
	 * @param up The ball above the current field which the initBoard is trying to set
	 * @param left The ball to the left of the current field which the initBoard is trying to set
	 * @return false if there exists a ball which is of different type then 
	 * 		   the balls above and to the left,
	 * 		   true otherwise.
	 */
	private boolean impossibleColors(List<Ball> balls, Ball up, Ball left)  {
		for (Ball ball : balls) {
			if (ball != up && ball != left) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * A completely random board initialization where random balls are picked from a list,
	 * and are assigned to the next available field with checks which make it impossible for two 
	 * adjacent colors to come next to each other.
	 * @return false if the randomization made it impossible to avoid adjacency,
	 * 		   true otherwise.
	 */
	public boolean initBoard() {
		List<Ball> colors = getColors();
		
		for (int row = 0; row < fields.length; row++) {
			for (int col = 0; col < fields[0].length; col++) {
				if (row == 3 && col == 3) {
					fields[row][col] = Ball.EMPTY;
					continue;
				}
				
				// Impossibility check.
				try {
					if (impossibleColors(colors, fields[row - 1][col], fields[row][col - 1])) {
						clearFields();
						return false;
					}
				} catch (IndexOutOfBoundsException e) { }

				while (true) {
					int randomIn = (int) (Math.random() * colors.size());
					Ball current = colors.get(randomIn);
					
					// Checking the field one above.
					try {
						if (current == fields[row - 1][col]) {
							continue;
						}
					} catch (IndexOutOfBoundsException e) { }
					
					// Checking the field one to the left.
					try {
						if (current == fields[row][col - 1]) {
							continue;
						}
					} catch (IndexOutOfBoundsException e) { }
						
					fields[row][col] = current;
					colors.remove(randomIn);
					break;
				}
			}
		}
		
		return true;
	}
	
	// Adjacency functionality
	
	/**
	 * Check every ball, and if they have an adjacent ball with the same color. Store its 
	 * row and col in an integer array and put that array to the resulting list.
	 * @requires field != null
	 * @return a list of integer arrays indicating the indexes of adjacent balls
	 */
	public List<int[]> getAdjacent(Ball[][] field) {
		List<int[]> adjIn = new ArrayList<>();
		
		for (int row = 0; row < field.length; row++) {
			for (int col = 0; col < field[0].length; col++) {
				if (checkFieldEmpty(field, row, col)) {
					continue;
				}
				
				try {
					if (field[row][col] == field[row][col - 1]) {
						int[] pos = {row, col};
						adjIn.add(pos);
						continue;
					}
				} catch (IndexOutOfBoundsException e) { }
				
				try {
					if (field[row][col] == field[row][col + 1]) {
						int[] pos = {row, col};
						adjIn.add(pos);
						continue;
					}
				} catch (IndexOutOfBoundsException e) { }
				
				try {
					if (field[row][col] == field[row - 1][col]) {
						int[] pos = {row, col};
						adjIn.add(pos);
						continue;
					}
				} catch (IndexOutOfBoundsException e) { }
				
				try {
					if (field[row][col] == field[row + 1][col]) {
						int[] pos = {row, col};
						adjIn.add(pos);
						continue;
					}
				} catch (IndexOutOfBoundsException e) { }
			}
		}
		
		return adjIn;
	}
	
	/**
	 * Change every adjacent ball to an empty one and store them in a list.
	 * @requires field != null
	 * @return a list of balls which are adjacent
	 */
	public List<Ball> handleAdjacency(Ball[][] field) {
		List<Ball> adjacentBalls = new ArrayList<>();
		
		for (int[] rowCol : getAdjacent(field)) {
			adjacentBalls.add(field[rowCol[0]][rowCol[1]]);
			field[rowCol[0]][rowCol[1]] = Ball.EMPTY;
		}
		
		return adjacentBalls;
	}
	
	/**
	 * Check every possible single move and see if there is any adjacency.
	 * @requires field != null
	 * @return true if there happened to be adjacent balls after a single move,
	 * 		   false otherwise.
	 */
	public boolean possibleSingleMove(Ball[][] field) {
  		Ball[][] copy;
  		
  		for (int row = 0; row < 7; row++) {
			copy = fieldDeepCopy(field);
			moveRight(row, copy);
			if (!getAdjacent(copy).isEmpty()) {
				return true;
			}
			
			copy = fieldDeepCopy(field);
			moveLeft(row, copy);
			if (!getAdjacent(copy).isEmpty()) {
				return true;
			}
		}
		
  		for (int col = 0; col < 7; col++) {
			copy = fieldDeepCopy(field);
			moveUp(col, copy);
			if (!getAdjacent(copy).isEmpty()) {
				return true;
			}
			
			copy = fieldDeepCopy(field);
			moveDown(col, copy);
			if (!getAdjacent(copy).isEmpty()) {
				return true;
			}
		}
		
		return false;
 
	}
	
	/**
	 * Check every possible double move and see if there is any adjacency.
	 * @requires field != null
	 * @return true if there happened to be adjacent balls after a double move,
	 * 		   false otherwise.
	 */
	public boolean possibleDoubleMove(Ball[][] field) {
		Ball[][] copy;
		
		for (int row = 0; row < 7; row++) {
			copy = fieldDeepCopy(field);
			moveRight(row, copy);
			if (possibleSingleMove(copy)) {
				return true;
			}
			
			copy = fieldDeepCopy(field);
			moveLeft(row, copy);
			if (possibleSingleMove(copy)) {
				return true;
			}
		}
		
		for (int col = 0; col < 7; col++) {
			copy = fieldDeepCopy(field);
			moveUp(col, copy);
			if (possibleSingleMove(copy)) {
				return true;
			}
			
			copy = fieldDeepCopy(field);
			moveDown(col, copy);
			if (possibleSingleMove(copy)) {
				return true;
			}
		}
		
		return false;
	}
	
	// Grid movement
	
	/**
	 * Move all the balls in the specified row to the right, without disturbing the order.
	 * @requires field != null && 0 <= row && row < field.length
	 * @param row, field
	 */
	public void moveRight(int row, Ball[][] field) {
		for (int col = field[0].length - 2; col >= 0; col--) {
			if (checkFieldEmpty(field, row, col)) {
				continue;
			}
			
			int i = 1;
			try {
				while (checkFieldEmpty(field, row, col + i)) {
					field[row][col + i] = field[row][col + i - 1];
					field[row][col + i - 1] = Ball.EMPTY;
					i++;
				}
			} catch (IndexOutOfBoundsException e) { }
		}
	}
	
	/**
	 * Move all the balls in the specified row to the left, without disturbing the order.
	 * @requires field != null && 0 <= row && row < field.length
	 * @param row, field
	 */
	public void moveLeft(int row, Ball[][] field) {
		for (int col = 1; col < field[0].length; col++) {
			if (checkFieldEmpty(field, row, col)) {
				continue;
			}
			
			int i = 1;
			try {
				while (checkFieldEmpty(field, row, col - i)) {
					field[row][col - i] = field[row][col - i + 1];
					field[row][col - i + 1] = Ball.EMPTY;
					i++;
				}
				
			} catch (IndexOutOfBoundsException e) { }
		}
	}
	
	/**
	 * Move all the balls in the specified column above, without disturbing the order.
	 * @requires field != null && 0 <= col && col < field[0].length
	 * @param col, field
	 */
	public void moveUp(int col, Ball[][] field) {
		for (int row = 1; row < field.length; row++) {
			if (checkFieldEmpty(field, row, col)) {
				continue;
			}
			
			int i = 1;
			try {
				while (checkFieldEmpty(field, row - i, col)) {
					field[row - i][col] = field[row - i + 1][col];
					field[row - i + 1][col] = Ball.EMPTY;
					i++;
				}
			} catch (IndexOutOfBoundsException e) { }
		}
	}
	
	/**
	 * Move all the balls in the specified column below, without disturbing the order.
	 * @requires field != null && 0 <= col && col < field[0].length
	 * @param col, field
	 */
	public void moveDown(int col, Ball[][] field) {
		for (int row = field.length - 2; row >= 0; row--) {
			if (checkFieldEmpty(field, row, col)) {
				continue;
			}
			
			int i = 1;
			try {
				while (checkFieldEmpty(field, row + i, col)) {
					field[row + i][col] = field[row + i - 1][col];
					field[row + i - 1][col] = Ball.EMPTY;
					i++;
				}
			} catch (IndexOutOfBoundsException e) { }
		}
	}
	
	// gameOver method
	
	/**
	 * A method to check if the game is over.
	 * @return true if the field is empty or there is no valid move left,
	 * 		   false otherwise.
	 */
	public boolean gameOver() {
		return checkBoardEmpty()
			   || (!possibleSingleMove(this.fields) && !possibleDoubleMove(this.fields));
	}
	
	// toString method
	
	@Override
	public String toString() {
        String result = "        21  22  23  24  25  26  27\n";
        result += "        |   |   |   |   |   |   |  \n";
        result += "        v   v   v   v   v   v   v  \n";
        
        for (int row = 0; row < fields.length; row++) {
            result += "      +---+---+---+---+---+---+---+\n";
            for (int col = 0; col < fields[0].length; col++) {
                String color = "";
                
                switch (fields[row][col]) {
                    case EMPTY:
                        color = "\033[0;97m";
                        break;
                    case RED:
                        color = "\033[1;31m";
                        break;
                    case GREEN:
                        color = "\033[1;32m";
                        break;
                    case YELLOW:
                        color = "\033[1;33m";
                        break;
                    case BLUE:
                        color = "\033[1;34m";
                        break;
                    case PURPLE:
                        color = "\033[1;35m";
                        break;
                    case ORANGE:
                        color = "\033[1;91m";
                        break;
                }
                
                if (fields[row][col] == Ball.EMPTY) {
                    if (col == fields[0].length - 1) {
                        result += " | " + color + " " + "\033[0m" + " | <- " + row;
                    } else if (col == 0) {
                        if (row + 7 < 10) {
                            result += " " + (row + 7) + " -> | " + color + " " + "\033[0m";
                        } else {
                            result += (row + 7) + " -> | " + color + " " + "\033[0m";
                        }
                    } else {
                        result += " | " + color + " " + "\033[0m";
                    }
                } else if (col == fields[0].length - 1) {
                    result += " | " + color + "•" + "\033[0m" + " | <- " + row;
                } else if (col == 0) {
                    if (row + 7 < 10) {
                        result += " " + (row + 7) + " -> | " + color + "•" + "\033[0m";
                    } else {
                        result += (row + 7) + " -> | " + color + "•" + "\033[0m";
                    }
                } else {
                    result += " | " + color + "•" + "\033[0m";
                }
            }
            result += "\n";
        }
        result += "      +---+---+---+---+---+---+---+\n";
        result += "        ^   ^   ^   ^   ^   ^   ^\n";
        result += "        |   |   |   |   |   |   |\n";
        result += "        14  15  16  17  18  19  20\n";
        
        return result;
    }	
}
