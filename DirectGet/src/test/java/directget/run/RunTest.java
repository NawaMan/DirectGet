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
package directget.run;

import static directget.run.Run.IgnoreHandledProblem;
import static directget.run.Run.OnNewThread;
import static directget.run.exceptions.ProblemHandler.problemHandler;
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
import directget.run.exceptions.ProblemHandledException;
import directget.run.exceptions.ProblemHandler;
import lombok.val;

public class RunTest {
    
    private static Ref<Integer> num = Ref.of(1);
    
    @Test
    public void testSameThreadSupplier() {
        assertTrue(7 == numPlus());
        assertTrue(16 == Run.with(num.butProvidedWith(10)).run(()-> numPlus()));
        assertTrue(26 == Run.with(num.butProvidedWith(20)).run(()-> numPlus()));
    }
    
    @Test(expected = IOException.class)
    public void testSameThreadSupplier_withException() throws IOException {
        Run.run(() -> {
            throw new IOException();
        });
    }
    
    @Test(expected = IOException.class)
    public void testSameThreadSupplier_withReturnAndException() throws Throwable {
        val value = new AtomicBoolean(false);
        Run.run(()->{
            if (value.get()) {
                throw new Throwable();
            }
        });
        
        value.set(true);
        Run
        .run(()->{
            if (value.get()) {
                throw new IOException();
            }
        });
    }
    
    @Test
    public void testDiffThreadSupplier() throws InterruptedException {
        val iAmHereFirst = new AtomicBoolean(false);
        val latch = new CountDownLatch(1);
        OnNewThread()
        .with(num.butProvidedWith(10))
        .run(()->{
            Thread.sleep(200);
            return numPlus();
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
    
    private int numPlus() {
        return Get.a(num) + 6;
    }
    
    @Test
    public void testDiffThreadSupplier_withException() throws InterruptedException {
        val latch = new CountDownLatch(1);
        OnNewThread()
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
        val pblmBuffer = new ArrayList<Throwable>();
        try {
            Run
            .with(problemHandler.butProvidedBy(()->new ProblemHandler(pblmBuffer::add)).retained().forAlways())
            .handleProblem()
            .run(()->{
                throw new IOException();
            });
            
            fail("It shoud never get here.");
        } finally {
            assertEquals("[java.io.IOException]", pblmBuffer.toString());
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
