package dataaccess;


import model.AuthData;


/**
 * Data Access Object interface for Authentication operations
 */
public interface AuthDAO {


    /**
     * Create a new authentication token
     * @param auth The auth data to insert
     * @throws DataAccessException if insertion fails
     */
    void insertAuth(AuthData auth) throws DataAccessException;


    /**
     * Retrieve authentication data by token
     * @param authToken The auth token to look up
     * @return The AuthData object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    AuthData getAuth(String authToken) throws DataAccessException;


    /**
     * Delete an authentication token (logout)
     * @param authToken The auth token to delete
     * @throws DataAccessException if the token doesn't exist or deletion fails
     */
    void deleteAuth(String authToken) throws DataAccessException;


    /**
     * Clear all authentication tokens from the database
     * @throws DataAccessException if clearing fails
     */
    void clear() throws DataAccessException;
}
