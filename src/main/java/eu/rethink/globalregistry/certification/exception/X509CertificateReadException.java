package eu.rethink.globalregistry.certification.exception;

public class X509CertificateReadException extends Exception{

    private static final long serialVersionUID = 1L;

    public X509CertificateReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public X509CertificateReadException(String message) {
        super(message);
    }
}
