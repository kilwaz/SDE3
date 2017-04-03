package application.net.proxy.snoop;

import application.error.Error;
import application.net.proxy.ProxyRequestListener;
import application.net.proxy.WebProxyManager;
import application.net.proxy.WebProxyRequestManager;
import application.utils.SDERunnable;
import application.utils.managers.ThreadManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.cert.CertificateException;
import java.util.concurrent.Callable;

public final class HttpProxyServer extends SDERunnable {
    public static final Integer STATUS_CLOSED = 2;
    final static boolean SSL = false;
    private static final Integer STATUS_INACTIVE = -1;
    private static final Integer STATUS_STARTING = 0;
    private static final Integer STATUS_CONNECTED = 1;
    private static int PORT = 8080;
    private static Logger log = Logger.getLogger(HttpProxyServer.class);
    private Integer status = STATUS_INACTIVE;
    private WebProxyRequestManager webProxyRequestManager = new WebProxyRequestManager();
    private int runningPort = 8080;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public HttpProxyServer() {
        WebProxyManager.getInstance().addConnection(this);
    }

    /**
     * Checks to see if a specific port is available.
     *
     * @param port the port to check for availability
     */
    public static synchronized boolean available(int port) {
        if (port < 8080 || port > 10000) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);

            // Here we check to see if the port has already been assigned, most likely very recently to another Http Proxy that hasn't finished initialising
            for (HttpProxyServer httpProxyServer : WebProxyManager.getInstance().getOpenProxies()) {
                if (httpProxyServer.getRunningPort() == port) {
                    return false;
                }
            }

            return true;
        } catch (IOException ex) {
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                /* should not be thrown */
                }
            }
        }

        return false;
    }

    public void addRequestListener(ProxyRequestListener proxyRequestListener) {
        webProxyRequestManager.addProxyRequestListener(proxyRequestListener);
    }

    public void threadRun() {
        status = STATUS_STARTING;

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

            Boolean availablePortFound = false;
            Integer testingPort = PORT;

            while (!availablePortFound) {
                if (!available(testingPort)) {
                    testingPort++;
                } else {
                    availablePortFound = true;
                }
            }

            runningPort = testingPort;

            log.info("Starting on port " + runningPort);
            Channel ch = b.bind(runningPort).sync().channel();
            webProxyRequestManager.getRecordedProxy().setConnectionString("localhost:" + runningPort);
            webProxyRequestManager.getRecordedProxy().save();
            status = STATUS_CONNECTED;
            ch.closeFuture().sync();
        } catch (InterruptedException | CertificateException | SSLException ex) {
            Error.START_HTTP_PROXY.record().create(ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
        status = STATUS_CLOSED;

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        ThreadManager.getInstance().removeInactiveThreads(); // Check to see if the thread has been removed
        WebProxyManager.getInstance().removeInactiveProxies(); // Check to see if the proxy can be removed
    }

    public WebProxyRequestManager getWebProxyRequestManager() {
        return webProxyRequestManager;
    }

    public String getConnectionString() {
        return "localhost:" + runningPort;
    }

    public int getRunningPort() {
        return runningPort;
    }

    public Callable<Boolean> nowConnected() {
        return this::isConnected;
    }

    public Boolean isConnected() {
        return status.equals(STATUS_CONNECTED);
    }

    public Integer getStatus() {
        return status;
    }
}
