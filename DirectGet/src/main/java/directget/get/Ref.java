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

import static directget.get.Get.the;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import directget.get.exceptions.DefaultRefException;
import directget.get.run.Named;
import directget.get.run.Named.RefSupplier;
import directget.get.supportive.HasProvider;
import directget.get.supportive.ObjectFactory;
import directget.get.supportive.Provider;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import dssb.callerid.impl.CallerId;
import lombok.val;

/***
 * Ref is a reference to an object that we want to get.
 * 
 * @param <T>
 *            the type of the reference.
 * 
 * @author NawaMan
 */
public abstract class Ref<T> implements Supplier<T>, dssb.failable.Failable.Supplier<T, RuntimeException>, HasProvider<T>, Comparable<Ref<T>> {
    
    /** The default factory. */
    public static final RefTo<ObjectFactory> objectFactory = Ref.toValue(ObjectFactory.instance);
    
    
    private final Class<T> targetClass;
    
    private final String targetClassName;
    
    protected Ref(Class<T> targetClass) {
        this.targetClass     = targetClass;
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
    
    /**
     * The caller trace when create this Ref.
     * 
     * @return the caller ref.
     */
    public String getCallerTrace() {
        return "Unkown";
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
    public T getDefaultValue() {
        return the(objectFactory).make(this);
    }
    
    /** @return the current value for this ref. */
    public final T value() {
        return the(this);
    }
    
    /** @return the current value for this ref. */
    public final T get() {
        return value();
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
        return CallerId.instance.trace(caller->{
            return new RefOf<>(targetClass);
        });
    }
    
    // -- RefTo ---------------------------------------------------------------
    
    /**
     * Create and return a reference to a target class with default factory. 
     * 
     * @param targetClass 
     * @return the ref.
     **/
    public static <T> RefTo<T> to(Class<T> targetClass) {
        return CallerId.instance.trace(caller->{
            return toValue(null, targetClass, Preferability.Default, null).defaultedToBy(null);
        });
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
        return CallerId.instance.trace(caller->{
            return ((RefTo<T>)to(defaultValue.getClass())).defaultedTo(defaultValue);
        });
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
        return CallerId.instance.trace(caller->{
            val theName    = name == null ? "" : name;
            val theFactory = new Named.ValueSupplier<T>(defaultValue);
            return new RefTo<>(theName, targetClass, Preferability.Default, theFactory);
        });
    }
    
    /**
     * Create and return a reference to a target class with default factory.
     * 
     * @param targetClass 
     * @return a reference to the given class.
     **/
    @SuppressWarnings("unchecked")
    public static <T, V extends T> RefTo<T> toValueOf(Class<V> targetClass) {
        return (RefTo<T>)to(targetClass).defaultedToThe(Ref.of(targetClass));
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> RefTo<T> defaultOf(Class<T> targetClass) {
        Optional<RefTo> refOpt = defeaultRefs.get(targetClass);
        if (refOpt == null) {
            val ref = declaredDefaultOf(targetClass);
            refOpt = Optional.of((ref != null) ? ref : Ref.to(targetClass).defaultedToThe(Ref.of(targetClass)));
            defeaultRefs.put(targetClass, refOpt);
        }
        return refOpt.orElse(null);
    }
    
    /**
     * Returns the default Ref that was explicitly declared on the class.
     * 
     * @param targetClass  the target class.
     * @return  the default Ref that was declared.
     */
    @SuppressWarnings("unchecked")
    public static <T> RefTo<T> declaredDefaultOf(Class<T> targetClass) {
        for (val field : targetClass.getDeclaredFields()) {
            if (!Modifier.isFinal(field.getModifiers()))
                continue;
            if (!Modifier.isPublic(field.getModifiers()))
                continue;
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            if (!Ref.class.isAssignableFrom(field.getType()))
                continue;
            
            val actualType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            if(!actualType.equals(targetClass))
                continue;
            
            try {
                return (RefTo<T>)field.get(targetClass);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new DefaultRefException(e);
            }
        }
        return null;
    }


    //== For substitution =============================================================================================
    
    //-- Preference only --
    
    /**
     * Create a provider that dictate the current. 
     * 
     * @return a new provider pretty much like this one but dictate.
     **/
    public Provider<T> butDictate() {
        return CallerId.instance.trace(caller->{
            val currentProvider = Get.getProvider(this);
            val currentSupplier = currentProvider.getSupplier();
            return new Provider<>(this, Preferability.Dictate, currentSupplier);
        });
    }

    /**
     * Create a provider that provide the current with Normal preferability. 
     * 
     * @return a new provider pretty much like this one but provide.
     **/
    public Provider<T> butProvideNormally() {
        return CallerId.instance.trace(caller->{
            val currentProvider = Get.getProvider(this);
            val currentSupplier = currentProvider.getSupplier();
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }

    /**
     * Create a provider that provide the current with Default preferability.
     * 
     * @return a new provider pretty much like this one but default.
     **/
    public Provider<T> butDefault() {
        return CallerId.instance.trace(caller->{
            val currentProvider = Get.getProvider(this);
            val currentSupplier = currentProvider.getSupplier();
            return new Provider<>(this, Preferability.Default, currentSupplier);
        });
    }
    
    //-- but Preference + Value --
    
    /**
     * Create a provider that dictate the given value. 
     * 
     * @param value  the given value.
     * @return a new provider pretty much like this one but dictate to the given value.
     **/
    public <V extends T> Provider<T> butDictatedTo(V value) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.ValueSupplier<T>(value);
            return new Provider<>(this, Preferability.Dictate, currentSupplier);
        });
    }
    
    /**
     * Create the provider that dictate the value of the given ref. 
     * 
     * @param ref  the given ref.
     * @return a new provider pretty much like this one but dictate to a value of the given ref.
     **/
    public <V extends T> Provider<T> butDictatedToThe(Ref<V> ref) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(ref);
            return new Provider<>(this, Preferability.Dictate, currentSupplier);
        });
    }
    
    /**
     * Create the provider that dictate the value of the given target class. 
     * 
     * @param targetClass  the target class.
     * @return a new provider pretty much like this one but dictate to the value of the given target class.
     **/
    public <V extends T> Provider<T> butDictatedToThe(Class<V> targetClass) {
        return CallerId.instance.trace(caller->{
            RefSupplier<V> currentSupplier = new Named.RefSupplier<V>(Ref.of(targetClass));
            return new Provider<>(this, Preferability.Dictate, currentSupplier);
        });
    }
    
    /**
     * Create the provider that dictate the result of the given supplier. 
     * 
     * @param supplier  the supplier.
     * @return a new provider pretty much like this one but dictate to the value got from the supplier.
     **/
    public <V extends T> Provider<T> butDictatedBy(Supplier<V> supplier) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(this, Preferability.Dictate, supplier);
        });
    }
    
    /**
     * Create the provider (normal preferability) the given value. 
     * 
     * @param value  the given value.
     * @return a new provider pretty much like this one but provided with the given value.
     **/
    public <V extends T> Provider<T> butProvidedWith(V value) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.ValueSupplier<T>(value);
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     * 
     * @param ref  the given ref.
     * @return  a new provider pretty much like this one but provided with the value from the given ref.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Ref<V> ref) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(ref);
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider (normal preferability) the value of the given target class.
     * 
     * @param targetClass  the target class.
     * @return  a new provider pretty much like this one but provided with the value from the given target class. 
     */
    public <V extends T> Provider<T> butProvidedWithThe(Class<V> targetClass) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(Ref.of(targetClass));
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider (normal preferability) the result of the given supplier.
     * 
     * @param supplier  the supplier.
     * @return   a new provider pretty much like this one but provided with the value got from the supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Supplier<V> supplier) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(this, Preferability.Normal, supplier);
        });
    }
    
    /**
     * Create the provider (using the given preferability) the given value. 
     * 
     * @param preferability  the preferability.
     * @param value 
     * @return   a new provider pretty much like this one but with the given perferaability and the value.
     **/
    public <V extends T> Provider<T> butProvidedWith(Preferability preferability, V value) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.ValueSupplier<T>(value);
            return new Provider<>(this, preferability, currentSupplier);
        });
    }
    
    /**
     * Create the provider (using the given preferability) the value of the given ref.
     * 
     * @param preferability  the preferability. 
     * @param ref            the ref.
     * @return  the provider.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Preferability preferability, Ref<V> ref) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(ref);
            return new Provider<>(this, preferability, currentSupplier);
        });
    }
    
    /**
     * Create the provider (using the given preferability) the value of the given target class.
     * 
     * @param preferability  the preferability. 
     * @param targetClass    the target class.
     * @return  the provider.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Preferability preferability, Class<V> targetClass) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(Ref.of(targetClass));
            return new Provider<>(this, preferability, currentSupplier);
        });
    }
    
    /**
     * Create the provider (using the given preferability) the result of the given supplier.
     * 
     * @param preferability  the preferability. 
     * @param supplier       the supplier.
     * @return  the provider.
     */
    public <V extends T> Provider<T> butProvidedBy(Preferability preferability, Supplier<V> supplier) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(this, preferability, supplier);
        });
    }
    
    /**
     * Create the provider that default to the given value. 
     * 
     * @param value  the value.
     * @return  the provider.
     **/
    public <V extends T> Provider<T> butDefaultedTo(V value) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.ValueSupplier<T>(value);
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider that default to the value of the given ref. 
     * 
     * @param ref  the ref. 
     * @return  the provider.
     **/
    public <V extends T> Provider<T> butDefaultedToThe(Ref<V> ref) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(ref);
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider that default to the value of the given target class. 
     * 
     * @param targetClass  the target class.
     * @return  the provider.
     **/
    public <V extends T> Provider<T> butDefaultedToThe(Class<V> targetClass) {
        return CallerId.instance.trace(caller->{
            val currentSupplier = new Named.RefSupplier<V>(Ref.of(targetClass));
            return new Provider<>(this, Preferability.Normal, currentSupplier);
        });
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     * 
     * @param supplier  the supplier.
     * @return the provider.
     */
    public <V extends T> Provider<T> butDefaultedToBy(Supplier<V> supplier) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(this, Preferability.Normal, supplier);
        });
    }
    
}