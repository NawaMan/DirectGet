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
public final class AppScope extends Scope {
	
	/** The instance of the Application scope. */
	public static final AppScope instance = new AppScope();

	/** The Get instance of this scope - static import friendly. */
	public static final Get.Instance Get = instance.get();

	/** The Get instance of this scope - non-static import friendly. */
	public static final Get.Instance get = Get;

	/** The Get instance of this scope - whatever float your boat. */
	public static final Get.Instance giveMe = Get;
	
	/**
	 * Initialize the application scope. This method can only be run once.
	 **/
	public static AppScope initialize(Configuration config) throws AppScopeAlreadyInitializedException {
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
	private AppScope() {
		super();
	}
	
}
