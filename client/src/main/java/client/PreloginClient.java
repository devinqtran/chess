package client;

import model.AuthData;
import ui.EscapeSequences;

import java.util.Scanner;

// Read the input command and handle it (help, quit, login, register)
public class PreloginClient {
    private final ServerFacade facade;
    private final Scanner scanner;
    private String authToken = null;

    public PreloginClient(ServerFacade facade, Scanner scanner) {
        this.facade = facade;
        this.scanner = scanner;
    }

    // Method to detect commands with switch for each command type
    public State run() {
        System.out.print("\n[LOGGED OUT] >>> ");
        String line = scanner.nextLine().trim();
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                printHelp();
                yield State.PRELOGIN;
            }
            case "quit" -> State.QUIT;
            case "register" -> handleRegister(tokens);
            case "login" -> handleLogin(tokens);
            default -> {
                System.out.println("Unknown command. Type 'help' for options.");
                yield State.PRELOGIN;
            }
        };
    }

    // Helper method to define commands
    private void printHelp() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + """
            Available commands:
              register <USERNAME> <PASSWORD> <EMAIL>  - Create a new account
              login <USERNAME> <PASSWORD>             - Login to your account
              quit                                    - Exit the program
              help                                    - Show this help text
            """ + EscapeSequences.RESET_TEXT_COLOR);
    }

    // Register command method
    private State handleRegister(String[] tokens) {
        if (tokens.length != 4) {
            System.out.println("Usage: register <username> <password> <email>");
            return State.PRELOGIN;
        }
        try {
            AuthData auth = facade.register(tokens[1], tokens[2], tokens[3]);
            authToken = auth.authToken();
            System.out.println("Registered and logged in as " + auth.username());
            return State.POSTLOGIN;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Registration failed: "
                    + ClientUtil.getFriendlyError(e.getMessage()) + EscapeSequences.RESET_TEXT_COLOR);
            return State.PRELOGIN;
        }
    }

    // Login command method
    private State handleLogin(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: login <username> <password>");
            return State.PRELOGIN;
        }
        try {
            AuthData auth = facade.login(tokens[1], tokens[2]);
            authToken = auth.authToken();
            System.out.println("Logged in as " + auth.username());
            return State.POSTLOGIN;
        } catch (Exception e) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Login failed: "
                    + ClientUtil.getFriendlyError(e.getMessage()) + EscapeSequences.RESET_TEXT_COLOR);
            return State.PRELOGIN;
        }
    }

    public String getAuthToken() { return authToken; }
    public void clearAuthToken() { authToken = null; }
}
