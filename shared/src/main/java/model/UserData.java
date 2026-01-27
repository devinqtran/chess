package model;

/**
 * Represents a user in the chess application
 *
 * @param username The user's unique username
 * @param password The user's password (should be hashed in production)
 * @param email The user's email address
 */
public record UserData(String username, String password, String email) {

}