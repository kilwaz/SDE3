package application.net.proxy.snoop;

import application.net.proxy.WebProxyRequestManager;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

public class HttpProxyServerInitializer extends ChannelInitializer<SocketChannel> {

    // Commands to keep
    // certutil -addstore Root cert.cer
    // keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass secret -validity 360 -keysize 2048 -dname "CN=*.uk.spl.com"

    private final SslContext sslCtx;
    private WebProxyRequestManager webProxyRequestManager;

    public HttpProxyServerInitializer(SslContext sslCtx, WebProxyRequestManager webProxyRequestManager) {
        this.webProxyRequestManager = webProxyRequestManager;
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        if (HttpProxyServer.SSL) {
            SSLEngine sslEngine = SSLContextProvider.get().createSSLEngine();
            sslEngine.setUseClientMode(false); // We are a server
            sslEngine.setEnabledCipherSuites(sslEngine.getSupportedCipherSuites());

            pipeline.addLast("ssl", new SslHandler(sslEngine));
        }

        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(1048576));
        pipeline.addLast(new HttpResponseEncoder());

        // Remove the following line if you don't want automatic content compression.
        //p.addLast(new HttpContentCompressor());

        pipeline.addLast(new HttpProxyServerHandler(pipeline, webProxyRequestManager));
    }
}
