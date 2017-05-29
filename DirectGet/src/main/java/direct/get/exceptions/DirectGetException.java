package direct.get.exceptions;

/**
 * 
 * @author nawaman
 */
public class DirectGetException extends Exception {

	private static final long serialVersionUID = -3600956526096753400L;

	public DirectGetException() {
        super();
    }

    public DirectGetException(String message) {
        super(message);
    }

    public DirectGetException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectGetException(Throwable cause) {
        super(cause);
    }

}
