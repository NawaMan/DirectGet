package direct.get;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;

public class Providing<T> implements Supplier<T> {
	
	private final Ref<T> ref;
	
	private final PriorityLevel level;
	
	public Providing(Ref<T> ref, PriorityLevel level) {
		super();
		this.ref   = Objects.requireNonNull(ref);
		this.level = Optional.ofNullable(level).orElse(PriorityLevel.Default);
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
	
	public PriorityLevel getLevel() {
		return level;
	}
	
	// == Sub implementations =================================================
	
	public static class Basic<T> extends Providing<T> {

		private final Supplier<T> supplier;
		
		public Basic(Ref<T> ref, PriorityLevel level, Supplier<T> supplier) {
			super(ref, level);
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
