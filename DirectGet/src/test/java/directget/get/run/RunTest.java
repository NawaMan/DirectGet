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

import static directget.get.Run.Asynchronously;
import static directget.get.Run.IgnoreHandledProblem;
import static directget.get.run.exceptions.ProblemHandler.problemHandler;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import directget.get.Get;
import directget.get.Ref;
import directget.get.Run;
import directget.get.run.exceptions.ProblemHandledException;
import directget.get.run.exceptions.ProblemHandler;
import lombok.val;

public class RunTest {
    
    private static Ref<Integer> num = Ref.ofValue(1);
    
    @Test
    public void testSameThreadSupplier() {
        assertTrue(7 == numPlusSix());
        assertTrue(16 == Run.with(num.butProvidedWith(10)).run(()-> numPlusSix()));
        assertTrue(26 == Run.with(num.butProvidedWith(20)).run(()-> numPlusSix()));
    }
    
    @Test(expected = IOException.class)
    public void testSameThreadSupplier_withException() throws IOException {
        Run.run(() -> {
            throw new IOException();
        });
    }
    
    @Test(expected = IOException.class)
    public void testSameThreadSupplier_withReturnAndException() throws Throwable {
        val toThrow = new AtomicBoolean();
        val theRunnable = Failable.Runnable.of(()->{
            if (toThrow.get()) {
                throw new IOException();
            }
        });
        
        toThrow.set(false);
        Run.run(theRunnable);
        
        toThrow.set(true);
        Run.run(theRunnable);
    }
    
    @Test
    public void testDiffThreadSupplier() throws InterruptedException {
        val iAmHereFirst = new AtomicBoolean(false);
        val latch = new CountDownLatch(1);
        Asynchronously()
        .with(num.butProvidedWith(10))
        .run(()->{
            Thread.sleep(200);
            return numPlusSix();
        })
        .thenAccept(result->{
            assertTrue(16 == result);
            assertTrue(iAmHereFirst.get());
            latch.countDown();
        });
        
        Thread.sleep(100);
        iAmHereFirst.set(true);
        
        latch.await();
    }
    
    private int numPlusSix() {
        return Get.a(num) + 6;
    }
    
    @Test
    public void testDiffThreadSupplier_withException() throws InterruptedException {
        val latch = new CountDownLatch(1);
        Asynchronously()
        .run(()->{
            Thread.sleep(200);
            throw new IOException();
        })
        .whenComplete((result, exception)->{
            try {
                assertTrue(exception instanceof IOException);
            } finally {
                latch.countDown();
            }
        });
        
        latch.await();
    }
    
    @Test(expected=ProblemHandledException.class)
    public void testHandleProlem() {
        val problemCollection = new ArrayList<Throwable>();
        try {
            Run
            .with(problemHandler.butProvidedBy(ProblemHandler.of(problemCollection::add)).retained().forAlways())
            .handleProblem()
            .run(()->{
                throw new IOException();
            });
            
            fail("It shoud never get here.");
        } finally {
            assertEquals("[java.io.IOException]", problemCollection.toString());
        }
    }
    
    @Test
    public void testIgnoreExceptions() {
        Run
        .ignoreException()
        .run(()->{
            throw new IOException();
        });
    }
    
    @Test
    public void testIgnoreHandledProblem() throws IOException {
        Failable.Runnable<IOException> runnable = ()->{ throw new IOException(); };
        
        IgnoreHandledProblem()
        .run(()->{
            runnable.handledly().run();
            fail("Except an exception here.");
        });
    }
}
