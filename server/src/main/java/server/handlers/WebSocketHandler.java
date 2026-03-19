package server;

import model.AuthData;
import model.GameData;
import websocket.commands.UserGameCommand;
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
                case MAKE_MOVE -> System.out.println("Make move coming soon");
                case LEAVE -> System.out.println("Leave coming soon");
                case RESIGN -> System.out.println("Resign coming soon");
            }
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // Helper methods
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

        // Send LOAD_GAME back to the connecting client
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

    private void sendError(Session session, String message) throws IOException {
        var error = new ErrorMessage(message);
        sessions.sendToSession(session, gson.toJson(error));
    }
}
