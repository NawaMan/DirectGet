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
import java.util.function.Supplier;

import directget.get.supportive.RefTo;

/**
 * This interface is for classes that has a retainer.
 * 
 * @author NawaMan
 **/
public interface WithRetainer<T, WR extends WithRetainer<T, WR>> {
	
	/** Returns the retainer. */
    public Retainer<T> getRetainer();

    /** Change the supplier. */
    WR __but(Supplier<T> newSupplier);

    
    /** @return the new provider similar to this one except that it retains globally. **/
    public default WR globally() {
        return __but(getRetainer().butGlobally());
    }

    /** @return the new provider similar to this one except that it retains locally. **/
    public default WR locally() {
        return __but(getRetainer().butLocally());
    }

    /** @return the new provider similar to this one except that it always retains its value. **/
    public default WR forAlways() {
        return __but(getRetainer().butAlways());
    }

    /** @return the new provider similar to this one except that it never retains its value. **/
    public default WR forNever() {
        return __but(getRetainer().butNever());
    }

    /** @return the new provider similar to this one except that it retains its value with in current thread. **/
    public default WR forCurrentThread() {
        return __but(getRetainer().forCurrentThread());
    }

    /** @return the new provider similar to this one except that it retains its value follow the give reference value ('same' rule). **/
    public default <R> WR forSame(RefTo<R> ref) {
        return __but(getRetainer().forSame(ref));
    }

    /** @return the new provider similar to this one except that it retains its value follow the give reference value ('equivalent' rule). **/
    public default <R> WR forEquivalent(RefTo<R> ref) {
        return __but(getRetainer().forEquivalent(ref));
    }

    /** @return the new provider similar to this one except that it retains its value for a given time period (in millisecond). **/
    public default <R> WR forTime(long time) {
        return __but(getRetainer().forTime(null, time));
    }

    /** @return the new provider similar to this one except that it retains its value for a given time period. **/
    public default <R> WR forTime(long time, TimeUnit unit) {
        return __but(getRetainer().forTime(null, time, unit));
    }

    /** @return the new provider similar to this one except that it retains its value for a given time period (in millisecond). **/
    public default <R> WR butForTime(Long startMilliseconds, long time) {
        return __but(getRetainer().forTime(startMilliseconds, time));
    }

    /** @return the new provider similar to this one except that it retains its value for a given time period. **/
    public default <R> WR forTime(Long startMilliseconds, long time, TimeUnit unit) {
        return __but(getRetainer().forTime(startMilliseconds, time, unit));
    }
    
}