package direct.get.exceptions;

/**
 * This exception is thrown when there is an attempt to initialize the Application
 *   Scope after it was aready been initialized.
 * 
 * @author nawaman
 */
public class AppScopeAlreadyInitializedException extends DirectGetException {

	private static final long serialVersionUID = 5582284564207362445L;

	/**
	 * Constructor.
	 */
	public AppScopeAlreadyInitializedException() {
        super();
    }
	
}
