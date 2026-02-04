package dataaccess;


import model.UserData;
import java.util.HashMap;
import java.util.Map;


/**
 * In-memory implementation of UserDAO using HashMap
 */
public class MemoryUserDAO implements UserDAO {


    private final Map<String, UserData> users = new HashMap<>();


    @Override
    public void insertUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null) {
            throw new DataAccessException("User or username cannot be null");
        }
        if (users.containsKey(user.username())) {
            throw new DataAccessException("User already exists");
        }
        users.put(user.username(), user);
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null) {
            throw new DataAccessException("Username cannot be null");
        }
        return users.get(username);
    }


    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }
}
