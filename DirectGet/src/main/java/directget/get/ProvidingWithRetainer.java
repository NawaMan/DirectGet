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

import java.util.function.Supplier;

import directget.get.supportive.retain.Retainer;
import directget.get.supportive.retain.RetainerBuilder;
import directget.get.supportive.retain.WithRetainer;
import lombok.val;

/**
 * Providing with a retainer.
 * 
 * @author NawaMan
 *
 * @param <T>
 */
public class ProvidingWithRetainer<T> extends Providing<T> implements WithRetainer<T, ProvidingWithRetainer<T>> {

    /** Constructor. */
    public ProvidingWithRetainer(
            Ref<T>                ref, 
            Preferability         preferability, 
            Supplier<? extends T> supplier) {
        super(ref, preferability, supplier);
    }

    public Retainer<T> getRetainer() {
        val retainer
            = ((supplier instanceof Retainer)
            ? ((Retainer<T>)supplier)
            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
        return retainer;
    }

    public ProvidingWithRetainer<T> __but(Supplier<T> newSupplier) {
        if (newSupplier == supplier) {
            return this;
        }
        return new ProvidingWithRetainer<T>(ref, preferability, newSupplier);
    }
    
}