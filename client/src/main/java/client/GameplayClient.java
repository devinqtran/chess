package client;

import chess.ChessGame;
import chess.ChessPosition;
import chess.ChessMove;
import chess.ChessPiece;
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
        System.out.print(EscapeSequences.SET_TEXT_COLOR_YELLOW + "\n[IN GAME] >>> " + EscapeSequences.RESET_TEXT_COLOR);
        String line = scanner.nextLine().trim();
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                printHelp();
                yield State.GAMEPLAY;
            }
            case "redraw" -> {
                drawBoard();
                yield State.GAMEPLAY;
            }
            case "leave" -> handleLeave();
            case "move" -> {
                handleMove(tokens);
                yield State.GAMEPLAY;
            }
            case "resign" -> {
                handleResign();
                yield State.GAMEPLAY;
            }
            case "highlight" -> {
                handleHighlight(tokens);
                yield State.GAMEPLAY;
            }
            default -> {
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Unknown command. Type 'help' for options." + EscapeSequences.RESET_TEXT_COLOR);
                yield State.GAMEPLAY;
            }
        };
    }

    private void drawBoard() {
        boolean isWhite = !"BLACK".equals(playerColor);
        BoardRenderer.render(currentGame, isWhite);
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
                System.out.println(EscapeSequences.SET_TEXT_COLOR_YELLOW +
                        "\n" + notification.getMessage() +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
            case ERROR -> {
                ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                        "\n" + error.getErrorMessage() +
                        EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    // Method for help command
    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_BOLD +
                EscapeSequences.SET_TEXT_COLOR_YELLOW +
                "Available commands:" +
                EscapeSequences.RESET_TEXT_COLOR +
                EscapeSequences.RESET_TEXT_BOLD_FAINT);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  redraw" +
                EscapeSequences.RESET_TEXT_COLOR +
                "                   - Redraw the chess board");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  move <from> <to>" +
                EscapeSequences.RESET_TEXT_COLOR +
                "         - Make a move (e.g. move e2 e4)");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  highlight <square>" +
                EscapeSequences.RESET_TEXT_COLOR +
                "      - Highlight legal moves for a piece");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  resign" +
                EscapeSequences.RESET_TEXT_COLOR +
                "                   - Forfeit the game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  leave" +
                EscapeSequences.RESET_TEXT_COLOR +
                "                    - Leave the game");
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA +
                "  help" +
                EscapeSequences.RESET_TEXT_COLOR +
                "                     - Show this help text");
    }

    // Method for leave command
    private State handleLeave() {
        try {
            ws.leave(authToken, gameID);
            ws.close();
            System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN +
                    "You left the game." +
                    EscapeSequences.RESET_TEXT_COLOR);
            return State.POSTLOGIN;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "Failed to leave: " + e.getMessage() +
                    EscapeSequences.RESET_TEXT_COLOR);
            return State.GAMEPLAY;
        }
    }

    // Method for move command
    private void handleMove(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "Usage: move <from> <to> [promotion]  (e.g. move e2 e4)" +
                    EscapeSequences.RESET_TEXT_COLOR);
            return;
        }
        try {
            ChessPosition from = parsePosition(tokens[1]);
            ChessPosition to = parsePosition(tokens[2]);

            // Check for promotion piece
            ChessPiece.PieceType promotion = null;
            if (tokens.length == 4) {
                promotion = parsePromotion(tokens[3]);
            }

            ChessMove move = new ChessMove(from, to, promotion);
            ws.makeMove(authToken, gameID, move);

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED +
                    "Invalid move: " + e.getMessage() +
                    EscapeSequences.RESET_TEXT_COLOR);
        }
    }

    // Method for resign command
    private void handleResign() {
        System.out.println("Resign not yet implemented!");
    }

    // Method for highlight command
    private void handleHighlight(String[] tokens) {
        System.out.println("Highlight not yet implemented!");
    }

    // Helper methods for parsing position and promotion
    private ChessPosition parsePosition(String pos) throws Exception {
        if (pos.length() != 2) {
            throw new Exception("Invalid position: " + pos);
        }
        char colChar = Character.toLowerCase(pos.charAt(0));
        char rowChar = pos.charAt(1);
        int col = colChar - 'a' + 1;
        int row = rowChar - '0';
        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new Exception("Position out of bounds: " + pos);
        }
        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String piece) throws Exception {
        return switch (piece.toLowerCase()) {
            case "queen", "q"  -> ChessPiece.PieceType.QUEEN;
            case "rook", "r"   -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new Exception("Invalid promotion piece: " + piece);
        };
    }
}
