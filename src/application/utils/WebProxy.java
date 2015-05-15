package application.utils;


import application.mitm.HostNameMitmManager;
import application.mitm.RootCertificateException;
import application.mitm.SubjectAlternativeNameHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.bouncycastle.operator.OperatorCreationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

public class WebProxy extends SDERunnable {
    private Integer port = 10000;    //default
    private HttpProxyServer server;

    public WebProxy() {
        WebProxyManager.getInstance().addConnection(this);
    }

    public void threadRun() {
        System.out.println("Starting web proxy");

        try {
            HostNameMitmManager hostNameMitmManager = new HostNameMitmManager();

            server = DefaultHttpProxyServer.bootstrap()
                    .withPort(port)
                    .withManInTheMiddle(hostNameMitmManager)
                    .withFiltersSource(new HttpFiltersSourceAdapter() {
                                           @Override
                                           public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                               return new HttpFiltersAdapter(originalRequest, ctx) {
                                                   private int count = 0;

                                                   @Override
                                                   public HttpObject serverToProxyResponse(HttpObject httpObject) {
                                                       //System.out.println("***** Hello - " + count++ + " - " + httpObject.getClass() + " - " + originalRequest.getUri());

                                                       if (httpObject instanceof DefaultHttpContent) {
                                                           DefaultHttpContent response = (DefaultHttpContent) httpObject;
                                                           ByteBuf buf = response.content();
                                                           ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
                                                           String originalContent = buf.toString(CharsetUtil.UTF_8);

                                                           ByteBuf conByteBuf = response.content();
                                                           ByteBuf copyBuf = conByteBuf.copy();

                                                           ByteBuffer conByteBuffer = ByteBuffer.allocate(copyBuf.readableBytes());
                                                           copyBuf.readBytes(conByteBuffer);

                                                           try {
                                                               if (originalRequest.getUri().contains("en.wikipedia.org")) {
                                                                   System.out.println("Unwrapping!");
                                                                   SubjectAlternativeNameHolder san = new SubjectAlternativeNameHolder();

                                                                   SSLEngine sslEngine = hostNameMitmManager.getSSLEngineSource().createCertForHost("en.wikipedia.org", san);
                                                                   sslEngine.setUseClientMode(true);
                                                                   //sslEngine.beginHandshake();
                                                                   ByteBuffer output = ByteBuffer.allocate(buf.capacity() * 10);
                                                                   //sslEngine.wrap()
                                                                   SSLEngineResult result = sslEngine.unwrap(conByteBuffer, output);
                                                                   SSLEngineResult result2 = sslEngine.unwrap(output, conByteBuffer);
                                                                   System.out.println("Finished " + result.getStatus() + " - " + result.getHandshakeStatus());
                                                                   System.out.println("Finished2 " + result2.getStatus() + " - " + result2.getHandshakeStatus());
                                                                   System.out.println("Before " + originalContent);
                                                                   System.out.println("After " + new String(output.array(), CharsetUtil.UTF_8));
                                                                   System.out.println("After2 " + new String(output.array(), CharsetUtil.UTF_8));
                                                               }
                                                           } catch (SSLException e) {
                                                               e.printStackTrace();
                                                           } catch (OperatorCreationException e) {
                                                               e.printStackTrace();
                                                           } catch (GeneralSecurityException e) {
                                                               e.printStackTrace();
                                                           } catch (ExecutionException e) {
                                                               e.printStackTrace();
                                                           } catch (IOException e) {
                                                               e.printStackTrace();
                                                           }

                                                           //System.out.println(originalContent);
                                                       }

                                                       if (httpObject instanceof FullHttpResponse) {

                                                           FullHttpResponse response = (FullHttpResponse) httpObject;
                                                           ByteBuf buf = response.content();

                                                           ByteBuf newBuf = Unpooled.wrappedBuffer(buf);

                                                           if (response.headers().get("Content-Type").contains("text/html")) {
                                                               StringBuilder builder = new StringBuilder();

                                                               String originalContent = buf.toString(CharsetUtil.UTF_8);

                                                               Document doc = Jsoup.parse(originalContent);

                                                               //                                        for (Element element : doc.getAllElements()) {
                                                               //                                            if (element.text().contains("Demo") && element.children().size() == 0) {
                                                               //                                                element.text(element.text().replace("Demo", "slap"));
                                                               //                                            }
                                                               //                                        }

                                                               URL bashEditorURL = getClass().getResource("/WebProxyRecord.js");

                                                               String content = "";
                                                               try {
                                                                   byte[] encoded = Files.readAllBytes(Paths.get(bashEditorURL.toExternalForm().replaceFirst("file:/", "")));
                                                                   content = new String(encoded, "UTF8");
                                                               } catch (IOException e) {
                                                                   e.printStackTrace();
                                                               }

                                                               DataNode dataNode = new DataNode("", "");
                                                               dataNode.setWholeData("<script>" + content + "</script>");
                                                               doc.body().appendChild(dataNode);

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
                                               };
                                           }
                                       }

                    )
                    .start();
        } catch (RootCertificateException e) {
            e.printStackTrace();
        }

        System.out.println("Started");
    }

    public void close() {
        server.stop();
    }
}

