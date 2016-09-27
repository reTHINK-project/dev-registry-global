package eu.rethink.globalregistry.certification;

import com.google.gson.Gson;
import eu.rethink.globalregistry.certification.exception.InvalidCertificateException;
import eu.rethink.globalregistry.certification.exception.X509CertificateReadException;
import eu.rethink.globalregistry.certification.model.Message;
import net.tomp2p.connection.PeerException;
import net.tomp2p.dht.FutureSend;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.message.Buffer;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.rpc.RawDataReply;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class HandshakeSetup {

    final static String ASK_CERTIFICATE = "ASK_CERTIFICATE";
    final static String SEND_CERTIFICATE = "SEND_CERTIFICATE";
    private final Gson GSON;
    private final X509Reader x509Reader;
    private final CertificateValidation certificateValidation;

    PeerDHT peer;

    public HandshakeSetup(PeerDHT peer) {
        this.peer = peer;
        this.GSON = new Gson();
        this.x509Reader = new X509Reader();
        this.certificateValidation = new X509Validation(x509Reader);
    }

    public void init() {

        //Setup reply handler
        peer.peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                System.out.println("[DIRECT-MESSAGE][REPLY HANDLER] RECEIVED MESSAGE from: " + sender.peerId());
                System.out.println("[DIRECT-MESSAGE][REPLY HANDLER] MESSAGE: " + (String)request);
                Message message = GSON.fromJson((String)request, Message.class);

                if(message.getType().equals(ASK_CERTIFICATE)) {
                    System.out.println("[CERTIFICATE][MESSAGE-TYPE] Received ASK_CERTIFICATE");
                    return sendCertificate(sender, peer.peerAddress());
                }else if(message.getType().equals(SEND_CERTIFICATE)) {
                    System.out.println("[CERTIFICATE][MESSAGE-TYPE] Received SEND_CERTIFICATE");
                    return verifyCertificate(sender, message);
                }

                return "NOT_OK";
            }
        });

        // Challenge new added peers
        peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            @Override
            public void peerInserted(PeerAddress peerAddress, boolean verified) {
                if(peer.peerID().compareTo(peerAddress.peerId()) != 0) {
                    System.out.println("[NEW PEER] Address: " + peerAddress.toString());
                    askCertificate(peerAddress, peer.peerAddress());
                }
            }

            @Override
            public void peerRemoved(PeerAddress peerAddress, PeerStatistic storedPeerAddress) {
                System.out.println("[PEER REMOVED]" + peerAddress.peerId());
            }

            @Override
            public void peerUpdated(PeerAddress peerAddress, PeerStatistic storedPeerAddress) {
                System.out.println("[PEER UPDATED] Address: " + peerAddress.toString());
            }
        });
    }


    public void sendMessage(final PeerAddress peerAddress, String data) {
        FutureDirect futureDirect = peer.peer().sendDirect(peerAddress).object(data).start();

        futureDirect.addListener(new BaseFutureListener<FutureDirect>() {
            @Override
            public void operationComplete(FutureDirect future) throws Exception {
                System.out.println("[DIRECT-MESSAGE] MESSAGE SENT SUCCESSFULLY TO PEER " + peerAddress.peerId());

                if(future.isSuccess()) {
                    System.out.println("[DIRECT-MESSAGE][RESPONSE] " + future.object());

                    Message message = GSON.fromJson((String)future.object(), Message.class);

                    if(message.getType().equals(SEND_CERTIFICATE)) {
                        System.out.println("[CERTIFICATE][MESSAGE-TYPE] Received SEND_CERTIFICATE");
                        verifyCertificate(peerAddress, message);
                    }

                } else {
                    System.out.println("[DIRECT-MESSAGE][RESPONSE] FUTURE NOT SUCCEDED");
                }
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {

            }
        });

    }

    public void askCertificate(PeerAddress destination, PeerAddress source) {

        Message message = new Message();

        message.setDestination(destination.peerId().toString());
        message.setNode(source.peerId().toString());
        message.setType(ASK_CERTIFICATE);

        sendMessage(destination, GSON.toJson(message));
    }

    public String sendCertificate(PeerAddress destination, PeerAddress source) throws InvalidCertificateException {

        Message message = new Message();

        message.setNode(source.peerId().toString());
        message.setType(SEND_CERTIFICATE);
        message.setDestination(destination.peerId().toString());

        try {
            X509Certificate certificate = x509Reader.readFromFile(peer.peerID().toString());
            message.setData(certificate.getEncoded());

            return GSON.toJson(message);
        } catch (CertificateEncodingException e) {
            System.out.println("[SEND-CERTIFICATE] Error encoding certificate in message.");
            throw new InvalidCertificateException("Error encoding certificate in message.");
        } catch (X509CertificateReadException e) {
            System.out.println("[SEND-CERTIFICATE] Invalid certificate: " + e.getMessage());
            throw new InvalidCertificateException("Invalid certificate: " + e.getMessage());
        }

    }

    public String verifyCertificate(PeerAddress peerAddress, Message message) {

        if(!certificateValidation.validate(peerAddress, message)) {
            PeerException cause = new PeerException(PeerException.AbortCause.USER_ABORT, "Invalid certificate presented.");
            peer.peerBean().peerMap().peerFailed(peerAddress, cause);
            System.out.println("[CERTIFICATE][PEER] Verification failed for peer " + message.getNode());
            return "NOT_OK";
        }

        System.out.println("[CERTIFICATE][PEER] Verification successful for peer " + message.getNode());

        return "OK";
    }
}
