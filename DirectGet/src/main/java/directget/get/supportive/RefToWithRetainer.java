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

import java.util.function.Supplier;

import directget.get.Preferability;
import directget.get.supportive.retain.Retainer;
import directget.get.supportive.retain.RetainerBuilder;
import directget.get.supportive.retain.WithRetainer;
import lombok.val;

/**
 * A direct ref with retainer.
 * 
 * @author NawaMan
 *
 * @param <T> the type of data this will be reference to.
 */
public class RefToWithRetainer<T> extends RefTo<T> implements WithRetainer<T, RefToWithRetainer<T>> {
    
    /** Constructor.
     * 
     * @param name           the name of the ref.
     * @param targetClass    the target class.
     * @param preferability  the preferability.
     * @param supplier       the supplier.
     **/
    public RefToWithRetainer(
            String name,
            Class<T> targetClass,
            Preferability preferability,
            Supplier<? extends T> supplier) {
        super(name, targetClass, preferability, supplier);
    }
    
    /** Returns the retainer. */
    public Retainer<T> getRetainer() {
        val supplier = getProvider().getSupplier();
        val retainer
            = ((supplier instanceof Retainer)
            ? ((Retainer<T>)supplier)
            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
        return retainer;
    }

    /** Change the supplier. */
    public RefToWithRetainer<T> __but(Supplier<T> newSupplier) {
        val supplier = getProvider().getSupplier();
        if (newSupplier == supplier) {
            return this;
        }
        return new RefToWithRetainer<>(this.getName(), this.getTargetClass(), this.getPreferability(), newSupplier);
    }
    
}
