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


import static directget.get.Preferability.Default;
import static java.lang.String.format;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import directget.get.App;
import directget.get.Preferability;
import directget.get.Ref;
import directget.get.run.Named;
import directget.get.run.Wrapper;
import dssb.callerid.impl.CallerId;
import lombok.val;

/**
 * Instance of this class can provide data.
 * 
 * @author NawaMan
 * @param <T> the data type.
 **/
public class Provider<T> implements HasProvider<T>, Supplier<T>, Wrapper {
    
    private final Ref<T> ref;
    
    private final Preferability preferability;
    
    private final Supplier<T> supplier;
    
    private final String caller;
    
    /**
     * Constructor.
     * 
     * @param ref
     *            the reference.
     * @param preferability
     *            the level of preferability.
     * @param supplier
     *            the supplier to get the value.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Provider(Ref<T> ref, Preferability preferability, Supplier<? extends T> supplier) {
        this.caller        = CallerId.instance.caller().toString();
        this.ref           = Objects.requireNonNull(ref);
        this.preferability = preferability != null ? preferability      : Default;
        this.supplier      = supplier      != null ? (Supplier)supplier : (Supplier)()->null;
    }
    
    @Override
    public Provider<T> getProvider() {
        return this;
    }
    
    /**
     * Returns the supplier underlining this provider.
     * 
     * @return the supplier.
     */
    public final Supplier<T> getSupplier() {
        return this.supplier;
    }
    
    /** @return the reference for this provider. */
    public final Ref<T> getRef() {
        return ref;
    }
    
    @Override
    public T get() {
        return supplier.get();
    }
    
    /** @return the preferability for this provider. */
    public final Preferability getPreferability() {
        return preferability;
    }
    
    /**
     * Returns the caller that create this provider.
     * 
     * @return the caller stacktrace entry.
     */
    public String getCallerTrace() {
        return this.caller;
    }
    
    public String toString() {
        val preferabilityStr = preferability.toString();
        val refCaller        = this.ref.getCallerTrace();
        val providerCaller   = (this.caller != null) ? this.caller : "";
        val length           = Math.max(preferabilityStr.length(), Math.max(refCaller.length(), providerCaller.length()));
        val template         = "%-" + length + "s <--- %s";
        return "Provider { \n" 
                + format(template, preferability,  "preferability")  + "\n"
                + format(template, refCaller,      "Ref<" + this.ref.getName() + ":" + this.ref.getTargetClass() + ">") + "\n" 
                + format(template, providerCaller, supplier)
                + "\n}";
    }

    @Override
    public Runnable apply(Runnable runnable) {
        return ()->{
            @SuppressWarnings("rawtypes")
            Stream<Provider> stream = Stream.of(this);
            App.Get().substitute(stream, runnable);
        };
    }
    
    //== Wither =======================================================================================================
    
    /**
     * @return the new provider similar to this one except the preferability of dictate.
     **/
    public Provider<T> butDictate() {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, Preferability.Dictate, supplier);
        });
    }
    
    /**
     * @return the new provider similar to this one except the preferability of normal.
     **/
    public Provider<T> butNormal() {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, Preferability.Normal, supplier);
        });
    }
    
    /**
     * @return the new provider similar to this one except the preferability of default.
     **/
    public Provider<T> butDefault() {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, Preferability.Default, supplier);
        });
    }
    
    /**
     * Return the modified provider from this provider to provide the given value.
     * 
     * @param value  the given value.
     * @return the new provider similar to this one except with the value.
     **/
    public Provider<T> butWith(T value) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, preferability, new Named.ValueSupplier<T>(value));
        });
    }
    
    /**
     * Return the modified provider from this provider to provide the value from the given ref.
     * 
     * @param ref  the given ref.
     * @return the new provider similar to this one except with the value.
     **/
    public Provider<T> butWithThe(Ref<T> ref) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, preferability, new Named.RefSupplier<T>(ref));
        });
    }
    
    /**
     * Return the modified provider from this provider to provide the value from the given supplier.
     * 
     * @param supplier  the given supplier.
     * @return the new provider similar to this one except the supplied by the given supplier.
     **/
    public Provider<T> butBy(Supplier<T> supplier) {
        return CallerId.instance.trace(caller->{
            return new Provider<>(ref, preferability, supplier);
        });
    }
    
}

