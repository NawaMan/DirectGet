package direct.get;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import direct.get.exceptions.ApplicationAlreadyInitializedException;

/***
 * 
 * 
 * @author nawaman
 */
public class RefSpace {

	private static final String APP_SPACE_NAME = "AppSpace";
	
	private static final Configuration DEF_CONFIG = new Configuration();

	private static final Object lock = new Object();
	
	private final String name;
	
	private final RefSpace parentSpace;
	
	private volatile Configuration config;
	
	private final ThreadLocal<Get.Instance> threadGet;
	
	private volatile List<StackTraceElement> stackTrace;
	
	// For AppSpace only.
	RefSpace() {
		this.name        = APP_SPACE_NAME;
		this.parentSpace = null;
		this.config      = DEF_CONFIG;
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(null, this));
	}
	
	// For other space.
	RefSpace(String name, RefSpace parentSpace, Configuration config) {
		this.name        = Optional.ofNullable(name).orElse("Space:" + this.getClass().getName());
		this.parentSpace = parentSpace;
		this.config      = Optional.ofNullable(config).orElseGet(Configuration::new);
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(null, this));
	}
	
	// -- For AppSpace only ---------------------------------------------------
	void init(Configuration newConfig) throws ApplicationAlreadyInitializedException {
		if (config == DEF_CONFIG) {
			initIfAbsent(newConfig);
			return;
		}
		throw new ApplicationAlreadyInitializedException();
	}
	
	void ensureInitialized() {
		initIfAbsent(null);
	}
	
	boolean initIfAbsent(Configuration newConfig) {
		if (config == DEF_CONFIG) {
			synchronized (lock) {
				if (config == DEF_CONFIG) {
					config     = (newConfig == null) ? new Configuration() : newConfig;
					stackTrace = Collections.unmodifiableList(Arrays.asList(new Throwable().getStackTrace()));
					return true;
				}
			}
		}
		return false;
	}
	
	boolean hasBeenInitialized() {
		return config != null;
	}
	
	public final List<StackTraceElement> getInitialzedStackTrace() {
		ensureInitialized();
		return stackTrace;
	}
	
	// -- For both types of RefSpace ------------------------------------------
	
	public String getName() {
		return name;
	}
	
	public RefSpace getParentSpace() {
		return this.parentSpace;
	}

	protected final Configuration getConfiguration() {
		ensureInitialized();
		return config;
	}
	
	protected final <T> Providing<T> getProviding(Ref<T> ref) {
		Optional<Providing<T>> parentProviding = Optional.ofNullable(parentSpace).map(parent->parent.getProviding(ref));
		if (parentProviding.filter(ProvidingLevel.Dictate::is).isPresent()) {
			return parentProviding.get();
		}
		Optional<Providing<T>> thisProviding = this.config._get(ref);
		if (thisProviding.filter(ProvidingLevel.Dictate::is).isPresent()) {
			return thisProviding.get();
		}
		
		// A this point, no dictate;

		if (thisProviding.filter(ProvidingLevel.Normal::is).isPresent()) {
			return thisProviding.get();
		}
		if (parentProviding.filter(ProvidingLevel.Normal::is).isPresent()) {
			return parentProviding.get();
		}
		
		// A this point, no normal;

		if (thisProviding.filter(ProvidingLevel.Default::is).isPresent()) {
			return thisProviding.get();
		}
		if (parentProviding.filter(ProvidingLevel.Default::is).isPresent()) {
			return parentProviding.get();
		}
		
		return null;
	}
	
	Get.Instance get() {
		return threadGet.get();
	}
	
	<T> Optional<T> doGet(Ref<T> ref) {
		Providing<T> providing = get().getProviding(ref);
		if (providing != null) {
			return Optional.ofNullable(providing.get());
		}
		
		return ref._get();
	}

	public <T> RefSpace substitute(Providing<T> providing, Runnable runnable) {
		get().substitute(providing, runnable);
		return this;
	}
	
	public <T> WithSubstitution withSubstitution(Providing<T> providing) {
		return get().withSubstitution(providing);
	}
	
	public RefSpace newSubSpace(Configuration config) {
		return new RefSpace(null, this, config);
	}
	
	public RefSpace newSubSpace(String name, Configuration config) {
		return new RefSpace(name, this, config);
	}
	
	public Thread newSubThread(Runnable runnable) {
		Get.Instance thisGet = get();
		Get.Instance newGet  = new Get.Instance(thisGet, this);
		return new Thread(()->{
			threadGet.set(newGet);
			runnable.run();
		});
	}
	
	public void runSubThread(Runnable runnable) {
		newSubThread(runnable).start();
	}
	
	public String toString() {
		return name;
	}
	
}
