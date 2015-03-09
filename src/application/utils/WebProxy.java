package application.utils;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class WebProxy extends SDERunnable {
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
                    @Override
                    public int getMaximumResponseBufferSizeInBytes() {
                        return 1024 * 1024;
                    }

                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        return new HttpFiltersAdapter(originalRequest) {
                            @Override
                            public HttpResponse requestPre(HttpObject httpObject) {
                                return null;
                            }

                            @Override
                            public HttpResponse requestPost(HttpObject httpObject) {
                                return null;
                            }

                            @Override
                            public HttpObject responsePre(HttpObject httpObject) {
                                if (httpObject instanceof FullHttpResponse) {

                                    FullHttpResponse response = (FullHttpResponse) httpObject;
                                    ByteBuf buf = response.content();

                                    ByteBuf newBuf = Unpooled.wrappedBuffer(buf);


                                    if (response.headers().get("Content-Type").contains("text/html")) {
                                        StringBuilder builder = new StringBuilder();

                                        String originalContent = buf.toString(CharsetUtil.UTF_8);

                                        Document doc = Jsoup.parse(originalContent);

                                        for (Element element : doc.getAllElements()) {
                                            if (element.text().contains("Demo") && element.children().size() == 0) {
                                                element.text(element.text().replace("Demo", "slap"));
                                            }
                                        }

                                        builder.append(doc.toString());
                                        newBuf = Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8);
                                    }

                                    DefaultFullHttpResponse newResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, newBuf);

                                    newResponse.headers().set("Content-Length", newResponse.content().readableBytes());

                                    HttpHeaders httpHeaders = response.headers();
                                    for (String name : httpHeaders.names()) {
                                        if (!"Content-Length".equals(name)) {
                                            newResponse.headers().set(name, httpHeaders.get(name));
                                        }
                                    }

                                    return newResponse;
                                }

                                return httpObject;
                            }

                            @Override
                            public HttpObject responsePost(HttpObject httpObject) {
                                return httpObject;
                            }
                        };
                    }
                })
                .start();

        System.out.println("Started");
    }

    public void close() {
        server.stop();
    }
}

