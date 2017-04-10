package direct.get;

import java.util.Optional;
import java.util.Stack;

import direct.get.exceptions.ProvideWrongTypeException;
import direct.get.exceptions.ProvidingException;
import direct.get.exceptions.UnknownProviderException;

public class Get {
	
	public static final Ref<Get> GLOBAL = Ref.of(Get.class, ref->Get.globalGet);
	public static final Ref<Get> CURRENT = Ref.ofClass(Get.class);
	
	private static final Get globalGet = new Get();
	
	private static final ThreadLocal<Get> localGet = ThreadLocal.withInitial(()->new Get(globalGet));
	
	private Optional<Get> parent;
	
	public Get() {
		this(null);
	}
	
	public Get(Get parent) {
		this.parent = Optional.ofNullable(parent);
	}
	
	public Optional<Get> getParentGet() {
		return this.parent;
	}
	
	final Stack<Context> contexts = new Stack<>(); {
		contexts.push(new Context());
	}
	
	@SuppressWarnings({ "rawtypes" })
	private static final Provider defaultProvider = ref -> {
		try {
			return ((Ref) ref).getTargetClass().newInstance();
		} catch (Exception e) {
			throw new ProvidingException((Ref)ref, e);
		}
	};
	@SuppressWarnings("rawtypes")
	private static final Optional<Provider> defaultProviderOptional
			= Optional.of(defaultProvider);
	
	// Return null for not known.
	// Return Optional.empty() for known to be null.
	@SuppressWarnings("rawtypes")
	static Optional<Provider> getProvider(Ref ref) {
		Get get = Get.localGet.get();
		Optional<Provider> provider = getProviderFromGet(ref, get);
		return provider;
	}

	@SuppressWarnings("rawtypes")
	private static Optional<Provider> getProviderFromGet(Ref ref, Get get) {
		Stack<Context> contexts = get.contexts;
		if (!contexts.isEmpty()) {
			for (int i = contexts.size() - 1; i >= 0; i--) {
				Context context = contexts.get(i);
				@SuppressWarnings("unchecked")
				Optional<Provider> provider = context.getProviders(ref);
				if (provider != null) {
					return provider;
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static <T> void ensureProvider(Ref<T> ref, Optional<Provider> provider)
			throws UnknownProviderException {
		if (provider == null) {
			throw new UnknownProviderException(ref);
		}
		
		if (!provider.isPresent()) {
			throw new UnknownProviderException(ref);
		}
	}
	
	Context getContext() {
		return this.contexts.peek();
	}
	
	public static <T, V extends T> V a(Class<T> targetClass) {
		return a(Ref.ofClass(targetClass));
	}
	
	@SuppressWarnings("unchecked")
	public static <T, V extends T> V a(Ref<T> ref) {
		if (ref == GLOBAL) {
			return (V)Get.globalGet;
		}
		
		if (ref.getTargetClass() == Get.class) {
			return (V)Get.localGet.get();
		}
		
		@SuppressWarnings("rawtypes")
		Optional<Provider> provider = getProvider(ref);
		if (provider == null) {
			provider = getDefaultProvider(ref, provider);
		}
		
		ensureProvider(ref, provider);
		
		Object object = provider.get().apply(ref);
		if ((object != null)
		 && !ref.getTargetClass().isInstance(object)) {
			throw new ProvideWrongTypeException();
		}
		return (V)object;
	}

	@SuppressWarnings("rawtypes")
	private static <T> Optional<Provider> getDefaultProvider(Ref<T> ref, Optional<Provider> provider) {
		Provider<T> defaultProvider = ref.getDefaultProvider();
		if (defaultProvider != null) {
			provider = Optional.of(clss -> defaultProvider.apply(ref));
		}
		if (provider == null) {
			provider = defaultProviderOptional;
		}
		return provider;
	}
	
}
