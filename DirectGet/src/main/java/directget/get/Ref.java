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

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import directget.get.exceptions.DefaultRefException;
import directget.get.run.Named;
import directget.get.supportive.HasProvider;
import directget.get.supportive.Provider;
import directget.get.supportive.RefFactory;
import directget.get.supportive.RefFor;
import directget.get.supportive.RefTo;
import lombok.val;

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T>
 *            the type of the reference.
 * 
 * @author NawaMan
 */
public abstract class Ref<T> implements HasProvider<T>, HasRef<T>, Comparable<Ref<T>> {
    
    /** The default factory. */
    public static final RefTo<RefFactory> refFactory = Ref.ofValue(new RefFactory());

    private static final String refToClassName = RefTo.class.getCanonicalName();
    
    @SuppressWarnings({ "rawtypes" })
    private static final ConcurrentHashMap<Class, Optional<RefTo>> defeaultRefs = new ConcurrentHashMap<>();
    
    
    private final Class<T> targetClass;
    
    private final String targetClassName;
    
    protected Ref(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.targetClassName = this.targetClass.getCanonicalName();
    }
    
    /** @return this reference **/
    public final Ref<T> getRef() {
        return this;
    }

    /**
     * The name of the reference.
     * 
     * This value is for the benefit of human who look at it. There is no use in
     * the program in anyway (except debugging/logging/auditing purposes).
     **/
    public String getName() {
        return this.targetClassName;
    }

    /** @return the class of the interested object. */
    public final Class<T> getTargetClass() {
        return this.targetClass;
    }
    
    /** {@inheritDoc} */ @Override
    public String toString() {
        return "Ref<" + this.targetClassName + ">";
    }
    
    /** {@inheritDoc} */ @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
    
    /** {@inheritDoc} */ @Override
    public int hashCode() {
        return this.targetClass.hashCode();
    }
    
    /** @return the default object. */
    public T get() {
        return Get.the(refFactory).make(this);
    }
    
    /** @return the optional default object. */
    public Optional<T> _get() {
        return Optional.ofNullable(get());
    }
    
    /** @return the providers for the default value */
    public abstract Provider<T> getProvider();
    
    /**
     * @return the compare result of between this Ref and the given reference.
     */
    public int compareTo(Ref<T> o) {
        if (o == null) {
            return Integer.MAX_VALUE;
        }
        
        if (this.equals(o)) {
            return 0;
        }
        
        return this.toString().compareTo(o.toString());
    }
    
    /**
     * Returns the default reference of the given class.
     * 
     * @param targetClass the target class.
     * @return the default reference or {@code null} if non exist.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> RefTo<T> defaultOf(Class<T> targetClass) {
        Optional<RefTo> refOpt = defeaultRefs.get(targetClass);
        if (refOpt == null) {
            for (val field : targetClass.getDeclaredFields()) {
                if (!Modifier.isFinal(field.getModifiers()))
                    continue;
                if (!Modifier.isPublic(field.getModifiers()))
                    continue;
                if (!Modifier.isStatic(field.getModifiers()))
                    continue;
                if (!Ref.class.isAssignableFrom(field.getType()))
                    continue;
                val targetClassName  = targetClass.getName();
                val expectedTypeName = refToClassName + "<" + targetClassName + ">";
                val actualTypeName   = field.getGenericType().getTypeName();
                if(!actualTypeName.equals(expectedTypeName))
                    continue;
                
                try {
                    refOpt = Optional.of((RefTo<T>)field.get(targetClass));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new DefaultRefException(e);
                }
            }
            refOpt = (refOpt != null) ? refOpt :Optional.empty();
            defeaultRefs.put(targetClass, refOpt);
        }
        return refOpt.orElse(null);
    }
    
    // == Basic implementations ===============================================
    
    // -- RefFor --------------------------------------------------------------
    
    /** @return the reference that represent the target class directly. */
    public static <T> RefFor<T> forClass(Class<T> targetClass) {
        return new RefFor<>(targetClass);
    }
    
    // -- RefTo ---------------------------------------------------------------
    
    /** Create and return a reference to a target class with default factory. **/
    public static <T> RefTo<T> of(Class<T> targetClass) {
        return ofValue(null, targetClass, Preferability.Default, null).defaultedToBy(null);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with default factory.
     **/
    public static <T> RefTo<T> of(String name, Class<T> targetClass) {
        return ofValue(name, targetClass, Preferability.Default, null);
    }
    
    //-- with default supplier --
    
    /**
     * Create and return a reference to a target class with the default supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> of(Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefTo<V>)ofValue(null, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the given supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> of(String name, Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefTo<V>)ofValue(name, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    //-- with other preferability supplier --
    
    /**
     * Create and return a reference to a target class with the given supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> of(Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return ofValue(null, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default supplier.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> of(String name, Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return ofValue(name, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
//    
//    //-- with default value --
//    
    /** Create and return a reference to the default value's class default to the default factory. **/
    @SuppressWarnings("unchecked")
    public static <T> RefTo<T> ofValue(T defaultValue) {
        return ((RefTo<T>)of(defaultValue.getClass())).defaultedTo(defaultValue);
    }
    
    /** Create and return a reference to the default value's class default to the default factory. **/
    public static <T> RefTo<T> ofValue(String name, T defaultValue) {
        @SuppressWarnings("unchecked")
        val targetClass = (Class<T>)defaultValue.getClass();
        return ofValue(name, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default value.
     **/
    private static <T, V extends T> RefTo<T> ofValue(String name, Class<T> targetClass, Preferability preferability, V defaultValue) {
        val theName    = whenNotNull(name).orElseGet(()->"#" + RefTo.getNewId());
        val theFactory = new Named.ValueSupplier<T>(defaultValue);
        return new RefTo<>(theName, targetClass, Preferability.Default, theFactory);
    }
    
    //-- factory --
    
    /**
     * Create and return a reference to a target factory class with the default factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> ofFactory(Factory<V> valueFactory) {
        Ref ref = of(Factory.class).defaultedTo(valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * factory class with the given supplier.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> ofFactory(String name, Class<T> targetClass, Factory<V> valueFactory) {
        Ref ref = of(name, Factory.class).defaultedTo(valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    //-- with other preferability factory --
    
    /**
     * Create and return a reference to a target factory class with the default factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> ofFactory(Class<T> targetClass, Preferability preferability, Factory<V> valueFactory) {
        Ref ref = of(Factory.class).providedWith(preferability, valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> ofFactory(String name, Class<T> targetClass, Preferability preferability, Factory<V> valueFactory) {
        Ref ref = of(name, Factory.class).providedWith(preferability, valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    
    
    //== For substitution =============================================================================================
    
    //-- Preference only --
    
    /** Create a provider that dictate the current. */
    public Provider<T> butDictate() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Dictate, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Normal preferability. */
    public Provider<T> butProvideNormally() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Normal, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Default preferability. */
    public Provider<T> butDefault() {
        val currentProvider = Get.getProvider(this);
        return new Provider<>(this, Preferability.Default, currentProvider.getSupplier());
    }
    
    //-- but Preference + Value --
    
    /** Create a provider that dictate the given value. */
    public <V extends T> Provider<T> butDictatedTo(V value) {
        return new Provider<>(this, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that dictate the value of the given ref. */
    public <V extends T> Provider<T> butDictatedToA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Dictate, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that dictate the value of the given target class. */
    public <V extends T> Provider<T> butDictatedToA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Dictate, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /** Create the provider that dictate the result of the given supplier. */
    public <V extends T> Provider<T> butDictatedBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Dictate, supplier);
    }
    
    /** Create the provider (normal preferability) the given value. */
    public <V extends T> Provider<T> butProvidedWith(V value) {
        return new Provider<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     */
    public <V extends T> Provider<T> butProvidedWithA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given target class.
     */
    public <V extends T> Provider<T> butProvidedWithA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (normal preferability) the result of the given
     * supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Normal, supplier);
    }
    
    /** Create the provider (using the given preferability) the given value. */
    public <V extends T> Provider<T> butProvidedWith(Preferability preferability, V value) {
        return new Provider<>(this, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given ref.
     */
    public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Ref<V> ref) {
        return new Provider<>(this, preferability, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given target class.
     */
    public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Class<V> targetClass) {
        return new Provider<>(this, preferability, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (using the given preferability) the result of the
     * given supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Preferability preferability, Supplier<V> supplier) {
        return new Provider<>(this, preferability, supplier);
    }
    
    /** Create the provider that default to the given value. */
    public <V extends T> Provider<T> butDefaultedTo(V value) {
        return new Provider<>(this, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that default to the value of the given ref. */
    public <V extends T> Provider<T> butDefaultedToA(Ref<V> ref) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that default to the value of the given target class. */
    public <V extends T> Provider<T> butDefaultedToA(Class<V> targetClass) {
        return new Provider<>(this, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     */
    public <V extends T> Provider<T> butDefaultedToBy(Supplier<V> supplier) {
        return new Provider<>(this, Preferability.Normal, supplier);
    }
    
}