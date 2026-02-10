package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;
import dataaccess.DataAccessException;
import model.AuthData;

public class LoginHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) throws DataAccessException {
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        AuthData authData = userService.login(request.username(), request.password());
        LoginResult result = new LoginResult(authData.username(), authData.authToken());
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResult(String username, String authToken) {}
}