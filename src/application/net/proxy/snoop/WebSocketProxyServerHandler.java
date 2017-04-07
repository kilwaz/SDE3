package application.net.proxy.snoop;

import application.error.Error;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpHeaders;

public class WebSocketProxyServerHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger log = Logger.getLogger(WebSocketProxyServerHandler.class);

    public WebSocketProxyServerHandler() {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                log.info("Http request");

                WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://" + request.headers().get(HttpHeaders.Names.HOST) + request.uri(), null, true);
                WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);

                if (handshaker == null) {
                    WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                } else {
                    handshaker.handshake(ctx.channel(), request);
                }

                ChannelPipeline pipeline = ctx.pipeline();

                pipeline.replace("wsdecoder", "wsdecoder", new WebSocket13FrameDecoder(true, true, 100000));
                pipeline.replace("wsencoder", "wsencoder", new WebSocket13FrameEncoder(false));
            }
            if (msg instanceof WebSocketFrame) {
                if (msg instanceof TextWebSocketFrame) {
                    TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) msg;
                    log.info("Got text from of " + textWebSocketFrame.text());
                } else {
                    log.info("We got a frame but we don't know how to handle it yet");
                }
            }
            if (msg instanceof CloseWebSocketFrame) {
                log.info("Closing frame!!");
            }
        } catch (IllegalStateException ex) {
            Error.HTTP_PROXY_RECEIVE_MESSAGE.record().create(ex);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
