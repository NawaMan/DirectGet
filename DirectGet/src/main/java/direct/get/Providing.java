package direct.get;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Instance of this class holds data for providing.
 * 
 * @author nawaman
 **/
public class Providing<T> implements Supplier<T> {
	
	private final Ref<T> ref;
	
	private final Preferability preferability;

	private final Supplier<T> supplier;
	
	/**
	 * Constructor.
	 * 
	 * @param ref            the reference.
	 * @param preferability  the level of preferability.
	 * @param supplier       the supplier to get the value.
	 */
	public Providing(Ref<T> ref, Preferability preferability, Supplier<T> supplier) {
		this.ref           = Objects.requireNonNull(ref);
		this.preferability = Optional.ofNullable(preferability).orElse(Preferability.Default);
		this.supplier      = Optional.ofNullable(supplier).orElse(()->null);
	}
	
	/** @return the reference for this providing. */
	public final Ref<T> getRef() {
		return ref;
	}

	@Override
	public T get() {
		return supplier.get();
	}

	/** @return the preferability for this providing. */
	public final Preferability getPreferability() {
		return preferability;
	}
	
	public String toString() {
		return "Providing (" + preferability + ":" + ref + "): " + supplier;
	}
	
}
