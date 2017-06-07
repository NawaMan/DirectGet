package direct.get;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class contains the providings for a scope.
 * 
 * @author nawaman
 **/
public final class Configuration {
	
	@SuppressWarnings("rawtypes")
	private final Map<Ref, Providing> providings;
	
	/**
	 * Default constructor.
	 */
	@SuppressWarnings("rawtypes")
	public Configuration() {
		this((Map<Ref, Providing>)null);
	}
	
	/**
	 * Constructor.
	 */
	public Configuration(
			@SuppressWarnings("rawtypes") Providing ... providings) {
		this(Arrays.asList(providings));
	}

	/**
	 * Constructor.
	 */
	public Configuration(
			@SuppressWarnings("rawtypes") Collection<Providing> providings) {
		this(providings.stream()
				.filter(providing->providing != null)
				.collect(toMap(Providing::getRef, p->p)));
	}
	
	/**
	 * Constructor.
	 */
	private Configuration(
			@SuppressWarnings("rawtypes") Map<Ref, Providing> providings) {
		@SuppressWarnings("rawtypes") 
		Map<Ref, Providing> providingMap = Optional
			.ofNullable(providings)
			.orElse(emptyMap());
		this.providings = unmodifiableMap(new TreeMap<>(providingMap));
	}
	
	/** @return all the refs specified by this configuration. */
	public Stream<? extends Ref<?>> getRefs() {
		@SuppressWarnings("unchecked")
		Stream<? extends Ref<?>> stream = (Stream<? extends Ref<?>>) providings.keySet().stream();
		return stream;
	}
	
	/** @return all the providings specified by this configuration. */
	public Stream<? extends Providing<?>> getProvidings() {
		@SuppressWarnings("unchecked")
		Stream<? extends Providing<?>> stream = (Stream<? extends Providing<?>>) providings.values().stream();
		return stream;
	}
	
	/** @return the providing for the given ref. */
	public <T> Providing<T> getProviding(Ref<T> ref) {
		@SuppressWarnings("unchecked")
		Providing<T> providing = providings.get(ref);
		return providing;
	}

	/** @return {@code} if this configuration specified the providing for the given ref. */
	public <T> boolean hasProviding(Ref<T> ref) {
		return providings.containsKey(ref);
	}
	
	public String toString() {
		return "Configuration(" + providings.size() + ")";
	}
	/** Return the detail string representation of this object. */
	public String toXRayString() {
		if (providings.isEmpty()) {
			return "{\n}";
		}
		
		return "{\n\t"
				+ providings.entrySet().stream()
					.map(each->each.getKey() + "=" + each.getValue())
					.collect(Collectors.joining(",\n\t"))
				+ "\n}"; 
	}
	
}
