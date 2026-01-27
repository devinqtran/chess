package model;

/**
 * Represents an authentication token for a logged-in user
 *
 * @param authToken A unique token identifying this authentication session
 * @param username The username associated with this auth token
 */
public record AuthData(String authToken, String username) {

}