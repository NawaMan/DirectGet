package direct.get.exceptions;

/**
 * This exception holds the exceptional result for Get.
 * 
 * @author nawaman
 */
public class GetException extends RuntimeException {

	private static final long serialVersionUID = -5821727183532729001L;

	public GetException() {
        super();
    }

    public GetException(String message) {
        super(message);
    }

    public GetException(String message, Throwable cause) {
        super(message, cause);
    }

    public GetException(Throwable cause) {
        super(cause);
    }
    
}
