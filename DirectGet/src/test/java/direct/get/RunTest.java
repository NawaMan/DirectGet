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
package direct.get;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static direct.get.Run.*;

import org.junit.Test;

import lombok.val;

public class RunTest {
    
    private static Ref<Integer> num = Ref.of(Integer.class, 1);
    
    @Test
    public void testSameThreadSupplier() {
        assertTrue(7 == numPlus());
        assertTrue(16 == Run.with(num.providedWith(10)).run(()-> numPlus()));
        assertTrue(26 == Run.with(num.providedWith(20)).run(()-> numPlus()));
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
        Run.run(()->{
        	if (value.get()) {
                throw new IOException();
        	}
        });
    }
    
    @Test
    public void testDiffThreadSupplier() throws InterruptedException {
        val iAmHereFirst = new AtomicBoolean(false);
        val latch = new CountDownLatch(1);
        Run.onNewThread().with(num.providedWith(10)).run(()->{
            Thread.sleep(200);
            return numPlus();
        }).thenAccept(result->{
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
        Run.onNewThread().with(num.providedWith(10)).run(()->{
            Thread.sleep(200);
            throw new IOException();
        }).whenComplete((result, completionException)->{
            assertTrue(completionException.getCause() instanceof IOException);
            latch.countDown();
        });
        
        latch.await();
    }
    
}
