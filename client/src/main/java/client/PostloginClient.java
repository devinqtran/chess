package client;

import java.util.Scanner;

public class PostloginClient {
    private final ServerFacade facade;
    private final Scanner scanner;
    private final String authToken;

    public PostloginClient(ServerFacade facade, Scanner scanner, String authToken) {
        this.facade = facade;
        this.scanner = scanner;
        this.authToken = authToken;
    }

    // Temporary stub
    public State run() {
        System.out.print("\n[LOGGED IN] >>> ");
        scanner.nextLine();
        return State.PRELOGIN;
    }
}

