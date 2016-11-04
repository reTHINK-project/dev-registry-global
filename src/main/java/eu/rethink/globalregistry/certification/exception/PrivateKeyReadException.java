package eu.rethink.globalregistry.certification.exception;

public class PrivateKeyReadException extends Exception{

    private static final long serialVersionUID = 1L;

    public PrivateKeyReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PrivateKeyReadException(String message) {
        super(message);
    }
}
