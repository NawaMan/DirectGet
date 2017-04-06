package direct.get;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
	
	@SuppressWarnings("rawtypes")
	final Map<Ref, Optional<Provider>> providers = new ConcurrentHashMap<>();
	
	<T> Optional<Provider<T>> getProviders(Ref<T> ref) {
		if (this.providers.containsKey(ref)) {
			Object obj = providers.get(ref);
			
			@SuppressWarnings("unchecked")
			Optional<Provider<T>> opt = (Optional<Provider<T>>)obj;
			
			return opt;
		} else {
			return null;
		}
	}

}
