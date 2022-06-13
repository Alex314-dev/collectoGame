package ss.project.players;

import java.util.NoSuchElementException;

import ss.project.gamelogic.Ball;
import ss.project.gamelogic.Board;
import ss.utils.TextIO;

/**
 * 
 * A human player class for executing human moves. A model class
 * as it models a real player.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class HumanPlayer extends Player {
	
	/**
	 * Constructing a human player with the given name.
	 * @requires name != null
	 * @param name The name given to that player
	 */
	public HumanPlayer(String name) {
		super(name);
	}
	
	/**
	 * A method to do the move onto the given field without handling adjacency.
	 * @requires move >= 0 && 27 >= move && field != null && board != null
	 * @param move The move to be made.
	 * @param field The field onto which the move will be made
	 * @param board A board object to access the move methods.
	 */
	public void doMove(int move, Ball[][] field, Board board) {
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
	 * A method to check if the given move is a legal single move.
	 * @requires move >= 0 && 27 >= move && board != null
	 * @param move The move to be checked.
	 * @param board The board object onto which the move legality is checked.
	 * @return true if the move is legal, false otherwise.
	 */
	public boolean checkSingleMoveLegality(int move, Board board) {
		Ball[][] field = board.fieldDeepCopy(board.getFields());
		
		if (move >= 0 && move <= 27) {
			doMove(move, field, board);
		} else {
			return false;
		}
		
		if (board.handleAdjacency(field).size() == 0) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * A method to check if the given moves make a legal double move.
	 * @requires move >= 0 && 27 >= move && move2 >= 0 && 27 >= move2 && board != null
	 * @param move The first move.
	 * @param move2 The second move.
	 * @param board The board onto which the legality will be checked.
	 * @return true if the moves make a legal double move, false otherwise.
	 */
	public boolean checkDoubleMoveLegality(int move, int move2, Board board) {
		Ball[][] field = board.fieldDeepCopy(board.getFields());
		
		if (move >= 0 && move <= 27) {
			doMove(move, field, board);
		} else {
			return false;
		}
		
		if (move2 >= 0 && move2 <= 27) {
			doMove(move2, field, board);
		} else {
			return false;
		}
		
		if (board.handleAdjacency(field).size() == 0) {
			return false;
		}
		
		return true;
	}
 	
	/**
	 * Only used in the console version of the game! Not to be used with the client or the server.
	 */
	@Override
	public int[] determineMove(Board board) {
		int[] result = new int[2];
	
		if (board.possibleSingleMove(board.getFields())) {
			System.out.print("> " + getName() + ", please play your single move: ");
			
			String command = TextIO.getln();
			String[] partsOfCom = command.split("~"); 
	
			while (!partsOfCom[0].equals("MOVE")) {
				System.out.println("Wrong argumment!");
				System.out.print("> " + getName() + ", please play your single move: ");
				command = TextIO.getln();
				partsOfCom = command.split("~");
				
			}
			
			while (true) {
				try {

					
					int move = Integer.parseInt(partsOfCom[1]);
					if (!checkSingleMoveLegality(move, board)) {
						System.out.println("Wrong argumment!");
						System.out.print("Please type your move number: ");
						command = TextIO.getln();
						partsOfCom = command.split("~");
						continue;
					}
					result[0] = move;
					result[1] = -1;
					break;
				} catch (NumberFormatException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move number: ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				} catch (NoSuchElementException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move number: ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move (MOVE~[number from 0 to 27]): ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				}
			}
			
			return result;
		} else {
			System.out.print("> " + getName() + ", please play your double move: ");
			String command = TextIO.getln();
			String[] partsOfCom = command.split("~");
			
			while (!partsOfCom[0].equals("MOVE")) {
				System.out.println("Wrong argumment!");
				System.out.print("> " + getName() + ", please play your double move: ");
				
				command = TextIO.getln();
				partsOfCom = command.split("~");
			}
			
			while (true) {
				try {
					int move = Integer.parseInt(partsOfCom[1]);
					int move2 = Integer.parseInt(partsOfCom[2]);
					if (!checkDoubleMoveLegality(move, move2, board)) {
						System.out.println("Wrong argumment!");
						System.out.print("Please type your move numbers: ");
						continue;
					}
					result[0] = move;
					result[1] = move2;
					break;
				} catch (NumberFormatException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move numbers: ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				} catch (NoSuchElementException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move numbers: ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("Wrong argumment!");
					System.out.print("Please type your move (MOVE~[number from 0 to 27]): ");
					command = TextIO.getln();
					partsOfCom = command.split("~");
					continue;
				}
			}
			
			return result;
		}		
	}
}
