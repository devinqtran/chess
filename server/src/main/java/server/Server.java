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
    private final UserDAO userDAO = new MemoryUserDAO();
    private final GameDAO gameDAO = new MemoryGameDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();

    // Services
    private final UserService userService = new UserService(userDAO, authDAO);
    private final GameService gameService = new GameService(gameDAO, authDAO);
    private final ClearService clearService = new ClearService(userDAO, gameDAO, authDAO);

    // Handlers
    private final ClearHandler clearHandler = new ClearHandler(clearService);
    private final RegisterHandler registerHandler = new RegisterHandler(userService);
    private final LoginHandler loginHandler = new LoginHandler(userService);
    private final LogoutHandler logoutHandler = new LogoutHandler(userService);
    private final ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
    private final CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
    private final JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);

    public Server() {
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