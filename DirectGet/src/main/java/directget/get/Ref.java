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
package directget.get;

import static directget.get.supportive.Utilities.whenNotNull;

import java.util.Optional;
import java.util.function.Supplier;

import directget.get.run.Named;
import directget.get.supportive.HasProvider;
import directget.get.supportive.Provider;
import directget.get.supportive.RefFactory;
import directget.get.supportive.RefFor;
import directget.get.supportive.RefOf;
import lombok.val;

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T>
 *            the type of the reference.
 * 
 * @author NawaMan
 */
public interface Ref<T> extends HasProvider<T>, HasRef<T>, Comparable<Ref<T>> {
    
    /** The default factory. */
    public static final RefOf<RefFactory> refFactory = Ref.ofValue(new RefFactory());
    
    
    
    /** @return this reference **/
    public default Ref<T> getRef() {
        return this;
    }
    
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
        return Get.the(refFactory).make(this);
    }
    
    /** @return the optional default object. */
    default public Optional<T> _get() {
        return Optional.ofNullable(get());
    }
    
    /** @return the providers for the default value */
    public Provider<T> getProvider();
    
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
    
    // == Basic implementations ===============================================
    
    // -- ForClass -------------------------------------------------------------
    
    /** @return the reference that represent the target class directly. */
    public static <T> RefFor<T> forClass(Class<T> targetClass) {
        return new RefFor<>(targetClass);
    }
    
    // -- Direct -------------------------------------------------------------
    
    /** Create and return a reference to the default value's class default to the default factory. **/
    public static <T> RefOf<T> ofValue(T defaultValue) {
        @SuppressWarnings("unchecked")
        val targetClass = (Class<T>)defaultValue.getClass();
        return ofValue(null, targetClass, Preferability.Default, defaultValue);
    }
    
    /** Create and return a reference to a target class with default factory. **/
    public static <T> RefOf<T> of(Class<T> targetClass) {
        return ofValue(null, targetClass, Preferability.Default, null).defaultedToBy(null);
    }
    
    /**
     * Create and return a reference to a target class with the default value.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefOf<V> ofValue(Class<T> targetClass, V defaultValue) {
        return (RefOf<V>)ofValue(null, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference to a target class with the default supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefOf<V> ofSupplier(Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefOf<V>)ofValue(null, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with default factory.
     **/
    public static <T> RefOf<T> of(String name, Class<T> targetClass) {
        return ofValue(name, targetClass, Preferability.Default, null);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default value.
     **/
    public static <T, V extends T> RefOf<T> ofValue(String name, Class<T> targetClass, V defaultValue) {
        return ofValue(name, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the given supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefOf<V> ofSupplier(String name, Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefOf<V>)ofValue(name, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference to a target class with the default value.
     **/
    public static <T, V extends T> RefOf<T> ofValue(Class<T> targetClass, Preferability preferability, V defaultValue) {
        return ofValue(null, targetClass, preferability, defaultValue);
    }
    
    /**
     * Create and return a reference to a target class with the given supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefOf<V> ofSupplier(Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return ofValue(null, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default vaule.
     **/
    public static <T, V extends T> RefOf<T> ofValue(String name, Class<T> targetClass, Preferability preferability, V defaultValue) {
        val theName    = whenNotNull(name).orElseGet(()->"#" + RefOf.getNewId());
        val theFactory = new Named.ValueSupplier<T>(defaultValue);
        return new RefOf<>(theName, targetClass, preferability, theFactory);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefOf<V> ofSupplier(String name, Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return ofValue(name, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
    
    //== For substitution =============================================================================================
    
    //-- Preference only --
    
    /** Create a provider that dictate the current. */
    default public Provider<T> butDictate() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Dictate, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Normal preferability. */
    default public Provider<T> butProvideNormally() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Normal, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Default preferability. */
    default public Provider<T> butDefault() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Default, currentProvider.getSupplier());
    }
    
    //-- but Preference + Value --
    
    /** Create a provider that dictate the given value. */
    default public <V extends T> Provider<T> butDictatedTo(V value) {
        return new Provider<>(this, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that dictate the value of the given ref. */
    default public <V extends T> Provider<T> butDictatedToA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Dictate, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that dictate the value of the given target class. */
    default public <V extends T> Provider<T> butDictatedToA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Dictate, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /** Create the provider that dictate the result of the given supplier. */
    default public <V extends T> Provider<T> butDictatedBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Dictate, supplier);
    }
    
    /** Create the provider (normal preferability) the given value. */
    default public <V extends T> Provider<T> butProvidedWith(V value) {
        return new Provider<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     */
    default public <V extends T> Provider<T> butProvidedWithA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given target class.
     */
    default public <V extends T> Provider<T> butProvidedWithA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (normal preferability) the result of the given
     * supplier.
     */
    default public <V extends T> Provider<T> butProvidedBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Normal, supplier);
    }
    
    /** Create the provider (using the given preferability) the given value. */
    default public <V extends T> Provider<T> butProvidedWith(Preferability preferability, V value) {
        return new Provider<>(this, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given ref.
     */
    default public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Ref<V> ref) {
        return new Provider<>(this, preferability, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given target class.
     */
    default public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Class<V> targetClass) {
        return new Provider<>(this, preferability, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (using the given preferability) the result of the
     * given supplier.
     */
    default public <V extends T> Provider<T> butProvidedBy(Preferability preferability, Supplier<V> supplier) {
        return new Provider<>(this, preferability, supplier);
    }
    
    /** Create the provider that default to the given value. */
    default public <V extends T> Provider<T> butDefaultedTo(V value) {
        return new Provider<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that default to the value of the given ref. */
    default public <V extends T> Provider<T> butDefaultedToA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that default to the value of the given target class. */
    default public <V extends T> Provider<T> butDefaultedToA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     */
    default public <V extends T> Provider<T> butDefaultedToBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Normal, supplier);
    }
    
}