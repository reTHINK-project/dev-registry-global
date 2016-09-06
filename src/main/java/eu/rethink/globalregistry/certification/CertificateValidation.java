package eu.rethink.globalregistry.certification;

import com.google.gson.JsonObject;
import eu.rethink.globalregistry.certification.model.Message;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.PeerAddress;

public interface CertificateValidation {

    public boolean validate(PeerAddress peer, Message data);
}
