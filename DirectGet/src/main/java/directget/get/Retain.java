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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.Timer;

import lombok.val;

/**
 * This utility class make it easy to create supplier with cache for its value.. 
 * 
 * @author nawaman
 */
public class Retain {
    
    /** Ref for current thread. */
    final static Ref<Thread> currentThread
            = Ref.of(Thread.class, ()->Thread.currentThread());
    
    /** Ref for current time in milliseconds. */
    final static Ref<Long> currentTimeMillis
            = Ref.of(Long.class, ()->Long.valueOf(System.currentTimeMillis()));
    
    /**
     * Create a retainer builder for the given supplier.
     * @return the retainer builder.
     */
    public static <V> RetainerBuilder<V> valueOf(Supplier<V> supplier) {
        return new RetainerBuilder<>(supplier);
    }
    /**
     * Create a retainer builder for the given supplier.
     * @return the retainer builder.
     */
    public static <V> RetainerBuilder<V> retain(Supplier<V> supplier) {
        return new RetainerBuilder<>(supplier);
    }
    
    /** The retainer builder. */
    public static class RetainerBuilder<V> {
        
        private static Predicate<?> ALWAYS = value->true;
        
        private static Predicate<?> NEVER = value->false;
        
        private boolean isGlobal = false;
        
        private final Supplier<V> supplier;
        
        /** Construct a retainer builder. */
        public RetainerBuilder(Supplier<V> supplier) {
            this.supplier = supplier;
        }
        
        /** Make the retainer global. */
        public RetainerBuilder<V> globally() {
            isGlobal = true;
            return this;
        }
        
        /** Make the retainer local. */
        public RetainerBuilder<V> locally() {
            isGlobal = false;
            return this;
        }
        
        /** Create and return a supplier that cache its value forever. */
        @SuppressWarnings("unchecked")
        public Supplier<V> always() {
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, (Predicate<V>)ALWAYS)
                    : new LocalRetainer<V>(supplier,  (Predicate<V>)ALWAYS);
        }
        
        /** Create and return a supplier that cache its value forever. */
        @SuppressWarnings("unchecked")
        public Supplier<V> never() {
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, (Predicate<V>)NEVER)
                    : new LocalRetainer<V>(supplier,  (Predicate<V>)NEVER);
        }
        
        /** Create and return a supplier that cache its value for each thread. */
        public Supplier<V> forCurrentThread() {
            return locally().always();
        }
        
        /** 
         * Create and return a supplier that cache its value until the value of the given ref is not the same
         *   the last call.
         **/
        public <T> Supplier<V> forSame(Ref<T> ref) {
            val refValue = new AtomicReference<T>(Get.a(ref));
            Predicate<V> shouldRetain = value->{
                val newValue = Get.a(ref);
                val isSame = newValue == refValue.get();
                if (!isSame) {
                    refValue.set(newValue);
                }
                return isSame;
            };
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, shouldRetain)
                    : new LocalRetainer<V>(supplier,  shouldRetain);
        }
        
        /** 
         * Create and return a supplier that cache its value until the value of the given ref does not equal
         *   the last call.
         **/
        public <T> Supplier<V> forEquivalent(Ref<T> ref) {
            val refValue = new AtomicReference<T>(Get.a(ref));
            Predicate<V> shouldRetain = value->{
                val newValue = Get.a(ref);
                val isEquivalent = Objects.equals(newValue, refValue.get());
                if (!isEquivalent) {
                    refValue.set(newValue);
                }
                return isEquivalent;
            };
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, shouldRetain)
                    : new LocalRetainer<V>(supplier,  shouldRetain); 
        }
        
        /** Create and return a supplier that cache its value until  for a specific amount of time. **/
        public <T> Supplier<V> forTime(long time) {
            return forTime(time, TimeUnit.MICROSECONDS);
        }
        
        /** Create and return a supplier that cache its value until for a specific amount of time. **/
        public <T> Supplier<V> forTime(long time, TimeUnit unit) {
            val expiredValue = new AtomicLong(Get.a(currentTimeMillis) + unit.toMillis(time));
            Predicate<V> shouldRetain = value->{
                val currentTime = Get.a(currentTimeMillis);
                val hasExpires = currentTime >= expiredValue.get();
                if (hasExpires) {
                    expiredValue.set(Get.a(currentTimeMillis) + unit.toMillis(time));
                }
                return !hasExpires;
            };
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, shouldRetain)
                    : new LocalRetainer<V>(supplier,  shouldRetain); 
        }
        
        /** Create and return a supplier that cache its value which is reset after a specific time. **/
        public <T> Supplier<V> expireAfter(long time) {
            return expireAfter(time, TimeUnit.MICROSECONDS);
        }
        
        /**
         * Create and return a supplier that cache its value which is reset after a specific time.
         * 
         * Note: This retainer actively reset the cache. It only allow for global retainer.
         * NOTE: For local retainer, this method will return forTime retainer. 
         **/
        @SuppressWarnings("unchecked")
        public <T> Supplier<V> expireAfter(long time, TimeUnit unit) {
            if (!isGlobal) {
                return forTime(time, unit);
            }
            
            val retanerRef = new AtomicReference<Retainer<V>>();
            val timer = new Timer((int) unit.toMillis(time), evt-> retanerRef.get().reset() );
            timer.setRepeats(true);
            retanerRef.set(new GlobalRetainer<V>(supplier, (Predicate<V>)ALWAYS));
            timer.start();
            return retanerRef.get();
        }
        
    }
    
    //== Supplementary class ==========================================================================================
    
    /** The retainer. */
    public static interface Retainer<V> extends Supplier<V>  {
        
        /** Reset the value */
        public void reset();
        
        public V get();
        
    }
    
    /** The global retainer. */
    public static class GlobalRetainer<V> implements Retainer<V> {
        
        private final Supplier<V> supplier;
        
        private final Predicate<V> shouldRetain;
        
        private final AtomicReference<Optional<V>> cache = new AtomicReference<Optional<V>>(null);
        
        /** Constructs a global retainer. */
        public GlobalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
            this.supplier     = supplier;
            this.shouldRetain = shouldRetain;
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
        
    }
    
    /** The local (thread) retainer. */
    public static class LocalRetainer<V> implements Retainer<V> {
        
        private final Supplier<V> supplier;
        
        private final Predicate<V> shouldRetain;
        
        private final ThreadLocal<Optional<V>> cache = ThreadLocal.withInitial(()->null);
        
        /** Constructs a local retainer. */
        public LocalRetainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
            this.supplier     = supplier;
            this.shouldRetain = shouldRetain;
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
        
    }
    
}
