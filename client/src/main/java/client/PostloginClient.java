package client;

import model.AuthData;
import model.GameData;
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
        System.out.println("""
            Available commands:
              list                             - List all available games
              create <name>                    - Create a new game
              play <game number> <WHITE|BLACK> - Join a game as a player
              observe <game number>            - Observe a game
              logout                           - Logout of your account
              help                             - Show this help text
            """);
    }

    // Logout command method
    private State handleLogout() {
        try {
            facade.logout(authToken);
            System.out.println("Logged out successfully.");
            return State.PRELOGIN;
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
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
            System.out.println("Create game failed: " + e.getMessage());
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
            System.out.println("List games failed: " + e.getMessage());
        }
        return State.POSTLOGIN;
    }

    // Play command method
    private State handlePlay(String[] tokens) {
        System.out.println("Play coming soon!");
        return State.POSTLOGIN;
    }

    // Observe command method
    private State handleObserve(String[] tokens) {
        System.out.println("Observe coming soon!");
        return State.POSTLOGIN;
    }
}

