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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;

import dssb.utils.common.Nulls;
import directget.get.Ref;
import directget.get.supportive.RefTo;
import lombok.val;
import lombok.experimental.ExtensionMethod;

// TODO - Make it more intuitive like     .retained().until(... time ...).repeatEvery();
/**
 * Constrcutor.
 * 
 * @author NawaMan
 *
 * @param <V> the type that is retained.
 */
@ExtensionMethod({ Nulls.class })
public class ForTimeRetainChecker<V> implements Predicate<V> {
    
    /** Ref for current time in milliseconds. */
    static final RefTo<Long> currentTimeMillis
            = Ref.to(Long.class, ()->Long.valueOf(System.currentTimeMillis()));
    
    static final Supplier<Long> nextExpire(long time, TimeUnit unit) {
        return ()->the(currentTimeMillis) + unit.toMillis(time);
    }
    
    private final AtomicLong expiredValue;
    
    private final long time;
    
    private final TimeUnit unit;
    
    /**
     * Retain checker for time.
     * 
     * @param time
     *          the time period.
     * @param unit
     *          the time unit.
     */
    public ForTimeRetainChecker(long time, TimeUnit unit) {
        this(null, time, unit);
    }
    
    /**
     * Retain checker for time.
     * 
     * @param startMilliseconds
     *          the first update time.
     * @param time
     *          the time period.
     * @param unit
     *          the time unit.
     */
    public ForTimeRetainChecker(Long startMilliseconds, long time, TimeUnit unit) {
        long startTime = startMilliseconds.orGet(nextExpire(time, unit));
        
        this.expiredValue = new AtomicLong(startTime);
        this.time = time;
        this.unit = unit;
    }
    /** @return the time period. */
    public long getTime() {
        return time;
    }
    
    /** @return the time unuit. */
    public TimeUnit getTimeUnit() {
        return unit;
    }
    
    @Override
    public boolean test(V value) {
        val currentTime = the(currentTimeMillis);
        val hasExpires = currentTime >= expiredValue.get();
        if (hasExpires) {
            expiredValue.set(nextExpire(time, unit).get());
        }
        return !hasExpires;
    }
    
}
