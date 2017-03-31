package direct.get.exceptions;

public class ProvidingException extends GetException {
	
	private static final long serialVersionUID = -5126058017744826352L;

	private final Class<?> clzz;
	
	private final String className;
	
	public ProvidingException(String message, Class<?> clzz, Throwable cause) {
        super((message != null ? message + ": " : "") + clzz.getCanonicalName(), cause);
        this.clzz = clzz;
        this.className = clzz.getCanonicalName();
    }

	public ProvidingException(Class<?> clzz) {
		this(null, clzz, null);
	}
	
	public ProvidingException(Class<?> clzz, Throwable cause) {
		this(null, clzz, cause);
    }

	public ProvidingException(String message, Class<?> clzz) {
		this(message, clzz, null);
	}
	
	public Class<?> getTargetClass() {
		// TODO - This might not be serializable so handle this properly.
		return this.clzz;
	}
	
	public String getTargetClassName() {
		return this.className;
	}
    
}
