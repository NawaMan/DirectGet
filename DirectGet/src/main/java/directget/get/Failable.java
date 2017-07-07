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

import directget.get.exceptions.FailableException;
import directget.get.exceptions.ProblemHandler;

/**
 * Failable actions.
 * 
 * @author nawaman
 */
public class Failable {
    
    private Failable() {
    }
    
    /** Failable runnable.  */
    @FunctionalInterface
    public static interface Runnable<T extends Throwable> {
        
        /** Run this runnable. */
        public void run() throws T;
        
        /** Change to regular runnable. */
        public default java.lang.Runnable toRunnable() {
            return gracefully();
        }
        
        /** Convert to a regular runnable and throw FailableException if there is an exception. */
        public default java.lang.Runnable gracefully() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        /** Convert to a regular runnable that completely ignore the exception throw from it. */
        public default java.lang.Runnable carelessly() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
                }
            };
        }
        
        /** Convert to a regular runnable that handle the problem using {@code ProblemHandler}. */
        public default java.lang.Runnable handledly() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
                    Get.a(ProblemHandler.refProblemHandler).handle(t);
                }
            };
        }
        
    }
    
    /** Failable consumer. **/
    @FunctionalInterface
    public static interface Supplier<V, T extends Throwable> {
        
        /** Run this supplier. */
        public V get() throws T;
        
        /** Convert to a regular supplier and throw FailableException if there is an exception. */
        public default java.util.function.Supplier<V> toSupplier() {
            return gracefully();
        }
        
        /** Convert to a regular supplier and throw FailableException if there is an exception. */
        public default java.util.function.Supplier<V> gracefully() {
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        /** Convert to a regular supplier that completely ignore the exception throw from it. */
        public default java.util.function.Supplier<V> carelessly() {
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
                    return null;
                }
            };
        }
        
        /** Convert to a regular supplier that handle the problem using {@code ProblemHandler}. */
        public default java.util.function.Supplier<V> handledly() {
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
                    Get.a(ProblemHandler.refProblemHandler).handle(t);
                    return null;
                }
            };
        }
        
    }
    
    /** Failable consumer. **/
    @FunctionalInterface
    public static interface Consumer<V, T extends Throwable> {
        
        /** Run this consumer. */
        public void accept(V value) throws T;
        
        /** Convert to a regular consumer and throw FailableException if there is an exception. */
        public default java.util.function.Consumer<V> toConsumer() {
            return gracefully();
        }
        
        /** Convert to a regular consumer and throw FailableException if there is an exception. */        
        public default java.util.function.Consumer<V> gracefully() {
            return v -> {
                try {
                    accept(v);
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        /** Convert to a regular consumer that completely ignore the exception throw from it. */
        public default java.util.function.Consumer<V> carelessly() {
            return v -> {
                try {
                    accept(v);
                } catch (Throwable t) {
                }
            };
        }
        
        /** Convert to a regular supplier that handle the problem using {@code ProblemHandler}. */
        public default java.util.function.Consumer<V> handledly() {
            return value -> {
                try {
                    accept(value);
                } catch (Throwable t) {
                	Get.a(ProblemHandler.refProblemHandler).handle(t);
                }
            };
        }
    }
    
}
