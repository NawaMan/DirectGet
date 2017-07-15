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
package directget.get.supportive.retain;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import java.util.function.Predicate;
import lombok.val;

/**
 * The global retainer.
 * 
 * @author NawaMan
 **/
public class GlobalRetainer<V> extends Retainer<V> {
    
    private final AtomicReference<Optional<V>> cache = new AtomicReference<Optional<V>>(null);
    
    /** Constructs a global retainer. */
    public GlobalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
        super(supplier, shouldRetain);
    }
    
    @Override
    public void reset() {
        cache.set(null);
    }
    
    /** Get the value. */
    @Override
    public V get() {
        if (cache.compareAndSet(null, Optional.empty())) {
            // First time
            val newValue = supplier.get();
            cache.set(Optional.ofNullable(newValue));
            return newValue;
        }
        
        val oldValue = cache.get().orElse(null);
        if (shouldRetain.test(oldValue)) {
            return oldValue;
        }
        
        val newValue = supplier.get();
        cache.set(Optional.ofNullable(newValue));
        return newValue;
    }
    
    public BiFunction<Supplier<V>, Predicate<V>, Retainer<V>> getCloner() {
        return (supplier, showRetain)->{
            return new GlobalRetainer<V>(supplier, showRetain);
        };
    }
    
}