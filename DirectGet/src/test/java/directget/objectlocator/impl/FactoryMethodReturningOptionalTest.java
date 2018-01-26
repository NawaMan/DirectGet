package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import org.junit.Test;

public class FactoryMethodReturningOptionalTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static class OptionalFactoryMethodFactoryMethod {
        
        private static int counter = 0;
        private String string;
        
        private OptionalFactoryMethodFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static Optional<OptionalFactoryMethodFactoryMethod> newInstance() {
            counter++;
            return Optional.of(new OptionalFactoryMethodFactoryMethod("factory"));
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheResultAsTheValue_optonal() {
        int prevCounter = OptionalFactoryMethodFactoryMethod.counter;
        
        assertEquals("factory", locator.get(OptionalFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 1, OptionalFactoryMethodFactoryMethod.counter);
        
        assertEquals("factory", locator.get(OptionalFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 2, OptionalFactoryMethodFactoryMethod.counter);
    }
    
}
