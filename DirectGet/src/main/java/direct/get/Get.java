package direct.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;
import direct.get.exceptions.RunWithSubstitutionException;

import static direct.get.Named.*;

/**
 * This class provide access to the application scope.
 * 
 * @author nawaman
 */
public final class Get {
	
	/** This predicate specifies that all of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_ALL = Predicate("InheritAll", ref->true);
	
	/** This predicate specifies that none of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_NONE = Predicate("InheritNone", ref->false);
	
	private static AtomicInteger threadCount = new AtomicInteger(1);
	
	/** The reference to the thread factory. */
	public static final Ref<ThreadFactory> _ThreadFactory_ = Ref.of(ThreadFactory.class, runnable->{
		Thread thread = new Thread(runnable);
		thread.setName("Thread#" + threadCount.getAndIncrement());
		return thread;
	});
	
	/** The reference to the executor. */
	public static final Ref<Executor> _Executor_  = Ref.of(Executor.class, ()->(Executor)(runnable->{
		Thread newThread = Get.a(_ThreadFactory_).newThread(runnable);
		newThread.start();
	}));
	
	/** This logger will not say a word */
	public static final Consumer<Supplier<String>> quiteLogger = sub->{};

	/** This logger will show the message and stack trace. */
	public static final Consumer<Supplier<String>> verboseLogger = sup->{
		System.out.println(((Supplier<String>)sup).get());
		Arrays.stream(Thread.currentThread().getStackTrace()).forEach(each->System.out.println("\t" + each));
	};
	
	// TODO - This seriously need to be cleaned up.
	/** Ref for logger used within DirectGet. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Ref<Consumer<Supplier<String>>> _Logger_
			= (Ref<Consumer<Supplier<String>>>)
				(Ref)Ref.of(Consumer.class,  quiteLogger);
	
	private Get() {
		
	}
	
	/** @return the optional value associated with the given ref.  */
	public static <T> Optional<T> _a(Ref<T> ref) {
		return App.Get()._a(ref);
	}
	
	/** @return the optional value associated with the given class.  */
	public static <T> Optional<T> _a(Class<T> clzz) {
		return _a(Ref.forClass(clzz));
	}
	
	/** @return the value associated with the given class.  */
	public static <T> T a(Class<T> clzz) {
		return a(Ref.forClass(clzz));
	}
	
	/** @return the value associated with the given ref.  */
	public static <T> T a(Ref<T> ref) {
		return _a(ref).orElse(null);
	}
	
	/** @return the value associated with the given class or return the elseValue if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, T elseValue) {
		return a(Ref.forClass(clzz), elseValue);
	}

	/** @return the value associated with the given ref or return the elseValue if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, T elseValue) {
		try {
			return _a(ref).orElse(elseValue);
		} catch (GetException e) {
			return elseValue;
		}
	}

	/** @return the value associated with the given class or return the from elseSupplier if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
		return a(Ref.forClass(clzz), elseSupplier);
	}
	
	/** @return the value associated with the given ref or return the from elseSupplier if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
		return _a(ref).orElseGet(elseSupplier);
	}
	
	/**
	 * Substitute the given providings and run the runnable.
	 */
	public static void substitute(@SuppressWarnings("rawtypes") List<Providing> providings, Runnable runnable) {
		 App.Get().substitute(providings, runnable);
	}
	
	/**
	 * Substitute the given providings and run the action.
	 */
	@SuppressWarnings("rawtypes")
	public <V, T extends Throwable> V substitute(List<Providing> providings, Computation<V, T> computation) throws T {
		return App.Get().substitute(providings, computation);
	}
	
	/**
	 * Create a sub thread with a get that inherits all substitution from the current Get
	 *   and run the runnable with it.
	 **/
	public static Thread newThread(Runnable runnable) {
		return App.Get().newThread(runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the given substitution from the current
	 *   Get and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
		return App.Get().newThread(refsToInherit, runnable);
	}

	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		return App.Get().newThread(refsToInherit, runnable);
	}
	
	/**
	 * Create and run a sub thread with a get that inherits all substitution from the current
	 *   Get and run the runnable with it.
	 **/
	public static void runNewThread(Runnable runnable) {
		App.Get().newThread(runnable).start();
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the providings of those given refs.
	 **/
	@SuppressWarnings("rawtypes") 
	public static void runNewThread(List<Ref> refsToInherit, Runnable runnable) {
		App.Get().runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test).
	 **/
	@SuppressWarnings("rawtypes") 
	public static void runNewThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		App.Get().runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static <V, T extends Throwable> CompletableFuture<V> runThread(
			Predicate<Ref> refsToInherit,
			Computation<V, T> computation) {
		return App.Get().runThread(refsToInherit, computation);
	}
	
	//== The implementation ===================================================
	
	/**
	 * Get is a service to allow access to other service.
	 * 
	 * @author nawaman
	 */
	public static final class Instance {
		
		private final Scope scope;
		
		@SuppressWarnings("rawtypes")
		private final Map<Ref, Stack<Providing>> providingStacks = new TreeMap<>();
		
		Instance(Scope scope) {
			this.scope = scope;
		}
		
		/** @return the scope this Get is in. */
		public Scope getScope() {
			return this.scope;
		}
		
		@SuppressWarnings("rawtypes")
		Stream<Ref> getStackRefs() {
			return providingStacks.keySet().stream();
		}
		
		<T> Providing<T> getProviding(Ref<T> ref) {
			if (ref == null) {
				return null;
			}
			
			Providing<T> providing = Preferability.determineGetProviding(
					ref,
					providingFromScope(ref),
					providingFromStack(ref));
			return providing;
		}

		private <T> Supplier<Providing<T>> providingFromScope(Ref<T> ref) {
			return ()->Optional.ofNullable(scope).map(rfSp->rfSp.getProviding(ref)).orElse(null);
		}
		
		@SuppressWarnings("unchecked")
		private <T> Supplier<Providing<T>> providingFromStack(Ref<T> ref) {
			return ()->{
				@SuppressWarnings("rawtypes")
				Stack<Providing> stack = providingStacks.get(ref);
				return ((stack == null) || stack.isEmpty()) ? null : stack.peek();
			};
		}

		/** @return the optional value associated with the given ref.  */
		public <T> Optional<T> _a(Ref<T> ref) {
			return scope.doGet(ref);
		}

		/** @return the optional value associated with the given class.  */
		public <T> Optional<T> _a(Class<T> clzz) {
			return _a(Ref.forClass(clzz));
		}
		
		/** @return the value associated with the given ref.  */
		public <T> T a(Class<T> clzz) {
			return a(Ref.forClass(clzz));
		}
		
		/** @return the value associated with the given class.  */
		public <T> T a(Ref<T> ref) {
			return _a(ref).orElse(null);
		}
		
		/** @return the value associated with the given class or return the elseValue if no value associated with the class.  */
		public <T> T a(Class<T> clzz, T elseValue) {
			return a(Ref.forClass(clzz), elseValue);
		}

		/** @return the value associated with the given ref or return the elseValue if no value associated with the ref.  */
		public <T> T a(Ref<T> ref, T elseValue) {
			try {
				return _a(ref).orElse(elseValue);
			} catch (GetException e) {
				return elseValue;
			}
		}

		/** @return the value associated with the given class or return the from elseSupplier if no value associated with the class.  */
		public <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
			return a(Ref.forClass(clzz), elseSupplier);
		}
		
		/** @return the value associated with the given ref or return the from elseSupplier if no value associated with the ref.  */
		public <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
			return _a(ref).orElseGet(elseSupplier);
		}
		
		// TODO - Make it array friendly.
		/**
		 * Substitute the given providings and run the runnable.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public <T> void substitute(List<Providing> providings, Runnable runnable) {
			AtomicReference<RuntimeException> problem = new AtomicReference<RuntimeException>(null);
			try {
				substitute(providings, (Computation)(()->{
					try {
						runnable.run();
					} catch (RuntimeException e) {
						problem.set(e);
					}
					return null;
				}));
			} catch (Throwable t) {
				throw new RunWithSubstitutionException(t);
			}
			
			if (problem.get() != null) {
				throw problem.get();
			}
		}
		
		/**
		 * Substitute the given providings and run the action.
		 */
		@SuppressWarnings("rawtypes")
		synchronized public <V, T extends Throwable> V substitute(List<Providing> providings, Computation<V, T> computation) throws T {
			if ((providings == null) || providings.isEmpty()) {
				return computation.run();
			}
			Map<Ref, Stack<Providing>> providingStacks = this.providingStacks;
			List<Ref> addedRefs = new ArrayList<>();
			try {
				for (Providing providing : providings) {
					if (providing == null) {
						continue;
					}
					
					Ref ref = providing.getRef();
					if (null == providingStacks.get(ref)) {
						providingStacks.put(ref, new Stack<>());
					}
					Stack<Providing> stack = providingStacks.get(ref);
					stack.push(providing);
				}

				return computation.run();
			} finally {
				addedRefs.forEach(ref->providingStacks.get(ref).pop());
			}
		}
		
		/**
		 * Create a sub thread with a get that inherits all substitution from the current Get
		 *   and run the runnable with it.
		 **/
		public Thread newThread(Runnable runnable) {
			return newThread(INHERIT_NONE, runnable);
		}
		
		/**
		 * Create a sub thread with a get that inherits the given substitution from the current
		 *   Get and run the runnable with it.
		 **/
		@SuppressWarnings("rawtypes")
		public Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
			return newThread(
					ref->refsToInherit.contains(ref),
					runnable);
		}
		
		/**
		 * Create a sub thread with a get that inherits the substitution from the current Get
		 *   (all Ref that pass the predicate test) and run the runnable with it.
		 **/
		@SuppressWarnings("rawtypes")
		public Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
			Get.Instance    newGet     = prepareNewGet();
			List<Providing> providings = prepareProvidings(refsToInherit);
			
			ThreadFactory newThread = a(_ThreadFactory_);
			return newThread.newThread(()->{
				scope.threadGet.set(newGet);
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
		public <V, T extends Throwable> CompletableFuture<V> runThread(
				Predicate<Ref> refsToInherit,
				Computation<V, T> computation) {
			Get.Instance    newGet     = prepareNewGet();
			List<Providing> providings = prepareProvidings(refsToInherit);
			
			Executor executor = a(_Executor_);
			return CompletableFuture.supplyAsync(()->{
				scope.threadGet.set(newGet);
				try {
					return newGet.substitute(providings, computation);
				} catch (Throwable t) {
					throw new CompletionException(t);
				}
			}, executor);
		}
		
		private Get.Instance prepareNewGet() {
			Get.Instance newGet = new Get.Instance(scope);
			return newGet;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private List<Providing> prepareProvidings(Predicate<Ref> refsToInherit) {
			Predicate<Ref> predicate = Optional.ofNullable(refsToInherit).orElse(ref->false);
			
			List<Providing> providings
				= (List<Providing>)this.getStackRefs()
				.filter(predicate)
				.map(this::getProviding)
				.collect(Collectors.toList());
			return providings;
		}
		
	}
}
