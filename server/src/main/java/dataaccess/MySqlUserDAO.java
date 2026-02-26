package dataaccess;

import model.UserData;
import java.sql.SQLException;

/**
 * MySQL version of UserDAO interface
 * Each method opens connection, prepares SQL statement, fills values, executes, then reads results/errors
 */
public class MySqlUserDAO implements UserDAO {

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        var sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Error code for duplicate primary key (existing user)
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("Error: already taken");
            }
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        var sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("TRUNCATE TABLE users")) {
            ps.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }
}
