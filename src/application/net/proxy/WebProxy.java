package application.net.proxy;


import application.mitm.HostNameMitmManager;
import application.mitm.RootCertificateException;
import application.node.implementations.RequestTrackerNode;
import application.utils.SDERunnable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class WebProxy extends SDERunnable {
    private Integer port = 10000;    //default
    private HttpProxyServer server;
    private WebProxyRequestManager webProxyRequestManager = new WebProxyRequestManager();

    public WebProxy() {
        WebProxyManager.getInstance().addConnection(this);
    }

    public void addRequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        webProxyRequestManager.addRequestTrackerNode(requestTrackerNode);
    }

    public void threadRun() {
        try {
            HostNameMitmManager hostNameMitmManager = new HostNameMitmManager();

            server = DefaultHttpProxyServer.bootstrap()
                    .withPort(port)
                            //.withManInTheMiddle(hostNameMitmManager)
                    .withFiltersSource(new HttpFiltersSourceAdapter() {
                                           @Override
                                           public int getMaximumRequestBufferSizeInBytes() {
                                               return Integer.MAX_VALUE;
                                           }

                                           @Override
                                           public int getMaximumResponseBufferSizeInBytes() {
                                               return Integer.MAX_VALUE;
                                           }

                                           @Override
                                           public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                                               return new HttpFiltersAdapter(originalRequest, ctx) {
                                                   @Override
                                                   public HttpResponse proxyToServerRequest(HttpObject httpObject) {
                                                       //System.out.println(originalRequest.hashCode() + "***** proxyToServerRequest - " + count++ + " - " + httpObject.getClass() + " - " + originalRequest.getUri());

                                                       // Check to see if the request is already being processed
                                                       if (!webProxyRequestManager.isCurrentActiveRequest(originalRequest.hashCode())) {
                                                           webProxyRequestManager.addNewActiveRequest(originalRequest.hashCode(), originalRequest);
                                                           webProxyRequestManager.setRequestStatus(originalRequest.hashCode(), WebProxyRequest.REQUEST_STATUS_PROXY_TO_SERVER);
                                                       }

                                                       webProxyRequestManager.addRequestHttpContentToRequest(originalRequest.hashCode(), httpObject);

                                                       if (httpObject instanceof LastHttpContent) {
                                                           if (webProxyRequestManager.isCurrentActiveRequest(originalRequest.hashCode())) {
                                                               webProxyRequestManager.setRequestStatus(originalRequest.hashCode(), WebProxyRequest.REQUEST_STATUS_SERVER_TO_PROXY);
                                                           }
                                                       }

                                                       if (httpObject instanceof FullHttpRequest) {
                                                           FullHttpRequest request = (FullHttpRequest) httpObject;
                                                           webProxyRequestManager.addFullHttpRequest(originalRequest.hashCode(), request);
                                                       }

                                                       return null;
                                                   }

                                                   @Override
                                                   public HttpObject serverToProxyResponse(HttpObject httpObject) {
                                                       //System.out.println(originalRequest.hashCode() + "***** serverToProxyResponse - " + count++ + " - " + httpObject.getClass() + " - " + originalRequest.getUri());

                                                       webProxyRequestManager.addResponseHttpContentToRequest(originalRequest.hashCode(), httpObject);

                                                       if (httpObject instanceof FullHttpResponse) {
                                                           FullHttpResponse response = (FullHttpResponse) httpObject;
                                                           webProxyRequestManager.addFullHttpRepsonse(originalRequest.hashCode(), response);
                                                       }

                                                       if (httpObject instanceof LastHttpContent) {
                                                           if (webProxyRequestManager.isCurrentActiveRequest(originalRequest.hashCode())) {
                                                               webProxyRequestManager.setRequestStatus(originalRequest.hashCode(), WebProxyRequest.REQUEST_STATUS_COMPLETED);
                                                               webProxyRequestManager.completeRequest(originalRequest.hashCode());
                                                           }
                                                       }

//                                                       if (httpObject instanceof DefaultHttpContent) {
//                                                           DefaultHttpContent response = (DefaultHttpContent) httpObject;
//                                                           ByteBuf buf = response.content();
//                                                           ByteBuf newBuf = Unpooled.wrappedBuffer(buf);
//                                                           String originalContent = buf.toString(CharsetUtil.UTF_8);
//
//                                                           ByteBuf conByteBuf = response.content();
//                                                           ByteBuf copyBuf = conByteBuf.copy();
//
//                                                           ByteBuffer conByteBuffer = ByteBuffer.allocate(copyBuf.readableBytes());
//                                                           copyBuf.readBytes(conByteBuffer);
//
//                                                           try {
//                                                               if (originalRequest.getUri().contains("en.wikipedia.org")) {
//                                                                   System.out.println("Unwrapping!");
//                                                                   SubjectAlternativeNameHolder san = new SubjectAlternativeNameHolder();
//
//                                                                   SSLEngine sslEngine = hostNameMitmManager.getSSLEngineSource().createCertForHost("en.wikipedia.org", san);
//                                                                   sslEngine.setUseClientMode(true);
//                                                                   //sslEngine.beginHandshake();
//                                                                   ByteBuffer output = ByteBuffer.allocate(buf.capacity() * 10);
//                                                                   //sslEngine.wrap()
//                                                                   SSLEngineResult result = sslEngine.unwrap(conByteBuffer, output);
//                                                                   SSLEngineResult result2 = sslEngine.unwrap(output, conByteBuffer);
//                                                                   System.out.println("Finished " + result.getStatus() + " - " + result.getHandshakeStatus());
//                                                                   System.out.println("Finished2 " + result2.getStatus() + " - " + result2.getHandshakeStatus());
//                                                                   System.out.println("Before " + originalContent);
//                                                                   System.out.println("After " + new String(output.array(), CharsetUtil.UTF_8));
//                                                                   System.out.println("After2 " + new String(output.array(), CharsetUtil.UTF_8));
//                                                               }
//                                                           } catch (OperatorCreationException | ExecutionException | GeneralSecurityException | IOException e) {
//                                                               e.printStackTrace();
//                                                           }
//
//                                                           //System.out.println(originalContent);
//                                                       }

//                                                       if (httpObject instanceof LastHttpContent) {
//                                                           LastHttpContent response = (LastHttpContent) httpObject;
//                                                           System.out.println("***** LastHttpContent - " + count++ + " - " + httpObject.getClass() + " - " + originalRequest.getUri());
//                                                       }

//                                                       if (httpObject instanceof FullHttpResponse) {
//                                                           FullHttpResponse response = (FullHttpResponse) httpObject;

                                                       //ByteBuf buf = response.content();

                                                       //ByteBuf newBuf = Unpooled.wrappedBuffer(buf);

//                                                           if (response.headers().get("Content-Type").contains("text/html")) {
//                                                               StringBuilder builder = new StringBuilder();
//
//                                                               String originalContent = buf.toString(CharsetUtil.UTF_8);
//
//                                                               Document doc = Jsoup.parse(originalContent);
//
//                                                               //                                        for (Element element : doc.getAllElements()) {
//                                                               //                                            if (element.text().contains("Demo") && element.children().size() == 0) {
//                                                               //                                                element.text(element.text().replace("Demo", "slap"));
//                                                               //                                            }
//                                                               //                                        }
//
//                                                               URL bashEditorURL = getClass().getResource("/WebProxyRecord.js");
//
//                                                               String content = "";
//                                                               try {
//                                                                   byte[] encoded = Files.readAllBytes(Paths.get(bashEditorURL.toExternalForm().replaceFirst("file:/", "")));
//                                                                   content = new String(encoded, "UTF8");
//                                                               } catch (IOException e) {
//                                                                   e.printStackTrace();
//                                                               }
//
//                                                               //DataNode dataNode = new DataNode("", "");
//                                                               //dataNode.setWholeData("<script>" + content + "</script>");
//                                                               //doc.body().appendChild(dataNode);
//
//                                                               builder.append(doc.toString());
//                                                               newBuf = Unpooled.copiedBuffer(builder.toString(), CharsetUtil.UTF_8);
//                                                           }

                                                       //DefaultFullHttpResponse newResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, newBuf);

//                                                           newResponse.headers().set("Content-Length", newResponse.content().readableBytes());
//
//                                                           HttpHeaders httpHeaders = response.headers();
//                                                           for (String name : httpHeaders.names()) {
//                                                               if (!"Content-Length".equals(name)) {
//                                                                   newResponse.headers().set(name, httpHeaders.get(name));
//                                                               }
//                                                           }

//                                                           return response;
//                                                       }
                                                       return httpObject;
                                                   }
                                               };
                                           }
                                       }

                    ).start();
        } catch (RootCertificateException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        server.stop();
    }
}

