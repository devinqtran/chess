package client;

import java.util.Scanner;

public class Repl {
    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);

    public Repl(int port) {
        this.facade = new ServerFacade(port);
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess! Type 'help' to get started. ♕");

        PreloginClient preloginClient = new PreloginClient(facade, scanner);

        // Start in prelogin state loop until "quit"
        State state = State.PRELOGIN;
        while (state != State.QUIT) {
            switch (state) {
                case PRELOGIN -> state = preloginClient.run();
                case POSTLOGIN -> {
                    PostloginClient postloginClient = new PostloginClient(facade, scanner, preloginClient.getAuthToken());
                    // Keep looping inside postlogin until logout or quit
                    while (state == State.POSTLOGIN) {
                        state = postloginClient.run();
                    }
                    preloginClient.clearAuthToken();
                }
            }
        }
        System.out.println("Goodbye!");
    }
}