package direct.get;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import direct.get.exceptions.ApplicationAlreadyInitializedException;

/***
 * 
 * 
 * @author nawaman
 */
public class Scope {
	
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_ALL = ref->true;

	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_NONE = ref->false;

	private static final String APP_SCOPE_NAME = "AppScope";
	
	private static final Configuration DEF_CONFIG = new Configuration();

	private static final Object lock = new Object();

	/**
	 * The name of the scope.
	 * 
	 * This value is for the benefit of human who look at it.
	 * There is no use in the program in anyway (except debugging/logging/auditing purposes).
	 **/
	private final String name;
	
	private final Scope parentScope;
	
	private volatile Configuration config;
	
	private final ThreadLocal<Get.Instance> threadGet;
	
	private volatile List<StackTraceElement> stackTraceAtCreation;
	
	// For AppScope only.
	Scope() {
		this.name        = APP_SCOPE_NAME;
		this.parentScope = null;
		this.config      = DEF_CONFIG;
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(null, this));
	}
	
	// For other scope.
	Scope(String name, Scope parentScope, Configuration config) {
		this.name        = Optional.ofNullable(name).orElse("Scope:" + this.getClass().getName());
		this.parentScope = parentScope;
		this.config      = Optional.ofNullable(config).orElseGet(Configuration::new);
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(null, this));
	}
	
	// -- For AppScope only ---------------------------------------------------
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
					stackTraceAtCreation = Collections.unmodifiableList(Arrays.asList(new Throwable().getStackTrace()));
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
		return stackTraceAtCreation;
	}
	
	// -- For both types of Scope ------------------------------------------
	
	public String getName() {
		return name;
	}
	
	public Scope getParentScope() {
		return this.parentScope;
	}

	protected final Configuration getConfiguration() {
		ensureInitialized();
		return config;
	}

	private <T> Supplier<Providing<T>> providingFromParentScope(Ref<T> ref) {
		return ()->Optional.ofNullable(parentScope).map(parent->parent.getProviding(ref)).orElse(null);
	}

	private <T> Supplier<Providing<T>> providingFromConfiguration(Ref<T> ref) {
		return ()->this.config._get(ref).orElse(null);
	}
	
	protected final <T> Providing<T> getProviding(Ref<T> ref) {
		if (ref == null) {
			return null;
		}		

		Providing<T> providing
				= Preferability.determineScopeProviding(
					providingFromParentScope(ref),
					providingFromConfiguration(ref));
		return providing;
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
	
	// TODO - Make it array friendly.

	@SuppressWarnings("rawtypes")
	public <T> Scope substitute(List<Providing> providings, Runnable runnable) {
		get().substitute(providings, runnable);
		return this;
	}
	
	public Scope newSubScope(Configuration config) {
		return new Scope(null, this, config);
	}
	
	public Scope newSubScope(String name, Configuration config) {
		return new Scope(name, this, config);
	}
	
	@SuppressWarnings("rawtypes")
	public Thread newSubThread(Runnable runnable) {
		return newSubThread((Predicate<Ref>)null, runnable);
	}
	
	@SuppressWarnings("rawtypes")
	public Thread newSubThread(List<Ref> refsToInherit, Runnable runnable) {
		return newSubThread(ref->refsToInherit.contains(ref), runnable);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Thread newSubThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		Get.Instance thisGet = get();
		Get.Instance newGet  = new Get.Instance(thisGet, this);
		
		Predicate<Ref> predicate = Optional.ofNullable(refsToInherit).orElse(ref->false);
		
		List<Providing> providings
			= (List<Providing>)thisGet.getStackRefs()
			.filter(predicate)
			.map(thisGet::getProviding)
			.collect(Collectors.toList());
		
		return new Thread(()->{
			threadGet.set(newGet);
			newGet.substitute(providings, runnable);
		});
	}
	
	public void runSubThread(Runnable runnable) {
		newSubThread(runnable).start();
	}
	
	public void runSubThread(@SuppressWarnings("rawtypes") List<Ref> refsToInherit, Runnable runnable) {
		newSubThread(refsToInherit, runnable).start();
	}
	
	public void runSubThread(@SuppressWarnings("rawtypes") Predicate<Ref> refsToInherit, Runnable runnable) {
		newSubThread(refsToInherit, runnable).start();
	}
	
	public String toString() {
		return name;
	}
	
}
