package sde.application.data.export.har;

public class Request extends HarObject {
    private HarObject method = new HarObject("method", ""); // Default to blank
    private HarObject url = new HarObject("url", ""); // Default to blank
    private HarObject httpVersion = new HarObject("httpVersion", ""); // Default to blank
    private Cookies cookies = new Cookies();
    private Headers headers = new Headers();
    private QueryString queryString = new QueryString();
    private PostData postData = new PostData();
    private HarObject headersSize = new HarObject("headersSize", new Integer(-1)); // Default to -1
    private HarObject bodySize = new HarObject("bodySize", new Integer(-1)); // Default to -1

    public Request() {
        super("request", OBJECT);
    }

    public HarObject getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = new HarObject("method", method);
    }

    public Cookies getCookies() {
        return cookies;
    }

    public Headers getHeaders() {
        return headers;
    }

    public QueryString getQueryString() {
        return queryString;
    }

    public PostData getPostData() {
        return postData;
    }

    public void setUrl(String url) {
        this.url = new HarObject("url", url);
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = new HarObject("httpVersion", httpVersion);
    }

    public void setHeadersSize(Integer headersSize) {
        this.headersSize = new HarObject("headersSize", headersSize);
    }

    public void setBodySize(Integer bodySize) {
        this.bodySize = new HarObject("bodySize", bodySize);
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(method);
        output.writeObject(url);
        output.writeObject(httpVersion);
        output.writeObject(cookies);
        output.writeObject(headers);
        output.writeObject(queryString);
        output.writeObject(postData);
        output.writeObject(headersSize);
        output.writeObject(bodySize);
        output.writeObject(getComment());
        output.endObject();
    }
}
