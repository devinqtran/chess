package client;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;
import java.util.Scanner;

public class GameplayClient implements NotificationHandler {
    private final WebSocketFacade ws;
    private final Scanner scanner;
    private final String authToken;
    private final int gameID;
    private final String playerColor;
    private final Gson gson = new Gson();

    public GameplayClient(ServerFacade facade, Scanner scanner,
                          String authToken, int gameID,
                          String playerColor, int port) throws Exception {
        this.scanner = scanner;
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;

        // Open WebSocket connection and send CONNECT
        this.ws = new WebSocketFacade(port, this);
        ws.connect(authToken, gameID);
    }

    public State run() {
        System.out.print("\n[IN GAME] >>> ");
        String line = scanner.nextLine().trim();
        System.out.println("Command coming soon: " + line);
        return State.GAMEPLAY;
    }

    @Override
    public void notify(ServerMessage message) {
        System.out.println("Message received: " + message.getServerMessageType());
    }
}
