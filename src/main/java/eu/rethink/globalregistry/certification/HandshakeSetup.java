package eu.rethink.globalregistry.certification;

import com.google.gson.Gson;
import eu.rethink.globalregistry.certification.exception.InvalidCertificateException;
import eu.rethink.globalregistry.certification.exception.X509CertificateReadException;
import eu.rethink.globalregistry.certification.model.Message;
import net.tomp2p.dht.FutureSend;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.p2p.RequestP2PConfiguration;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;
import net.tomp2p.rpc.ObjectDataReply;

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
        this.certificateValidation = new X509Validation();
    }

    public void init() {

        //Setup reply handler
        peer.peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                Message message = GSON.fromJson((String)request, Message.class);

                if(message.getType().equals(ASK_CERTIFICATE)) {
                    System.out.println("[CERTIFICATE][MESSAGE-TYPE] Received ASK_CERTIFICATE");
                    return "OK";
                }else if(message.getType().equals(SEND_CERTIFICATE)) {
                    System.out.println("[CERTIFICATE][MESSAGE-TYPE] Received SEND_CERTIFICATE");
                    return "OK";
                }

                return "NOT_OK";
            }
        });

        // Challenge new added peers
        peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            @Override
            public void peerInserted(PeerAddress peerAddress, boolean verified) {
                System.out.println("[NEW PEER] Address: " + peerAddress.toString());
                askCertificate(peerAddress);
            }

            @Override
            public void peerRemoved(PeerAddress peerAddress, PeerStatistic storedPeerAddress) {
                System.out.println("[PEER REMOVED] " + "Invalid challenge response from " + peerAddress.peerId());
            }

            @Override
            public void peerUpdated(PeerAddress peerAddress, PeerStatistic storedPeerAddress) {
                System.out.println("[PEER UPDATED] Address: " + peerAddress.toString());
            }
        });
    }


    public void sendMessage(Number160 peerId, String data) {
        peer.send(peerId).object(data)
                .requestP2PConfiguration(new RequestP2PConfiguration(1, 10, 0)).start()
                .addListener(new BaseFutureListener<FutureSend>() {
                    @Override
                    public void operationComplete(FutureSend future) throws Exception {
                        Object[] values = future.rawDirectData2().values().toArray();

                        if(values.length == 1) {
                            System.out.println("[CERTIFICATE][RESPONSE] " + values[0]);
                        }

                        System.out.println("RECEIVED REPLY.");
                    }

                    @Override
                    public void exceptionCaught(Throwable t) throws Exception {

                    }
                });

    }

    public void askCertificate(PeerAddress peerAddress) {

        Message message = new Message();

        message.setNode(peerAddress.peerId().toString());
        message.setType(ASK_CERTIFICATE);

        sendMessage(peerAddress.peerId(), GSON.toJson(message));
    }

    public void sendCertificate(PeerAddress peerAddress) throws InvalidCertificateException {

        Message message = new Message();

        message.setNode(peerAddress.peerId().toString());
        message.setType(SEND_CERTIFICATE);

        try {
            X509Certificate certificate = x509Reader.readFromFile(peer.peerID().toString());
            message.setData(certificate.getEncoded());

            sendMessage(peerAddress.peerId(), GSON.toJson(message));
        } catch (CertificateEncodingException e) {
            throw new InvalidCertificateException("Error encoding certificate in message.");
        } catch (X509CertificateReadException e) {
            throw new InvalidCertificateException("Invalid certificated.");
        }

    }

    public void verifyCertificate(PeerAddress peerAddress, Message message) {

        certificateValidation.validate(peerAddress, message);
    }
}
