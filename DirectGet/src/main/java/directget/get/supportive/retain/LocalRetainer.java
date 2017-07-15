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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.val;

/**
 * The local (thread) retainer.
 * 
 * @author NawaMan
 **/
public class LocalRetainer<V> extends Retainer<V> {
    
    private final ThreadLocal<Optional<V>> cache = ThreadLocal.withInitial(()->null);
    
    /** Constructs a global retainer. */
    public LocalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
        super(supplier, shouldRetain);
    }
    
    @Override
    public void reset() {
        cache.set(null);
    }
    
    /** Get the value. */
    @Override
    public V get() {
        if (cache.get() == null) {
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
            return new LocalRetainer<V>(supplier, showRetain);
        };
    }
    
    Retainer<V> newRetainer(Predicate<Predicate<V>> sameShouldRetain, Function<Predicate<V>, Predicate<V>> newShouldRetain) {
        if (sameShouldRetain.test(this.shouldRetain)) {
            return this;
        }
        return getCloner().apply(supplier, newShouldRetain.apply(this.shouldRetain));
    }
    
}