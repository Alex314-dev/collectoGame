package ss.project;

import ss.project.gamelogic.Board;
import ss.project.players.Player;

/**
 * Meant for testing purposes, not related to the server/client.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class Game {
	public static final int NUMBER_PLAYERS = 2;
	
	private Board board;
	
	private Player[] players;
	
	private int current;
	
	public Game(Player s0, Player s1) {
        board = new Board();
        players = new Player[NUMBER_PLAYERS];
        players[0] = s0;
        players[1] = s1;
        current = 0;
    }
	
    public void play() { 
        while (!(board.gameOver())) {
        	update();
        	
        	if (current == NUMBER_PLAYERS) {
        		current = 0;
        	}
        	
        	players[current].makeMove(board);
        	
        	current++;
        }
        
        update();
        System.out.println(printResult());
    }
    
    private void update() {
        System.out.println("\nCurrent game situation: \n\n" + board.toString()
                + "\n");
    }

    private String printResult() {
    	if (players[0].evaluatePoints() > players[1].evaluatePoints()) {
    		return players[0].getName() + " has won!";
    	} else if (players[1].evaluatePoints() > players[0].evaluatePoints()) {
    		return players[1].getName() + " has won!";
    	} else if (players[0].numOfBalls() > players[1].numOfBalls()) {
    		return players[0].getName() + " has won!";
    	} else if (players[1].numOfBalls() > players[0].numOfBalls()) {
    		return players[1].getName() + " has won!";
    	} else {
    		return "Tie!";
    	}
    }
}