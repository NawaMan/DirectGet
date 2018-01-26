package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import dssb.utils.common.Nulls;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class DefaultImplementationTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultImplementation {
        
        public String value();
        
    }
    
    
    @DefaultImplementation("directget.objectlocator.impl.TheClass2")
    public static interface TheInterface2 {
        
        public String getText();
        
    }
    
    public static class TheInterface2User {
        
        private TheInterface2 i2;
        
        public TheInterface2User(TheInterface2 i2) {
            this.i2 = i2;
        }
        
        public String getText() {
            return this.i2.whenNotNull().map(TheInterface2::getText).orElse("I am TheInterface2User.");
        }
        
    }
    
    @Test
    public void testThat_whenAnnotatedWithDefaultImplementation_findTheClassAndUseItsDefaultAsThis() {
        assertTrue(locator.get(TheInterface2.class) instanceof TheClass2);
        assertEquals(TheClass2.TEXT, locator.get(TheInterface2User.class).getText());
    }

    @DefaultImplementation("directget.get.TheClassThatDoesNotExist")
    public static interface TheInterface3 {
        
        public String getText();
        
    }
    
    public static class TheInterface3User {
        
        public static final String TEXT = "I am TheInterface3User.";
        
        private TheInterface3 i3;
        
        public TheInterface3User(TheInterface3 i3) {
            this.i3 = i3;
        }
        
        public String getText() {
            return this.i3.whenNotNull().map(TheInterface3::getText).orElse(TEXT);
        }
        
    }
    
    @Test
    public void testThat_whenAnnotatedWithDefaultImplementation_findTheClassAndUseItsDefaultAsThis_nullWhenNotExist() {
        assertEquals(TheInterface3User.TEXT, locator.get(TheInterface3User.class).getText());
    }
    
}
