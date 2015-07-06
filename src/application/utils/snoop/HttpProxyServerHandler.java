package application.utils.snoop;

import application.net.proxy.WebProxyRequestManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<Object> {
    private ChannelPipeline pipeline;
    private Boolean SSL = false;
    private WebProxyRequestManager webProxyRequestManager;

    private static Logger log = Logger.getLogger(HttpProxyServerHandler.class);

    public HttpProxyServerHandler(ChannelPipeline pipeline, WebProxyRequestManager webProxyRequestManager) {
        this.pipeline = pipeline;
        this.webProxyRequestManager = webProxyRequestManager;
    }

    private HttpRequest request;

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (HttpHeaderUtil.is100ContinueExpected(request)) {
                send100Continue(ctx);
            }

            //log.info("Method " + request.method());

            if ("CONNECT".equals(request.method().toString())) {
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
                ctx.write(response);

                //log.info("SWITCHING TO SSL");

                SSLEngine sslEngine = SSLContextProvider.get().createSSLEngine();
                sslEngine.setUseClientMode(false); // We are a server
                sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());

                pipeline.addFirst("ssl", new SslHandler(sslEngine));
                //log.info("SSL READY ON THIS CHANNEL NOW");
                SSL = true;

                return;
            } else {
//                try {
//                    HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
//
//                    for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
//                        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
//                            Attribute attribute = (Attribute) data;
//                            //log.info(attribute.getName() + ":" + attribute.getValue());
//                        }
//                    }
//                } catch (IOException ex) {
//                    log.error(ex);
//                }

                //log.info("URI!!! " + request.uri() + " " + request);

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
                Map<String, List<String>> params = queryStringDecoder.parameters();
                if (!params.isEmpty()) {
                    for (Map.Entry<String, List<String>> p : params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        for (String val : vals) {
                            //log.info("PARAMS " + key + " = " + val);
                        }
                    }
                }
            }
        }

        if (msg instanceof LastHttpContent) {
            LastHttpContent trailer = (LastHttpContent) msg;
            if (!writeResponse(trailer, ctx)) {
                // If keep-alive is off, close the connection once the content is fully written.
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);

        // Pass on the headers sent to the proxy
        HttpHeaders headers = request.headers();
        HashMap<String, String> requestHeaders = new HashMap<>();
        if (!headers.isEmpty()) {
            for (Map.Entry<CharSequence, CharSequence> h : headers) {
                CharSequence key = h.getKey();
                CharSequence value = h.getValue();
                //log.info("REQUEST TO PROXY HEADER " + key + " = " + value);
                requestHeaders.put(key.toString(), value.toString());
            }
        }

        // Pass on the parameters sent by the proxy
        HashMap<String, String> requestParameters = new HashMap<>();
        try {
            HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);

            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    requestParameters.put(attribute.getName(), attribute.getValue());
                }
            }
        } catch (IOException ex) {
            log.error(ex);
        }

        String uri = request.uri();
        if (SSL) {
            uri = "https://" + requestHeaders.get("Host") + request.uri();
        }

        StandaloneHTTPRequest standaloneHTTPRequest = new StandaloneHTTPRequest()
                .setUrl(uri)
                .setMethod(request.method().toString())
                .setRequestHeaders(requestHeaders)
                .setRequestParameters(requestParameters)
                .setHttps(SSL)
                .setRequestManager(webProxyRequestManager)
                .execute();


        // Create our response object
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                Unpooled.copiedBuffer(standaloneHTTPRequest.getResponse().array()));

        HashMap<String, String> responseHeaders = standaloneHTTPRequest.getResponseHeaders();
        for (String header : responseHeaders.keySet()) {
            response.headers().set(header, responseHeaders.get(header));
            //log.info("RESPONSE FROM SERVER THEN TO CLIENT HEADER " + header + " - " + responseHeaders.get(header));
        }

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
