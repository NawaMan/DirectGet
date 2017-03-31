package direct.get;

import java.util.Optional;
import java.util.Stack;

import direct.get.exceptions.ProvideWrongTypeException;
import direct.get.exceptions.ProvidingException;
import direct.get.exceptions.UnknownProviderException;

public class Get {
	
	private static final Get get = new Get();
	
	final Stack<Context> contexts = new Stack<>(); {
		contexts.push(new Context());
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static final Provider defaultProvider = clzz -> {
		try {
			return ((Class) clzz).newInstance();
		} catch (Exception e) {
			throw new ProvidingException((Class)clzz, e);
		}
	};
	@SuppressWarnings("rawtypes")
	private static final Optional<Provider> defaultProviderOptional
			= Optional.of(defaultProvider);
	
	// Return null for not known.
	// Return Optional.empty() for known to be null.
	@SuppressWarnings("rawtypes")
	static Optional<Provider> getProvider(Class clzz) {
		Get get = Get.get;
		Optional<Provider> provider = getProviderFromGet(clzz, get);
		return provider;
	}

	@SuppressWarnings("rawtypes")
	private static Optional<Provider> getProviderFromGet(Class clzz, Get get) {
		Stack<Context> contexts = get.contexts;
		if (!contexts.isEmpty()) {
			for (int i = contexts.size() - 1; i >= 0; i--) {
				Context context = contexts.get(i);
				@SuppressWarnings("unchecked")
				Optional<Provider> provider = context.getProviders(clzz);
				if (provider != null) {
					return provider;
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static <T> void ensureProvider(Class<T> clzz, Optional<Provider> provider)
			throws UnknownProviderException {
		if (provider == null) {
			throw new UnknownProviderException(clzz);
		}
		
		if (!provider.isPresent()) {
			throw new UnknownProviderException(clzz);
		}
	}
	
	Context getContext() {
		return this.contexts.peek();
	}
	
	@SuppressWarnings("unchecked")
	public static <T, V extends T> V a(Class<T> clzz) {
		if (clzz == Get.class) {
			return (V)Get.get;
		}
		
		@SuppressWarnings("rawtypes")
		Optional<Provider> provider = getProvider(clzz);
		if (provider == null) {
			provider = defaultProviderOptional;
		}
		
		ensureProvider(clzz, provider);
		
		Object object = provider.get().apply(clzz);
		if ((object != null)
		 && !clzz.isInstance(object)) {
			throw new ProvideWrongTypeException();
		}
		return (V)object;
	}
	
}
