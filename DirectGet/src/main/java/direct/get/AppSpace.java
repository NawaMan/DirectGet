package direct.get;

import direct.get.exceptions.ApplicationAlreadyInitializedException;

/**
 * This is the application context.
 * 
 * IMPORTANT NOTE: This class is complicated to test.
 *   So you should have a really really good reason to change it!!!
 * 
 * @author nawaman
 */
public final class AppSpace extends RefSpace {
	
	public static final AppSpace current = new AppSpace();
	
	public static final GetInstance Get = current.get();
	
	public static final GetInstance get = Get;
	
	public static AppSpace initialize(Configuration config) throws ApplicationAlreadyInitializedException {
		current.init(config);
		return current;
	}
	
	public static boolean initializeIfAbsent(Configuration config) {
		return current.initIfAbsent(config);
	}
	
	public static boolean isInitialized() {
		return current.hasBeenInitialized();
	}
	
	private AppSpace() {
		super();
	}
	
}
