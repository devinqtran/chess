package client;

import ui.EscapeSequences;

import java.util.Scanner;

public class Repl {
    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);
    private final int port;

    public Repl(int port) {
        this.port = port;
        this.facade = new ServerFacade(port);
    }

    public void run() {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_BLUE + "♕ Welcome to 240 Chess! Type 'help' to get started. ♕" + EscapeSequences.RESET_TEXT_COLOR);

        PreloginClient preloginClient = new PreloginClient(facade, scanner);

        // Prelogin state loop until "quit"
        State state = State.PRELOGIN;
        while (state != State.QUIT) {
            switch (state) {
                case PRELOGIN -> state = preloginClient.run();
                case POSTLOGIN -> {
                    PostloginClient postloginClient = new PostloginClient(facade, scanner, preloginClient.getAuthToken(), port);
                    while (state == State.POSTLOGIN) {
                        state = postloginClient.run();
                    }
                    preloginClient.clearAuthToken();
                }
            }
        }
        System.out.println(EscapeSequences.SET_TEXT_COLOR_MAGENTA + "Goodbye!" + EscapeSequences.RESET_TEXT_COLOR);
    }
}