package dataaccess;

import java.sql.SQLException;

/**
 * Class for initializing DB
 */
public class DatabaseInitializer {

    private static final String[] CREATE_STATEMENTS = {
        """
        CREATE TABLE IF NOT EXISTS users (
            username VARCHAR(256) NOT NULL PRIMARY KEY,
            password VARCHAR(256) NOT NULL,
            email VARCHAR(256) NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS authTokens (
            authToken VARCHAR(256) NOT NULL PRIMARY KEY,
            username VARCHAR(256) NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS games (
            gameID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
            whiteUsername VARCHAR(256),
            blackUsername VARCHAR(256),
            gameName VARCHAR(256) NOT NULL,
            game TEXT NOT NULL
        )
        """
    };

    public static void initialize() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (String statement : CREATE_STATEMENTS) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to initialize database: " + e.getMessage());
        }
    }
}