package direct.get.exceptions;

import direct.get.Ref;

/**
 * This exception holds the exceptional result for Get.
 * 
 * @author nawaman
 */
public class GetException extends DirectGetRuntimeException {

	private static final long serialVersionUID = -5821727183532729001L;
	
	private final Ref<?> ref;
	
	/** Constructor */
    public GetException(Ref<?> ref, Throwable cause) {
        super(ref.toString(), cause);
        this.ref = ref;
    }
    
    /** @return the reference with the problem. */
    public Ref<?> getRef() {
    	return ref;
    }
    
}
