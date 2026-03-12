package client;

import java.util.Scanner;

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
        scanner.nextLine();
        return State.QUIT;
    }

    public String getAuthToken() { return authToken; }
    public void clearAuthToken() { authToken = null; }
}
