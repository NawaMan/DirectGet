package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;

import org.junit.Test;

import directget.get.Get;

public class SingletonFieldSupplierTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static class SupplierSingleton {
        
        private static int counter = 0;
        
        private static final SupplierSingleton secretInstance = new SupplierSingleton();
        
        @Default
        public static final Supplier<SupplierSingleton> instance = ()->{
            counter++;
            return secretInstance;
        };
        
        private SupplierSingleton() {}
    }
    
    @Test
    public void testThat_supplierSingletonClassWithDefaultAnnotationHasResultOfThatInstanceAsValue() {
        int prevCounter = SupplierSingleton.counter;
        
        assertEquals(SupplierSingleton.secretInstance, locator.get(SupplierSingleton.class));
        assertEquals(prevCounter + 1, SupplierSingleton.counter);
        
        assertEquals(SupplierSingleton.secretInstance, locator.get(SupplierSingleton.class));
        assertEquals(prevCounter + 2, SupplierSingleton.counter);
    }
    
}
