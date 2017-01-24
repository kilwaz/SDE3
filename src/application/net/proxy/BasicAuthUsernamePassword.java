package application.net.proxy;

public class BasicAuthUsernamePassword {
    private String username;
    private String password;

    public BasicAuthUsernamePassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
