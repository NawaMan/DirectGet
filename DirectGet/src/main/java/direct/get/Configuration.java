package direct.get;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * 
 * @author nawaman
 **/
public final class Configuration {
	
	@SuppressWarnings("rawtypes")
	private final Map<Ref, Providing> providings;
	
	public Configuration() {
		this(null);
	}
	
	@SuppressWarnings("rawtypes") 
	public Configuration(Map<Ref, Providing> providings) {
		Map<Ref, Providing> providingMap = Optional
			.ofNullable(providings)
			.orElse(Collections.emptyMap());
		this.providings = Collections.unmodifiableMap(new TreeMap<>(providingMap));
	}
	
	@SuppressWarnings("rawtypes")
	public Stream<Ref> getRefs() {
		return providings.keySet().stream();
	}
	
	@SuppressWarnings("rawtypes")
	public Stream<Providing> getProvidings() {
		return providings.values().stream();
	}
	
	@SuppressWarnings("unchecked")
	public <T> Providing<T> get(Ref<T> ref) {
		return providings.get(ref);
	}
	
	public <T> boolean hasProviding(Ref<T> ref) {
		return providings.containsKey(ref);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<Providing<T>> _get(Ref<T> ref) {
		return Optional.ofNullable(providings.get(ref));
	}
	
}
