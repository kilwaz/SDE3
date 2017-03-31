package application.net.proxy.snoop;

import application.net.proxy.WebProxyRequestManager;
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

    private Boolean sslEnabled = false;
    private Boolean httpEnabled = false;

    public UnifiedPortProtocolDetector(SslContext sslCtx, WebProxyRequestManager webProxyRequestManager, Boolean sslEnabled, Boolean httpEnabled) {
        this.sslCtx = sslCtx;
        this.webProxyRequestManager = webProxyRequestManager;
        this.sslEnabled = sslEnabled;
        this.httpEnabled = httpEnabled;
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

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        if (byteBuf.readableBytes() < 5) { // Can't read it if there are less than 5 bytes
            return;
        }

        if (!sslEnabled && isSsl(byteBuf)) {
            enabledSsl(channelHandlerContext);
        } else {
            final byte char1 = byteBuf.getByte(byteBuf.readerIndex());
            final byte char2 = byteBuf.getByte(byteBuf.readerIndex() + 1);
            if (!httpEnabled && isHttp(char1, char2)) {
                enableHttp(channelHandlerContext);
            }
        }

        // Pass the buffer onto the next handler
        if (byteBuf.refCnt() > 0) {
            byteBuf.resetReaderIndex();
            out.add(byteBuf.readBytes(byteBuf.readableBytes()));
        }
    }

    // Switch to interpreting HTTP
    private void enableHttp(ChannelHandlerContext ctx) {
        httpEnabled = true;

        ChannelPipeline pipeline = ctx.pipeline();

        //pipeline.addLast("webSocketDetector", new WebSocketDetector()); // Switches to web socket if an upgrade request is sent
        pipeline.addLast("unifiedDetectorHttp", new UnifiedPortProtocolDetector(sslCtx, webProxyRequestManager, sslEnabled, httpEnabled));
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("httpHandler", new HttpProxyServerHandler(webProxyRequestManager));

        pipeline.remove(this);
    }

    // Turn SSL on
    private void enabledSsl(ChannelHandlerContext ctx) {
        sslEnabled = true;

        ChannelPipeline pipeline = ctx.pipeline();

        SSLEngine sslEngine = SSLContextProvider.get().createSSLEngine();
        sslEngine.setUseClientMode(false); // We are a server
        sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());

        pipeline.addAfter("unifiedDetectorHttp", "ssl", new SslHandler(sslEngine));
        webProxyRequestManager.setSSL(true);

        pipeline.remove(this);
    }
}
