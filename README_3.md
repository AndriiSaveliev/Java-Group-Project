# Java Group Project: Guess the Number

A simple networked game written in Java. The **Host (Server)** picks a secret number from 1 to 100 and keeps the game state. The **Client (Player)** sends guesses over HTTP and the server answers whether each guess is too low, too high, or correct.

## Team

| Member | Role |
| --- | --- |
| Andrii Saveliev | Host/Server |
| Jonathan Charles Cook | Host/Server |
| Jadon Russell | Client/Player |
| KayLeiss Hutchinson | Client/Player |

## Files

| File | Description |
| --- | --- |
| `GameServer.java` | HTTP server that exposes the game API on port 8080 |
| `GuessGame.java` | Game logic and state (secret number, attempts, win status) |
| `GameClient.java` | Console client the player uses to make guesses |

## Requirements

* Java JDK 11 or newer (uses the built in `java.net.http.HttpClient` and `com.sun.net.httpserver.HttpServer`)
* No external libraries are needed

Check your version with:

```
java -version
```

## How to Run

1. Clone the repository:

   ```
   git clone https://github.com/AndriiSaveliev/Java-Group-Project.git
   cd Java-Group-Project
   ```

2. Compile all files:

   ```
   javac *.java
   ```

3. Start the server in one terminal window:

   ```
   java GameServer
   ```

   You should see `GameServer is running on http://localhost:8080`. Leave this window open.

4. Start the client in a second terminal window:

   ```
   java GameClient
   ```

You can also run both classes from an IDE (IntelliJ, Eclipse, VS Code). Always start `GameServer` first.

## How to Play

1. The server picks a secret number from 1 to 100.
2. Type a number in the client and press Enter.
3. The server replies **Too low**, **Too high**, or **Correct**.
4. Keep guessing until you find the number. The server counts your attempts.
5. Type `R` to start a new game, or `Q` to quit.

## API

The client and server communicate over HTTP. All responses are JSON.

| Method | Endpoint | Request body | Description |
| --- | --- | --- | --- |
| POST | `/guess` | a number, for example `42` | Checks a guess |
| POST | `/reset` | empty | Starts a new game with a new secret number |
| GET | `/status` | none | Returns the current attempts and win status |

Example response from `/guess`:

```json
{"result":"low","message":"Too low! Try a higher number.","attempts":3,"won":false}
```

Possible `result` values: `low`, `high`, `correct`, `already_won`, `reset`, `status`.

Invalid input (not a number or outside 1 to 100) returns HTTP 400 with an error message:

```json
{"error":"Guess must be a whole number."}
```

## Troubleshooting

* **"Could not connect to the server"**: make sure `GameServer` is running before starting the client.
* **"Address already in use"**: another program is using port 8080. Stop the old server (Ctrl+C) and start it again.
