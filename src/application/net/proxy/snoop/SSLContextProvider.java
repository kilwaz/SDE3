package application.net.proxy.snoop;

import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class SSLContextProvider {
    private static SSLContext sslContext = null;
    private static Logger log = Logger.getLogger(SSLContextProvider.class);

    public static SSLContext get() {
        if (sslContext == null) {
            try {


//                CertificateFactory factory = CertificateFactory.getInstance("X.509");
//                X509Certificate cert = (X509Certificate) factory.generateCertificate(new FileInputStream("C:\\Users\\alex\\keystore.jks"));
//

                sslContext = SSLContext.getInstance("TLS");
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(new FileInputStream("C:\\Users\\alex\\keystore.jks"), "secret".toCharArray());
//                ks.setCertificateEntry("SDE", cert);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, "secret".toCharArray());
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            } catch (Exception ex) {
                log.error(ex);
            }
        }

        return sslContext;
    }

    public static void print() {
        try {


            // Load the JDK's cacerts keystore file
            String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            FileInputStream is = new FileInputStream(filename);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            String password = "changeit";
            keystore.load(is, password.toCharArray());

            // This class retrieves the most-trusted CAs from the keystore
            PKIXParameters params = new PKIXParameters(keystore);

            // Get the set of trust anchors, which contain the most-trusted CA certificates
            Iterator it = params.getTrustAnchors().iterator();
            while (it.hasNext()) {
                TrustAnchor ta = (TrustAnchor) it.next();
                // Get certificate
                X509Certificate cert = ta.getTrustedCert();

                Logger log = Logger.getLogger(SSLContextProvider.class);
                log.info(cert);
            }

        } catch (Exception ex) {
            log.error(ex);
        }
    }
}