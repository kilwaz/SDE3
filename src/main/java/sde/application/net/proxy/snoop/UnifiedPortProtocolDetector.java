package sde.application.net.proxy.snoop;

import sde.application.net.proxy.WebProxyRequestManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLEngine;
import java.util.List;

public class UnifiedPortProtocolDetector extends ByteToMessageDecoder {

    private final SslContext sslCtx;
    private final WebProxyRequestManager webProxyRequestManager;
    private static Logger log = Logger.getLogger(UnifiedPortProtocolDetector.class);

    public UnifiedPortProtocolDetector(SslContext sslCtx, WebProxyRequestManager webProxyRequestManager) {
        this.sslCtx = sslCtx;
        this.webProxyRequestManager = webProxyRequestManager;
    }

    private boolean isSsl(ByteBuf byteBuf) {
        return SslHandler.isEncrypted(byteBuf);
    }

    private boolean isHttp(byte char1, byte char2) {
        return
                char1 == 'G' && char2 == 'E' || // GET
                        char1 == 'P' && char2 == 'O' || // POST
                        char1 == 'P' && char2 == 'U' || // PUT
                        char1 == 'H' && char2 == 'E' || // HEAD
                        char1 == 'O' && char2 == 'P' || // OPTIONS
                        char1 == 'P' && char2 == 'A' || // PATCH
                        char1 == 'D' && char2 == 'E' || // DELETE
                        char1 == 'T' && char2 == 'R' || // TRACE
                        char1 == 'C' && char2 == 'O';   // CONNECT
    }

    private boolean isWebSocket() {
        return false;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 5) { // Can't read it if there are less than 5 bytes
            return;
        }

        if (isSsl(byteBuf)) {
            enabledSsl(channelHandlerContext);
        } else {
            final byte char1 = byteBuf.getByte(byteBuf.readerIndex());
            final byte char2 = byteBuf.getByte(byteBuf.readerIndex() + 1);
            if (isHttp(char1, char2)) {
                enableHttp(channelHandlerContext);
            }
        }
    }

    // Switch to interpreting HTTP
    private void enableHttp(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(1048576));
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(new HttpProxyServerHandler(pipeline, webProxyRequestManager));

        pipeline.remove(this);
    }

    // Turn SSL on
    private void enabledSsl(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        SSLEngine sslEngine = SSLContextProvider.get().createSSLEngine();
        sslEngine.setUseClientMode(false); // We are a server
        sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());

        pipeline.addLast("ssl", new SslHandler(sslEngine));
        pipeline.addLast(new UnifiedPortProtocolDetector(sslCtx, webProxyRequestManager));

        pipeline.remove(this);
    }
}
