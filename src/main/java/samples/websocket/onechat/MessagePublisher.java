package samples.websocket.onechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import samples.websocket.model.Message;
import samples.websocket.model.User;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by M1011579 on 5/7/2016.
 */
public class MessagePublisher {

    private Map<WebSocketSession, User> sessionUserMap =
            new ConcurrentHashMap<WebSocketSession, User>();

    public void setSessionUserMap(Map<WebSocketSession, User> sessionUserMap) {
        this.sessionUserMap = sessionUserMap;
    }

    private Gson gsonHandler = new GsonBuilder().disableHtmlEscaping().create();

    private Gson getGsonHandler() {
        return gsonHandler;
    }

    public void send(Message message) throws IOException {
        if (null != message.getFrom()) {
            if (null != message.getTo()) {
                WebSocketSession session = sessionUserMap.entrySet().stream()
                        .filter(e -> (e.getValue().equals(message.getTo())))
                        .map(e -> e.getKey())
                        .findFirst().orElse(null);

                if (null != session) {
                    String msg = getGsonHandler().toJson(message, Message.class);
                    System.out.println(msg);
                    session.sendMessage(new TextMessage(msg));
                }
            }
        }
    }
}