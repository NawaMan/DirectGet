package direct.get;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import direct.get.exceptions.AppScopeAlreadyInitializedException;

/***
 * Scope holds a configuration which specify providings.
 * 
 * @author nawaman
 */
public class Scope {
	
	/** This predicate specifies that all of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_ALL = ref->true;
	
	/** This predicate specifies that none of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_NONE = ref->false;

	private static final String APP_SCOPE_NAME = "AppScope";
	
	private static final Configuration DEF_CONFIG = new Configuration();

	private static final Object lock = new Object();
	
	/** The reference to the thread factory. */
	public static final Ref<ThreadFactory> _ThreadFactory_ = Ref.of(ThreadFactory.class, Executors.defaultThreadFactory());
	/** The reference to the executor. */
	public static final Ref<Executor>      _Executor_      = Ref.of(Executor.class,      Executors.newCachedThreadPool());
	

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
	void init(Configuration newConfig) throws AppScopeAlreadyInitializedException {
		if (config == DEF_CONFIG) {
			initIfAbsent(newConfig);
			return;
		}
		throw new AppScopeAlreadyInitializedException();
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
	
	/** @return the stacktrace when this scope is initialized. */
	public final Stream<StackTraceElement> getInitialzedStackTrace() {
		ensureInitialized();
		return stackTraceAtCreation.stream();
	}
	
	// -- For both types of Scope ------------------------------------------
	
	/** @return the name of the scope. */
	public String getName() {
		return name;
	}

	/** @return the name of the scope. */
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
		return ()->this.config.getProviding(ref);
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
	
	/** @return the get for the current thread that is associated with this scope. */
	public Get.Instance get() {
		return threadGet.get();
	}
	
	<T> Optional<T> doGet(Ref<T> ref) {
		Providing<T> providing = get().getProviding(ref);
		if (providing != null) {
			return Optional.ofNullable(providing.get());
		}
		
		return ref._get();
	}
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name;
	}
	
	/**
	 * Create and return a new sub scope with the given configuration.
	 */
	public Scope newSubScope(Configuration config) {
		return new Scope(null, this, config);
	}
	
	/**
	 * Create and return a new sub scope with the given name and configuration.
	 */
	public Scope newSubScope(String name, Configuration config) {
		return new Scope(name, this, config);
	}
	
	/**
	 * Create a sub thread with a get that inherits all substitution from the current Get
	 *   and run the runnable with it.
	 **/
	public Thread newThread(Runnable runnable) {
		return newThread(INHERIT_ALL, runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the given substitution from the current
	 *   Get and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
		return newThread(ref->refsToInherit.contains(ref), runnable);
	}

	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		Get.Instance    newGet     = prepareNewGet();
		List<Providing> providings = prepareProvidings(refsToInherit);
		
		ThreadFactory newThread = get().a(_ThreadFactory_);
		return newThread.newThread(()->{
			threadGet.set(newGet);
			newGet.substitute(providings, runnable);
		});
	}
	
	/**
	 * Create and run a sub thread with a get that inherits all substitution from the current
	 *   Get and run the runnable with it.
	 **/
	public void runNewThread(Runnable runnable) {
		newThread(INHERIT_ALL, runnable).start();
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the providings of those given refs.
	 **/
	@SuppressWarnings("rawtypes") 
	public void runNewThread(List<Ref> refsToInherit, Runnable runnable) {
		newThread(refsToInherit, runnable).start();
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test).
	 **/
	@SuppressWarnings("rawtypes") 
	public void runNewThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		newThread(refsToInherit, runnable).start();
	}
	
	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public <V, T extends Throwable> CompletableFuture<V> runThread(Predicate<Ref> refsToInherit, Computation<V, T> computation) {
		Get.Instance    newGet     = prepareNewGet();
		List<Providing> providings = prepareProvidings(refsToInherit);
		
		
		Executor executor = get().a(_Executor_);
		return CompletableFuture.supplyAsync(()->{
			threadGet.set(newGet);
			try {
				return newGet.substitute(providings, computation);
			} catch (Throwable t) {
				throw new CompletionException(t);
			}
		}, executor);
	}
	
	private Get.Instance prepareNewGet() {
		Get.Instance thisGet = get();
		Get.Instance newGet  = new Get.Instance(thisGet, this);
		return newGet;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Providing> prepareProvidings(Predicate<Ref> refsToInherit) {
		Get.Instance thisGet = get();
		Predicate<Ref> predicate = Optional.ofNullable(refsToInherit).orElse(ref->false);
		
		List<Providing> providings
			= (List<Providing>)thisGet.getStackRefs()
			.filter(predicate)
			.map(thisGet::getProviding)
			.collect(Collectors.toList());
		return providings;
	}
	
}
