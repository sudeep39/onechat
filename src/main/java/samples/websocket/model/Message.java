package samples.websocket.model;

import java.util.Date;

/**
 * Created by M1011579 on 5/3/2016.
 */
public class Message {

    private String type;
    private Object body;
    private User from;
    private User to;
    private long date;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public Date getDate() {
        return new Date(date);
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", body='" + body + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}