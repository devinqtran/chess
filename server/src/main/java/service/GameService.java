package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import java.util.Collection;

/**
 * Service class for game-related operations (list, create, join)
 */
public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * List all games
     * @param authToken The auth token for authentication
     * @return Collection of all games
     * @throws DataAccessException if unauthorized or listing fails
     */
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        // Verify auth token
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        return gameDAO.listGames();
    }

    /**
     * Create a new game
     * @param authToken The auth token for authentication
     * @param gameName The name of the new game
     * @return The gameID of the created game
     * @throws DataAccessException if unauthorized or creation fails
     */
    public int createGame(String authToken, String gameName) throws DataAccessException {
        // Verify auth token
        if (authDAO.getAuth(authToken) == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Validate input
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // Create new game
        ChessGame newChessGame = new ChessGame();
        GameData newGame = new GameData(0, null, null, gameName, newChessGame);
        return gameDAO.insertGame(newGame);
    }

    /**
     * Join a game as a player
     * @param authToken The auth token for authentication
     * @param gameID The ID of the game to join
     * @param playerColor The color to play as (WHITE or BLACK), or null to spectate
     * @throws DataAccessException if unauthorized, bad request, or spot already taken
     */
    public void joinGame(String authToken, Integer gameID, ChessGame.TeamColor playerColor)
            throws DataAccessException {
        // Verify auth token
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // Validate gameID
        if (gameID == null) {
            throw new DataAccessException("Error: bad request");
        }

        // Get game
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        // If playerColor is null, they're just spectating (no update needed)
        if (playerColor == null) {
            return;
        }

        // Check if spot is available and update game
        String username = auth.username();
        GameData updatedGame;

        if (playerColor == ChessGame.TeamColor.WHITE) {
            // Check if WHITE spot is taken by a DIFFERENT user
            if (game.whiteUsername() != null && !game.whiteUsername().equals(username)) {
                throw new DataAccessException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), username, game.blackUsername(),
                    game.gameName(), game.game());
        } else { // BLACK
            // Check if BLACK spot is taken by a DIFFERENT user
            if (game.blackUsername() != null && !game.blackUsername().equals(username)) {
                throw new DataAccessException("Error: already taken");
            }
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), username,
                    game.gameName(), game.game());
        }

        gameDAO.updateGame(updatedGame);
    }
}