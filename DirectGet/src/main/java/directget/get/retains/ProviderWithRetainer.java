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
package directget.get.retains;

import java.util.function.Supplier;

import directget.get.Preferability;
import directget.get.Ref;
import directget.get.supportive.Provider;
import lombok.val;

/**
 * Provider with a retainer.
 * 
 * @author NawaMan
 *
 * @param <T>
 */
public class ProviderWithRetainer<T> extends Provider<T> implements WithRetainer<T, ProviderWithRetainer<T>> {

    /**
     * Constructor. 
     * 
     * @param ref            the ref.
     * @param preferability  the preferability.
     * @param supplier       the supplier.
     **/
    public ProviderWithRetainer(
            Ref<T>                ref, 
            Preferability         preferability, 
            Supplier<? extends T> supplier) {
        super(ref, preferability, supplier);
    }

    public Retainer<T> getRetainer() {
        val supplier = getSupplier();
        val retainer
            = ((supplier instanceof Retainer)
            ? ((Retainer<T>)supplier)
            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
        return retainer;
    }

    public ProviderWithRetainer<T> __but(Supplier<T> newSupplier) {
        val supplier = getSupplier();
        if (newSupplier == supplier) {
            return this;
        }
        val ref           = getRef();
        val preferability = getPreferability();
        return new ProviderWithRetainer<T>(ref, preferability, newSupplier);
    }
    
    static <T> Supplier<? extends T> getSupplierOf(Provider<T> provider) {
        return provider.getSupplier();
    }
    
}