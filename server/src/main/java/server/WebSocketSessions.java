package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketSessions {
    // Maps gameID → list of connected sessions
    private final ConcurrentHashMap<Integer, ArrayList<Session>> sessions
            = new ConcurrentHashMap<>();

    // Adds a client to the list of sessions
    public void addSession(int gameID, Session session) {
        sessions.computeIfAbsent(gameID, k -> new ArrayList<>()).add(session);
    }

    // Removes a client from the list of sessions
    public void removeSession(int gameID, Session session) {
        if (sessions.containsKey(gameID)) {
            sessions.get(gameID).remove(session);
        }
    }

    // Returns all the sessions for a game
    public ArrayList<Session> getSessions(int gameID) {
        return sessions.getOrDefault(gameID, new ArrayList<>());
    }

    // Sends to everyone except a single session
    public void broadcast(int gameID, String message, Session exclude) throws IOException {
        var toRemove = new ArrayList<Session>();
        for (Session s : getSessions(gameID)) {
            if (s.isOpen()) {
                if (!s.equals(exclude)) {
                    s.getRemote().sendString(message);
                }
            } else {
                toRemove.add(s); // clean up closed sessions
            }
        }
        if (sessions.containsKey(gameID)) {
            sessions.get(gameID).removeAll(toRemove);
        }
    }

    // Sends to everyone including the sender
    public void broadcastAll(int gameID, String message) throws IOException {
        var toRemove = new ArrayList<Session>();
        for (Session s : getSessions(gameID)) {
            if (s.isOpen()) {
                s.getRemote().sendString(message);
            } else {
                toRemove.add(s);
            }
        }
        if (sessions.containsKey(gameID)) {
            sessions.get(gameID).removeAll(toRemove);
        }
    }

    // Sends to a specific session
    public void sendToSession(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }
}
