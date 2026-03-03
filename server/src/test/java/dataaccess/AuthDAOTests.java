package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOTests {

    private static AuthDAO authDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        DatabaseInitializer.initialize();
        authDAO = new MySqlAuthDAO();
    }

    @BeforeEach
    public void clearAll() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    public void insertAuthPositive() throws DataAccessException {
        authDAO.insertAuth(new AuthData("token-abc", "alice"));
        AuthData result = authDAO.getAuth("token-abc");
        assertNotNull(result);
        assertEquals("alice", result.username());
    }

    @Test
    public void insertAuthNegativeDuplicate() throws DataAccessException {
        authDAO.insertAuth(new AuthData("token-abc", "alice"));
        assertThrows(DataAccessException.class, () ->
                authDAO.insertAuth(new AuthData("token-abc", "bob"))
        );
    }

    @Test
    public void getAuthPositive() throws DataAccessException {
        authDAO.insertAuth(new AuthData("token-xyz", "dave"));
        AuthData result = authDAO.getAuth("token-xyz");
        assertNotNull(result);
        assertEquals("token-xyz", result.authToken());
    }

    @Test
    public void getAuthNegativeNotFound() throws DataAccessException {
        AuthData result = authDAO.getAuth("bogus-token");
        assertNull(result);
    }

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        authDAO.insertAuth(new AuthData("token-del", "eve"));
        authDAO.deleteAuth("token-del");
        assertNull(authDAO.getAuth("token-del"));
    }

    @Test
    public void deleteAuthNegativeNotFound() {
        assertThrows(DataAccessException.class, () ->
                authDAO.deleteAuth("nonexistent-token")
        );
    }

    @Test
    public void clearAuthPositive() throws DataAccessException {
        authDAO.insertAuth(new AuthData("t1", "u1"));
        authDAO.insertAuth(new AuthData("t2", "u2"));
        authDAO.clear();
        assertNull(authDAO.getAuth("t1"));
        assertNull(authDAO.getAuth("t2"));
    }
}