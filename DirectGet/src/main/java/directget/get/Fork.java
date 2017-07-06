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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This class offer a natural way to fork an execution.
 * 
 * @author nawaman
 **/
@ExtensionMethod({ Extensions.class })
public class Fork {
    
    private static final Failable.Consumer<Session, Throwable> joinSession = Session::join;
    
    private ThreadLocal<Session> forkSession = new ThreadLocal<>();
    
    /** Constructor. */
    public Fork() {
        
    }
    
    <T extends Throwable> void setSession(Session fork) {
        this.forkSession.set(fork);
    }
    
    /** Run something. */
    public Runnable run(Runnable runnable) {
        val fork = new Session(runnable);
        this.setSession(fork);
        return fork.runnable();
    }
    
    /** Join the latest run with this thread. */
    public void join() throws Throwable {
        this.forkSession.get()._do(joinSession.gracefully());
    }
    
    /** Fork session. */
    public static class Session {
        
        private final AtomicReference<Throwable> problem = new AtomicReference<Throwable>(null);
        
        private final CountDownLatch latch = new CountDownLatch(1);
        
        private final Runnable runnable;
        
        /** Constructor */
        public Session(Runnable runnable) {
            this.runnable = runnable;
        }
        
        /** Get the forked runnable - **NOT THE ORIGINAL RUNNABLE** */
        public Runnable runnable() {
            return () -> {
                try {
                    runnable.run();
                } catch (Throwable t) {
                    problem.set(t);
                } finally {
                    latch.countDown();
                }
            };
        }
        
        /** Join the latest run with the current thread. */
        public void join() throws Throwable {
            latch.await();
            Throwable theProblem = problem.get();
            if (theProblem != null) {
                throw theProblem;
            }
        }
        
    }
}