package eu.rethink.globalregistry.certification;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class CertificateManager {

    //concurrency?
    private Map<Number160, PeerCertificate> certificates;

    public CertificateManager() {
        this.certificates = new HashMap<>();
    }

    public PeerCertificate get(Number160 peerId) {
        return certificates.get(peerId);
    }

    public void put(Number160 peerId, PeerCertificate certificate) {
        certificates.put(peerId, certificate);
    }

    public boolean exists(Number160 peerId) {
        return certificates.containsKey(peerId);
    }

}

