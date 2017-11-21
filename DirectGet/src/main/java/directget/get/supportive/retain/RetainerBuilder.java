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

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

import directget.get.Ref;

/** The retainer builder. */
public class RetainerBuilder<V> {
    
    static Predicate<?> ALWAYS = value->true;
    
    static Predicate<?> NEVER = value->false;
    
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
    
    /** Make the retainer local - local thread. */
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
    
    /** Create and return a supplier that never cache its. */
    @SuppressWarnings("unchecked")
    public Supplier<V> never() {
        return isGlobal
                ? new GlobalRetainer<V>(supplier, (Predicate<V>)NEVER)
                : new LocalRetainer<V>(supplier,  (Predicate<V>)NEVER);
    }
    
    /** Create and return a supplier that cache its value for each thread (alias for locally().always()). */
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
        return forTime(null, time, TimeUnit.MICROSECONDS);
    }
    
    /** Create and return a supplier that cache its value until for a specific amount of time. **/
    public <T> Supplier<V> forTime(long time, TimeUnit unit) {
        return forTime(null, time, unit);
    }
    
    /** Create and return a supplier that cache its value until  for a specific amount of time. **/
    public <T> Supplier<V> forTime(Long startMilliseconds, long time) {
        return forTime(startMilliseconds, time, TimeUnit.MICROSECONDS);
    }
    
    /** Create and return a supplier that cache its value until for a specific amount of time. **/
    public <T> Supplier<V> forTime(Long startMilliseconds, long time, TimeUnit unit) {
        Predicate<V> shouldRetain = new ForTimeRetainChecker<V>(startMilliseconds, time, unit);
        return isGlobal
                ? new GlobalRetainer<V>(supplier, shouldRetain)
                : new LocalRetainer<V>(supplier,  shouldRetain); 
    }
    
}