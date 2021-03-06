package sde.application.net.proxy.snoop;

import sde.application.net.proxy.WebProxyRequestManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import org.apache.log4j.Logger;

public class HttpProxyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private WebProxyRequestManager webProxyRequestManager;
    private static Logger log = Logger.getLogger(HttpProxyServerInitializer.class);

    public HttpProxyServerInitializer(SslContext sslCtx, WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // Here we create a protocol detector to try and figure out what we are receiving
        pipeline.addFirst("unifiedDetector", new UnifiedPortProtocolDetector(sslCtx, webProxyRequestManager, false, false));
    }
}
