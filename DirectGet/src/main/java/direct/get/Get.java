package direct.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;

public final class Get {
	
	private Get() {
		
	}
		
	public static <T> Optional<T> _a(Ref<T> ref) {
		return AppScope.get._a(ref);
	}

	public static <T> T a(Class<T> clzz) {
		return a(Ref.forClass(clzz));
	}
	
	public static <T> T a(Ref<T> ref) {
		return _a(ref).orElse(null);
	}
	
	public static <T> T a(Class<T> clzz, T elseValue) {
		return a(Ref.forClass(clzz), elseValue);
	}
	
	public static <T> T a(Ref<T> ref, T elseValue) {
		try {
			return _a(ref).orElse(elseValue);
		} catch (GetException e) {
			return elseValue;
		}
	}
	
	public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
		return a(Ref.forClass(clzz), elseSupplier);
	}
	
	public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
		return _a(ref).orElseGet(elseSupplier);
	}
	
	public static <T> Optional<T> _a(Class<T> clzz) {
		return _a(Ref.forClass(clzz));
	}
	
	public static <T> Instance substitute(Providing<T> providing, Runnable runnable) {
		 return AppScope.get.substitute(Arrays.asList(providing), runnable);
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
					= PriorityLevel.determineGetProviding(
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
		
		public <T> Optional<T> _a(Ref<T> ref) {
			return scope.doGet(ref);
		}
		
		public <T> Optional<T> _a(Class<T> clzz) {
			return _a(Ref.forClass(clzz));
		}
	
		public <T> T a(Class<T> clzz) {
			return a(Ref.forClass(clzz));
		}
		
		public <T> T a(Ref<T> ref) {
			return _a(ref).orElse(null);
		}
		
		public <T> T a(Class<T> clzz, T elseValue) {
			return a(Ref.forClass(clzz), elseValue);
		}
		
		public <T> T a(Ref<T> ref, T elseValue) {
			try {
				return _a(ref).orElse(elseValue);
			} catch (GetException e) {
				return elseValue;
			}
		}
		
		// TODO - Make it array friendly.
		
		@SuppressWarnings("rawtypes")
		public <T> Instance substitute(List<Providing> providings, Runnable runnable) {
			if ((providings == null) || providings.isEmpty()) {
				runnable.run();
			} else {
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
					
					runnable.run();
				} finally {
					addedRefs.forEach(ref->providingStacks.get(ref).pop());
				}
			}
			return this;
		}
		
	}
}
