package application.net.proxy;

import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.Socket;

public class WebProxyThread extends Thread {
    private Socket socket = null;
    private static final int BUFFER_SIZE = 32768;

    public WebProxyThread(Socket socket) {
        super("WebProxyThread");
        this.socket = socket;
    }

    public void run() {


        HttpProxyServer server = DefaultHttpProxyServer.bootstrap()
                .withPort(10000)
                .start();


//        try {
//            final OutputStream outToClient = socket.getOutputStream();
//            // connects a socket to the server
//            String inputLine = "";
//
//            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//            String requestURL = "";
//            int requestLineCounter = 0;
//            List<String> requestInputList = new ArrayList<>();
//            while ((inputLine = in.readLine()) != null) {
//                requestInputList.add(inputLine);
//                try {
//                    StringTokenizer tok = new StringTokenizer(inputLine);
//                    tok.nextToken();
//                } catch (Exception e) {
//                    break;
//                }
//                //parse the first line of the request to find the url
//                if (requestLineCounter == 0) {
//                    String[] tokens = inputLine.split(" ");
//                    requestURL = tokens[1];
//                    //can redirect this to output log
//                    System.out.println("Request for : " + requestURL);
//                }
//
//                requestLineCounter++;
//            }
//
//            if (!requestURL.contains("http")) {
//                requestURL = "https://" + requestURL;
//            }
//
//            URL url = new URL(requestURL);
//            System.out.println("HOST " + url.getHost() + " PORT " + url.getPort());
//
//            if (url.getPort() == 443) {
//
//            }
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            if (requestInputList.get(0).contains("GET")) {
//                System.out.println("GET");
//                connection.setRequestMethod("GET");
//            } else if (requestInputList.get(0).contains("POST")) {
//                System.out.println("POST");
//                connection.setRequestMethod("POST");
//            }
//
//            for (String property : requestInputList) {
//                if (property.contains(": ")) {
//                    String start = property.substring(0, property.indexOf(":"));
//                    String rest = property.substring(property.indexOf(":") + 1);
//
//                    System.out.println("Property " + start + " -> " + rest);
//
//                    connection.setRequestProperty(start, rest);
//                }
//            }
//
//            connection.setUseCaches(false);
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//
//
//            InputStream is = connection.getInputStream();
//            //BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//
//            System.out.println("Reading output?");
//
//
//            int len;
//            byte[] buffer = new byte[4096];
//            while (-1 != (len = is.read(buffer))) {
//                System.out.println(len);
//                out.write(buffer, 0, len);
//            }
//
//            out.flush();
//            out.close();
//            in.close();
//            outToClient.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
