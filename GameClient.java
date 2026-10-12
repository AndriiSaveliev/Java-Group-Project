import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GameClient {
    private static final String SERVER_URL = "http://localhost:8080";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("=== Guess the Number ===");
            System.out.println("I'm thinking of a number from 1 to 100.");

            boolean playing = true;
            while (playing) {
                System.out.print("Enter your guess, or type R to restart, or Q to quit: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("q")) {
                    playing = false;
                } else if (input.equalsIgnoreCase("r")) {
                    System.out.println(sendPost("/reset", ""));
                } else {
                    try {
                        int guess = Integer.parseInt(input);
                        if (guess < 1 || guess > 100) {
                            System.out.println("Please enter a number from 1 to 100.");
                            continue;
                        }

                        String response = sendPost("/guess", String.valueOf(guess));
                        System.out.println(response);
                        if (response.contains("\"won\":true") && response.contains("Correct!")) {
                            System.out.println("Type R to play again, or Q to quit.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a whole number, R, or Q.");
                    }
                }
            }
            System.out.println("Thanks for playing!");
        } catch (IOException e) {
            System.out.println("Could not connect to the server. Make sure GameServer is running.");
            System.out.println("Details: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("The request was interrupted.");
        }
    }

    private static String sendPost(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL + path))
                .header("Content-Type", "text/plain; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = CLIENT.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response.body();
    }
}