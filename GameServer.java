import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Host (server) side of the Guess the Number game.
 *
 * API (all responses are JSON):
 *   POST /guess   body: a whole number from 1 to 100 (plain text)
 *   POST /reset   body: empty, starts a new game
 *   GET  /status  returns the current number of attempts and whether the game is won
 *
 * Uses only the built in JDK HTTP server, so no external libraries are needed.
 */
public class GameServer {
    private static final int PORT = 8080;
    private static final GuessGame GAME = new GuessGame();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/guess", GameServer::handleGuess);
        server.createContext("/reset", GameServer::handleReset);
        server.createContext("/status", GameServer::handleStatus);
        server.start();

        System.out.println("GameServer is running on http://localhost:" + PORT);
        System.out.println("Waiting for players... (press Ctrl+C to stop)");
    }

    /** POST /guess: checks the player's guess and tells them if it is too low, too high or correct. */
    private static void handleGuess(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use POST for /guess."));
            return;
        }

        String body = readBody(exchange).trim();
        int guess;
        try {
            guess = Integer.parseInt(body);
        } catch (NumberFormatException e) {
            sendJson(exchange, 400, error("Guess must be a whole number."));
            return;
        }
        if (guess < GuessGame.MIN || guess > GuessGame.MAX) {
            sendJson(exchange, 400, error("Guess must be from " + GuessGame.MIN + " to " + GuessGame.MAX + "."));
            return;
        }

        String result = GAME.checkGuess(guess);
        int attempts = GAME.getAttempts();
        String message;
        switch (result) {
            case "low":
                message = "Too low! Try a higher number.";
                break;
            case "high":
                message = "Too high! Try a lower number.";
                break;
            case "correct":
                message = "Correct! You guessed it in " + attempts + " attempt" + (attempts == 1 ? "" : "s") + ".";
                break;
            default:
                message = "You already won this round. Type R to start a new game.";
                break;
        }

        System.out.println("Guess " + guess + " -> " + result + " (attempts: " + attempts + ")");
        sendJson(exchange, 200, gameJson(result, message));
    }

    /** POST /reset: starts a new round with a new secret number. */
    private static void handleReset(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use POST for /reset."));
            return;
        }
        readBody(exchange);
        GAME.reset();
        System.out.println("Game reset: new secret number chosen.");
        sendJson(exchange, 200, gameJson("reset", "New game started! Guess a number from 1 to 100."));
    }

    /** GET /status: returns the current state of the game without making a guess. */
    private static void handleStatus(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use GET for /status."));
            return;
        }
        String message = GAME.isWon() ? "This round is won." : "Game in progress.";
        sendJson(exchange, 200, gameJson("status", message));
    }

    // ---------- helpers ----------

    private static String gameJson(String result, String message) {
        return "{\"result\":\"" + result + "\","
                + "\"message\":\"" + message + "\","
                + "\"attempts\":" + GAME.getAttempts() + ","
                + "\"won\":" + GAME.isWon() + "}";
    }

    private static String error(String message) {
        return "{\"error\":\"" + message + "\"}";
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
