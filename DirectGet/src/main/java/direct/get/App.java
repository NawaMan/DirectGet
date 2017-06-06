package direct.get;

import direct.get.exceptions.AppScopeAlreadyInitializedException;

/**
 * This is the application scope.
 * 
 * IMPORTANT NOTE: This class is complicated to test.
 *   So you should have a really really good reason to change it!!!
 * 
 * @author nawaman
 */
public final class App {
	
	/** The instance of the Application scope. */
	public static final Scope instance = new Scope();

	/** @return the get for the current thread that is associated with this scope. NOTE: capital 'G' is intentional. */
	public static Get.Instance Get() {
		return instance.Get();
	}
	
	/**
	 * Initialize the application scope. This method can only be run once.
	 **/
	public static Scope initialize(Configuration config) throws AppScopeAlreadyInitializedException {
		instance.init(config);
		return instance;
	}
	
	/**
	 * Initialize the application scope if it has yet to be initialized.
	 * 
	 * @return {@code true} if the initialization actually happen with this call.
	 */
	public static boolean initializeIfAbsent(Configuration configuration) {
		return instance.initIfAbsent(configuration);
	}
	
	/** @return {@code true} if the application scope has been initialized */
	public static boolean isInitialized() {
		return instance.hasBeenInitialized();
	}
	
	/** Private part */
	private App() {
		super();
	}
	
}
