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
package directget.get.supportive;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.val;

/**
 * This ThreadFactory create thread with unique name with id.
 * 
 * @author nawaman
 */
public class CounterThreadFactory implements ThreadFactory {
    
    /** The default instance. */
    public static final CounterThreadFactory instance = new CounterThreadFactory();
    
    private static AtomicInteger threadCount = new AtomicInteger(1);
    
    @Override
    public Thread newThread(Runnable runnable) {
        val thread = new Thread(runnable);
        thread.setName("Thread#" + threadCount.getAndIncrement());
        return thread;
    }
    
}
