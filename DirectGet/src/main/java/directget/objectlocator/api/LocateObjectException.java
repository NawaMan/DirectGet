package directget.objectlocator.api;

public class LocateObjectException extends RuntimeException {
    
    private static final long serialVersionUID = -1503175854525324555L;
    
    private final Class<?> clazz;
    
    /**
     * Constructor 
     * 
     * @param clazz  the class that this fail creation is attempted too.
     **/
    public LocateObjectException(Class<?> clazz) {
        this(clazz, null);
    }
    
    /**
     * Constructor 
     * 
     * @param clazz 
     * @param cause
     **/
    public LocateObjectException(Class<?> clazz, Throwable cause) {
        super(clazz.getCanonicalName(), cause);
        this.clazz = clazz;
    }
    
    /** @return the target class with the problem. */
    public Class<?> getTargetClass() {
        return clazz;
    }
    
}
