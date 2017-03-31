package direct.get.exceptions;

public class ImmutableContextException extends GetException {

	private static final long serialVersionUID = -6784826475048749418L;
	
	public ImmutableContextException() {
        super();
    }

    public ImmutableContextException(String message) {
        super(message);
    }

    public ImmutableContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImmutableContextException(Throwable cause) {
        super(cause);
    }

}
