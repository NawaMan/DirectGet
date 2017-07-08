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

import directget.run.Wrapper;
import lombok.experimental.ExtensionMethod;

/**
 * Instance of this class holds data for providing.
 * 
 * @author nawaman
 **/
@ExtensionMethod({ utils.class })
public class Providing<T> implements Supplier<T>, Wrapper {
    
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
	public Providing(Ref<T> ref, Preferability preferability, Supplier<? extends T> supplier) {
        this.ref = Objects.requireNonNull(ref);
        this.preferability = preferability._or(Preferability.Default);
        this.supplier = supplier._or((Supplier)()->null);
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
    public Runnable apply(Runnable t) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
