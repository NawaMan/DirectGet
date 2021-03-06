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

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import directget.get.supportive.RefTo;

/**
 * The retainer.
 * 
 * @author NawaMan
 * @param <V>  the data type.
 **/
public abstract class Retainer<V> implements Supplier<V> {
    
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
    
    
    // TODO - explain better.
    /**
     * Create a new retainer for the same supplier of this one.
     * 
     * @param sameShouldRetain  the predicate to check if the current shouldRetain is the one expected.
     * @param newShouldRetain   the supplier of the new shouldRetain value.
     * @return  a new retainer.
     */
    public Retainer<V> newRetainer(Predicate<Predicate<V>> sameShouldRetain, Supplier<Predicate<V>> newShouldRetain) {
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

    /** @return the new provider similar to this one except that it retains its value with in current thread. **/
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

    /**
     * Create a retainer for same value which is obtained from the ref.
     * 
     * @param ref  the reference to get the data from.
     * @return the new retainer similar to this one except that it retains its value follow the give reference value ('same' rule).
     **/
    public <R> Retainer<V> forSame(RefTo<R> ref) {
        @SuppressWarnings("rawtypes")
        Predicate<Predicate<V>> sameShouldRetain
                = shouldRetain ->
                        (shouldRetain instanceof ForSameRetainChecker)
                     && ((ForSameRetainChecker)shouldRetain).getRef().equals(ref);
        Supplier<Predicate<V>> newShouldRetain = ()->new ForSameRetainChecker<R, V>(ref);
        
        Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
        return newRetainer;
    }

    /**
     * Create a retainer for equivalent value which is obtained from the ref.
     * 
     * @param ref  the reference to get the data from. 
     * @return the new retainer similar to this one except that it retains its value follow the give reference value ('equivalent' rule).
     **/
    public <R> Retainer<V> forEquivalent(RefTo<R> ref) {
        @SuppressWarnings("rawtypes")
        Predicate<Predicate<V>> sameShouldRetain
                = shouldRetain ->
                        (shouldRetain instanceof ForEquivalentRetainChecker)
                     && ((ForEquivalentRetainChecker)shouldRetain).getRef().equals(ref);
        Supplier<Predicate<V>> newShouldRetain = ()->new ForEquivalentRetainChecker<R, V>(ref);
        
        Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
        return newRetainer;
    }

    /**
     * 
     * 
     * @param time 
     * @return the new retainer similar to this one except that it retains its value for a given time period (in millisecond).
     **/
    public <R> Retainer<V> forTime(long time) {
        return forTime(null, time, TimeUnit.MILLISECONDS);
    }

    /**
     * Retain for a specific amount of time.
     * 
     * @param time  the time amount.
     * @param unit  the time unit.
     * @return the new retainer similar to this one except that it retains its value for a given time period (in millisecond).
     **/
    public <R> Retainer<V> forTime(long time, TimeUnit unit) {
        return forTime(null, time, unit);
    }

    /**
     * Retain for a specific amount of time after an initial start millisecond.
     * 
     * @param startMilliseconds  the initial start time.
     * @param time               the retain time in millisecond.
     * @return the new retainer similar to this one except that it retains its value for a given time period (in millisecond).
     **/
    public <R> Retainer<V> forTime(Long startMilliseconds, long time) {
        return forTime(startMilliseconds, time, TimeUnit.MILLISECONDS);
    }

    /**
     * Retain for a specific amount of time after an initial start millisecond.
     * 
     * @param startMilliseconds  the initial start time.
     * @param time               the retain time in the time unit.
     * @param unit               the time unit.
     * @return the new retainer similar to this one except that it retains its value for a given time period (in the unit).
     **/
    public <R> Retainer<V> forTime(Long startMilliseconds, long time, TimeUnit unit) {
        @SuppressWarnings("rawtypes")
        Predicate<Predicate<V>> sameShouldRetain
                = shouldRetain ->
                        (shouldRetain instanceof ForTimeRetainChecker)
                     && ((ForTimeRetainChecker)shouldRetain).getTime()     == time
                     && ((ForTimeRetainChecker)shouldRetain).getTimeUnit() == unit;
        Supplier<Predicate<V>> newShouldRetain = ()->new ForTimeRetainChecker<V>(startMilliseconds, time, unit);
        
        Retainer<V> newRetainer = newRetainer(sameShouldRetain, newShouldRetain);
        return newRetainer;
    }
    
}