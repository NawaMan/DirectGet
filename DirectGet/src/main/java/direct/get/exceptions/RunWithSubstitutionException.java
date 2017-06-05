package direct.get.exceptions;

/**
 * This exception wrap the exception throw while trying to run with a substitution.
 * 
 * @author nawaman
 */
public class RunWithSubstitutionException extends DirectGetRuntimeException {

	private static final long serialVersionUID = -6016449881081091295L;

    /** Default */
    public RunWithSubstitutionException(Throwable cause) {
        super(cause);
    }
    
}
