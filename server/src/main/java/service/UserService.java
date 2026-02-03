package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import java.util.UUID;

/**
 * Service class for user-related operations (register, login, logout)
 */
public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    /**
     * Register a new user
     * @param username The desired username
     * @param password The user's password
     * @param email The user's email
     * @return AuthData containing the new auth token and username
     * @throws DataAccessException if registration fails (e.g., username taken, bad request)
     */
    public AuthData register(String username, String password, String email) throws DataAccessException {
        // Validate input
        if (username == null || password == null || email == null ||
                username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // Check if user already exists
        if (userDAO.getUser(username) != null) {
            throw new DataAccessException("Error: already taken");
        }

        // Create new user
        UserData newUser = new UserData(username, password, email);
        userDAO.insertUser(newUser);

        // Create auth token
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);

        return authData;
    }

    /**
     * Login an existing user
     * @param username The username
     * @param password The password
     * @return AuthData containing the new auth token and username
     * @throws DataAccessException if login fails (e.g., wrong password, bad request)
     */
    public AuthData login(String username, String password) throws DataAccessException {
        // Validate input
        if (username == null || password == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Get user
        UserData user = userDAO.getUser(username);
        if (user == null || !user.password().equals(password)) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Create new auth token
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.insertAuth(authData);

        return authData;
    }

    /**
     * Logout a user
     * @param authToken The auth token to invalidate
     * @throws DataAccessException if logout fails (e.g., invalid token)
     */
    public void logout(String authToken) throws DataAccessException {
        // Verify auth token exists
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Delete auth token
        authDAO.deleteAuth(authToken);
    }
}