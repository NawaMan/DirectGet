package directget.objectlocator.impl;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.bindings.FactoryBinding;
import directget.objectlocator.impl.bindings.InstanceBinding;
import directget.objectlocator.impl.bindings.TypeBinding;
import lombok.val;

public class BindingTest {
    
    @Test
    public void testInstanceBinding() {
        val expectedString = "I am a string.";
        
        val bindings = new Bindings.Builder()
                .bind(String.class, new InstanceBinding<>(expectedString))
                .build();
        val locator = new ObjectLocator.Builder().bingings(bindings).build();
        
        assertEquals(expectedString, locator.get(String.class));
    }
    
    public static class MyRunnable implements Runnable {
        @Override
        public void run() {}
    }
    
    @Test
    public void testTypeBinding() {
        val expectedClass = MyRunnable.class;
        
        val bindings = new Bindings.Builder()
                .bind(Runnable.class, new TypeBinding<>(MyRunnable.class))
                .build();
        val locator = new ObjectLocator.Builder().bingings(bindings).build();
        
        assertTrue(expectedClass.isInstance(locator.get(Runnable.class)));
    }
    
    public static class IntegerFactory implements ICreateObject<Integer> {
        private AtomicInteger integer = new AtomicInteger(0);
        @Override
        public Integer create(ILocateObject objectLocator) {
            return integer.getAndIncrement();
        }
        
    }
    
    @Test
    public void testFactoryBinding() {
        val bindings = new Bindings.Builder()
                .bind(Integer.class, new FactoryBinding<>(new IntegerFactory()))
                .build();
        val locator = new ObjectLocator.Builder().bingings(bindings).build();
        
        assertTrue(0 == locator.get(Integer.class));
        assertTrue(1 == locator.get(Integer.class));
        assertTrue(2 == locator.get(Integer.class));
    }
    
}
