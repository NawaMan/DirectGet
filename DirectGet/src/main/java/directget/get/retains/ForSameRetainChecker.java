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

import static directget.get.Get.the;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import directget.get.Ref;
import directget.get.supportive.RefTo;
import lombok.val;


/**
 * Retain checker that check another if the current value same to the previous.
 * 
 * @author NawaMan
 *
 * @param <T> the type to watch for.
 * @param <V> the type that is retained.
 */
public class ForSameRetainChecker<T, V> implements Predicate<V> {
    
    private final RefTo<T> ref;
    
    private final AtomicReference<T> refValue;
    
    /**
     * Constructor.
     * 
     * @param ref the reference ref.
     */
    public ForSameRetainChecker(RefTo<T> ref) {
        this.ref = ref;
        refValue = new AtomicReference<T>(the(ref));
    }

    /** @return the reference ref.  **/
    public Ref<T> getRef() {
        return ref;
    }

    @Override
    public boolean test(V value) {
        val newValue = the(ref);
        val isSame = newValue == refValue.get();
        if (!isSame) {
            refValue.set(newValue);
        }
        return isSame;
    }
    
}