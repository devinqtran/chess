package dataaccess;


import model.UserData;


/**
 * Data Access Object interface for User operations
 */
public interface UserDAO {

    /**
     * Create a new user in the database
     * @param user The user data to insert
     * @throws DataAccessException if the user already exists or insertion fails
     */
    void insertUser(UserData user) throws DataAccessException;

    /**
     * Retrieve a user by username
     * @param username The username to look up
     * @return The UserData object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    UserData getUser(String username) throws DataAccessException;

    /**
     * Clear all users from the database
     * @throws DataAccessException if clearing fails
     */
    void clear() throws DataAccessException;
}
