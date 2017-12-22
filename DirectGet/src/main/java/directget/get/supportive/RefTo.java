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

import static directget.get.Get.the;
import static directget.get.supportive.Caller.trace;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import directget.get.Preferability;
import directget.get.Ref;
import directget.get.run.Named;
import directget.get.supportive.Caller.Capture;
import lombok.val;

/**
 * This reference implementation allows multiple references to a class to
 * mean different things.
 * 
 * @author NawaMan
 * @param <T>  the type of the data this ref is refering to.
 **/
public class RefTo<T> extends Ref<T> {
    
    private final String name;
    
    private final Provider<T> provider;
    
    private final String caller;
    
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
    public RefTo(String name, Class<T> targetClass, Preferability preferability, Supplier<? extends T> factory) {
        super(targetClass);
        AtomicReference<String>        theName          = new AtomicReference<>();
        AtomicReference<Provider<T>>   theProvider      = new AtomicReference<>();
        this.caller = Caller.trace(Capture.Continue, (String caller)->{
            val prefer  = (preferability != null) ? preferability : Preferability.Default;
            theName    .set(name != null ? name : "");
            theProvider.set((factory == null) ? null : new Provider<>(RefTo.this, prefer, factory));
            return caller;
        });
        this.name     = theName.get();
        this.provider = theProvider.get();
    }
    
    /** @return the name of the reference */
    public String getName() {
        return this.name;
    }
    
    /** @return the default object. */
    @Override
    public final T getDefaultValue() {
        if (provider == null) {
            return super.getDefaultValue();
        } else {
            return provider.get();
        }
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
    
    /** {@inheritDoc} */ @Override
    public String getCallerTrace() {
        return this.caller;
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
        return "RefTo<" + this.name + ":" + this.getTargetClass().getName() + ">" + " at {\n" + this.caller + "\n}";
    }
    
    /** @return the retainer. */
    public RefToWithRetainer<T> retained() {
        return new RefToWithRetainer<>(name, getTargetClass(), getPreferability(), provider.getSupplier());
    }
    
    //== Modifier =====================================================================================================
    
    /**
     * Create a ref with a dictating provide that dictate the given value. 
     * 
     * @param value  the value to be dictated as.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> dictatedTo(V value) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Dictate, new Named.ValueSupplier<T>(value));
        });
    }
    
    /**
     * Create a ref with a dictating provide the value of the given ref. 
     * 
     * @param ref  the reference.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> dictatedToThe(Ref<V> ref) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Dictate, new Named.RefSupplier<V>(ref));
        });
    }
    
    /**
     * Create a ref with a dictating provide the result of the given supplier. 
     * 
     * @param supplier  the supplier.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> dictatedBy(Supplier<V> supplier) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Dictate, supplier);
        });
    }
    
    /**
     * Create a ref with a dictating provide that use the orgRef and the mapper.
     * 
     * @param orgRef  the original Ref.
     * @param mapper  the mapper to get the value.
     * @return  a new ref with the dictated provider.
     */
    public <F, V extends T> RefTo<T> dictatedUsing(Ref<F> orgRef, Function<F, V> mapper) {
        return this.dictatedBy(()->{
            F value = the(orgRef);
            V mapped = mapper.apply(value);
            return mapped;
        });
    }
    
    /**
     * Create the ref (normal preferability) the given value. 
     * 
     * @param value  the value.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> providedWith(V value) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, new Named.ValueSupplier<T>(value));
        });
    }
    
    /**
     * Create the ref with the provider (normal preferability) that the value of the given ref.
     * 
     * @param ref  the ref.
     * @return  the RefTo.
     */
    public <V extends T> RefTo<T> providedWithThe(Ref<V> ref) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, new Named.RefSupplier<V>(ref));
        });
    }
    
    /**
     * Create the ref with the provider (normal preferability) that the result of the given supplier.
     * 
     * @param supplier  the supplier.
     * @return  the RefTo.
     */
    public <V extends T> RefTo<T> providedBy(Supplier<V> supplier) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, supplier);
        });
    }
    
    /**
     * Create a ref with a provide that use the orgRef and the mapper.
     * 
     * @param orgRef  the original Ref.
     * @param mapper  the mapper to get the value.
     * @return  a new ref with the dictated provider.
     */
    public <F, V extends T> RefTo<T> providedUsing(Ref<F> orgRef, Function<F, V> mapper) {
        return this.providedBy(()->{
            F value = the(orgRef);
            V mapped = mapper.apply(value);
            return mapped;
        });
    }
    
    /**
     * Create the ref with the provider (using the given preferability) of the given value. 
     * 
     * @param preferability  the preferability.
     * @param value          the value.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> providedWith(Preferability preferability, V value) {
        return trace(Capture.Continue, caller->{
            return providedBy(preferability, new Named.ValueSupplier<T>(value));
        });
    }
    
    /**
     * Create the ref with the provider (using the given preferability) that the value of the given ref.
     * 
     * @param preferability  the preferability.
     * @param ref            the ref.
     * @return  the RefTo.
     */
    public <V extends T> RefTo<T> providedWithThe(Preferability preferability, Ref<V> ref) {
        return trace(Capture.Continue, caller->{
            return providedBy(preferability, new Named.RefSupplier<V>(ref));
        });
    }
    
    /**
     * Create the ref with the provider (using the given preferability) the result of the given supplier.
     * 
     * @param preferability  the preferability.
     * @param supplier       the supplier.
     * @return  the RefTo.
     */
    public <V extends T> RefTo<T> providedBy(Preferability preferability, Supplier<V> supplier) {
        return trace(Capture.Continue, caller->{
            val name        = this.getName();
            val targetClass = this.getTargetClass();
            return new RefTo<>(name, targetClass, preferability, supplier);
        });
    }
    
    /**
     * Create a ref with a provide that use the orgRef and the mapper.
     * 
     * @param preferability  the preferability. 
     * @param orgRef         the original Ref.
     * @param mapper         the mapper to get the value.
     * @return  a new ref with the dictated provider.
     */
    public <F, V extends T> RefTo<T> providedUsing(Preferability preferability, Ref<F> orgRef, Function<F, V> mapper) {
        return this.providedBy(preferability, ()->{
            F value = the(orgRef);
            V mapped = mapper.apply(value);
            return mapped;
        });
    }
    
    /**
     * Create the ref with the provider that default to the given value. 
     * 
     * @param value  the value.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> defaultedTo(V value) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, new Named.ValueSupplier<T>(value));
        });
    }
    
    /**
     * Create the ref with the provider that default to the value of the given ref. 
     * 
     * @param ref  the ref.
     * @return  the RefTo.
     **/
    public <V extends T> RefTo<T> defaultedToThe(Ref<V> ref) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, new Named.RefSupplier<V>(ref));
        });
    }
    
    /**
     * Create the ref with the provider that default to the result of the given supplier.
     * 
     * @param supplier  the supplier.
     * @return  the RefTo.
     */
    public <V extends T> RefTo<T> defaultedToBy(Supplier<V> supplier) {
        return trace(Capture.Continue, caller->{
            return providedBy(Preferability.Normal, supplier);
        });
    }
    
    /**
     * Create a ref with a default provide that use the orgRef and the mapper.
     * 
     * @param orgRef  the original Ref.
     * @param mapper  the mapper to get the value.
     * @return  a new ref with the dictated provider.
     */
    public <F> RefTo<T> defaultedUsing(Ref<F> orgRef, Function<F, T> mapper) {
        return this.defaultedToBy(()->{
            F value = the(orgRef);
            T mapped = mapper.apply(value);
            return mapped;
        });
    }

}