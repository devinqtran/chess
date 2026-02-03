package model;

import chess.ChessGame;

/**
 * Represents a chess game in the application
 *
 * @param gameID Unique identifier for the game
 * @param whiteUsername Username of the player playing as white (null if no player)
 * @param blackUsername Username of the player playing as black (null if no player)
 * @param gameName The name/title of the game
 * @param game The actual ChessGame object containing the game state
 */
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

}