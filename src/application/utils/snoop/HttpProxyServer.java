package application.utils.snoop;

import application.net.proxy.WebProxyManager;
import application.net.proxy.WebProxyRequestManager;
import application.node.implementations.RequestTrackerNode;
import application.utils.SDERunnable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

public final class HttpProxyServer extends SDERunnable {
    public static boolean SSL = false;
    static final int PORT = 8080;
    private WebProxyRequestManager webProxyRequestManager = new WebProxyRequestManager();

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HttpProxyServer() {
        WebProxyManager.getInstance().addConnection(this);
    }

    public void addRequestTrackerNode(RequestTrackerNode requestTrackerNode) {
        webProxyRequestManager.addRequestTrackerNode(requestTrackerNode);
    }

    public void threadRun() {
        // Configure SSL.

        // Configure the server
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            // Configure SSL.
            SslContext sslCtx;
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            } else {
                sslCtx = null;
            }

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpProxyServerInitializer(sslCtx, webProxyRequestManager));

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " + (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } catch (InterruptedException | CertificateException | SSLException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new HttpProxyServer();
    }
}
