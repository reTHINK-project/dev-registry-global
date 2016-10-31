package eu.rethink.globalregistry.certification;

import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMap;
import net.tomp2p.peers.PeerMapFilter;

import java.util.Collection;

public class CertificationPeerMapFilter implements PeerMapFilter {


    private CertificateManager certificateManager;

    public CertificationPeerMapFilter(CertificateManager certificateManager) {
        this.certificateManager = certificateManager;
    }

    @Override
    public boolean rejectPeerMap(PeerAddress peerAddress, PeerMap peerMap) {
        System.out.println("[PEER MAP FILTER][REJECT PEER MAP] Peer: " + peerAddress.peerId());

        return rejectPreRouting(peerAddress, peerMap.all());
    }

    @Override
    public boolean rejectPreRouting(PeerAddress peerAddress, Collection<PeerAddress> all) {


        if(certificateManager.exists(peerAddress.peerId())) {

            if(certificateManager.get(peerAddress.peerId()).isValid()) {
                System.out.println("[PEER MAP FILTER][REJECT PRE ROUTING] Peer: " + peerAddress.peerId() + " already validated.");
                return false;
            } else {
                System.out.println("[PEER MAP FILTER][REJECT PRE ROUTING] Peer: " + peerAddress.peerId() + " already declared as invalid peer.");
                return true;
            }
        } else {
            System.out.println("[PEER MAP FILTER][REJECT PRE ROUTING] Peer: " + peerAddress.peerId() + " first contact.");
            return false;
        }

    }
}
