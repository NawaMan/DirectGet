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
package directget.get.supportive.retain;

import static directget.get.Get.the;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import directget.get.Ref;
import lombok.val;

/**
 * Retain checker that check another if the current value equals to the previous.
 * 
 * @author NawaMan
 *
 * @param <T> the type to watch for.
 * @param <V> the type that is retained.
 */
public class ForEquivalentRetainChecker<T, V> implements Predicate<V> {
    
    private final Ref<T> ref;
    
    private final AtomicReference<T> refValue;
    
    /**
     * Constructor.
     * 
     * @param ref the reference ref.
     */
    public ForEquivalentRetainChecker(Ref<T> ref) {
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
        val isEquivalent = Objects.equals(newValue, refValue.get());
        if (!isEquivalent) {
            refValue.set(newValue);
        }
        return isEquivalent;
    }
}
    