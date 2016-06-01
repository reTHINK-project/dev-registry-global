package main.java.eu.rethink.globalregistry.certification;

import net.tomp2p.peers.MapAcceptHandler;
import net.tomp2p.peers.PeerAddress;

public class CertificateMapAcceptHandler implements MapAcceptHandler{

    @Override
    public boolean acceptPeer(boolean b, PeerAddress peerAddress) {
        return false;
    }
}
