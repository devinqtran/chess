package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void registerPositive() throws Exception {
        AuthData auth = facade.register("alice", "password", "alice@email.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
        assertEquals("alice", auth.username());
    }

    @Test
    void registerNegative() throws Exception {
        // Registering the same username twice throws
        facade.register("alice", "password", "alice@email.com");
        assertThrows(Exception.class, () ->
                facade.register("alice", "password", "alice@email.com")
        );
    }

    @Test
    void logoutPositive() throws Exception {
        // Register and then logout successfully
        AuthData auth = facade.register("alice", "password", "alice@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegative() throws Exception {
        // Logging out with a bad token throws exception
        assertThrows(Exception.class, () ->
                facade.logout("invalidTokenThatDoesNotExist")
        );
    }

    @Test
    void loginPositive() throws Exception {
        // Register first then login
        facade.register("alice", "password", "alice@email.com");
        AuthData auth = facade.login("alice", "password");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
        assertEquals("alice", auth.username());
    }

    @Test
    void loginNegative() throws Exception {
        // Login with a fake username
        assertThrows(Exception.class, () ->
                facade.login("nonexistentUser", "password")
        );
    }

    @Test
    void listGamesPositive() throws Exception {
        // Register, create games then list them
        AuthData auth = facade.register("alice", "password", "alice@email.com");
        facade.createGame(auth.authToken(), "Game 1");
        facade.createGame(auth.authToken(), "Game 2");
        GameData[] games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(2, games.length);
    }

    @Test
    void listGamesNegative() throws Exception {
        // Listing games with an invalid auth token should throw
        assertThrows(Exception.class, () ->
                facade.listGames("invalidTokenThatDoesNotExist")
        );
    }

}
