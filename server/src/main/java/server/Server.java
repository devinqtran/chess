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

    public Server() {
        // Initialize the DB tables
        try {
            DatabaseInitializer.initialize();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to initialize database", e);
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
        javalin.delete("/db", clearHandler::handle);
        javalin.post("/user", registerHandler::handle);
        javalin.post("/session", loginHandler::handle);
        javalin.delete("/session", logoutHandler::handle);
        javalin.get("/game", listGamesHandler::handle);
        javalin.post("/game", createGameHandler::handle);
        javalin.put("/game", joinGameHandler::handle);

        // Exception handler
        javalin.exception(DataAccessException.class, this::exceptionHandler);
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