package application.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WebRecordListenServer {
    HttpServer server = null;

    public WebRecordListenServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(10001), 0);
            server.createContext("/record", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("Started record listen server!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());

            System.out.println("*** Action ***");
            System.out.println("Action is " + params.get("action"));
            System.out.println("Type is " + params.get("tag"));
            System.out.println("ID is " + params.get("id"));
            System.out.println("Value is " + params.get("value"));
            System.out.println("");

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
