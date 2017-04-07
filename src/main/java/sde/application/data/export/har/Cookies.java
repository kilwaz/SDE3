package sde.application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class Cookies extends HarObject {
    private List<Cookie> cookies = new ArrayList<>();

    public Cookies() {
        super("cookies", LIST);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        cookies.forEach(output::writeObject);
        output.endObject();
    }
}
