package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;
import model.*;
import chess.ChessGame;
import java.util.*;

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

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
        });

        // Register endpoints
        javalin.delete("/db", this::clearHandler);
        javalin.post("/user", this::registerHandler);
        javalin.post("/session", this::loginHandler);
        javalin.delete("/session", this::logoutHandler);
        javalin.get("/game", this::listGamesHandler);
        javalin.post("/game", this::createGameHandler);
        javalin.put("/game", this::joinGameHandler);

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

    // Handler methods

    private void clearHandler(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200);
        ctx.result(gson.toJson(Collections.emptyMap()));
    }

    private void registerHandler(Context ctx) throws DataAccessException {
        var request = gson.fromJson(ctx.body(), RegisterRequest.class);
        AuthData authData = userService.register(request.username, request.password, request.email);
        ctx.status(200);
        ctx.result(gson.toJson(new RegisterResult(authData.username(), authData.authToken())));
    }

    private void loginHandler(Context ctx) throws DataAccessException {
        var request = gson.fromJson(ctx.body(), LoginRequest.class);
        AuthData authData = userService.login(request.username, request.password);
        ctx.status(200);
        ctx.result(gson.toJson(new LoginResult(authData.username(), authData.authToken())));
    }

    private void logoutHandler(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200);
        ctx.result(gson.toJson(Collections.emptyMap()));
    }

    private void listGamesHandler(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        Collection<GameData> games = gameService.listGames(authToken);
        ctx.status(200);
        ctx.result(gson.toJson(new ListGamesResult(games)));
    }

    private void createGameHandler(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var request = gson.fromJson(ctx.body(), CreateGameRequest.class);
        int gameID = gameService.createGame(authToken, request.gameName);
        ctx.status(200);
        ctx.result(gson.toJson(new CreateGameResult(gameID)));
    }

    private void joinGameHandler(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        var request = gson.fromJson(ctx.body(), JoinGameRequest.class);

        // playerColor is REQUIRED - null, "null", or empty string are all invalid
        if (request.playerColor == null ||
                request.playerColor.equals("null") ||
                request.playerColor.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        // Validate it's WHITE or BLACK
        String colorUpper = request.playerColor.toUpperCase();
        if (!colorUpper.equals("WHITE") && !colorUpper.equals("BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(colorUpper);
        gameService.joinGame(authToken, request.gameID, color);
        ctx.status(200);
        ctx.result(gson.toJson(Collections.emptyMap()));
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

    // Request/Result records
    record RegisterRequest(String username, String password, String email) {}
    record RegisterResult(String username, String authToken) {}
    record LoginRequest(String username, String password) {}
    record LoginResult(String username, String authToken) {}
    record CreateGameRequest(String gameName) {}
    record CreateGameResult(Integer gameID) {}
    record JoinGameRequest(String playerColor, Integer gameID) {}
    record ListGamesResult(Collection<GameData> games) {}
    record ErrorResult(String message) {}
}