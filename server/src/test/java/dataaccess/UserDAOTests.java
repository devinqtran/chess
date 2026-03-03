package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests {

    private static UserDAO userDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        DatabaseInitializer.initialize();
        userDAO = new MySqlUserDAO();
    }

    @BeforeEach
    public void clearAll() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    public void insertUserPositive() throws DataAccessException {
        userDAO.insertUser(new UserData("alice", "hashedpw", "alice@test.com"));
        UserData result = userDAO.getUser("alice");
        assertNotNull(result);
        assertEquals("alice", result.username());
    }

    @Test
    public void insertUserNegativeDuplicate() throws DataAccessException {
        userDAO.insertUser(new UserData("alice", "hashedpw", "alice@test.com"));
        assertThrows(DataAccessException.class, () ->
                userDAO.insertUser(new UserData("alice", "other", "other@test.com"))
        );
    }

    @Test
    public void getUserPositive() throws DataAccessException {
        userDAO.insertUser(new UserData("bob", "pw", "bob@test.com"));
        UserData result = userDAO.getUser("bob");
        assertNotNull(result);
        assertEquals("bob@test.com", result.email());
    }

    @Test
    public void getUserNegativeNotFound() throws DataAccessException {
        UserData result = userDAO.getUser("nonexistent");
        assertNull(result);
    }

    @Test
    public void clearUsersPositive() throws DataAccessException {
        userDAO.insertUser(new UserData("carol", "pw", "carol@test.com"));
        userDAO.clear();
        assertNull(userDAO.getUser("carol"));
    }
}