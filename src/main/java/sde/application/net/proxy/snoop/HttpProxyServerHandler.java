package sde.application.net.proxy.snoop;

import sde.application.error.Error;
import sde.application.net.proxy.WebProxyRequestManager;
import sde.application.utils.managers.StatisticsManager;
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
import io.netty.handler.codec.http.websocketx.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger log = Logger.getLogger(HttpProxyServerHandler.class);
    private WebProxyRequestManager webProxyRequestManager;
    private HttpRequest request;
    private WebSocketServerHandshaker handshaker;

    public HttpProxyServerHandler(WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = this.request = (HttpRequest) msg;

                if (HttpUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                // Move this to the UnifiedProtocolDetector
                if ("CONNECT".equals(request.method().toString())) {
                    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
                    ctx.write(response);

                    return;
                } else if ("websocket".equals(request.headers().get("Upgrade"))) { // Upgrade to websocket support if requested
                    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaderNames.HOST) + request.uri(), null, true);
                    handshaker = wsFactory.newHandshaker(request);

                    if (handshaker == null) {
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    } else {
                        handshaker.handshake(ctx.channel(), request);
                    }

                    ChannelPipeline pipeline = ctx.pipeline();

                    pipeline.replace("wsdecoder", "wsdecoder", new WebSocket13FrameDecoder(true, true, 100000));
                    pipeline.replace("wsencoder", "wsencoder", new WebSocket13FrameEncoder(false));
                    return;
                }

                if (msg instanceof LastHttpContent) {
                    LastHttpContent trailer = (LastHttpContent) msg;

                    if (!writeResponse(trailer, ctx)) {
                        // If keep-alive is off, close the connection once the content is fully written.
                        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                    }
                }
            } else if (msg instanceof WebSocketFrame) {
                if (msg instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
                    log.info("Got text from of " + textWebSocketFrame.text());
                    //return;
                } else if (msg instanceof CloseWebSocketFrame) {
                    log.info("Closing web socket connection");
                    if (handshaker != null) {
                        handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg);
                    }
                    //return;
                } else {
                    log.info("We got a frame but we don't know how to handle it yet");
                }
            }
        } catch (IllegalStateException ex) {
            Error.HTTP_PROXY_RECEIVE_MESSAGE.record().create(ex);
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        // Pass on the headers sent to the proxy
        HttpHeaders headers = request.headers();
        HashMap<String, String> requestHeaders = new HashMap<>();
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> h : headers) {
                CharSequence key = h.getKey();
                CharSequence value = h.getValue();
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
            Error.HTTP_PROXY_RESPONSE.record().create(ex);
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException ex) {
            Error.HTTP_PROXY_BAD_END_OF_LINE.record().create(ex);
        }

        String uri = request.uri();
        if (webProxyRequestManager.getSSL()) {
            if (!uri.startsWith("http://") && !uri.startsWith("https://")) {
                uri = "https://" + requestHeaders.get("Host") + request.uri();
            }
        }

        log.info("Loading " + uri);

        // Performs and returns the request we want to make from proxy server to outside world
        StandaloneHTTPRequest standaloneHTTPRequest = new StandaloneHTTPRequest()
                .setRequestManager(webProxyRequestManager)
                .setUrl(uri)
                .setMethod(request.method().toString())
                .setRequestHeaders(requestHeaders)
                .setRequestParameters(requestParameters)
                .setHttps(webProxyRequestManager.getSSL())
                .execute();

        // This can happen if we get an exception when executing the request, for example a bad URL
        if (standaloneHTTPRequest != null) {
            // Create our response object
            ByteBuffer responseBuffer = standaloneHTTPRequest.getResponse();
            if (responseBuffer != null) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HTTP_1_1, currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST,
                        Unpooled.copiedBuffer(responseBuffer.array()));

                HashMap<String, String> responseHeaders = standaloneHTTPRequest.getResponseHeaders();
                for (String header : responseHeaders.keySet()) {
                    response.headers().set(header, responseHeaders.get(header));
                }

                if (keepAlive) {
                    // Add 'Content-Length' header only for a keep-alive connection.
                    int readableByte = response.content().readableBytes();
                    response.headers().setInt(CONTENT_LENGTH, readableByte);
                    // Add keep alive header as per:
                    StatisticsManager.getInstance().getTotalStatisticStore().addResponseSize(readableByte);
                    StatisticsManager.getInstance().getSessionStatisticStore().addResponseSize(readableByte);
                    response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }

                // Write the response.
                ctx.write(response);
            }
        }

        return keepAlive;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
