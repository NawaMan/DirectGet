//  ========================================================================
//  Copyright (c) 2017 The Direct Solution Software Builder.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
package direct.get;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;
import lombok.val;

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
			val clzz     = getTargetClass();
			val instance = clzz.newInstance();
			return instance;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GetException(this, e);
		}
	}

	/** @return the optional default object. */
	default public Optional<T> _get() {
		return Optional.ofNullable(get());
	}
	
	/** @return the providing for the default value */
	public Providing<T> getProviding();
	
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
    
    default public Providing<T> dictatedTo(T value) {
    	return new Providing<>(this, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    default public Providing<T> dictatedToA(Ref<T> ref) {
    	return new Providing<>(this, Preferability.Dictate, new Named.RefSupplier<T>(ref));
    }
    
    default public Providing<T> dictatedBy(Supplier<T> supplier) {
    	return new Providing<>(this, Preferability.Dictate, supplier);
    }
    
    default public Providing<T> providedWith(T value) {
    	return new Providing<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    default public Providing<T> providedWithA(Ref<T> ref) {
    	// TODO - The supplier should be made a separate class.
    	return new Providing<>(this, Preferability.Normal, new Named.RefSupplier<T>(ref));
    }
    
    default public Providing<T> providedBy(Supplier<T> supplier) {
    	return new Providing<>(this, Preferability.Normal, supplier);
    }
    
    default public Providing<T> providedWith(Preferability preferability, T value) {
    	return new Providing<>(this, preferability, new Named.ValueSupplier<T>(value));
    }
    
    default public Providing<T> providedWithA(Preferability preferability, Ref<T> ref) {
    	// TODO - The supplier should be made a separate class.
    	return new Providing<>(this, preferability, new Named.RefSupplier<T>(ref));
    }
    
    default public Providing<T> providedBy(Preferability preferability, Supplier<T> supplier) {
    	return new Providing<>(this, preferability, supplier);
    }
    
    default public Providing<T> defaultedTo(T value) {
    	return new Providing<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    default public Providing<T> defaultedToA(Ref<T> ref) {
    	// TODO - The supplier should be made a separate class.
    	return new Providing<>(this, Preferability.Normal, new Named.RefSupplier<T>(ref));
    }
    
    default public Providing<T> defaultedToBy(Supplier<T> supplier) {
    	return new Providing<>(this, Preferability.Normal, supplier);
    }
	
    // TODO - Should there be another one that get a preferability instead.
	
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
		@Override
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
		
		private final Providing<T> providing;
		
		ForClass(Class<T> targetClass) {
			super(targetClass);
			this.providing = new Providing<>(this, Preferability.Default, ()->get());
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
			val thisTargetClass = this.getTargetClass();
			val thatTargetClass = ((Ref.AbstractRef) obj).getTargetClass();
			return thisTargetClass == thatTargetClass;
		}
		
		@Override
		public Providing<T> getProviding() {
			return providing;
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
		
		private final Providing<T> providing;
		
		Direct(String name, Class<T> targetClass, Supplier<T> factory) {
			super(targetClass);
			this.name      = Optional.ofNullable(name).orElse(targetClass.getName() + "#" + id.getAndIncrement());
			this.providing = (factory == null) ? null : new Providing<>(this, Preferability.Default, factory);
		}
		
		/** @return the name of the reference  */
		public String getName() {
			return this.name;
		}
		
		/** @return the default object. */
		@Override
		public final T get() {
			if (providing == null) {
				return super.get();
			} else {
				return providing.get();
			}
		}

		/** @return the optional default object. */
		@Override
		public final Optional<T> _get() {
			return Optional.ofNullable(get());
		}
		
		@Override
		public Providing<T> getProviding() {
			return this.providing;
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