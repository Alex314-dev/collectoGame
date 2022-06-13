package ss.project.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ss.project.server.CollectoClientHandler;
import ss.project.server.CollectoServer;
import ss.project.protocol.*;

class ServerTest {
	private final static ByteArrayOutputStream OUTCONTENT = new ByteArrayOutputStream();
	private final static PrintStream ORIGINALOUT = System.out;
	private CollectoServer server;
	private CollectoClientHandler cch1;
	private CollectoClientHandler cch2;
	private CollectoClientHandler cch3;
	private CollectoClientHandler cch4;
    private List<CollectoClientHandler> players;
	private List<String> loggedIn;
	private List<CollectoClientHandler> inQueue;
	
	
	@BeforeEach
	void setUp() throws Exception {
		System.setOut(new PrintStream(OUTCONTENT));
		server = new CollectoServer();
		players = server.getPlayers();
		inQueue = server.getQueue();
		loggedIn = server.getLoggedInList();
		cch1 = new CollectoClientHandler("Alex");
		cch2 = new CollectoClientHandler("Kagan");
		cch3 = new CollectoClientHandler("Tom");
		cch4 = new CollectoClientHandler("Alex");
	}
	
	@Test
	void doHelloTest() {
		String clientDesc = "client desc.";
		String serverDesc = "Alex and Kagan's Beatiful Server";
		String helloMessage = server.doHello(clientDesc);
		assertThat(OUTCONTENT.toString(),
				containsString("> Handshake with " + clientDesc));
		OUTCONTENT.reset();
		assertEquals(ProtocolMessages.HELLO + ProtocolMessages.DELIMITER + serverDesc,
				helloMessage);
	}
	
	@Test
	void addAndRemoveClientTest() {
		players.add(cch1);
		players.add(cch2);
		players.add(cch3);
		players.add(cch4);
	 	server.removeClient(cch1);
		assertTrue(players.size() == 3);
		List<CollectoClientHandler> test = new ArrayList<>(List.of(cch2, cch3, cch4));
		assertEquals(test, server.getPlayers());
	}
	
	@Test
	void doLogInTest() {
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch1.getName(), cch1));
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch2.getName(), cch2));
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch3.getName(), cch3));
		assertEquals(ProtocolMessages.ALREADYLOGGEDIN, server.doLogIn(cch4.getName(), cch4));
		assertTrue(loggedIn.contains(cch1.getName()));
		assertTrue(loggedIn.contains(cch2.getName()));
		assertTrue(loggedIn.contains(cch3.getName()));
	}
	
	@Test
	void doListTest() {
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch1.getName(), cch1));
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch2.getName(), cch2));
		assertEquals(ProtocolMessages.LOGIN, server.doLogIn(cch3.getName(), cch3));
		assertEquals(ProtocolMessages.LIST + ProtocolMessages.DELIMITER +
				cch1.getName() + ProtocolMessages.DELIMITER +
				cch2.getName() + ProtocolMessages.DELIMITER +
				cch3.getName(), server.doList());
	}
	
	@Test
	void doQueueTest() {
		server.doQueue(cch1);
		assertTrue(inQueue.contains(cch1));
		server.doQueue(cch1);
		assertFalse(inQueue.contains(cch1));
		server.doQueue(cch2);
		assertTrue(inQueue.contains(cch2));
		inQueue.clear();
	}
	
	@Test
	void doGameTest() {
		cch1.setName("test1");
		cch2.setName("test2");
		try {
			server.doGame(cch1, cch2);
		} catch (NullPointerException e) {
			// coming from the view.showMessage(), simply ignored.
		}
		assertThat(OUTCONTENT.toString(), containsString(ProtocolMessages.NEWGAME));
		assertThat(OUTCONTENT.toString(), containsString("test1"));
		OUTCONTENT.reset();
	}
	
	@AfterAll
	static void restoreStream() {
	    System.setOut(ORIGINALOUT);
	}
}
