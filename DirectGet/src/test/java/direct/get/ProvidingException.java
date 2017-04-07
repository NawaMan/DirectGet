package direct.get;

import direct.get.exceptions.GetException;

public class ProvidingException extends GetException {
	
	private static final long serialVersionUID = -5126058017744826352L;

	private final Ref<?> ref;
	
	private final String className;
	
	public ProvidingException(String message, Ref<?> ref, Throwable cause) {
        super((message != null ? message + ": " : "") + ref.getTargetClass().getCanonicalName(), cause);
        this.ref = ref;
        this.className = ref.getTargetClass().getCanonicalName();
    }

	public ProvidingException(Ref<?> ref) {
		this(null, ref, null);
	}
	
	public ProvidingException(Ref<?> ref, Throwable cause) {
		this(null, ref, cause);
    }

	public ProvidingException(String message, Ref<?> ref) {
		this(message, ref, null);
	}
	
	public Ref<?> getTargetRef() {
		// TODO - This might not be serializable so handle this properly.
		return this.ref;
	}
	
	public Class<?> getTargetClass() {
		// TODO - This might not be serializable so handle this properly.
		return this.ref.getTargetClass();
	}
	
	public String getTargetClassName() {
		return this.className;
	}
    
}
