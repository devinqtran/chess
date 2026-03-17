package client;

public class ClientUtil {
    public static String getFriendlyError(String rawMessage) {
        if (rawMessage.contains("401")) {
            return "Incorrect username or password.";
        } else if (rawMessage.contains("403")) {
            return "That username is already taken.";
        } else if (rawMessage.contains("400")) {
            return "Missing or invalid information provided.";
        } else if (rawMessage.contains("500")) {
            return "Something went wrong on the server. Please try again.";
        } else {
            return "An unexpected error occurred. Please try again.";
        }
    }
}
