package eu.rethink.globalregistry.certification;

import eu.rethink.globalregistry.certification.exception.InvalidCertificateException;
import eu.rethink.globalregistry.certification.exception.X509CertificateReadException;
import eu.rethink.globalregistry.certification.model.Message;
import net.tomp2p.peers.PeerAddress;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class X509Validation implements CertificateValidation{

    private final X509Reader certificateReader;
    private X509CertificateVerifier x509Verifier;
    private CertificateManager certificateManager;

    public X509Validation(X509Reader reader, CertificateManager certificateManager) {
        this.certificateReader = reader;
        this.x509Verifier = new X509CertificateVerifier();
        this.certificateManager = certificateManager;
    }

    @Override
    public boolean validate(PeerAddress peer, Message data) {

        X509Certificate certificate = null;

        try {
            certificate = certificateReader.readFromMessage(data);
            FileInputStream fis = null;

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            fis = new FileInputStream("rethink-ca");
            keystore.load(fis, System.getenv("KEYSTORE_PASS").toCharArray());
            fis.close();

            x509Verifier.verifyCertificate(certificate, keystore);

            certificateManager.put(peer.peerId(), new PeerCertificate(peer.peerId(), certificate, true));

            //TODO: check peerID against the one in the certificate

            return true;

        } catch (InvalidCertificateException e) {
            System.out.println("Invalid certificate.");
            certificateManager.put(peer.peerId(), new PeerCertificate(peer.peerId(), certificate, false));
            return false;
        } catch (X509CertificateReadException | CertificateException | NoSuchAlgorithmException e) {
            System.out.println("Invalid certificate.");
            return false;
        } catch (KeyStoreException | IOException e) {
            System.out.println("Error loading keystore");
            return false;
        }

    }
}
