package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;

import org.junit.Test;

import directget.get.Get;
import directget.objectlocator.impl.FactoryMethodReturningSupplierTest.SupplierFactoryMethodFactoryMethod;
import dssb.failable.Failable;

public class FactoryMethodReturningSupplierTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static class SupplierFactoryMethodFactoryMethod {
        
        private static int counter = 0;
        private String string;
        
        private SupplierFactoryMethodFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static Supplier<SupplierFactoryMethodFactoryMethod> newInstance() {
            return ()->{
                counter++;
                return new SupplierFactoryMethodFactoryMethod("factory");
            };
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheResultAsTheValue_supplier() {
        int prevCounter = SupplierFactoryMethodFactoryMethod.counter;
        
        assertEquals("factory", locator.get(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 1, SupplierFactoryMethodFactoryMethod.counter);
        
        assertEquals("factory", locator.get(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 2, SupplierFactoryMethodFactoryMethod.counter);
    }
    
    public static class FailableSupplierFactoryMethodFactoryMethod {
        
        private static int counter = 0;
        private String string;
        
        private FailableSupplierFactoryMethodFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static Failable.Supplier<SupplierFactoryMethodFactoryMethod, RuntimeException> newInstance() {
            return ()->{
                counter++;
                return new SupplierFactoryMethodFactoryMethod("factory");
            };
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheResultAsTheValue_supplier_failable() {
        int prevCounter = SupplierFactoryMethodFactoryMethod.counter;
        
        assertEquals("factory", locator.get(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 1, SupplierFactoryMethodFactoryMethod.counter);
        
        assertEquals("factory", locator.get(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 2, SupplierFactoryMethodFactoryMethod.counter);
    }
    
}
