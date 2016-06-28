package main.java.eu.rethink.globalregistry.exception;

public class CertificateNotFound extends Exception {
    private static final long serialVersionUID = 1L;

    public CertificateNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public CertificateNotFound(String message) {
        super(message);
    }
}