package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;
import server.handlers.*;

public class Server {
    private final Javalin javalin;
    private final Gson gson = new Gson();

    // DAOs
    private final UserDAO userDAO;
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    // Services
    private final UserService userService;
    private final GameService gameService;
    private final ClearService clearService;

    // Handlers
    private final ClearHandler clearHandler;
    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final ListGamesHandler listGamesHandler;
    private final CreateGameHandler createGameHandler;
    private final JoinGameHandler joinGameHandler;
    private final WebSocketHandler webSocketHandler;

    public Server() {
        // Initialize the DB tables
        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException e) {
            System.err.println("Warning: database initialization failed: " + e.getMessage());
        }

        // MySQL DAOs
        userDAO = new MySqlUserDAO();
        gameDAO = new MySqlGameDAO();
        authDAO = new MySqlAuthDAO();

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, gameDAO, authDAO);

        clearHandler = new ClearHandler(clearService);
        registerHandler = new RegisterHandler(userService);
        loginHandler = new LoginHandler(userService);
        logoutHandler = new LogoutHandler(userService);
        listGamesHandler = new ListGamesHandler(gameService);
        createGameHandler = new CreateGameHandler(gameService);
        joinGameHandler = new JoinGameHandler(gameService);

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        // Register endpoints with handler methods
        javalin.delete("/db", ctx -> clearHandler.handle(ctx));
        javalin.post("/user", ctx -> registerHandler.handle(ctx));
        javalin.post("/session", ctx -> loginHandler.handle(ctx));
        javalin.delete("/session", ctx -> logoutHandler.handle(ctx));
        javalin.get("/game", ctx -> listGamesHandler.handle(ctx));
        javalin.post("/game", ctx -> createGameHandler.handle(ctx));
        javalin.put("/game", ctx -> joinGameHandler.handle(ctx));

        // Exception handler
        javalin.exception(DataAccessException.class, this::exceptionHandler);
        javalin.exception(Exception.class, (ex, ctx) -> {
            ctx.status(500);
            ctx.result(gson.toJson(new ErrorResult("Error: " + ex.getMessage())));
        });

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void exceptionHandler(DataAccessException ex, Context ctx) {
        String message = ex.getMessage();

        // Default to 500 if message is null
        if (message == null) {
            message = "Error: internal server error";
            ctx.status(500);
        } else if (message.contains("bad request")) {
            ctx.status(400);
        } else if (message.contains("unauthorized")) {
            ctx.status(401);
        } else if (message.contains("already taken")) {
            ctx.status(403);
        } else {
            ctx.status(500);
        }
        ctx.result(gson.toJson(new ErrorResult(message)));
    }

    record ErrorResult(String message) {}
}