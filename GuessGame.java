import java.util.Random;

/**
 * Holds the state and rules of a single "Guess the Number" game.
 * The server owns one instance of this class and calls it for every request.
 */
public class GuessGame {
    public static final int MIN = 1;
    public static final int MAX = 100;

    private final Random random = new Random();
    private int secretNumber;
    private int attempts;
    private boolean won;

    public GuessGame() {
        reset();
    }

    /** Starts a new game with a new secret number. */
    public synchronized void reset() {
        secretNumber = random.nextInt(MAX - MIN + 1) + MIN;
        attempts = 0;
        won = false;
    }

    /**
     * Checks a guess against the secret number.
     *
     * @return "low", "high", "correct" or "already_won"
     */
    public synchronized String checkGuess(int guess) {
        if (won) {
            return "already_won";
        }
        attempts++;
        if (guess < secretNumber) {
            return "low";
        } else if (guess > secretNumber) {
            return "high";
        }
        won = true;
        return "correct";
    }

    public synchronized int getAttempts() {
        return attempts;
    }

    public synchronized boolean isWon() {
        return won;
    }
}
