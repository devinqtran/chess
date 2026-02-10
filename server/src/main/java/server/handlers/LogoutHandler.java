package server.handlers;

import io.javalin.http.Context;
import service.UserService;
import dataaccess.DataAccessException;

public class LogoutHandler {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.status(200);
        ctx.result("{}");
    }
}