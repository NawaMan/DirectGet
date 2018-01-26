package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

public class SingletonFieldTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static class BasicSingleton {
        @Default
        public static final BasicSingleton instance = new BasicSingleton("instance");
        
        private String string;
        
        private BasicSingleton(String string) {
            this.string = string;
        }
        
        @Default
        public static BasicSingleton newInstance() {
            return new BasicSingleton("factory");
        }
    }
    
    @Test
    public void testThat_singletonClassWithDefaultAnnotationHasTheInstanceAsTheValue_withFieldMorePreferThanFactory() {
        assertEquals(BasicSingleton.instance, locator.get(BasicSingleton.class));
        assertEquals("instance", locator.get(BasicSingleton.class).string);
    }
    
}
