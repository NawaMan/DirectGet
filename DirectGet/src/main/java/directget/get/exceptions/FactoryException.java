package directget.get.exceptions;

/**
 * This exception is thrown when a factory is unable to make something.
 * 
 * @author NawaMan
 */
public class FactoryException extends DirectGetRuntimeException {

    private static final long serialVersionUID = -1444549293673722463L;

    /** Constructor */
    public FactoryException() {
        super();
    }
    
    /** Constructor */
    public FactoryException(String message) {
        super(message);
    }
    
    /** Constructor */
    public FactoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** Constructor */
    public FactoryException(Throwable cause) {
        super(cause);
    }
    
}
