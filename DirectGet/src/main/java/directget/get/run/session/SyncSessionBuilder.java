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

import directget.get.Get;
import directget.get.run.Failable;
import directget.get.run.exceptions.FailableException;
import directget.get.run.exceptions.ProblemHandler;
import lombok.val;

/**
 * This class make building a run a bit easier.
 * 
 * @author NawaMan
 */
public class SyncSessionBuilder
        extends SessionBuilder<SyncSessionBuilder>
        implements SynchronousRunSessionBuilder {
    
    // TODO - Do this for now.
    private static enum FailureHandler {
        Gracefully, Carefully, Handledly
    }
    
    public static class RunNoException {
        
        private final SyncWrapperContext context;
        private final FailureHandler     handler;
        
        
        private RunNoException(SyncWrapperContext context, FailureHandler handler) {
            this.context = context;
            this.handler = handler;
        }
        
        /**
         * Run the given supplier and return a value.
         */
        public <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) {
            try {
                return context.run(supplier);
            } catch (RuntimeException t) {
                if (handler == FailureHandler.Gracefully)
                    throw t;
                if (handler == FailureHandler.Handledly)
                    Get.the(ProblemHandler.problemHandler).handle(t);
            } catch (Throwable e) {
                if (handler == FailureHandler.Gracefully)
                    throw new FailableException(e);
                if (handler == FailureHandler.Handledly)
                    Get.the(ProblemHandler.problemHandler).handle(e);
            }
            return null;
        }
        
        /** Run the session now. */
        public <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
            try {
                context.run(runnable);
            } catch (RuntimeException t) {
                if (handler == FailureHandler.Gracefully)
                    throw t;
                if (handler == FailureHandler.Handledly)
                    Get.the(ProblemHandler.problemHandler).handle(t);
            } catch (Throwable e) {
                if (handler == FailureHandler.Gracefully)
                    throw new FailableException(e);
                if (handler == FailureHandler.Handledly)
                    Get.the(ProblemHandler.problemHandler).handle(e);
            }
        }
        
    }
    
    /** Build the session for later use. */
    public SyncWrapperContext build() {
        return new SyncWrapperContext(failHandler, wrappers);
    }
    
    public RunNoException gracefully() {
        val context = build();
        return new RunNoException(context, FailureHandler.Gracefully);
    }
    
    public RunNoException carelessly() {
        val context = build();
        return new RunNoException(context, FailureHandler.Carefully);
    }
    
    public RunNoException handledly() {
        val context = build();
        return new RunNoException(context, FailureHandler.Handledly);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @throws T
     */
    public <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        return build().run(supplier);
    }
    
    /** Run the session now. */
    public <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
        build().run(runnable);
    }
    
}