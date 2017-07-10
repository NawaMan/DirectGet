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
package directget.run.session;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import directget.run.Failable;
import directget.run.exceptions.FailableException;
import lombok.val;

/**
 * The contains the wrappers so that we can run something within them.
 * 
 * @author NawaMan
 **/
public class NewThreadWrapperContext extends WrapperContext {
    
    @SuppressWarnings("rawtypes")
    NewThreadWrapperContext(Function<Failable.Runnable, Runnable> failHandler, List<Function<Runnable, Runnable>> functions) {
        super(failHandler, functions);
    }
    
    /** Run the given supplier and return a value. */
    public <R, T extends Throwable> CompletableFuture<R> start(Failable.Supplier<R, T> supplier) {
        return run(supplier);
    }
    
    /** Run the given supplier and return a value. */
    @SuppressWarnings("unchecked")
    public <R, T extends Throwable> CompletableFuture<R> run(Failable.Supplier<R, T> supplier) {
        CompletableFuture<R> future = new CompletableFuture<R>();
        val runnable = (Failable.Runnable<T>) () -> {
            try {
                val theResult = supplier.get();
                future.complete(theResult);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        };
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
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            }
            
            future.completeExceptionally((T)cause);
        }
        return future;
    }
    
}