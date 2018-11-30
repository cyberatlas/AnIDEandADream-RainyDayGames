package iastate.cs309.server.Snake;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import iastate.cs309.server.Chat.Message;
import iastate.cs309.server.Snake.SnakeEnums.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@ServerEndpoint(value = "/snake/{username}/{isSnake}")
public class SnakeEndpoint {
    private static Set<SnakeEndpoint> snakeEndpoints
            = new CopyOnWriteArraySet<>();
    private static Logger logger = LoggerFactory.getLogger(iastate.cs309.server.Snake.SnakeEndpoint.class);
    private static HashBiMap<String, String> users = HashBiMap.create();
    private static HashMap<String, Snake> sessionSnakes = new HashMap<>();
    private static Map map = new Map();
    private static Gson gson = new Gson();
    private Session session;
    private static boolean isRunning;

    public static void broadcastMap() {
        snakeEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    if (sessionSnakes.containsKey(endpoint.session.getId())) {
                        endpoint.session.getBasicRemote().
                                sendText(gson.toJson(map));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        });
    }

    private static void broadcast(Message message) {
        snakeEndpoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    if (!sessionSnakes.containsKey(endpoint.session)) {
                        endpoint.session.getBasicRemote().
                                sendText(message.getFrom() + ": " + message.getContent());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
        });
    }

    private static void sendMessageToAnotherUser(Session session, Message msg) {
        try {
            String toId = users.inverse().get(msg.getTo());
            snakeEndpoints.forEach(endpoint -> {
                synchronized (endpoint) {
                    try {
                        //Find the target and send the message to them.
                        if (endpoint.session.getId() == toId) {
                            endpoint.session.getBasicRemote().
                                    sendText(msg.getFrom() + "->" + msg.getTo() + ": " + msg.getContent());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error(e.getMessage());
                    }
                }
            });
            //Duplicate message for sender's chat log
            session.getBasicRemote().sendText(msg.getFrom() + "->" + msg.getTo() + ": " + msg.getContent());
        } catch (IOException e) {
            logger.info("Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }

    private void run() {
        if (!isRunning) {
            Thread t = new Thread(map);
            t.start();
            isRunning = true;
        }
    }

    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("username") String username, @PathParam("isSnake") Boolean isSnake) throws IOException, EncodeException {

        this.session = session;
        snakeEndpoints.add(this);

        if (isSnake) {
            Snake snake = new Snake(username, map.findSnakeSpawn());
            sessionSnakes.put(session.getId(), snake);
            map.addSnake(snake);
        }

        //When the first snake connects run.
        run();

        if (!isSnake) {
            users.put(session.getId(), username);

            Message message = new Message();
            message.setFrom(username);
            message.setContent("Connected!");
            broadcast(message);

        }
        logger.info(username + " connected");
    }

    @OnMessage
    public void onMessage(Session session, String message)
            throws IOException, EncodeException {
        //format: dir <N/S/E/W>
        if (sessionSnakes.containsKey(session.getId())) {
            if (message.contains("dir")) {
                String[] parts = message.split("\\s");
                switch (parts[1].toLowerCase()) {
                    case "n":
                    case "N":
                        sessionSnakes.get(session.getId()).updateDirection(Direction.North);
                        break;
                    case "s":
                    case "S":
                        sessionSnakes.get(session.getId()).updateDirection(Direction.South);
                        break;
                    case "e":
                    case "E":
                        sessionSnakes.get(session.getId()).updateDirection(Direction.East);
                        break;
                    case "w":
                    case "W":
                        sessionSnakes.get(session.getId()).updateDirection(Direction.West);
                        break;
                }
            }
        } else {
            Message msg = new Message();
            msg.setFrom(users.get(session.getId()));
            msg.setContent(message);

            if (message.startsWith("/whisper")) {
                String[] from = message.split(" ");
                if (from.length > 1) {
                    msg.setTo(from[1]);
                    //update message content to remove syntactic keywords
                    msg.setContent(message.replaceFirst("(/whisper\\s+)", "").replaceFirst("(" + from[1] + "\\s+)", ""));
                    sendMessageToAnotherUser(session, msg);
                }
            } else {
                broadcast(msg);
            }
        }

    }

    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        if (sessionSnakes.get(session.getId()) != null)
            sessionSnakes.get(session.getId()).endSnake();
        snakeEndpoints.remove(this);


        if (sessionSnakes.size() == 0) {
            //cleanup the game
        }
        ;
        //broadcastMap(message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error(session.toString());
        logger.error(throwable.getMessage());
    }
}


