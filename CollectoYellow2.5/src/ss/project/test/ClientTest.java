package ss.project.test;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import ss.project.client.*;
import ss.project.exceptions.*;
import ss.project.players.ComputerPlayer;
import ss.project.players.HumanPlayer;
import ss.project.protocol.ProtocolMessages;

class ClientTest {
	private final static ByteArrayOutputStream OUTCONTENT = new ByteArrayOutputStream();
	private final static PrintStream ORIGINALOUT = System.out;
	private ThreadedCollectoClient client;

	@BeforeEach
	void setUp() throws Exception {
		System.setOut(new PrintStream(OUTCONTENT));
		client = new ThreadedCollectoClient(false);
	}
	
	@Test
	void sendHelloTest() throws ServerUnavailableException {
		String desc = "client desc.";
		client.sendHello(desc);
		assertThat(OUTCONTENT.toString(), containsString(ProtocolMessages.HELLO + 
				ProtocolMessages.DELIMITER + desc));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendHelloInitialized() throws ServerUnavailableException, IOException {
		String desc = "client desc.";
		client.handleCommand(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + desc);
		client.sendHello(desc);
		assertThat(OUTCONTENT.toString(), containsString("Already initialized!"));
		OUTCONTENT.reset();
	}
	
	@Test
	void handleHelloTest() throws ServerUnavailableException {
		String serverDesc = "desc";
		try {
			client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + serverDesc);
			assertThat(OUTCONTENT.toString(), containsString(serverDesc));
			OUTCONTENT.reset();
			
			client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + serverDesc +
					ProtocolMessages.DELIMITER + "CRYPT" + ProtocolMessages.DELIMITER + "CHAT");
			assertThat(OUTCONTENT.toString(), containsString(serverDesc));
			assertThat(OUTCONTENT.toString(), containsString("CRYPT"));
			assertThat(OUTCONTENT.toString(), containsString("CHAT"));
			OUTCONTENT.reset();
			
			client.handleHello(ProtocolMessages.HELLO);
		} catch (ProtocolException e) {
			assertThat(e.getMessage(), containsString("Invalid response from server."
		            + "HELLO~<server description>[~extension] expected."));
		}
		
		try {
			client.handleHello("Test");
		} catch (ProtocolException e) {
			assertThat(e.getMessage(), containsString("Invalid response from server."
		            + "HELLO~<server description>[~extension] expected."));
		}
		
		try {
			client.handleHello("Test" + ProtocolMessages.DELIMITER + "Test");
		} catch (ProtocolException e) {
			assertThat(e.getMessage(), containsString("Invalid response from server."
		            + "HELLO~<server description>[~extension] expected."));
		}
		
		try {
			client.handleHello("Test" + ProtocolMessages.DELIMITER + "Test" +
					ProtocolMessages.DELIMITER + "Test");
		} catch (ProtocolException e) {
			assertThat(e.getMessage(), containsString("Invalid response from server."
		            + "HELLO~<server description>[~extension] expected."));
		}
	}
	
	@Test
	void sendListInitializedAndLoggedInTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "server");
		client.handleCommand(ProtocolMessages.LOGIN);
		client.sendList();
		assertThat(OUTCONTENT.toString(), containsString(ProtocolMessages.LIST));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendListNotInitializedTest() throws ServerUnavailableException, IOException {
		client.handleCommand(ProtocolMessages.LOGIN);
		client.sendList();
		assertThat(OUTCONTENT.toString(), containsString("Waiting for initialization."));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendListNotLoggedInTest() throws ProtocolException, ServerUnavailableException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "server");
		client.sendList();
		assertThat(OUTCONTENT.toString(), containsString("You are not logged in."));
		OUTCONTENT.reset();
	}
	
	@Test
	void handleListTest() throws ProtocolException, ServerUnavailableException {
		String msg = ProtocolMessages.LIST + ProtocolMessages.DELIMITER + "Alex" + "Kagan";
		client.handleList(msg);
		assertThat(OUTCONTENT.toString(), containsString("Alex"));
		assertThat(OUTCONTENT.toString(), containsString("Kagan"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendLogInInitializedNotLoggedInTest() 
			throws ProtocolException, ServerUnavailableException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "server");
		client.sendLogIn("Cool guy");
		assertThat(OUTCONTENT.toString(), containsString("Cool guy"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendLogInLoggedInTest() throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "server");
		client.handleCommand(ProtocolMessages.LOGIN);
		client.sendLogIn("Cool guy");
		assertThat(OUTCONTENT.toString(), containsString("You already logged in."));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendLogInNotInitialized() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.sendLogIn("Cool guy");
		assertThat(OUTCONTENT.toString(), containsString("Waiting for initialization."));
		OUTCONTENT.reset();
	}
	
	@Test
	void handleAlreadyLoggedIn() throws IOException, ServerUnavailableException {
		client.handleCommand(ProtocolMessages.ALREADYLOGGEDIN);
		assertThat(OUTCONTENT.toString(), containsString("Username taken"));
		OUTCONTENT.reset();
	}
	
	@Test
	void handleList() throws IOException, ServerUnavailableException {
		client.handleCommand(ProtocolMessages.LIST + ProtocolMessages.DELIMITER + "Cool guy");
		assertThat(OUTCONTENT.toString(), containsString("Currently logged in"));
		assertThat(OUTCONTENT.toString(), containsString("Cool guy"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendQueueNotInitialized() throws ServerUnavailableException {
		client.sendQueue();
		assertThat(OUTCONTENT.toString(), 
				containsString("Please initialize first! Hint: HELLO~<client description>"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendQueueNotLoggedIn() throws ProtocolException, ServerUnavailableException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendQueue();
		assertThat(OUTCONTENT.toString(), 
				containsString("Please log in first! Hint: LOGIN~<username>"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendQueueInAGame() throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		client.handleCommand(ProtocolMessages.LOGIN);
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Alex~Kagan";
		client.handleNewGame(msg);
		client.sendQueue();
		assertThat(OUTCONTENT.toString(), 
				containsString("Can't queue while in a game!"));
		OUTCONTENT.reset();
	}
	
	void sendQueue() throws ProtocolException, ServerUnavailableException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		client.sendQueue();
		assertThat(OUTCONTENT.toString(), 
				containsString("Queue request sent."));
		client.sendQueue();
		assertThat(OUTCONTENT.toString(), 
				containsString("Queue exit request sent."));
	}
	
	@Test
	void handleNewGameTest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Alex~Kagan";
		client.handleNewGame(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof HumanPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void handleNewGameAITest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("AlexAI");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~AlexAI~Kagan";
		client.handleNewGame(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof ComputerPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void handleNewGameAIPlusTest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("AlexAI+");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~AlexAI+~Kagan";
		client.handleNewGame(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof ComputerPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void handleNewGameSecondPlayerTest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Kagan~Alex";
		client.handleNewGame(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof HumanPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void handleNewGameSecondPlayerAITest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("AlexAI");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Kagan~AlexAI";
		client.handleNewGame(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof ComputerPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void handleNewGameSecondPlayerAIPlusTest() 
					throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("AlexAI+");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Kagan~AlexAI+";
		client.handleCommand(msg);
		assertEquals(5, client.getBoard().convertBallToInt(client.getBoard().getBall(0, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
		assertEquals(6, client.getBoard().convertBallToInt(client.getBoard().getBall(4, 5)));
		assertTrue(client.getThisPlayer() instanceof ComputerPlayer);
		assertTrue(client.getOpponentPlayer() instanceof HumanPlayer);
	}
	
	@Test
	void sendLegalSingleMoveTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Alex~Kagan";
		client.handleCommand(msg);
		client.sendMove(3, -1);
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 2)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 3)));
	}
	
	@Test
	void sendIllegalSingleMoveTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Alex~Kagan";
		client.handleCommand(msg);
		client.sendMove(6, -1);
		assertThat(OUTCONTENT.toString(), containsString("Not a legal move!"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendLegalDoubleMoveTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~1~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~1~0~0~0~0~0~0"
						  + "~Alex~Kagan";
		client.handleCommand(msg);
		client.sendMove(23, 6);
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(6, 0)));
		assertEquals(0, client.getBoard().convertBallToInt(client.getBoard().getBall(3, 2)));
	}
	
	@Test
	void sendIllegalDoubleMoveTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Alex~Kagan";
		client.handleCommand(msg);
		client.sendMove(23, 6);
		assertThat(OUTCONTENT.toString(), containsString("Please enter a single move!"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendLegalSingleMoveNotOurTurnTest() 
			throws ProtocolException, ServerUnavailableException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~5~3~4~2~5~3~6"
						  + "~4~6~3~4~3~1~2"
						  + "~5~3~2~1~2~6~5"
						  + "~4~1~4~0~4~1~4"
						  + "~5~6~2~1~5~6~2"
						  + "~3~1~5~4~6~5~3"
						  + "~6~3~6~2~1~2~1"
						  + "~Kagan~Alex";
		client.handleCommand(msg);
		client.sendMove(3, -1);
		assertThat(OUTCONTENT.toString(), containsString("Not your turn to play!"));
		OUTCONTENT.reset();
	}
	
	@Test
	void sendMoveNotInAGameTest() throws ProtocolException, ServerUnavailableException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		client.sendMove(3, -1);
		assertThat(OUTCONTENT.toString(), containsString("You are not in a game!"));
		OUTCONTENT.reset();
	}
	
	@Test
	void handleGameOverWinnerTest() 
			throws ServerUnavailableException, ProtocolException, IOException {
		client.handleHello(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + "desc");
		client.sendLogIn("Alex");
		String msg = "NEWGAME~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~0~0~0~0~0~0~0"
						  + "~Alex~Kagan";
		client.handleCommand(msg);
		String msg2 = ProtocolMessages.GAMEOVER +
				ProtocolMessages.DELIMITER + "VICTORY" + 
				ProtocolMessages.DELIMITER + "TheWinner";
		client.handleGameOver(msg2);
		assertThat(OUTCONTENT.toString(), containsString("The server evaluated the winner"));
		assertThat(OUTCONTENT.toString(), containsString("draw"));
		OUTCONTENT.reset();
	}
	
	@AfterAll
	static void restoreStream() {
	    System.setOut(ORIGINALOUT);
	}
}
