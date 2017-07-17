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

import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import directget.get.supportive.directget_internal_utilities;
import directget.run.Named;
import directget.run.Wrapper;
import lombok.experimental.ExtensionMethod;

/**
 * Instance of this class holds data for providing.
 * 
 * @author nawaman
 **/
@ExtensionMethod({ directget_internal_utilities.class })
public class Providing<T> implements Supplier<T>, Wrapper {
    
    final Ref<T> ref;
    
    final Preferability preferability;
    
    final Supplier<T> supplier;
    
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
	public Providing(Ref<T> ref, Preferability preferability, Supplier<? extends T> supplier) {
        this.ref = Objects.requireNonNull(ref);
        this.preferability = preferability._or(Preferability.Default);
        this.supplier = supplier._or((Supplier)()->null);
    }
    
    /**
     * Returns the supplier underlining this providing.
     * 
     * @return the supplier.
     */
    public final Supplier<T> getSupplier() {
        return this.supplier;
    }
    
    /** @return the reference for this providing. */
    public final Ref<T> getRef() {
        return ref;
    }
    
    @Override
    public T get() {
        return supplier.get();
    }
    
    /** @return the preferability for this providing. */
    public final Preferability getPreferability() {
        return preferability;
    }
    
    public String toString() {
        return "Providing (" + preferability + ":" + ref + "): " + supplier;
    }

    @Override
    public Runnable apply(Runnable runnable) {
        return ()->{
            @SuppressWarnings("rawtypes")
            Stream<Providing> stream = Stream.of(this);
            App.Get().substitute(stream, runnable);
        };
    }
    
    public final ProvidingWithRetainer<T> retained() {
        return new ProvidingWithRetainer<T>(ref, preferability, supplier);
    }
    
    //== Wither =======================================================================================================
    
    /**
     * @return the new providing similar to this one except the preferability of dictate.
     **/
    public Providing<T> butDictate() {
        return new Providing<>(ref, Preferability.Dictate, supplier);
    }
    
    /**
     * @return the new providing similar to this one except the preferability of normal.
     **/
    public Providing<T> butNormal() {
        return new Providing<>(ref, Preferability.Normal, supplier);
    }
    
    /**
     * @return the new providing similar to this one except the preferability of default.
     **/
    public Providing<T> butDefault() {
        return new Providing<>(ref, Preferability.Default, supplier);
    }
    
    /**
     * @return the new providing similar to this one except with the value.
     **/
    public Providing<T> butWith(T value) {
        return new Providing<>(ref, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * @return the new providing similar to this one except with the value.
     **/
    public Providing<T> butWithA(Ref<T> ref) {
        return new Providing<>(ref, preferability, new Named.RefSupplier<T>(ref));
    }
    
    /** @return the new providing similar to this one except the supplied by the given supplier. **/
    public Providing<T> butBy(Supplier<T> supplier) {
        return new Providing<>(ref, preferability, supplier);
    }
    
}

