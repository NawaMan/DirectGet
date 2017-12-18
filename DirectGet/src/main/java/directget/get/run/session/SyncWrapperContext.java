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
package directget.get.run.session;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import directget.get.run.Failable;
import directget.get.run.exceptions.FailableException;
import lombok.val;


/**
 * The contains the wrappers so that we can run something within them.
 * 
 * @author NawaMan
 **/
public class SyncWrapperContext extends WrapperContext {
    
    @SuppressWarnings("rawtypes")
    SyncWrapperContext(
            Function<Failable.Runnable, Runnable> failHandler,
            List<Function<Runnable, Runnable>> functions) {
        super(failHandler, functions);
    }
    
    /** Run something within this context. */
    public <T extends Throwable> void start(Failable.Runnable<T> runnable) throws T {
        run(runnable);
    }
    
    /** Run something within this context. */
    @SuppressWarnings("unchecked")
    public <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
        Runnable current = failHandler.apply(runnable);
        for (int i = wrappers.size(); i-- > 0;) {
            val wrapper = wrappers.get(i);
            val wrapped = wrapper.apply(current);
            if (wrapped != null) {
                current = wrapped;
            }
        }
        try {
            current.run();
        } catch (FailableException e) {
            // NOTE: If there is a need to change this ... see SessionBuilder#failGracefully
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                if (cause instanceof FailableException) {
                    val causeOfCause = ((FailableException)cause).getCause();
                    if (causeOfCause instanceof RuntimeException)
                        throw (RuntimeException)causeOfCause;
                }
                throw (RuntimeException)cause;
            }
            
            throw (T)cause;
        }
    }
    
    /** Run the given supplier and return a value. */
    public <R, T extends Throwable> R start(Failable.Supplier<R, T> supplier) throws T {
        return run(supplier);
    }
    
    /** Run the given supplier and return a value. */
    public <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        val result = new AtomicReference<R>();
        val runnable = (Failable.Runnable<T>) () -> {
            val theResult = supplier.get();
            result.set(theResult);
        };
        run(runnable);
        return result.get();
    }
    
}