package direct.get;

import java.util.Optional;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;

/***
 * 
 * @param <T>
 * 
 * @author nawaman
 */
@FunctionalInterface
public interface Ref<T> extends Comparable<Ref<T>> {
	
	public Class<T> getTargetClass();
	
	default public T get() {
		try {
			Class<T> clzz = getTargetClass();
			return clzz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GetException(e);
		}
	}
	
	default public Optional<T> _get() {
		return Optional.ofNullable(get());
	}
	
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

	public static abstract class AbstractRef<T> implements Ref<T> {

		private final Class<T> targetClass;

		private final String targetClassName;

		protected AbstractRef(Class<T> targetClass) {
			this.targetClass = targetClass;
			this.targetClassName = this.targetClass.getCanonicalName();
		}

		@Override
		public final Class<T> getTargetClass() {
			return this.targetClass;
		}
		
		@Override
		public String toString() {
			return "Ref<" + this.targetClassName + ">";
		}
		
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return this.targetClass.hashCode();
		}

	}
	
	//-- ForClass -------------------------------------------------------------
	
	public static <T> Ref<T> forClass(Class<T> targetClass) {
		return new ForClass<>(targetClass);
	}
	
	/**
	 * This implements allow reference to a specific class. All instance of
	 *   this reference for the same class refer to the same thing.
	 */
	public static final class ForClass<T> extends AbstractRef<T> implements Ref<T> {
		
		public ForClass(Class<T> targetClass) {
			super(targetClass);
		}
		
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
	
	public static <T> Ref<T> of(Class<T> targetClass) {
		return of(targetClass, null);
	}
	
	public static <T> Ref<T> of(Class<T> targetClass, Supplier<T> factory) {
		return new Direct<>(targetClass, factory);
	}
	
	/**
	 * This reference implementation allows multiple references to a class to
	 *   mean different things.
	 **/
	public static class Direct<T> extends AbstractRef<T> implements Ref<T> {
		
		private final Providing<T> proviging;
		
		public Direct(Class<T> targetClass, Supplier<T> factory) {
			super(targetClass);
			this.proviging = (factory == null) ? null : new Providing.Basic<>(this, ProvidingLevel.Default, factory);
		}
		
		@Override
		public final T get() {
			if (proviging == null) {
				return super.get();
			} else {
				return proviging.get();
			}
		}
		
		@Override
		public final Optional<T> _get() {
			return Optional.ofNullable(get());
		}
		
		@Override
		public final boolean equals(Object obj) {
			return this == obj;
		}
		
	}
	
}