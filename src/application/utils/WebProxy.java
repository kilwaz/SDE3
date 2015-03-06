package application.utils;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.net.ServerSocket;
import java.nio.charset.Charset;

public class WebProxy extends SDERunnable {
    private ServerSocket serverSocket = null;
    private Boolean listening = true;
    private Integer port = 10000;    //default
    private HttpProxyServer server;

    public WebProxy() {
        WebProxyManager.getInstance().addConnection(this);
    }

    public void threadRun() {
        System.out.println("Starting");
        server = DefaultHttpProxyServer.bootstrap()
                .withPort(port)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        return new HttpFiltersAdapter(originalRequest) {
                            @Override
                            public HttpResponse requestPre(HttpObject httpObject) {
                                //System.out.println("1");


                                return null;
                            }

                            @Override
                            public HttpResponse requestPost(HttpObject httpObject) {
                                //System.out.println("2");

                                return null;
                            }

                            @Override
                            public HttpObject responsePre(HttpObject httpObject) {
                                // System.out.println("3");

                                if (httpObject instanceof HttpResponse) {
                                    System.out.println("RESPONSE!");
                                    HttpResponse response = (HttpResponse) httpObject;
                                    HttpHeaders httpHeaders = response.headers();
                                    for (String name : httpHeaders.names()) {
                                        System.out.println("HEADER " + name + " -> " + httpHeaders.get(name));
                                    }
                                }

                                if (httpObject instanceof HttpContent) {
                                    HttpContent content = (HttpContent) httpObject;
                                    ByteBuf bytebuf = content.content();
                                    System.out.println(bytebuf.toString(Charset.forName("UTF-8")));
                                }

                                return httpObject;
                            }

                            @Override
                            public HttpObject responsePost(HttpObject httpObject) {
                                // System.out.println("4");

                                return httpObject;
                            }
                        };
                    }
                })
                .start();

        System.out.println("Started");

//        try {
//            serverSocket = new ServerSocket(port);
//            System.out.println("Started on: " + port);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        try {
//            while (listening) {
//                new WebProxyThread(serverSocket.accept()).start();
//            }
//            serverSocket.close();
//        } catch (IOException ex) {
//            // If we are not listening then the socket has been closed by us
//            if (listening) {
//                ex.printStackTrace();
//            }
//        }
    }

    public void close() {
        server.stop();
    }
}

