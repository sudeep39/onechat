/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package samples.websocket.onechat;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.*;
import samples.websocket.model.Message;
import samples.websocket.model.User;

import java.io.IOException;
import java.util.Collection;

/**
 * Echo messages by implementing a Spring {@link WebSocketHandler} abstraction.
 */
public class OneChatWebSocketHandler extends TextWebSocketHandler {

    private static Logger logger = LoggerFactory.getLogger(OneChatWebSocketHandler.class);

    private final OneChatService oneChatService;

    private PerConnectionWebSocketHandler connectionHandler;

    private User me;

    @Autowired
    public OneChatWebSocketHandler(OneChatService oneChatService) {
        this.oneChatService = oneChatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.debug("Opened new session in instance " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // Remove the user from the list from all the user sessions
        addOrRemoveUser(session, me, false);
    }

    private MessagePublisher messagePublisher = new MessagePublisher();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        String payload = message.getPayload();
        Gson gson = new Gson();
        Message msg = gson.fromJson(payload, Message.class);
        processMessage(session, msg);
    }

    private void processMessage(WebSocketSession session, Message msg) throws IOException {
        switch (msg.getType()) {
            case "user_added":
                me = msg.getFrom();
                addOrRemoveUser(session, me, true);
                msg.setBody(connectionHandler.getOtherUsers(session));
                msg.setType("user_listing");
                messagePublisher.send(msg);
                break;
            case "pair":
                messagePublisher.send(msg);
                break;
            case "user_removed":
                me = msg.getFrom();
                addOrRemoveUser(session, me, false);
                break;
            case "chat":
                messagePublisher.send(msg);
                break;
            case "handshake":
                break;
            default:
                System.out.println("Not a valid type " + msg.getType());
        }
    }

    public void setConnectionHandler(PerConnectionWebSocketHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        this.messagePublisher.setSessionUserMap(connectionHandler.getSessionUserMap());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
            throws Exception {
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void addOrRemoveUser(WebSocketSession session, User modified, boolean added) throws IOException {
        if (added)
            connectionHandler.addUser(modified, session);
        else
            connectionHandler.removeUser(session);

        Collection<User> users = connectionHandler.getOtherUsers(session);
        for (User user : users) {
            Message msg = new Message();
            msg.setBody(modified);
            msg.setFrom(user);
            msg.setTo(user);
            msg.setType("user_" + (added ? "added" : "removed"));
            messagePublisher.send(msg);
        }
    }

}