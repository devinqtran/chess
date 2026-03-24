package server;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import com.google.gson.Gson;
import dataaccess.*;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketHandler {
    private final WebSocketSessions sessions = new WebSocketSessions();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        System.out.println("WebSocket message received: " + message);
        UserGameCommand command = gson.fromJson(message, UserGameCommand.class);

        try {
            // Validate the auth token
            AuthData auth = authDAO.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Error: invalid auth token");
                return;
            }

            // Route to handler
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(session, command, auth.username());
                case MAKE_MOVE -> handleMakeMove(session,
                        gson.fromJson(message, MakeMoveCommand.class), auth.username());
                case LEAVE -> handleLeave(session, command, auth.username());
                case RESIGN -> handleResign(session, command, auth.username());
            }
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // Leave handler method
    private void handleLeave(Session session, UserGameCommand command,
                             String username) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);

        if (username.equals(gameData.whiteUsername())) {
            gameDAO.updateGame(new GameData(gameData.gameID(), null,
                    gameData.blackUsername(), gameData.gameName(), gameData.game()));
        } else if (username.equals(gameData.blackUsername())) {
            gameDAO.updateGame(new GameData(gameData.gameID(), gameData.whiteUsername(),
                    null, gameData.gameName(), gameData.game()));
        }
        // Remove session from the tracking
        sessions.removeSession(gameID, session);

        // Notify the other clients
        var notification = new NotificationMessage(username + " left the game");
        sessions.broadcast(gameID, gson.toJson(notification), session);
    }

    // Resign handler method
    private void handleResign(Session session, UserGameCommand command, String username) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);

        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Make sure it is a player resigning
        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            sendError(session, "Error: observers cannot resign");
            return;
        }

        // Check game status
        if (gameData.game().isGameOver()) {
            sendError(session, "Error: the game is already over");
            return;
        }

        // Update and mark game as over
        gameData.game().setGameOver(true);
        gameDAO.updateGame(gameData);

        // Notify all clients
        var notification = new NotificationMessage(username + " has resigned. Game over.");
        sessions.broadcastAll(gameID, gson.toJson(notification));
    }

    // Make move handler method
    private void handleMakeMove(Session session, MakeMoveCommand command, String username) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);

        // Check and see if the game exists
        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Check game status
        if (gameData.game().isGameOver()) {
            sendError(session, "Error: the game is already over");
            return;
        }

        // Check which player's turn it is
        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            sendError(session, "Error: you are not a player in this game");
            return;
        }
        if (playerColor != gameData.game().getTeamTurn()) {
            sendError(session, "Error: it is not your turn");
            return;
        }

        // Attempt to make the move
        try {
            gameData.game().makeMove(command.getMove());
        } catch (chess.InvalidMoveException e) {
            sendError(session, "Error: invalid move - " + e.getMessage());
            return;
        }

        // Save updated game to database
        gameDAO.updateGame(gameData);

        // Send the new and updated board to all clients
        var loadGame = new LoadGameMessage(gameData.game());
        sessions.broadcastAll(gameID, gson.toJson(loadGame));

        // Notify others of move
        String moveDesc = command.getMove().getStartPosition() +
                " to " + command.getMove().getEndPosition();
        var notification = new NotificationMessage(username + " moved " + moveDesc);
        sessions.broadcast(gameID, gson.toJson(notification), session);

        // Check for 'check/checkmate/stalemate'
        ChessGame.TeamColor opponent = playerColor == ChessGame.TeamColor.WHITE ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (gameData.game().isInCheckmate(opponent)) {
            var msg = new NotificationMessage(username + " has put the opponent in checkmate! Game over.");
            sessions.broadcastAll(gameID, gson.toJson(msg));
            gameData.game().setGameOver(true);
            gameDAO.updateGame(gameData);
        } else if (gameData.game().isInStalemate(opponent)) {
            var msg = new NotificationMessage("Stalemate! The game is a draw.");
            sessions.broadcastAll(gameID, gson.toJson(msg));
            gameData.game().setGameOver(true);
            gameDAO.updateGame(gameData);
        } else if (gameData.game().isInCheck(opponent)) {
            var msg = new NotificationMessage(username + " has put the opponent in check!");
            sessions.broadcastAll(gameID, gson.toJson(msg));
        }
    }

    // Method to retrieve player color
    private ChessGame.TeamColor getPlayerColor(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    // Connect handler method
    private void handleConnect(Session session, UserGameCommand command,
                               String username) throws IOException, DataAccessException {
        int gameID = command.getGameID();
        GameData gameData = gameDAO.getGame(gameID);

        if (gameData == null) {
            sendError(session, "Error: game not found");
            return;
        }

        // Add session to the game
        sessions.addSession(gameID, session);

        // Send LOAD_GAME to the connecting client
        var loadGame = new LoadGameMessage(gameData.game());
        sessions.sendToSession(session, gson.toJson(loadGame));

        // Build notification message
        String notification;
        if (username.equals(gameData.whiteUsername())) {
            notification = username + " joined the game as WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            notification = username + " joined the game as BLACK";
        } else {
            notification = username + " is observing the game";
        }

        // Send to all other clients
        var notificationMessage = new NotificationMessage(notification);
        sessions.broadcast(gameID, gson.toJson(notificationMessage), session);
    }

    // Method for sending errors
    private void sendError(Session session, String message) throws IOException {
        var error = new ErrorMessage(message);
        sessions.sendToSession(session, gson.toJson(error));
    }
}
