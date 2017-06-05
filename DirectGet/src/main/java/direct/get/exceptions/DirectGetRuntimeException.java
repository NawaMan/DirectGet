package direct.get.exceptions;

/**
 * General unchecked exception for DirectGet.
 * 
 * @author nawaman
 */
public abstract class DirectGetRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 202231858308724170L;

	/** Constructor */
	public DirectGetRuntimeException() {
        super();
    }
	
	/** Constructor */
    public DirectGetRuntimeException(String message) {
        super(message);
    }
    
	/** Constructor */
    public DirectGetRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
	/** Constructor */
    public DirectGetRuntimeException(Throwable cause) {
        super(cause);
    }

}
