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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import directget.get.Ref;
import lombok.val;

/**
 * This utility class make it easy to create supplier with cache for its value.. 
 * 
 * @author nawaman
 */
public class Retain {
    
    /** Ref for current thread. */
    final static Ref<Thread> currentThread
            = Ref.of(Thread.class).by(()->Thread.currentThread());
    
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
            Predicate<V> shouldRetain = new ForSameRetainChecker<T, V>(ref);
            return isGlobal
                    ? new GlobalRetainer<V>(supplier, shouldRetain)
                    : new LocalRetainer<V>(supplier,  shouldRetain);
        }

        
        /** 
         * Create and return a supplier that cache its value until the value of the given ref does not equal
         *   the last call.
         **/
        public <T> Supplier<V> forEquivalent(Ref<T> ref) {
            Predicate<V> shouldRetain = new ForEquivalentRetainChecker<T, V>(ref);
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
            Predicate<V> shouldRetain = new ForTimeRetainChecker<V>(time, unit);
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
        public <T> Supplier<V> expireAfter(long time, TimeUnit unit) {
            if (!isGlobal) {
                return forTime(time, unit);
            }
            
            val rerainerRef = new AtomicReference<Retainer<V>>();
            new ExpireAfterRetainChecker<V>(rerainerRef, supplier, time, unit);
            return rerainerRef.get();
        }
        
    }
    
    //== Supplementary class ==========================================================================================
    
    /** The retainer. */
    public static abstract class Retainer<V> implements Supplier<V> {
        
        final Supplier<V> supplier;
        
        final Predicate<V> shouldRetain;

        /** Constructs a global retainer. */
        Retainer(Supplier<V> supplier, Predicate<V> shouldRetain) {
            this.supplier     = supplier;
            this.shouldRetain = shouldRetain;
        }
        
        /** Reset the value */
        public abstract void reset();
        
        public abstract V get();
        
        abstract BiFunction<Supplier<V>, Predicate<V>, Retainer<V>> getCloner();
        
        Retainer<V> newRetainer(Predicate<Predicate<V>> sameShouldRetain, Supplier<Predicate<V>> newShouldRetain) {
            if (sameShouldRetain.test(this.shouldRetain)) {
                return this;
            }
            return getCloner().apply(supplier, newShouldRetain.get());
        }

        /** @return the new retainer similar to this one except that it retains globally. **/
        public Retainer<V> butGlobally() {
            if (this instanceof GlobalRetainer) {
                return this;
            }
            return new GlobalRetainer<V>(supplier, shouldRetain);
        }

        /** @return the new retainer similar to this one except that it retains locally. **/
        public Retainer<V> butLocally() {
            if (this instanceof LocalRetainer) {
                return this;
            }
            return new LocalRetainer<V>(supplier, shouldRetain);
        }
        
        /** @return the retainer similar to this one but always retain. */
        @SuppressWarnings({ "unchecked" })
        public Retainer<V> butAlways() {
            Predicate<Predicate<V>> sameShouldRetain = shouldRetain -> (shouldRetain == RetainerBuilder.ALWAYS);
            Supplier<Predicate<V>>  newShouldRetain  = ()->(Predicate<V>)RetainerBuilder.ALWAYS;
            
            Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
            return newRetainer;
        }

        /** @return the retainer similar to this one but never retain. */
        @SuppressWarnings({ "unchecked" })
        public Retainer<V> butNever() {
            Predicate<Predicate<V>> sameShouldRetain = shouldRetain -> (shouldRetain == RetainerBuilder.NEVER);
            Supplier<Predicate<V>>  newShouldRetain  = ()->(Predicate<V>)RetainerBuilder.NEVER;
            
            Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
            return newRetainer;
        }

        /** @return the new providing similar to this one except that it retains its value with in current thread. **/
        public Retainer<V> forCurrentThread() {
            boolean                 isLocal          = (this instanceof LocalRetainer);
            Predicate<Predicate<V>> sameShouldRetain = shouldRetain -> isLocal && (shouldRetain == RetainerBuilder.ALWAYS);
            @SuppressWarnings("unchecked")
            Supplier<Predicate<V>>  newShouldRetain  = ()->(Predicate<V>)RetainerBuilder.ALWAYS;
            
            if (sameShouldRetain.test(shouldRetain)) {
                return this;
            }
            
            Retainer<V> newRetainer = new LocalRetainer<V>(supplier, newShouldRetain.get());
            return newRetainer;
        }

        /** @return the new retainer similar to this one except that it retains its value follow the give reference value ('same' rule). **/
        public <R> Retainer<V> forSame(Ref<R> ref) {
            @SuppressWarnings("rawtypes")
            Predicate<Predicate<V>> sameShouldRetain
                    = shouldRetain ->
                            (shouldRetain instanceof ForSameRetainChecker)
                         && ((ForSameRetainChecker)shouldRetain).getRef().equals(ref);
            Supplier<Predicate<V>> newShouldRetain = ()->new ForSameRetainChecker<R, V>(ref);
            
            Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
            return newRetainer;
        }

        /** @return the new retainer similar to this one except that it retains its value follow the give reference value ('equivalent' rule). **/
        public <R> Retainer<V> forEquivalent(Ref<R> ref) {
            @SuppressWarnings("rawtypes")
            Predicate<Predicate<V>> sameShouldRetain
                    = shouldRetain ->
                            (shouldRetain instanceof ForEquivalentRetainChecker)
                         && ((ForEquivalentRetainChecker)shouldRetain).getRef().equals(ref);
            Supplier<Predicate<V>> newShouldRetain = ()->new ForEquivalentRetainChecker<R, V>(ref);
            
            Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
            return newRetainer;
        }

        /** @return the new retainer similar to this one except that it retains its value for a given time period (in millisecond). **/
        public <R> Retainer<V> forTime(long time) {
            return forTime(time, TimeUnit.MILLISECONDS);
        }

        /** @return the new retainer similar to this one except that it retains its value for a given time period (in millisecond). **/
        public <R> Retainer<V> forTime(long time, TimeUnit unit) {
            @SuppressWarnings("rawtypes")
            Predicate<Predicate<V>> sameShouldRetain
                    = shouldRetain ->
                            (shouldRetain instanceof ForTimeRetainChecker)
                         && ((ForTimeRetainChecker)shouldRetain).getTime()     == time
                         && ((ForTimeRetainChecker)shouldRetain).getTimeUnit() == unit;
            Supplier<Predicate<V>> newShouldRetain = ()->new ForTimeRetainChecker<V>(time, unit);
            
            Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
            return newRetainer;
        }

        /** @return the new retainer similar to this one except that its retained value expired after a given time period (in millisecond). **/
        public <R> Retainer<V> expireAfter(long time) {
            return expireAfter(time, TimeUnit.MILLISECONDS);
        }

        /** @return the new retainer similar to this one except that its retained value expired after a given time period. **/
        public <R> Retainer<V> expireAfter(long time, TimeUnit unit) {
            AtomicReference<Retainer<V>> rerainerRef = new AtomicReference<Retain.Retainer<V>>(null);
            @SuppressWarnings("rawtypes")
            Predicate<Predicate<V>> sameShouldRetain
                    = shouldRetain ->
                            (shouldRetain instanceof ExpireAfterRetainChecker)
                         && ((ExpireAfterRetainChecker)shouldRetain).getTime()     == time
                         && ((ExpireAfterRetainChecker)shouldRetain).getTimeUnit() == unit;
            Supplier<Predicate<V>> newShouldRetain = ()->new ExpireAfterRetainChecker<V>(rerainerRef, supplier, time, unit);
            newRetainer(sameShouldRetain, newShouldRetain);
            return rerainerRef.get();
        }
        
    }
    
    /** The global retainer. */
    public static class GlobalRetainer<V> extends Retainer<V> {
        
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
    
    /** The local (thread) retainer. */
    public static class LocalRetainer<V> extends Retainer<V> {
        
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
    
}
