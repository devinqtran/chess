package dataaccess;


import model.AuthData;
import java.util.HashMap;
import java.util.Map;


/**
 * In-memory implementation of AuthDAO using HashMap
 */
public class MemoryAuthDAO implements AuthDAO {
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void insertAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.authToken() == null) {
            throw new DataAccessException("Auth or authToken cannot be null");
        }
        authTokens.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("AuthToken cannot be null");
        }
        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) {
            throw new DataAccessException("AuthToken cannot be null");
        }
        if (!authTokens.containsKey(authToken)) {
            throw new DataAccessException("AuthToken not found");
        }
        authTokens.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        authTokens.clear();
    }
}
