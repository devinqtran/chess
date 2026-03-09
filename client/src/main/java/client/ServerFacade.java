package client;

import com.google.gson.Gson;
import model.AuthData;
import model.UserData;
import model.GameData;

import java.io.*;
import java.net.*;
import java.util.HashMap;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        // Open connection
        URI uri = new URI(serverUrl + "/user");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json");
        http.setDoOutput(true);

        // Write the request body
        var body = new UserData(username, password, email);
        try (OutputStream os = http.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os)) {
            gson.toJson(body, writer);
        }

        http.connect();

        // Check for errors
        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Register failed: " + http.getResponseCode());
        }

        // Read and return response
        try (InputStream is = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            return gson.fromJson(reader, AuthData.class);
        }
    }

    public void logout(String authToken) throws Exception {
        // Open connection
        URI uri = new URI(serverUrl + "/session");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("DELETE");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);

        http.connect();

        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Logout failed: " + http.getResponseCode());
        }
    }

    public AuthData login(String username, String password) throws Exception {
        // Open the connection
        URI uri = new URI(serverUrl + "/session");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json");
        http.setDoOutput(true);

        // Write the request body
        var body = new UserData(username, password, null);
        try (OutputStream os = http.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os)) {
            gson.toJson(body, writer);
        }

        http.connect();

        // Check for errors
        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Login failed: " + http.getResponseCode());
        }

        // Read and return response
        try (InputStream is = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            return gson.fromJson(reader, AuthData.class);
        }
    }

    // List games method
    public GameData[] listGames(String authToken) throws Exception {
        // Open the connection
        URI uri = new URI(serverUrl + "/game");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("GET");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);

        http.connect();

        // Check for errors
        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("List games failed: " + http.getResponseCode());
        }

        // Read and return response
        try (InputStream is = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            record GamesResponse(GameData[] games) {}
            return gson.fromJson(reader, GamesResponse.class).games();
        }
    }

    // Create game method
    public int createGame(String authToken, String gameName) throws Exception {
        // Open the connection
        URI uri = new URI(serverUrl + "/game");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("POST");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);
        http.setDoOutput(true);

        // Write the request body
        var body = new HashMap<String, String>();
        body.put("gameName", gameName);
        try (OutputStream os = http.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os)) {
            gson.toJson(body, writer);
        }

        http.connect();

        // Check for errors
        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Create game failed: " + http.getResponseCode());
        }

        // Read and return the new game ID
        try (InputStream is = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            record CreateGameResponse(int gameID) {}
            return gson.fromJson(reader, CreateGameResponse.class).gameID();
        }
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws Exception {
        // Open the connection
        URI uri = new URI(serverUrl + "/game");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("PUT");
        http.setRequestProperty("Content-Type", "application/json");
        http.setRequestProperty("Authorization", authToken);
        http.setDoOutput(true);

        // Write the request body
        var body = new HashMap<String, Object>();
        body.put("gameID", gameID);
        if (playerColor != null) {
            body.put("playerColor", playerColor);
        }
        try (OutputStream os = http.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(os)) {
            gson.toJson(body, writer);
        }

        http.connect();

        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Join game failed: " + http.getResponseCode());
        }
    }

    // Clear method
    public void clear() throws Exception {
        URI uri = new URI(serverUrl + "/db");
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod("DELETE");
        http.connect();

        if (http.getResponseCode() / 100 != 2) {
            throw new Exception("Clear failed: " + http.getResponseCode());
        }
    }

}
