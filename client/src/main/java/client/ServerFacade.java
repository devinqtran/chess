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

        // Read and return the response
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

        // Read and return the response
        try (InputStream is = http.getInputStream();
             InputStreamReader reader = new InputStreamReader(is)) {
            return gson.fromJson(reader, AuthData.class);
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
