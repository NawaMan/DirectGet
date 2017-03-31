package direct.get.exceptions;

public class ProvideWrongTypeException extends GetException {

	private static final long serialVersionUID = -6784826475048749418L;
	
	public ProvideWrongTypeException() {
        super();
    }

    public ProvideWrongTypeException(String message) {
        super(message);
    }

    public ProvideWrongTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProvideWrongTypeException(Throwable cause) {
        super(cause);
    }

}
