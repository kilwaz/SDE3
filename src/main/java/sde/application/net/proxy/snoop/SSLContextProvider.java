package sde.application.net.proxy.snoop;

import org.apache.log4j.Logger;
import sde.application.error.Error;
import sde.application.utils.SDEUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.Iterator;

// Commands to keep - creates the keystore file - keystore certs expire eventually!
// Keytool can be found in java folder under /bin
// certutil -addstore Root cert.cer
// keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass secret -validity 2000 -keysize 2048 -dname "CN=*.uk.spl.com"

public class SSLContextProvider {
    private static SSLContext sslContext = null;
    private static Logger log = Logger.getLogger(SSLContextProvider.class);

    private static String KEY_STORE_PASSWORD = "secret";
    private static String KEY_STORE_FILE_NAME = "keystore.jks";

    public static synchronized SSLContext get() {
        if (sslContext == null) {
            FileInputStream fis = null;
            try {
                String keyStoreFileName = "";
                if (SDEUtils.isJar()) {
                    try {
                        URI uri = SDEUtils.getFile(SDEUtils.getJarURI(), KEY_STORE_FILE_NAME);
                        keyStoreFileName = uri.getPath();
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                } else {
                    keyStoreFileName = SDEUtils.getResourcePath() + "/" + KEY_STORE_FILE_NAME;
                }

                log.info("File path " + keyStoreFileName);
                File keyStore = new File(keyStoreFileName);
                if (!keyStore.exists()) {
                    Boolean fileCreateResult = keyStore.createNewFile();
                }

                sslContext = SSLContext.getInstance("TLS");
                KeyStore ks = KeyStore.getInstance("JKS");
                fis = new FileInputStream(keyStoreFileName);
                ks.load(fis, KEY_STORE_PASSWORD.toCharArray());

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);

                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(ks, KEY_STORE_PASSWORD.toCharArray());
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