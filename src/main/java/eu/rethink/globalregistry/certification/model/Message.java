package eu.rethink.globalregistry.certification.model;


import org.apache.commons.codec.binary.Base64;

public class Message {

    private String node;
    private String signature;
    private String destination;
    private String data;
    private String type;

    public Message() {}

    public byte[] getData() {
        return Base64.decodeBase64(data);
    }

    public void setData(byte[] data) {
        this.data = Base64.encodeBase64String(data);
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

}
