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

    private State handleCreate(String[] tokens) {
        System.out.println("Create not yet implemented!");
        return State.POSTLOGIN;
    }

    private State handleList() {
        System.out.println("List not yet implemented!");
        return State.POSTLOGIN;
    }

    private State handlePlay(String[] tokens) {
        System.out.println("Play not yet implemented!");
        return State.POSTLOGIN;
    }

    private State handleObserve(String[] tokens) {
        System.out.println("Observe not yet implemented!");
        return State.POSTLOGIN;
    }
}

