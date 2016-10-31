package eu.rethink.globalregistry.certification;

import net.tomp2p.peers.Number160;

import java.security.cert.X509Certificate;

public class PeerCertificate {

    private Number160 peerId;
    private X509Certificate certificate;
    private boolean valid;

    public PeerCertificate(Number160 peerId, X509Certificate certificate, boolean valid) {
        this.peerId = peerId;
        this.certificate = certificate;
        this.valid = valid;
    }

    public Number160 getPeerId() {
        return peerId;
    }

    public void setPeerId(Number160 peerId) {
        this.peerId = peerId;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
