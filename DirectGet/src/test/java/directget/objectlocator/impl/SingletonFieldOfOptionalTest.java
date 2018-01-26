package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

import org.junit.Test;

import directget.get.Get;

public class SingletonFieldOfOptionalTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static class OptionalSingleton {
        @Default
        public static final Optional<OptionalSingleton> instance = Optional.of(new OptionalSingleton());
        
        private OptionalSingleton() {}
    }
    
    @Test
    public void testThat_optionalSingletonClassWithDefaultAnnotationHasTheInstanceAsTheValue() {
        OptionalSingleton value = locator.get(OptionalSingleton.class);
        assertEquals(OptionalSingleton.instance.get(), value);
    }
    
    
    public static class EmptyOptionalSingleton {
        @Default
        public static final Optional<EmptyOptionalSingleton> instance = Optional.empty();
        
        private EmptyOptionalSingleton() {}
    }
    
    @Test
    public void testThat_optionalSingletonClassWithDefaultAnnotationHasTheInstanceAsTheValue_empty() {
        EmptyOptionalSingleton value = locator.get(EmptyOptionalSingleton.class);
        assertEquals(EmptyOptionalSingleton.instance.orElse(null), value);
        assertNull(value);
    }
    
}
