package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit tests for UserService
 */
public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
        // Clear data before each test
        userDAO.clear();
        authDAO.clear();
    }

    // Register Tests
    @Test
    @DisplayName("Register user positive test")
    public void registerPositive() throws DataAccessException {
        AuthData result = userService.register("testuser", "password123", "test@email.com");
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
        // Verify user was created
        assertNotNull(userDAO.getUser("testuser"));
    }

    @Test
    @DisplayName("Register user negative test (User Already Exists)")
    public void registerNegative() throws DataAccessException {
        userService.register("testuser", "password123", "test@email.com");
        // Try to register same user again
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register("testuser", "different", "different@email.com");
        });
        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    @DisplayName("Register user negative test (Bad Request)")
    public void registerBadRequest() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.register(null, "password", "email@test.com");
        });
        assertTrue(exception.getMessage().contains("bad request"));
    }

    // Login Tests
    @Test
    @DisplayName("Login user positive test")
    public void loginPositive() throws DataAccessException {
        userService.register("testuser", "password123", "test@email.com");
        AuthData result = userService.login("testuser", "password123");
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    @DisplayName("Login user negative test (Wrong Password)")
    public void loginNegative() throws DataAccessException {
        userService.register("testuser", "password123", "test@email.com");
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    @Test
    @DisplayName("Login user negative test (User Doesn't Exist)")
    public void loginUserNotFound() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.login("nonexistent", "password");
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    // Logout Tests
    @Test
    @DisplayName("Logout user positive test")
    public void logoutPositive() throws DataAccessException {
        AuthData auth = userService.register("testuser", "password123", "test@email.com");
        assertDoesNotThrow(() -> {
            userService.logout(auth.authToken());
        });
        // Verify token was deleted
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    @DisplayName("Logout user negative test (Invalid Token)")
    public void logoutNegative() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            userService.logout("invalidtoken");
        });
        assertTrue(exception.getMessage().contains("unauthorized"));
    }
}
