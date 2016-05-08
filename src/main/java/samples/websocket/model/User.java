package samples.websocket.model;

/**
 * Created by M1011579 on 5/4/2016.
 */
public class User {

    private String id;
    private String handle;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (!getId().equals(user.getId())) return false;
        return getHandle().equals(user.getHandle());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getHandle().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", handle='" + handle + '\'' +
                '}';
    }
}
