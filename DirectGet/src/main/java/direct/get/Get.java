package direct.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;

public final class Get {
	
	private Get() {
		
	}
		
	public static <T> Optional<T> _a(Ref<T> ref) {
		return AppSpace.get._a(ref);
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
		 return AppSpace.get.substitute(Arrays.asList(providing), runnable);
	}
	
	//== The implementation ===================================================
	
	/**
	 * Get is a service to allow access to other service.
	 * 
	 * @author nawaman
	 */
	public static final class Instance {
		
		private final RefSpace refSpace;
		
		private final Instance parentGet;
		
		@SuppressWarnings("rawtypes")
		private final Map<Ref, Stack<Providing>> providingStacks = new TreeMap<>();
		
		Instance(Instance parentGet, RefSpace refSpace) {
			this.parentGet = parentGet;
			this.refSpace  = refSpace;
			
			// TODO - Check for conflict between parentGet and config.
		}
		
		public RefSpace getSpace() {
			return this.refSpace;
		}
		
		@SuppressWarnings("rawtypes")
		Stream<Ref> getStackRefs() {
			return providingStacks.keySet().stream();
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		<T> Providing<T> getProviding(Ref<T> ref) {
			Providing<T> parentProviding = null;
			if (parentGet != null) {
				parentProviding = parentGet.getProviding(ref);
				if (ProvidingLevel.Dictate.is(parentProviding)) {
					return parentProviding;
				}
			}
			
			RefSpace curSpace = Optional.ofNullable(refSpace).orElse(AppSpace.current);
			Providing<T> configProviding = curSpace.getProviding(ref);
			if (ProvidingLevel.Dictate.is(configProviding)) {
				return configProviding;
			}
	
			Providing stackProviding = peekStackProviding(ref);
			if (ProvidingLevel.Dictate.is(stackProviding)) {
				return stackProviding;
			}
			
			// At this point, non is dictate.
			
			if (ProvidingLevel.Normal.is(stackProviding)) {
				return stackProviding;
			}
			if (ProvidingLevel.Normal.is(configProviding)) {
				return configProviding;
			}
			if (ProvidingLevel.Normal.is(parentProviding)) {
				return parentProviding;
			}
			
			// At this point, non is normal.
			
			if (ProvidingLevel.Default.is(stackProviding)) {
				return stackProviding;
			}
			if (ProvidingLevel.Default.is(configProviding)) {
				return configProviding;
			}
			if (ProvidingLevel.Default.is(parentProviding)) {
				return parentProviding;
			}
			
			return null;
		}
	
		@SuppressWarnings("rawtypes")
		private <T> Providing peekStackProviding(Ref<T> ref) {
			Stack<Providing> stackProvidingStack = providingStacks.get(ref);
			Providing        stackProviding      = ((stackProvidingStack != null) && !stackProvidingStack.isEmpty()) ? stackProvidingStack.peek() : null;
			return stackProviding;
		}
		
		public <T> Optional<T> _a(Ref<T> ref) {
			return refSpace.doGet(ref);
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
		
		public <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
			return a(Ref.forClass(clzz), elseSupplier);
		}
		
		public <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
			return _a(ref).orElseGet(elseSupplier);
		}
		
		public <T> Optional<T> _a(Class<T> clzz) {
			return _a(Ref.forClass(clzz));
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
