package eu.rethink.globalregistry.certification;

import io.netty.buffer.ByteBuf;
import net.tomp2p.connection.SignatureFactory;
import net.tomp2p.message.RSASignatureCodec;
import net.tomp2p.message.SignatureCodec;
import net.tomp2p.peers.PeerAddress;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import org.slf4j.Logger;

public class P2PSignatureFactory implements SignatureFactory{

    private CertificateManager certManager;
    private static final Logger LOG = LoggerFactory.getLogger(P2PSignatureFactory.class);

    public P2PSignatureFactory(CertificateManager certmanager) {
        this.certManager = certmanager;
    }

    @Override
    public PublicKey decodePublicKey(byte[] bytes, PeerAddress peerAddress) {


        if(certManager.exists(peerAddress.peerId())) {
            PeerCertificate cert = this.certManager.get(peerAddress.peerId());

            if(cert.isValid()) {
                return cert.getCertificate().getPublicKey();
            }
        }

        return null;
    }

    @Override
    public PublicKey decodePublicKey(ByteBuf byteBuf, PeerAddress peerAddress) {

        return this.decodePublicKey(new byte[0], peerAddress);
    }

    @Override
    public void encodePublicKey(PublicKey publicKey, ByteBuf byteBuf) {
        byte[] data = publicKey.getEncoded();
        byteBuf.writeShort(data.length);
        byteBuf.writeBytes(data);
    }

    @Override
    public SignatureCodec sign(PrivateKey privateKey, ByteBuffer[] byteBuffers) throws InvalidKeyException, SignatureException, IOException {
        Signature signature = this.signatureInstance();
        signature.initSign(privateKey);
        int len = byteBuffers.length;

        for(int signatureData = 0; signatureData < len; ++signatureData) {
            ByteBuffer buffer = byteBuffers[signatureData];
            signature.update(buffer);
        }

        byte[] signatureData = signature.sign();
        return new RSASignatureCodec(signatureData);
    }

    @Override
    public boolean verify(PublicKey publicKey, ByteBuffer[] byteBuffers, SignatureCodec signatureCodec, PeerAddress peerAddress) throws SignatureException, InvalidKeyException {

        Signature signature = this.signatureInstance();
        PublicKey pub = decodePublicKey(new byte[0], peerAddress);

        signature.initVerify(pub);
        int len = byteBuffers.length;

        for(int i = 0; i < len; i++) {
            ByteBuffer buffer = byteBuffers[i];
            signature.update(buffer);
        }

        byte[] signatureReceived = signatureCodec.encode();

        return signature.verify(signatureReceived);

    }

    @Override
    public Signature update(PublicKey publicKey, ByteBuffer[] byteBuffers) throws InvalidKeyException, SignatureException {
        Signature signature = signatureInstance();
        signature.initVerify(publicKey);

        int arrayLength = byteBuffers.length;

        for(int i = 0; i < arrayLength; i++) {
            signature.update(byteBuffers[i]);
        }

        return signature;
    }

    @Override
    public SignatureCodec signatureCodec(ByteBuf byteBuf) {
        return new P2PSignatureCodec(byteBuf);
    }

    @Override
    public int signatureSize() {
        return 256;
    }

    private Signature signatureInstance() {
        try {
            return Signature.getInstance("SHA256withRSA");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("could not find algorithm", e);
            return null;
        }
    }
}
