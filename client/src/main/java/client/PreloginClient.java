package client;

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

    // Temporary stub
    public State run() {
        System.out.print("\n[LOGGED OUT] >>> ");
        String line = scanner.nextLine().trim();
        String[] tokens = line.split("\\s+");
        String command = tokens[0].toLowerCase();

        // Print command
        System.out.println("You typed: " + command);
        return State.PRELOGIN;
    }

    public String getAuthToken() { return authToken; }
    public void clearAuthToken() { authToken = null; }
}
