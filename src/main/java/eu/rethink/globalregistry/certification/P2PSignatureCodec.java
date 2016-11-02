package eu.rethink.globalregistry.certification;

import io.netty.buffer.ByteBuf;
import net.tomp2p.message.SignatureCodec;

import java.io.IOException;
import java.util.Arrays;

public class P2PSignatureCodec implements SignatureCodec{

    public static final int SIGNATURE_SIZE = 256;
    private final byte[] encodedData;

    public P2PSignatureCodec(byte[] encodedData) throws IOException{
        if(encodedData.length != this.signatureSize()) {
            throw new IOException("RSA signature has size " + this.signatureSize() + " received: " + encodedData.length);
        } else {
            this.encodedData = encodedData;
        }
    }

    public P2PSignatureCodec(ByteBuf buf) {
        this.encodedData = new byte[this.signatureSize()];
        buf.readBytes(this.encodedData);
    }


    @Override
    public byte[] encode() {
        return this.encodedData;
    }

    @Override
    public SignatureCodec write(ByteBuf byteBuf) {
        byteBuf.writeBytes(this.encodedData);
        return this;
    }

    @Override
    public int signatureSize() {
        return SIGNATURE_SIZE;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof P2PSignatureCodec)) {
            return false;
        } else if(obj == this) {
            return true;
        } else {
            P2PSignatureCodec s = (P2PSignatureCodec)obj;
            return Arrays.equals(s.encodedData, this.encodedData);
        }
    }
}
