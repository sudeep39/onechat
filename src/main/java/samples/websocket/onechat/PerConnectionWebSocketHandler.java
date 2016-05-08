package samples.websocket.onechat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BeanCreatingHandlerProvider;
import samples.websocket.model.User;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PerConnectionWebSocketHandler implements WebSocketHandler, BeanFactoryAware {

    private static final Log logger = LogFactory.getLog(PerConnectionWebSocketHandler.class);

    private final BeanCreatingHandlerProvider<WebSocketHandler> provider;

    private final Map<WebSocketSession, User> sessionUserMap =
            new ConcurrentHashMap<WebSocketSession, User>();

    private final Map<WebSocketSession, WebSocketHandler> sessionHandlerMap =
            new ConcurrentHashMap<WebSocketSession, WebSocketHandler>();

    private final boolean supportsPartialMessages;

    public PerConnectionWebSocketHandler(Class<? extends WebSocketHandler> handlerType) {
        this(handlerType, false);
    }

    public PerConnectionWebSocketHandler(Class<? extends WebSocketHandler> handlerType, boolean supportsPartialMessages) {
        this.provider = new BeanCreatingHandlerProvider<WebSocketHandler>(handlerType);
        this.supportsPartialMessages = supportsPartialMessages;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.provider.setBeanFactory(beanFactory);
    }

    public void addUser(User user, WebSocketSession session) {
        sessionUserMap.put(session, user);
    }

    public void removeUser(WebSocketSession session) {
        sessionUserMap.remove(session);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        OneChatWebSocketHandler handler = (OneChatWebSocketHandler) this.provider.getHandler();
        handler.setConnectionHandler(this);
        this.sessionHandlerMap.put(session, handler);
        handler.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        getHandler(session).handleMessage(session, message);
    }

    private WebSocketHandler getHandler(WebSocketSession session) {
        WebSocketHandler handler = this.sessionHandlerMap.get(session);
        Assert.isTrue(handler != null, "WebSocketHandler not found for " + session);
        return handler;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        getHandler(session).handleTransportError(session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        try {
            getHandler(session).afterConnectionClosed(session, closeStatus);
        } finally {
            destroy(session);
        }
    }

    private void destroy(WebSocketSession session) {
        WebSocketHandler handler = this.sessionHandlerMap.remove(session.getId());
        try {
            if (handler != null) {
                this.provider.destroy(handler);
            }
        } catch (Throwable t) {
            logger.warn("Error while destroying handler", t);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return this.supportsPartialMessages;
    }

    @Override
    public String toString() {
        return "PerConnectionWebSocketHandler [handlerType=" + this.provider.getHandlerType() + "]";
    }

    public Collection<User> getAllUsers() {
        return sessionUserMap.values();
    }

    public Map<WebSocketSession, User> getSessionUserMap() {
        return sessionUserMap;
    }

    public Collection<User> getOtherUsers(final WebSocketSession session) {
        return sessionUserMap.entrySet().stream()
                .filter(e -> (e.getKey() != session))
                .map(e -> e.getValue())
                .collect(Collectors.toSet());
    }
}