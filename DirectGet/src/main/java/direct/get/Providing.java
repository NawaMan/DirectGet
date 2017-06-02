package direct.get;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;

public class Providing<T> implements Supplier<T> {
	
	private final Ref<T> ref;
	
	private final Preferability preferability;
	
	public Providing(Ref<T> ref, Preferability preferability) {
		this.ref           = Objects.requireNonNull(ref);
		this.preferability = Optional.ofNullable(preferability).orElse(Preferability.Default);
	}
	
	public Ref<T> getRef() {
		return ref;
	}

	@Override
	public T get() {
		Optional<T> optT = ref._get();
		if (optT.isPresent()) {
			return optT.get();
		}
		throw new GetException(ref.toString());
	}
	
	public Preferability getPreferability() {
		return preferability;
	}
	
	// == Sub implementations =================================================
	
	public static class Basic<T> extends Providing<T> {

		private final Supplier<T> supplier;
		
		public Basic(Ref<T> ref, Preferability preferability, Supplier<T> supplier) {
			super(ref, preferability);
			this.supplier = supplier;
		}

		@Override
		public T get() {
			if (supplier == null) {
				return super.get();
			}
			return supplier.get();
		}
		
	}
	
}
