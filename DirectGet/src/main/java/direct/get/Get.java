package direct.get;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;
import direct.get.exceptions.RunWithSubstitutionException;

/**
 * This class provide access to the application scope.
 * 
 * @author nawaman
 */
public final class Get {
	
	private Get() {
		
	}
	
	/** @return the optional value associated with the given ref.  */
	public static <T> Optional<T> _a(Ref<T> ref) {
		return AppScope.get._a(ref);
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
		 AppScope.get.substitute(providings, runnable);
	}
	
	/**
	 * Substitute the given providings and run the action.
	 */
	@SuppressWarnings("rawtypes")
	public <V, T extends Throwable> V substitute(List<Providing> providings, Computation<V, T> computation) throws T {
		return AppScope.get.substitute(providings, computation);
	}
	
	//== The implementation ===================================================
	
	/**
	 * Get is a service to allow access to other service.
	 * 
	 * @author nawaman
	 */
	public static final class Instance {
		
		private final Scope scope;
		
		private final Instance parentGet;
		
		@SuppressWarnings("rawtypes")
		private final Map<Ref, Stack<Providing>> providingStacks = new TreeMap<>();
		
		Instance(Instance parentGet, Scope scope) {
			this.parentGet = parentGet;
			this.scope     = scope;
			
			// TODO - Check for conflict between parentGet and config.
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
			
			Providing<T> providing
					= Preferability.determineGetProviding(
						providingFromParent(ref),
						providingFromScope(ref),
						providingFromStack(ref));
			return providing;
		}

		private <T> Supplier<Providing<T>> providingFromParent(Ref<T> ref) {
			return ()->Optional.ofNullable(parentGet).map(pGet->pGet.getProviding(ref)).orElse(null);
		}
		
		private <T> Supplier<Providing<T>> providingFromScope(Ref<T> ref) {
			return ()->Optional.ofNullable(scope).map(rfSp->rfSp.getProviding(ref)).orElse(null);
		}
		
		private <T> Predicate<Stack<T>> not(Predicate<Stack<T>> check) {
			return stack->!check.test(stack);
		}
	
		@SuppressWarnings("unchecked")
		private <T> Supplier<Providing<T>> providingFromStack(Ref<T> ref) {
			return ()->Optional.ofNullable(providingStacks.get(ref))
						.filter(not(Stack::isEmpty))
						.map(Stack::peek)
						.orElse(null);
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
		public <V, T extends Throwable> V substitute(List<Providing> providings, Computation<V, T> computation) throws T {
			if ((providings == null) || providings.isEmpty()) {
				return computation.run();
			}
			
			List<Ref> addedRefs = new ArrayList<>();
			try {
				providings
				.stream()
				.filter(Objects::nonNull)
				.forEach(providing->{
					Ref ref = providing.getRef();
					providingStacks.computeIfAbsent(ref, r->new Stack<>());
					providingStacks.get(ref).push(providing);
					addedRefs.add(ref);
				});
				
				return computation.run();
			} finally {
				addedRefs.forEach(ref->providingStacks.get(ref).pop());
			}
		}
		
	}
}
