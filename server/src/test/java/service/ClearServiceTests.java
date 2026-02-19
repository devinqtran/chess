package service;


import dataaccess.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for ClearService
 */
public class ClearServiceTests {
    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);
        // Clear data before each test
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }

    @Test
    @DisplayName("Clear service positive test")
    public void clearPositive() throws DataAccessException {
        // Add some data
        var auth = userService.register("user1", "pass1", "email1@test.com");
        gameService.createGame(auth.authToken(), "Game1");
        // Verify data exists
        assertNotNull(userDAO.getUser("user1"));
        assertFalse(gameDAO.listGames().isEmpty());
        assertNotNull(authDAO.getAuth(auth.authToken()));
        // Clear all data
        clearService.clear();
        // Verify all data is gone
        assertNull(userDAO.getUser("user1"));
        assertTrue(gameDAO.listGames().isEmpty());
        assertNull(authDAO.getAuth(auth.authToken()));
    }
}

