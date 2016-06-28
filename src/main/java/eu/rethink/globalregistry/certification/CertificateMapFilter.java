package main.java.eu.rethink.globalregistry.certification;

import main.java.eu.rethink.globalregistry.exception.CertificateNotFound;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMap;
import net.tomp2p.peers.PeerMapFilter;

import java.util.Collection;

public class CertificateMapFilter implements PeerMapFilter{

    private PeerCertification peerCertification;

    public CertificateMapFilter() {
        this.peerCertification = new PeerCertification();
    }

    @Override
    public boolean rejectPeerMap(PeerAddress peerAddress, PeerMap peerMap) {
        return rejectPreRouting(peerAddress, peerMap.all());
    }

    @Override
    public boolean rejectPreRouting(PeerAddress peerAddress, Collection<PeerAddress> collection) {
        try {
            return peerCertification.verifyCertificate(peerAddress);
        } catch (CertificateNotFound certificateNotFound) {
            return false;
        }
    }
}
