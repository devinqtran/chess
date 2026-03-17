package client;

import model.AuthData;
import model.GameData;
import ui.EscapeSequences;

import java.util.Scanner;

public class PostloginClient {
    private final ServerFacade facade;
    private final Scanner scanner;
    private final String authToken;
    private GameData[] cachedGames = new GameData[0];

    public PostloginClient(ServerFacade facade, Scanner scanner, String authToken) {
        this.facade = facade;
        this.scanner = scanner;
        this.authToken = authToken;
    }

    public State run() {
        System.out.print("\n[LOGGED IN] >>> ");
        String line = scanner.nextLine().trim();
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                printHelp();
                yield State.POSTLOGIN;
            }
            case "logout" -> handleLogout();
            case "create" -> handleCreate(tokens);
            case "list" -> handleList();
            case "play" -> handlePlay(tokens);
            case "observe" -> handleObserve(tokens);
            default -> {
                System.out.println("Unknown command. Type 'help' for options.");
                yield State.POSTLOGIN;
            }
        };
    }

    // Help command method
    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + """
            Available commands:
              list                             - List all available games
              create <NAME>                    - Create a new game
              play <ID> <WHITE|BLACK>          - Join a game as a player
              observe <ID>                     - Observe a game
              logout                           - Logout of your account
              help                             - Show this help text
            """ + EscapeSequences.RESET_TEXT_COLOR);
    }

    // Logout command method
    private State handleLogout() {
        try {
            facade.logout(authToken);
            System.out.println("Logged out successfully.");
            return State.PRELOGIN;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Logout failed: " + ClientUtil.getFriendlyError(e.getMessage()) + EscapeSequences.RESET_TEXT_COLOR);
            return State.POSTLOGIN;
        }
    }

    // Create command method
    private State handleCreate(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: create <game name>");
            return State.POSTLOGIN;
        }
        String gameName = String.join(" ", java.util.Arrays.copyOfRange(tokens, 1, tokens.length));
        try {
            facade.createGame(authToken, gameName);
            System.out.println("Game '" + gameName + "' created successfully.");
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Create game failed: " + ClientUtil.getFriendlyError(e.getMessage()) + EscapeSequences.RESET_TEXT_COLOR);
        }
        return State.POSTLOGIN;
    }

    // List command method
    private State handleList() {
        try {
            cachedGames = facade.listGames(authToken);
            if (cachedGames.length == 0) {
                System.out.println("No games available.");
            } else {
                System.out.println("Available games:");
                for (int i = 0; i < cachedGames.length; i++) {
                    GameData game = cachedGames[i];
                    String white = game.whiteUsername() != null ? game.whiteUsername() : "open";
                    String black = game.blackUsername() != null ? game.blackUsername() : "open";
                    System.out.printf("  %d. %s  [white: %s | black: %s]%n",
                            i + 1, game.gameName(), white, black);
                }
            }
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "List games failed: " + ClientUtil.getFriendlyError(e.getMessage()) + EscapeSequences.RESET_TEXT_COLOR);
        }
        return State.POSTLOGIN;
    }

    // Play command method
    private State handlePlay(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: play <game number> <WHITE|BLACK>");
            return State.POSTLOGIN;
        }
        try {
            int gameNumber = Integer.parseInt(tokens[1]);
            String color = tokens[2].toUpperCase();

            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK.");
                return State.POSTLOGIN;
            }
            if (cachedGames.length == 0) {
                System.out.println("Please run 'list' first to see available games.");
                return State.POSTLOGIN;
            }
            if (gameNumber < 1 || gameNumber > cachedGames.length) {
                System.out.println("Invalid game number. Please run 'list' to see available games.");
                return State.POSTLOGIN;
            }

            int gameID = cachedGames[gameNumber - 1].gameID();
            facade.joinGame(authToken, gameID, color);
            System.out.println("Joined game as " + color);
            BoardRenderer.render(color.equals("WHITE"));

        } catch (NumberFormatException e) {
            System.out.println("Game number must be a number.");
        } catch (Exception e) {
            System.out.println("Join game failed: " + ClientUtil.getFriendlyError(e.getMessage()));
        }
        return State.POSTLOGIN;
    }

    // Observe command method
    private State handleObserve(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: observe <game number>");
            return State.POSTLOGIN;
        }
        try {
            int gameNumber = Integer.parseInt(tokens[1]);

            if (cachedGames.length == 0) {
                System.out.println("Please run 'list' first to see available games.");
                return State.POSTLOGIN;
            }
            if (gameNumber < 1 || gameNumber > cachedGames.length) {
                System.out.println("Invalid game number. Please run 'list' to see available games.");
                return State.POSTLOGIN;
            }

            System.out.println("Observing game " + gameNumber);
            BoardRenderer.render(true);

        } catch (NumberFormatException e) {
            System.out.println("Game number must be a number.");
        }
        return State.POSTLOGIN;
    }
}

