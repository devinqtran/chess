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
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
        assertEquals("testuser", auth.username());
    }

    @Test
    void registerNegative() throws Exception {
        // Registering the same username twice throws
        facade.register("testuser", "password", "testuser@email.com");
        assertThrows(Exception.class, () ->
                facade.register("testuser", "password", "testuser@email.com")
        );
    }

    @Test
    void logoutPositive() throws Exception {
        // Register and then logout successfully
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
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
        facade.register("testuser", "password", "testuser@email.com");
        AuthData auth = facade.login("testuser", "password");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
        assertEquals("testuser", auth.username());
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
        // Register create games and list them
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
        facade.createGame(auth.authToken(), "Game 1");
        facade.createGame(auth.authToken(), "Game 2");
        GameData[] games = facade.listGames(auth.authToken());
        assertNotNull(games);
        assertEquals(2, games.length);
    }

    @Test
    void listGamesNegative() throws Exception {
        // Listing games with an invalid auth token throws
        assertThrows(Exception.class, () ->
                facade.listGames("invalidTokenThatDoesNotExist")
        );
    }

    @Test
    void createGamePositive() throws Exception {
        // Register and create a game verify valid game ID
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
        int gameID = facade.createGame(auth.authToken(), "Test Game");
        assertTrue(gameID > 0);
    }

    @Test
    void createGameNegative() throws Exception {
        // Create game with an invalid auth token throws
        assertThrows(Exception.class, () ->
                facade.createGame("invalidTokenThatDoesNotExist", "Test Game")
        );
    }

    @Test
    void joinGamePositive() throws Exception {
        // Register, create a game, and join as white
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
        int gameID = facade.createGame(auth.authToken(), "Test Game");
        assertDoesNotThrow(() -> facade.joinGame(auth.authToken(), gameID, "WHITE"));
    }

    @Test
    void joinGameNegative() throws Exception {
        // Joining a nonexistent game throws exception
        AuthData auth = facade.register("testuser", "password", "testuser@email.com");
        assertThrows(Exception.class, () ->
                facade.joinGame(auth.authToken(), 99999, "WHITE")
        );
    }

}
