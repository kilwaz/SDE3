package application.utils;

import application.error.*;
import application.error.Error;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WebRecordListenServer {
    HttpServer server = null;
    private static Logger log = Logger.getLogger(WebRecordListenServer.class);

    public WebRecordListenServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(10001), 0);
            server.createContext("/record", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            log.info("Started record listen server!");
        } catch (IOException ex) {
            Error.WEB_RECORD_SERVER.record().create(ex);
        }
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());

            log.info("*** Action ***");
            log.info("Action is " + params.get("action"));
            log.info("Type is " + params.get("tag"));
            log.info("ID is " + params.get("id"));
            log.info("Value is " + params.get("value"));
            log.info("");

            String response = "Action recorded";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
