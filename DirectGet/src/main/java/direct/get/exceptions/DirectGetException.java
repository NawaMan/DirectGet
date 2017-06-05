package direct.get.exceptions;

/**
 * General checked exception for DirectGet.
 * 
 * @author nawaman
 */
public abstract class DirectGetException extends Exception {

	private static final long serialVersionUID = -6611252364944586803L;
	
	/** Constructor */
	protected DirectGetException() {
        super();
    }
	
	/** Constructor */
	protected DirectGetException(String message) {
        super(message);
    }
	
	/** Constructor */
	protected DirectGetException(String message, Throwable cause) {
        super(message, cause);
    }
	
	/** Constructor */
	protected DirectGetException(Throwable cause) {
        super(cause);
    }

}
