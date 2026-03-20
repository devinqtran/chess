package client;

import chess.ChessGame;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;
import ui.EscapeSequences;
import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import java.util.Scanner;

public class GameplayClient implements NotificationHandler {
    private final WebSocketFacade ws;
    private final Scanner scanner;
    private final String authToken;
    private final int gameID;
    private ChessGame currentGame = new ChessGame();
    private final String playerColor;
    private final Gson gson = new Gson();

    public GameplayClient(ServerFacade facade, Scanner scanner, String authToken, int gameID, String playerColor, int port) throws Exception {
        this.scanner = scanner;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        // Open WebSocket connection and send CONNECT
        this.ws = new WebSocketFacade(port, this);
        ws.connect(authToken, gameID);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public State run() {
        System.out.print("\n[IN GAME] >>> ");
        String line = scanner.nextLine().trim();
        System.out.println("Command coming soon: " + line);
        return State.GAMEPLAY;
    }

    @Override
    public void notify(String message) {
        ServerMessage base = gson.fromJson(message, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage loadGame = gson.fromJson(message, LoadGameMessage.class);
                currentGame = loadGame.getGame();
                boolean isWhite = !"BLACK".equals(playerColor);
                BoardRenderer.render(currentGame, isWhite);
            }
            case NOTIFICATION -> {
                NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW + "\n" + notification.getMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
            case ERROR -> {
                ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "\nError: " + error.getErrorMessage() + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }
}
