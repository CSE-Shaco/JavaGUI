package shared.domain;

import java.io.Serial;
import java.io.Serializable;

public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String displayName;

    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return "User{" + "username='" + username + '\'' + ", displayName='" + displayName + '\'' + '}';
    }
}