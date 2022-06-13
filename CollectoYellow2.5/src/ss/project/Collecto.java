package ss.project;

import ss.project.players.*;
import ss.project.strategy.*;

/**
 * Meant for testing purposes, not related to the server/client.
 * @author Alex Petrov (s2478412) and Kagan Gulsum (s2596091)
 *
 */
public class Collecto {
	public static void main(String[] args) {
//		List<Integer> stats = new ArrayList<>();
		Player player3 = new HumanPlayer("Kagan");
		Player player4 = new ComputerPlayer(new SmartStrategy());
		
		Game game = new Game(player3, player4);
		game.play();
		
//		stats.add(game.play());		
//		int counter = 0;
//		for (int ones : stats) {
//			if (ones == 1) {
//				counter++;
//			}
//		}
//		System.out.println((counter / 100) * stats.size());
	}
}
