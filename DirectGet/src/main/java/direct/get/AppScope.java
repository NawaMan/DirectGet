package direct.get;

import direct.get.exceptions.ApplicationAlreadyInitializedException;

/**
 * This is the application scope.
 * 
 * IMPORTANT NOTE: This class is complicated to test.
 *   So you should have a really really good reason to change it!!!
 * 
 * @author nawaman
 */
public final class AppScope extends Scope {
	
	public static final AppScope current = new AppScope();
	
	public static final Get.Instance Get = current.get();
	
	public static final Get.Instance get = Get;
	
	public static AppScope initialize(Configuration config) throws ApplicationAlreadyInitializedException {
		current.init(config);
		return current;
	}
	
	public static boolean initializeIfAbsent(Configuration config) {
		return current.initIfAbsent(config);
	}
	
	public static boolean isInitialized() {
		return current.hasBeenInitialized();
	}
	
	private AppScope() {
		super();
	}
	
}
