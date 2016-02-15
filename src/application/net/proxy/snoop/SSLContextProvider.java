package application.net.proxy.snoop;

import application.error.Error;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Iterator;

public class SSLContextProvider {
    private static SSLContext sslContext = null;
    private static Logger log = Logger.getLogger(SSLContextProvider.class);

    public static synchronized SSLContext get() {
        if (sslContext == null) {
            FileInputStream fis = null;
            try {
                String keyStoreFileName = SDEUtils.getResourcePath() + "/keystore.jks";
                //log.info("File path " + keyStoreFileName);
                File keyStore = new File(keyStoreFileName);
                if (!keyStore.exists()) {
                    Boolean fileCreateResult = keyStore.createNewFile();
                }

                //CertificateFactory factory = CertificateFactory.getInstance("X.509");
                //X509Certificate cert = (X509Certificate) factory.generateCertificate(new FileInputStream(keyStoreFileName));

                sslContext = SSLContext.getInstance("TLS");
                KeyStore ks = KeyStore.getInstance("JKS");
                fis = new FileInputStream(keyStoreFileName);
                ks.load(fis, "secret".toCharArray());

//                ks.setCertificateEntry("SDE", cert);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, "secret".toCharArray());
                sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            } catch (Exception ex) {
                Error.SSL_CONTEXT.record().create(ex);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        Error.CLOSE_FILE_STREAM.record().create(ex);
                    }
                }
            }
        }

        return sslContext;
    }

    public static void print() {
        FileInputStream is = null;
        try {
            // Load the JDK's cacerts keystore file
            String filename = System.getProperty("java.home") + "/lib/security/cacerts".replace('/', File.separatorChar);
            is = new FileInputStream(filename);
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
            Error.SSL_CONTEXT.record().create(ex);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    Error.CLOSE_FILE_STREAM.record().create(ex);
                }
            }
        }
    }
}