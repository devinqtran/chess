package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import dataaccess.DataAccessException;

public class CreateGameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
        int gameID = gameService.createGame(authToken, request.gameName());
        CreateGameResult result = new CreateGameResult(gameID);
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public record CreateGameRequest(String gameName) {}
    public record CreateGameResult(Integer gameID) {}
}