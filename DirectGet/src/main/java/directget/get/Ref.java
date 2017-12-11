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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import directcommon.common.Nulls;
import directget.get.exceptions.DefaultRefException;
import directget.get.run.Named;
import directget.get.supportive.HasProvider;
import directget.get.supportive.Provider;
import directget.get.supportive.RefFactory;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import directget.get.supportive.RefWithSubstitute;
import lombok.val;
import lombok.experimental.Delegate;
import lombok.experimental.ExtensionMethod;

// TODO - Record where Ref and its provider are created.

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T>
 *            the type of the reference.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class })
public abstract class Ref<T> implements HasProvider<T>, Comparable<Ref<T>> {
    
    /** The default factory. */
    public static final RefTo<RefFactory> refFactory = Ref.toValue(new RefFactory());
    
    
    private final Class<T> targetClass;
    
    private final String targetClassName;
    
    @Delegate
    private final RefWithSubstitute<T> withSubstitute = new RefWithSubstitute<>(this, ()->Get.getProvider(this));
    
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
     * 
     * @return the name.
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
    public T getValue() {
        return Get.the(refFactory).make(this);
    }
    
    /** @return the optional default object. */
    public Optional<T> _getValue() {
        return Optional.ofNullable(getValue());
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
    
    // == Factory method ======================================================
    
    // -- RefOf ---------------------------------------------------------------
    
    /**
     * Create a RefOf of a class.
     * 
     * @param targetClass 
     * @return the reference that represent the target class directly.
     **/
    public static <T> RefOf<T> of(Class<T> targetClass) {
        return new RefOf<>(targetClass);
    }
    
    // -- RefTo ---------------------------------------------------------------
    
    /**
     * Create and return a reference to a target class with default factory. 
     * 
     * @param targetClass 
     * @return the ref.
     **/
    public static <T> RefTo<T> to(Class<T> targetClass) {
        return toValue(null, targetClass, Preferability.Default, null).defaultedToBy(null);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with default factory.
     * 
     * @param name 
     * @param targetClass 
     * @return the ref.
     **/
    public static <T> RefTo<T> to(String name, Class<T> targetClass) {
        return toValue(name, targetClass, Preferability.Default, null);
    }
    
    //-- with default supplier --
    
    /**
     * Create and return a reference to a target class with the default supplier.
     * 
     * @param targetClass 
     * @param valueSupplier 
     * @return the ref.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> to(Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefTo<V>)toValue(null, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the given supplier.
     * 
     * @param name 
     * @param targetClass 
     * @param valueSupplier 
     * @return the ref.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> to(String name, Class<T> targetClass, Supplier<V> valueSupplier) {
        return (RefTo<V>)toValue(name, (Class<V>)targetClass, Preferability.Default, (V)null)
                .defaultedToBy(valueSupplier);
    }
    
    //-- with other preferability supplier --
    
    /**
     * Create and return a reference to a target class with the given supplier.
     * 
     * @param targetClass 
     * @param preferability 
     * @param valueSupplier 
     * @return the ref.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> to(Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return toValue(null, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default supplier.
     * 
     * @param name 
     * @param targetClass 
     * @param preferability 
     * @param valueSupplier
     * @return the ref.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<V> to(String name, Class<T> targetClass, Preferability preferability, Supplier<V> valueSupplier) {
        return toValue(name, (Class<V>)targetClass, preferability, (V)null).defaultedToBy(valueSupplier);
    }
//    
//    //-- with default value --
//    
    /**
     * Create and return a reference to the default value's class default to the default factory. 
     * 
     * @param defaultValue 
     * @return the ref.
     **/
    @SuppressWarnings("unchecked")
    public static <T> RefTo<T> toValue(T defaultValue) {
        return ((RefTo<T>)to(defaultValue.getClass())).defaultedTo(defaultValue);
    }
    
    /**
     * Create and return a reference to the default value's class default to the default factory. 
     * 
     * @param name 
     * @param defaultValue 
     * @return the ref.
     **/
    public static <T> RefTo<T> toValue(String name, T defaultValue) {
        @SuppressWarnings("unchecked")
        val targetClass = (Class<T>)defaultValue.getClass();
        return toValue(name, targetClass, Preferability.Default, defaultValue);
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default value.
     **/
    private static <T, V extends T> RefTo<T> toValue(String name, Class<T> targetClass, Preferability preferability, V defaultValue) {
        val theName    = (String)name.whenNotNull().orElseGet(()->"#" + RefTo.getNewId());
        val theFactory = new Named.ValueSupplier<T>(defaultValue);
        return new RefTo<>(theName, targetClass, Preferability.Default, theFactory);
    }

    /**
     * Create and return a reference to a target class with default factory.
     * 
     * @param targetClass 
     * @return a reference to the given class.
     **/
    public static <T> RefTo<T> toValueOf(Class<T> targetClass) {
        return to(targetClass).defaultedToA(Ref.of(targetClass));
    }
    
    //-- factory --
    
    /**
     * Create and return a reference to a target factory class with the default factory.
     * 
     * @param valueFactory 
     * @return a reference to the factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> toFactory(Factory<V> valueFactory) {
        Ref ref = to(Factory.class).defaultedTo(valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    /**
     * Create and return a reference with a human readable name to a target factory class with 
     *   the given supplier.
     * 
     * @param name 
     * @param targetClass 
     * @param valueFactory 
     * @return a reference to the factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> toFactory(String name, Class<T> targetClass, Factory<V> valueFactory) {
        Ref ref = to(name, Factory.class).defaultedTo(valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    //-- with other preferability factory --
    
    /**
     * Create and return a reference to a target factory class with the default factory.
     * 
     * @param targetClass 
     * @param preferability 
     * @param valueFactory 
     * @return a reference to the factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> toFactory(Class<T> targetClass, Preferability preferability, Factory<V> valueFactory) {
        Ref ref = to(Factory.class).providedWith(preferability, valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    /**
     * Create and return a reference with a human readable name to a target
     * class with the default factory.
     * 
     * @param name 
     * @param targetClass 
     * @param preferability 
     * @param valueFactory 
     * @return a reference to the factory.
     **/
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T, V extends T> RefTo<Factory<T>> toFactory(String name, Class<T> targetClass, Preferability preferability, Factory<V> valueFactory) {
        Ref ref = to(name, Factory.class).providedWith(preferability, valueFactory);
        return (RefTo<Factory<T>>)ref;
    }
    
    //== Default ==============================================================

    private static final String refToClassName = RefTo.class.getCanonicalName();
    
    @SuppressWarnings({ "rawtypes" })
    private static final ConcurrentHashMap<Class, Optional<RefTo>> defeaultRefs = new ConcurrentHashMap<>();

    // NOTE: Put it here to use 'Ref' as a namespace.
    
    /**
     * This annotation is used to mark a static field of Ref of the same class,
     *   so that its value is used as a default ref when Get.the(class).
     */
    @Target(value=ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Default {
    }

    
    /**
     * Returns the default reference of the given class.
     * 
     * @param targetClass the target class.
     * @return the default reference or {@code null} if non exist.
     */
    @SuppressWarnings("unchecked")
    public static <T> RefTo<T> defaultOf(Class<T> targetClass) {
        @SuppressWarnings("rawtypes")
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
                
                // This is hacky -- but no better way now.
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
            refOpt = (refOpt != null) ? refOpt : Optional.of(Ref.to(targetClass).defaultedToA(Ref.of(targetClass)));
            defeaultRefs.put(targetClass, refOpt);
        }
        return refOpt.orElse(null);
    }
    
}