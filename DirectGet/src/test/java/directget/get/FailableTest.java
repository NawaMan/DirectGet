package directget.get;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import directget.get.exceptions.FailableException;
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
    
}
