package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GameService
 */
public class GameServiceTests {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserService userService;
    private GameService gameService;
    private String validAuthToken;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        // Clear data before each test
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
        // Create a user and get auth token for tests
        AuthData auth = userService.register("testuser", "password", "test@email.com");
        validAuthToken = auth.authToken();
    }

    // List Games Tests
    @Test
    @DisplayName("List games positive test")
    public void listGamesPositive() throws DataAccessException {
        gameService.createGame(validAuthToken, "Game1");
        gameService.createGame(validAuthToken, "Game2");
        Collection<GameData> games = gameService.listGames(validAuthToken);
        assertNotNull(games);
        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List games negative test (Unauthorized)")
    public void listGamesNegative() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.listGames("invalidtoken");
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    // Create Game Tests
    @Test
    @DisplayName("Create game positive test")
    public void createGamePositive() throws DataAccessException {
        int gameID = gameService.createGame(validAuthToken, "TestGame");
        assertTrue(gameID > 0);
        // Verify game was created
        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
        assertEquals("TestGame", game.gameName());
    }

    @Test
    @DisplayName("Create game negative test (Unauthorized)")
    public void createGameNegative() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalidtoken", "TestGame");
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Create game negative test (Bad Request)")
    public void createGameBadRequest() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(validAuthToken, null);
        });
        assertTrue(exception.getMessage().contains("bad request"));
    }

    // Join Game Tests
    @Test
    @DisplayName("Join game positive test")
    public void joinGamePositive() throws DataAccessException {
        int gameID = gameService.createGame(validAuthToken, "TestGame");
        assertDoesNotThrow(() -> {
            gameService.joinGame(validAuthToken, gameID, ChessGame.TeamColor.WHITE);
        });
        // Verify user was added to game
        GameData game = gameDAO.getGame(gameID);
        assertEquals("testuser", game.whiteUsername());
    }

    @Test
    @DisplayName("Join game negative test (Spot Already Taken)")
    public void joinGameNegative() throws DataAccessException {
        int gameID = gameService.createGame(validAuthToken, "TestGame");
        gameService.joinGame(validAuthToken, gameID, ChessGame.TeamColor.WHITE);
        // Create another user
        AuthData auth2 = userService.register("user2", "pass2", "user2@email.com");
        // Try to join same spot
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(auth2.authToken(), gameID, ChessGame.TeamColor.WHITE);
        });
        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Join game negative test (Invalid Game)")
    public void joinGameInvalidGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(validAuthToken, 999, ChessGame.TeamColor.WHITE);
        });
        assertTrue(exception.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("Join game positive test (Spectator)")
    public void joinGameSpectator() throws DataAccessException {
        int gameID = gameService.createGame(validAuthToken, "TestGame");
        assertDoesNotThrow(() -> {
            gameService.joinGame(validAuthToken, gameID, null);
        });
        // Verify no players were added
        GameData game = gameDAO.getGame(gameID);
        assertNull(game.whiteUsername());
        assertNull(game.blackUsername());
    }
}