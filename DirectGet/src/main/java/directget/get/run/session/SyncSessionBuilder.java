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

import directget.get.run.HandledFailable;

/**
 * This class make building a run a bit easier.
 * 
 * @author NawaMan
 */
public class SyncSessionBuilder
        extends SessionBuilder<SyncSessionBuilder>
        implements SynchronousRunSessionBuilder {
    
    /** Build the session for later use. */
    public SyncWrapperContext build() {
        return new SyncWrapperContext(failHandler, wrappers);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @throws T
     */
    public <R, T extends Throwable> R run(HandledFailable.Supplier<R, T> supplier) throws T {
        return build().run(supplier);
    }
    
    /** Run the session now. */
    public <T extends Throwable> void run(HandledFailable.Runnable<T> runnable) throws T {
        build().run(runnable);
    }
    
}