package sde.application.data.export.har;

public class Response extends HarObject {
    private HarObject status = new HarObject("status", -1); // Defaults to -1
    private HarObject statusText = new HarObject("statusText", ""); // Defaults to blank
    private HarObject httpVersion = new HarObject("httpVersion", ""); // Defaults to blank
    private Cookies cookies = new Cookies();
    private Headers headers = new Headers();
    private Content content = new Content();
    private HarObject redirectURL = new HarObject("redirectURL", ""); // Defaults to blank
    private HarObject headersSize = new HarObject("headersSize", ""); // Defaults to blank
    private HarObject bodySize = new HarObject("bodySize", ""); // Defaults to blank

    public Response() {
        super("response", HarObject.OBJECT);
    }

    public HarObject getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = new HarObject("status", status);
    }

    public Cookies getCookies() {
        return cookies;
    }

    public Headers getHeaders() {
        return headers;
    }

    public Content getContent() {
        return content;
    }

    public void setStatusText(String statusText) {
        this.statusText = new HarObject("statusText", statusText);
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = new HarObject("httpVersion", httpVersion);
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = new HarObject("redirectURL", redirectURL);
    }

    public void setHeadersSize(String headersSize) {
        this.headersSize = new HarObject("headersSize", headersSize);
    }

    public void setBodySize(String bodySize) {
        this.bodySize = new HarObject("bodySize", bodySize);
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(status);
        output.writeObject(statusText);
        output.writeObject(httpVersion);
        output.writeObject(cookies);
        output.writeObject(headers);
        output.writeObject(content);
        output.writeObject(redirectURL);
        output.writeObject(headersSize);
        output.writeObject(bodySize);
        output.writeObject(getComment());
        output.endObject();
    }
}
