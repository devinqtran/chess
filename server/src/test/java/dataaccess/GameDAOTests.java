package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GameDAOTests {

    private static GameDAO gameDAO;

    @BeforeAll
    public static void init() throws DataAccessException {
        DatabaseInitializer.initialize();
        gameDAO = new MySqlGameDAO();
    }

    @BeforeEach
    public void clearAll() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    public void insertGamePositive() throws DataAccessException {
        int id = gameDAO.insertGame(new GameData(0, null, null, "TestGame", new ChessGame()));
        assertTrue(id > 0);
    }

    @Test
    public void insertGameNegativeNullName() {
        assertThrows(DataAccessException.class, () ->
                gameDAO.insertGame(new GameData(0, null, null, null, new ChessGame()))
        );
    }

    @Test
    public void getGamePositive() throws DataAccessException {
        int id = gameDAO.insertGame(new GameData(0, null, null, "FindMe", new ChessGame()));
        GameData result = gameDAO.getGame(id);
        assertNotNull(result);
        assertEquals("FindMe", result.gameName());
    }

    @Test
    public void getGameNegativeNotFound() throws DataAccessException {
        GameData result = gameDAO.getGame(999999);
        assertNull(result);
    }

    @Test
    public void listGamesPositive() throws DataAccessException {
        gameDAO.insertGame(new GameData(0, null, null, "Game1", new ChessGame()));
        gameDAO.insertGame(new GameData(0, null, null, "Game2", new ChessGame()));
        var games = gameDAO.listGames();
        assertEquals(2, games.size());
    }

    @Test
    public void listGamesNegativeEmpty() throws DataAccessException {
        var games = gameDAO.listGames();
        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    public void updateGamePositive() throws DataAccessException {
        int id = gameDAO.insertGame(new GameData(0, null, null, "UpdateMe", new ChessGame()));
        GameData updated = new GameData(id, "whitePlayer", null, "UpdateMe", new ChessGame());
        gameDAO.updateGame(updated);
        GameData result = gameDAO.getGame(id);
        assertEquals("whitePlayer", result.whiteUsername());
    }

    @Test
    public void updateGameNegativeNotFound() {
        assertThrows(DataAccessException.class, () ->
                gameDAO.updateGame(new GameData(999999, "user", null, "Ghost", new ChessGame()))
        );
    }

    @Test
    public void clearGamesPositive() throws DataAccessException {
        int id = gameDAO.insertGame(new GameData(0, null, null, "ClearMe", new ChessGame()));
        gameDAO.clear();
        assertNull(gameDAO.getGame(id));
    }

    @Test
    public void gameBoardPersistsAfterInsert() throws DataAccessException {
        ChessGame game = new ChessGame();
        int id = gameDAO.insertGame(new GameData(0, null, null, "BoardTest", game));
        GameData result = gameDAO.getGame(id);
        assertNotNull(result.game());
        assertNotNull(result.game().getBoard());
        assertEquals(game.getTeamTurn(), result.game().getTeamTurn());
    }

    @Test
    public void gameStateUpdatesAfterMove() throws DataAccessException {
        ChessGame game = new ChessGame();
        int id = gameDAO.insertGame(new GameData(0, null, null, "MoveTest", game));

        try {
            game.makeMove(new ChessMove(
                    new ChessPosition(2, 1),
                    new ChessPosition(3, 1),
                    null
            ));
        } catch (chess.InvalidMoveException e) {
            fail("Move should be valid: " + e.getMessage());
        }

        gameDAO.updateGame(new GameData(id, null, null, "MoveTest", game));
        GameData result = gameDAO.getGame(id);

        assertNotNull(result.game());
        assertEquals(ChessGame.TeamColor.BLACK, result.game().getTeamTurn());
        assertNull(result.game().getBoard().getPiece(new ChessPosition(2, 1)));
        assertNotNull(result.game().getBoard().getPiece(new ChessPosition(3, 1)));
    }

    @Test
    public void addPlayersToGame() throws DataAccessException {
        int id = gameDAO.insertGame(new GameData(0, null, null, "PlayerTest", new ChessGame()));
        GameData withPlayers = new GameData(id, "whitePlayer", "blackPlayer", "PlayerTest", new ChessGame());
        gameDAO.updateGame(withPlayers);
        GameData result = gameDAO.getGame(id);
        assertEquals("whitePlayer", result.whiteUsername());
        assertEquals("blackPlayer", result.blackUsername());
    }
}