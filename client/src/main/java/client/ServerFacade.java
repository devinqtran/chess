package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import java.io.*;
import java.net.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

}
