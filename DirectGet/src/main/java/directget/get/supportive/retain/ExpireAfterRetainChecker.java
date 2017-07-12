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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.swing.Timer;

import directget.get.supportive.retain.Retain.GlobalRetainer;
import directget.get.supportive.retain.Retain.Retainer;
import lombok.val;

/**
 * Retainer checker for ExpireAfter.
 * 
 * @author nawaman
 *
 * @param <V> the reference type.
 */
public class ExpireAfterRetainChecker<V> implements Predicate<V> {
    
    private final Retainer<V> retainer;
    private final long time;
    private final TimeUnit unit;
    
    /**
     * Constructor.
     * 
     * @param rerainerRef
     * @param supplier
     * @param time
     * @param unit
     */
    public ExpireAfterRetainChecker(AtomicReference<Retainer<V>> rerainerRef, Supplier<V> supplier, long time, TimeUnit unit) {
        this.time     = time;
        this.unit     = unit;
        this.retainer = new GlobalRetainer<V>(supplier, this);
        rerainerRef.set(retainer);
        
        val timer = new Timer((int) unit.toMillis(time), evt-> rerainerRef.get().reset() );
        timer.setRepeats(true);
        timer.start();
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
    public boolean test(V t) {
        return true;
    }
    
}
