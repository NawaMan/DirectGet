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

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import directget.get.App;
import directget.get.HasRef;
import directget.get.Preferability;
import directget.get.Ref;
import directget.get.run.Named;
import directget.get.run.Wrapper;
import lombok.experimental.ExtensionMethod;

/**
 * Instance of this class can provide data.
 * 
 * @author NawaMan
 **/
@ExtensionMethod({ Utilities.class })
public class Provider<T> implements HasProvider<T>, HasRef<T>, Supplier<T>, Wrapper {
    
    private final Ref<T> ref;
    
    private final Preferability preferability;
    
    private final Supplier<T> supplier;
    
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
        this.ref = Objects.requireNonNull(ref);
        this.preferability = preferability._or(Preferability.Default);
        this.supplier = supplier._or((Supplier)()->null);
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
    
    public String toString() {
        return "Provider (" + preferability + ":" + ref + "): " + supplier;
    }

    @Override
    public Runnable apply(Runnable runnable) {
        return ()->{
            @SuppressWarnings("rawtypes")
            Stream<Provider> stream = Stream.of(this);
            App.Get().substitute(stream, runnable);
        };
    }
    
    /** @return the retainer. */
    public final ProviderWithRetainer<T> retained() {
        return new ProviderWithRetainer<T>(ref, preferability, supplier);
    }
    
    /** @return the singleton retainer. */
    public final ProviderWithRetainer<T> singleton() {
        return retained().globally().forAlways();
    }
    
    //== Wither =======================================================================================================
    
    /**
     * @return the new provider similar to this one except the preferability of dictate.
     **/
    public Provider<T> butDictate() {
        return new Provider<>(ref, Preferability.Dictate, supplier);
    }
    
    /**
     * @return the new provider similar to this one except the preferability of normal.
     **/
    public Provider<T> butNormal() {
        return new Provider<>(ref, Preferability.Normal, supplier);
    }
    
    /**
     * @return the new provider similar to this one except the preferability of default.
     **/
    public Provider<T> butDefault() {
        return new Provider<>(ref, Preferability.Default, supplier);
    }
    
    /**
     * @return the new provider similar to this one except with the value.
     **/
    public Provider<T> butWith(T value) {
        return new Provider<>(ref, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * @return the new provider similar to this one except with the value.
     **/
    public Provider<T> butWithA(Ref<T> ref) {
        return new Provider<>(ref, preferability, new Named.RefSupplier<T>(ref));
    }
    
    /** @return the new provider similar to this one except the supplied by the given supplier. **/
    public Provider<T> butBy(Supplier<T> supplier) {
        return new Provider<>(ref, preferability, supplier);
    }
    
}

