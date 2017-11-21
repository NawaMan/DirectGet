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
package directget.get.run;

import static directget.get.run.exceptions.ProblemHandler.problemHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import directget.get.run.Failable;
import directget.get.run.Run;
import directget.get.run.exceptions.FailableException;
import directget.get.run.exceptions.ProblemHandledException;
import directget.get.run.exceptions.ProblemHandler;
import lombok.val;

public class FailableTest {
    
    @Test
    public void testRunnable_run() {
        val counter = new AtomicInteger(0);
        Failable.Runnable<RuntimeException> runnable = ()->{ counter.incrementAndGet(); };
       
        assertEquals(0, counter.get());
        runnable.run();
        assertEquals(1, counter.get());
        runnable.toRunnable().run();
        assertEquals(2, counter.get());
        runnable.gracefully().run();
        assertEquals(3, counter.get());
        runnable.carelessly().run();
        assertEquals(4, counter.get());
        runnable.handledly().run();
        assertEquals(5, counter.get());
    }
    
    @Test(expected=IOException.class)
    public void testRunnable_fail() throws IOException {
        Failable.Runnable<IOException> runnable = ()->{ throw new IOException(); };
       
        runnable.run();
    }
    
    @Test
    public void testRunnable_failCarelessly() {
        Failable.Runnable<IOException> runnable = ()->{ throw new IOException(); };
       
        runnable.carelessly().run();
    }
    
    @Test
    public void testRunnable_failGracefully() {
        Failable.Runnable<IOException> runnable = ()->{ throw new IOException(); };
       
        try {
            runnable.gracefully().run();
            fail("Except an exception");
        } catch (FailableException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }
    
    @Test(expected=ProblemHandledException.class)
    public void testRunnable_failHandledly() {
        Failable.Runnable<IOException> runnable = ()->{ throw new IOException(); };
        
        val problemCollection = new ArrayList<Throwable>();
        try {
            Run
            .with(problemHandler.butProvidedBy(()->new ProblemHandler(problemCollection::add)).retained().forAlways())
            .run(()->{
                runnable.handledly().run();
                fail("Except an exception");
            });
        } finally {
            assertEquals("[java.io.IOException]", problemCollection.toString());
        }
    }
   
}