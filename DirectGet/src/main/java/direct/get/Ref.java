package direct.get;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T> the type of the reference.
 * 
 * @author nawaman
 */
public interface Ref<T> extends Comparable<Ref<T>> {
	
	/** @return the class of the interested object. */
	public Class<T> getTargetClass();
	
	/**
	 * The name of the reference.
	 * 
	 * This value is for the benefit of human who look at it.
	 * There is no use in the program in anyway (except debugging/logging/auditing purposes).
	 **/
	public String getName();
	
	/** @return the default object. */
	default public T get() {
		try {
			Class<T> clzz = getTargetClass();
			return clzz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GetException(this, e);
		}
	}

	/** @return the optional default object. */
	default public Optional<T> _get() {
		return Optional.ofNullable(get());
	}
	
	/** @return the compare result of between this Ref and the given reference. */
    default public int compareTo(Ref<T> o) {
    	if (o == null) {
    		return Integer.MAX_VALUE;
    	}
    	
    	if (this.equals(o)) {
    		return 0;
    	}
    	
    	return this.toString().compareTo(o.toString());
    }
	
	
	// == Basic implementations ===============================================
	
	// -- Abstract or base implementation -------------------------------------

    /**
     * Base implementation of references.
     **/
	public static abstract class AbstractRef<T> implements Ref<T> {

		private final Class<T> targetClass;

		private final String targetClassName;

		AbstractRef(Class<T> targetClass) {
			this.targetClass = targetClass;
			this.targetClassName = this.targetClass.getCanonicalName();
		}
		
		/** {@inheritDoc} */
		public String getName() {
			return this.targetClassName;
		}
		
		/** {@inheritDoc} */
		@Override
		public final Class<T> getTargetClass() {
			return this.targetClass;
		}
		
		/** {@inheritDoc} */
		@Override
		public String toString() {
			return "Ref<" + this.targetClassName + ">";
		}
		
		/** {@inheritDoc} */
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
		
		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return this.targetClass.hashCode();
		}

	}
	
	//-- ForClass -------------------------------------------------------------
	
	/** @return the reference that represent the target class directly. */
	public static <T> Ref<T> forClass(Class<T> targetClass) {
		return new ForClass<>(targetClass);
	}
	
	/**
	 * This implements allow reference to a specific class. All instance of
	 *   this reference for the same class refer to the same thing.
	 */
	public static final class ForClass<T> extends AbstractRef<T> implements Ref<T> {
		
		ForClass(Class<T> targetClass) {
			super(targetClass);
		}
		
		/**
		 * For ForClass ref to be equals, they have to point to the same target class.
		 **/
		@Override
		@SuppressWarnings("rawtypes")
		public final boolean equals(Object obj) {
			if (!(obj instanceof Ref.AbstractRef)) {
				return false;
			}
			Class<T> thisTargetClass = this.getTargetClass();
			Class    thatTargetClass = ((Ref.AbstractRef) obj).getTargetClass();
			return thisTargetClass == thatTargetClass;
		}
		
	}
	
	//-- Direct -------------------------------------------------------------
	
	/** Create and return a reference to a target class. **/
	public static <T> Ref<T> of(Class<T> targetClass) {
		return of(null, targetClass, (Supplier<T>)null);
	}

	/** Create and return a reference to a target class with the default factory. **/
	public static <T> Ref<T> of(Class<T> targetClass, Supplier<T> factory) {
		return of(null, targetClass, factory);
	}

	/** Create and return a reference to a target class with the default value. **/
	public static <T> Ref<T> of(Class<T> targetClass, T defaultValue) {
		return of(null, targetClass, defaultValue);
	}

	/** Create and return a reference with a human readable name to a target class. **/
	public static <T> Ref<T> of(String name, Class<T> targetClass) {
		return of(name, targetClass, (Supplier<T>)null);
	}

	/** Create and return a reference with a human readable name to a target class with the default value. **/
	public static <T> Ref<T> of(String name, Class<T> targetClass, T defaultValue) {
		return of(name, targetClass, (Supplier<T>)(()->defaultValue));
	}

	/** Create and return a reference with a human readable name to a target class with the default factory. **/
	public static <T> Ref<T> of(String name, Class<T> targetClass, Supplier<T> factory) {
		return new Direct<>((name != null) ? name : ("#" + Direct.id.incrementAndGet()), targetClass, factory);
	}
	
	/**
	 * This reference implementation allows multiple references to a class to
	 *   mean different things.
	 **/
	public static class Direct<T> extends AbstractRef<T> implements Ref<T> {
		
		private static final AtomicLong id = new AtomicLong();
		
		private final String name;
		
		private final Providing<T> proviging;
		
		Direct(String name, Class<T> targetClass, Supplier<T> factory) {
			super(targetClass);
			this.name = Optional.ofNullable(name).orElse(targetClass.getName() + "#" + id.getAndIncrement());
			this.proviging = (factory == null) ? null : new Providing<>(this, Preferability.Default, factory);
		}
		
		/** @return the name of the reference  */
		public String getName() {
			return this.name;
		}
		
		/** @return the default object. */
		@Override
		public final T get() {
			if (proviging == null) {
				return super.get();
			} else {
				return proviging.get();
			}
		}

		/** @return the optional default object. */
		@Override
		public final Optional<T> _get() {
			return Optional.ofNullable(get());
		}

		/**
		 * For Direct ref to be equals, they have to be the same object.
		 **/
		@Override
		public final boolean equals(Object obj) {
			return this == obj;
		}
		
		/** {@inheritDoc} */
		@Override
		public final String toString() {
			return "Ref<" + this.name + ":" + this.getTargetClass().getName() + ">";
		}
		
	}
	
}