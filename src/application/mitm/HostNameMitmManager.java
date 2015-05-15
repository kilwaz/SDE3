package application.mitm;

import org.littleshoot.proxy.MitmManager;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

/**
 * {@link MitmManager} that uses the given host name to create a dynamic
 * certificate for. If a port is given, it will be truncated.
 */
public class HostNameMitmManager implements MitmManager {

    private BouncyCastleSslEngineSource sslEngineSource;

    public HostNameMitmManager() throws RootCertificateException {
        this(new Authority());
    }

    public HostNameMitmManager(Authority authority)
            throws RootCertificateException {
        try {
            sslEngineSource = new BouncyCastleSslEngineSource(authority, true, true);
        } catch (final Exception e) {
            throw new RootCertificateException("Errors during assembling root CA.", e);
        }
    }

    public BouncyCastleSslEngineSource getSSLEngineSource() {
        return sslEngineSource;
    }

    public SSLEngine serverSslEngine() {
        return sslEngineSource.newSslEngine();
    }

    public SSLEngine clientSslEngineFor(SSLSession serverSslSession, String serverHostAndPort) {
        try {
            String serverName = serverHostAndPort.split(":")[0];
            SubjectAlternativeNameHolder san = new SubjectAlternativeNameHolder();

            SSLEngine sslEngine = sslEngineSource.createCertForHost(serverName, san);

            return sslEngine;
        } catch (Exception e) {
            throw new FakeCertificateException("Creation dynamic certificate failed for " + serverHostAndPort, e);
        }
    }
}
