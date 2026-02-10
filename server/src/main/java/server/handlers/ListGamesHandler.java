package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import dataaccess.DataAccessException;
import model.GameData;
import java.util.Collection;

public class ListGamesHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        Collection<GameData> games = gameService.listGames(authToken);
        ListGamesResult result = new ListGamesResult(games);
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public record ListGamesResult(Collection<GameData> games) {}
}