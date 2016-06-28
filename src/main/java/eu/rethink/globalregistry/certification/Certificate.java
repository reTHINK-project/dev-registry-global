package main.java.eu.rethink.globalregistry.certification;

public class Certificate {

    private String publicKey;
    private String date;
    private String id;
    private boolean valid;

    public Certificate(String publicKey, String date, String id, boolean valid) {
        this.publicKey = publicKey;
        this.date = date;
        this.id = id;
        this.valid = valid;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public boolean isValid() {
        return valid;
    }
}
