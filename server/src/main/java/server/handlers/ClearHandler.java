package server.handlers;

import io.javalin.http.Context;
import service.ClearService;
import dataaccess.DataAccessException;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.status(200);
        ctx.result("{}");
    }
}
