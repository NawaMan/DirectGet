//  ========================================================================
//  Copyright (c) 2017 Nawapunth Manusitthipol.
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
package directget.get.supportive;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import directget.get.Preferability;
import directget.get.Ref;
import directget.get.run.Named;
import lombok.val;

/**
 * This reference implementation allows multiple references to a class to
 * mean different things.
 * 
 * @author NawaMan
 **/
public class RefOf<T> extends AbstractRef<T> implements Ref<T> {
    
    static final AtomicLong id = new AtomicLong();

    /**
     * The new id.
     * 
     * @return the new id.
     */
    public static long getNewId() {
        return  id.incrementAndGet();
    }
    
    private final String name;
    
    private final Provider<T> provider;
    
    /**
     * Constructor.
     * 
     * @param name
     *          the ref name.
     * @param targetClass
     *          the target class.
     * @param preferability
     *          the preferability.
     * @param factory
     *          the value factory.
     */
    public RefOf(String name, Class<T> targetClass, Preferability preferability, Supplier<? extends T> factory) {
        super(targetClass);
        val prefer = (preferability != null) ? preferability : Preferability.Default;
        
        this.name = Optional.ofNullable(name).orElse(targetClass.getName() + "#" + id.getAndIncrement());
        this.provider = (factory == null) 
                ? null 
                : new Provider<>(this, prefer, factory);
    }
    
    /** @return the name of the reference */
    public String getName() {
        return this.name;
    }
    
    /** @return the default object. */
    @Override
    public final T get() {
        if (provider == null) {
            return super.get();
        } else {
            return provider.get();
        }
    }
    
    /** @return the optional default object. */
    @Override
    public final Optional<T> _get() {
        return Optional.ofNullable(get());
    }
    
    @Override
    public Provider<T> getProvider() {
        return this.provider;
    }
    
    /**
     * Returns the preferability.
     * 
     * @return preferability
     */
    public Preferability getPreferability() {
        return (this.provider != null) ? this.provider.getPreferability() : Preferability.Default;
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
    
    /** Returns the retainer. */
    public RefOfWithRetainer<T> retained() {
        return new RefOfWithRetainer<>(name, getTargetClass(), getPreferability(), provider.getSupplier());
    }
    
    //== Modifier =====================================================================================================
    
    /** Create a provider that dictate the given value. */
    public <V extends T> RefOf<T> dictatedTo(V value) {
        return providedBy(Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that dictate the value of the given ref. */
    public <V extends T> RefOf<T> dictatedToA(Ref<V> ref) {
        return providedBy(Preferability.Dictate, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that dictate the result of the given supplier. */
    public <V extends T> RefOf<T> dictatedBy(Supplier<V> supplier) {
        return providedBy(Preferability.Dictate, supplier);
    }
    
    /** Create the provider (normal preferability) the given value. */
    public <V extends T> RefOf<T> providedWith(V value) {
        return providedBy(Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     */
    public <V extends T> RefOf<T> providedWithA(Ref<V> ref) {
        return providedBy(Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (normal preferability) the result of the given
     * supplier.
     */
    public <V extends T> RefOf<T> providedBy(Supplier<V> supplier) {
        return providedBy(Preferability.Normal, supplier);
    }
    
    /** Create the provider (using the given preferability) the given value. */
    public <V extends T> RefOf<T> providedWith(Preferability preferability, V value) {
        return providedBy(preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given ref.
     */
    public <V extends T> RefOf<T> providedWithA(Preferability preferability, Ref<V> ref) {
        return providedBy(preferability, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (using the given preferability) the result of the
     * given supplier.
     */
    public <V extends T> RefOf<T> providedBy(Preferability preferability, Supplier<V> supplier) {
        return new RefOf<>(
                this.getName(),
                this.getTargetClass(),
                preferability,
                supplier);
    }
    
    /** Create the provider that default to the given value. */
    public <V extends T> RefOf<T> defaultedTo(V value) {
        return providedBy(Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that default to the value of the given ref. */
    public <V extends T> RefOf<T> defaultedToA(Ref<V> ref) {
        return providedBy(Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     */
    public <V extends T> RefOf<T> defaultedToBy(Supplier<V> supplier) {
        return providedBy(Preferability.Normal, supplier);
    }

}