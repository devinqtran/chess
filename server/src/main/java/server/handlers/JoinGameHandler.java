package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import dataaccess.DataAccessException;
import chess.ChessGame;

public class JoinGameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);

        // Validate playerColor is present and valid
        if (request.playerColor() == null ||
                request.playerColor().equals("null") ||
                request.playerColor().isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        String colorUpper = request.playerColor().toUpperCase();
        if (!colorUpper.equals("WHITE") && !colorUpper.equals("BLACK")) {
            throw new DataAccessException("Error: bad request");
        }

        ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(colorUpper);
        gameService.joinGame(authToken, request.gameID(), color);
        ctx.status(200);
        ctx.result("{}");
    }

    public record JoinGameRequest(String playerColor, Integer gameID) {}
}