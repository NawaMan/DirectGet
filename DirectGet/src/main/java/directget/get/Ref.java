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
package directget.get;

import java.util.Optional;
import java.util.function.Supplier;

import directget.get.exceptions.GetException;
import directget.get.supportive.DirectRef;
import directget.get.supportive.ForClassRef;
import directget.run.Named;
import lombok.val;

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T>
 *            the type of the reference.
 * 
 * @author nawaman
 */
public interface Ref<T> extends Comparable<Ref<T>> {
    
    /** @return the class of the interested object. */
    public Class<T> getTargetClass();
    
    /**
     * The name of the reference.
     * 
     * This value is for the benefit of human who look at it. There is no use in
     * the program in anyway (except debugging/logging/auditing purposes).
     **/
    public String getName();
    
    /** @return the default object. */
    default public T get() {
        try {
            val clzz = getTargetClass();
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
    
    /**
     * @return the compare result of between this Ref and the given reference.
     */
    default public int compareTo(Ref<T> o) {
        if (o == null) {
            return Integer.MAX_VALUE;
        }
        
        if (this.equals(o)) {
            return 0;
        }
        
        return this.toString().compareTo(o.toString());
    }
    
    /**
     * Create another ref of the same type and preferibility but with the given default supplier.
     * 
     * @param name
     *          the supplier name.
     * @param defaultSupplier
     *          the default supplier
     * @return a new ref with the default value.
     */
    @SuppressWarnings("rawtypes")
    default public DirectRef<T> by(String name, Supplier<? extends T> defaultSupplier) {
        return new DirectRef<>(
                this.getName(),
                this.getTargetClass(),
                ((DirectRef)this).getPreferability(),
                new Named.Supplier<>(name, defaultSupplier));
    }
        
    /**
     * Create another ref of the same type and preferibility but with the given default supplier.
     * 
     * @param defaultSupplier the default supplier
     * @return a new ref with the default value.
     */
    @SuppressWarnings("rawtypes")
    default public DirectRef<T> by(Supplier<? extends T> defaultSupplier) {
        return new DirectRef<>(
                this.getName(),
                this.getTargetClass(),
                ((DirectRef)this).getPreferability(),
                defaultSupplier);
    }
    
    /** Create a providing that dictate the current. */
    default public Providing<T> dictateCurrent() {
        val currentProviding = Get.getProvider(this);
        return new Providing<>(this, Preferability.Dictate, currentProviding.getSupplier());
    }
    
    /** Create a providing that dictate the given value. */
    default public Providing<T> dictatedTo(T value) {
        return new Providing<>(this, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the providing that dictate the value of the given ref. */
    default public Providing<T> dictatedToA(Ref<T> ref) {
        return new Providing<>(this, Preferability.Dictate, new Named.RefSupplier<T>(ref));
    }
    
    /** Create the providing that dictate the result of the given supplier. */
    default public Providing<T> dictatedBy(Supplier<T> supplier) {
        return new Providing<>(this, Preferability.Dictate, supplier);
    }

    /** Create a providing that provide the current. */
    default public Providing<T> provideCurrent() {
        val currentProviding = Get.getProvider(this);
        return new Providing<>(this, Preferability.Normal, currentProviding.getSupplier());
    }
    
    /** Create the providing (normal preferability) the given value. */
    default public Providing<T> providedWith(T value) {
        return new Providing<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the providing (normal preferability) the value of the given ref.
     */
    default public Providing<T> providedWithA(Ref<T> ref) {
        return new Providing<>(this, Preferability.Normal, new Named.RefSupplier<T>(ref));
    }
    
    /**
     * Create the providing (normal preferability) the result of the given
     * supplier.
     */
    default public Providing<T> providedBy(Supplier<T> supplier) {
        return new Providing<>(this, Preferability.Normal, supplier);
    }
    
    /** Create the providing (using the given preferability) the given value. */
    default public Providing<T> providedWith(Preferability preferability, T value) {
        return new Providing<>(this, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the providing (using the given preferability) the value of the
     * given ref.
     */
    default public Providing<T> providedWithA(Preferability preferability, Ref<T> ref) {
        return new Providing<>(this, preferability, new Named.RefSupplier<T>(ref));
    }
    
    /**
     * Create the providing (using the given preferability) the result of the
     * given supplier.
     */
    default public Providing<T> providedBy(Preferability preferability, Supplier<T> supplier) {
        return new Providing<>(this, preferability, supplier);
    }

    /** Create a providing that provide the current. */
    default public Providing<T> defaultToCurrent() {
        val currentProviding = Get.getProvider(this);
        return new Providing<>(this, Preferability.Default, currentProviding.getSupplier());
    }
    
    /** Create the providing that default to the given value. */
    default public Providing<T> defaultedTo(T value) {
        return new Providing<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the providing that default to the value of the given ref. */
    default public Providing<T> defaultedToA(Ref<T> ref) {
        return new Providing<>(this, Preferability.Normal, new Named.RefSupplier<T>(ref));
    }
    
    /**
     * Create the providing that default to the result of the given supplier.
     */
    default public Providing<T> defaultedToBy(Supplier<T> supplier) {
        return new Providing<>(this, Preferability.Normal, supplier);
    }
    
    // == Basic implementations ===============================================
    
    // -- ForClass -------------------------------------------------------------
    
    /** @return the reference that represent the target class directly. */
    public static <T> Ref<T> forClass(Class<T> targetClass) {
        return new ForClassRef<>(targetClass);
    }
    
    // -- Direct -------------------------------------------------------------
    
    /** Create and return a reference to the default value's class. **/
    public static <T> Ref<T> of(T defaultValue) {
        @SuppressWarnings("unchecked")
        val targetClass = (Class<T>)defaultValue.getClass();
        return of(null, targetClass, Preferability.Default, defaultValue);
    }
    
    /** Create and return a reference to a target class. **/
    public static <T> Ref<T> of(Class<T> targetClass) {
        return of(null, targetClass, Preferability.Default, null).by(null);
    }
    
    /**
     * Create and return a reference to a target class with the default value.
     **/
    public static <T, V extends T> Ref<T> of(Class<T> targetClass, V defaultValue) {
        return of(null, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class.
     **/
    public static <T> Ref<T> of(String name, Class<T> targetClass) {
        return of(name, targetClass, Preferability.Default, null).by(null);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default factory.
     **/
    public static <T, V extends T> Ref<T> of(String name, Class<T> targetClass, V defaultValue) {
        return of(name, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference to a target class with the default factory.
     **/
    public static <T, V extends T> Ref<T> of(Class<T> targetClass, Preferability preferability, V defaultValue) {
        return of(null, targetClass, preferability, defaultValue);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default factory.
     **/
    public static <T, V extends T> Ref<T> of(String name, Class<T> targetClass, Preferability preferability, V defaultValue) {
        val theName    = (name != null) ? name : ("#" + DirectRef.getNewId());
        val theFactory = new Named.ValueSupplier<T>(defaultValue);
        return new DirectRef<>(theName, targetClass, preferability, theFactory);
    }
    
}