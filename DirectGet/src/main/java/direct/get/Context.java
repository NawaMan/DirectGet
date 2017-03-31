package direct.get;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
	
	@SuppressWarnings("rawtypes")
	final Map<Class, Optional<Provider>> providers = new ConcurrentHashMap<>();
	
	<T> Optional<Provider<T>> getProviders(Class<T> clzz) {
		if (this.providers.containsKey(clzz)) {
			Object obj = providers.get(clzz);
			
			@SuppressWarnings("unchecked")
			Optional<Provider<T>> opt = (Optional<Provider<T>>)obj;
			
			return opt;
		} else {
			return null;
		}
	}

}
