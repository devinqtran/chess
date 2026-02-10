package server.handlers;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;
import dataaccess.DataAccessException;
import model.AuthData;

public class RegisterHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) throws DataAccessException {
        RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
        AuthData authData = userService.register(request.username(), request.password(), request.email());
        RegisterResult result = new RegisterResult(authData.username(), authData.authToken());
        ctx.status(200);
        ctx.result(gson.toJson(result));
    }

    public record RegisterRequest(String username, String password, String email) {}
    public record RegisterResult(String username, String authToken) {}
}