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

import static directget.get.Get.DefaultThreadFactory;
import static directget.get.Get.the;

import java.util.concurrent.Executor;

import lombok.val;

/**
 * This executor create thread using the Get._ThreadFactory_.
 * 
 * @author NawaMan
 */
public class GetThreadFactoryExecutor implements Executor {
    
    /** The default instance. */
    public static final GetThreadFactoryExecutor instance = new GetThreadFactoryExecutor();

    @Override
    public void execute(Runnable runnable) {
        val newThread = the(DefaultThreadFactory).newThread(runnable);
        newThread.start();
    }
    
}
