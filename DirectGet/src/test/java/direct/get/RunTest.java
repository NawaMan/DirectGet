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
        assertTrue(16 == Run.with(num.providedWith(10)).run(() -> Get.a(num) + 6));
        assertTrue(26 == Run.with(num.providedWith(20)).run(() -> Get.a(num) + 6));
    }
    
    @Test(expected = IOException.class)
    public void testSameThreadSupplier_withException() throws IOException {
        Run.with().run(() -> {
            throw new IOException();
        });
    }
    
    @Test
    public void testDiffThreadSupplier() throws InterruptedException {
        val iAmHereFirst = new AtomicBoolean(false);
        val latch = new CountDownLatch(1);
        Run.onNewThread().with(num.providedWith(10)).run(() -> {
            Thread.sleep(200);
            return Get.a(num) + 6;
        }).thenAccept(result -> {
            assertTrue(16 == result);
            assertTrue(iAmHereFirst.get());
            latch.countDown();
        });
        
        Thread.sleep(100);
        iAmHereFirst.set(true);
        
        latch.await();
    }
    
    @Test
    public void testDiffThreadSupplier_withException() throws InterruptedException {
        val latch = new CountDownLatch(1);
        Run.onNewThread().with(num.providedWith(10)).run(() -> {
            Thread.sleep(200);
            throw new IOException();
        }).whenComplete((result, exception) -> {
            assertTrue(exception.getCause() instanceof IOException);
            latch.countDown();
        });
        
        latch.await();
    }
    
}
