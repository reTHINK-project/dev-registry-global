package eu.rethink.globalregistry.certification.exception;

public class InvalidCertificateException extends Exception{

    private static final long serialVersionUID = 1L;

    public InvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCertificateException(String message) {
        super(message);
    }
}
