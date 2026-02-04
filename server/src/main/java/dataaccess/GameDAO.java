package dataaccess;


import model.GameData;
import java.util.Collection;


/**
 * Data Access Object interface for Game operations
 */
public interface GameDAO {


    /**
     * Create a new game in the database
     * @param game The game data to insert
     * @return The gameID of the created game
     * @throws DataAccessException if insertion fails
     */
    int insertGame(GameData game) throws DataAccessException;


    /**
     * Retrieve a game by its ID
     * @param gameID The game ID to look up
     * @return The GameData object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    GameData getGame(int gameID) throws DataAccessException;


    /**
     * Retrieve all games from the database
     * @return Collection of all GameData objects
     * @throws DataAccessException if retrieval fails
     */
    Collection<GameData> listGames() throws DataAccessException;


    /**
     * Update an existing game in the database
     * @param game The updated game data
     * @throws DataAccessException if the game doesn't exist or update fails
     */
    void updateGame(GameData game) throws DataAccessException;


    /**
     * Clear all games from the database
     * @throws DataAccessException if clearing fails
     */
    void clear() throws DataAccessException;
}
