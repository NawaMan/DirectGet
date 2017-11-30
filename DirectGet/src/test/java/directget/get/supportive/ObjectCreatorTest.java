package directget.get.supportive;

import static org.junit.Assert.*;

import org.junit.Test;

import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ ObjectCreatator.class })
public class ObjectCreatorTest {
    
    public static class Simple {}
    
    @Test
    public void testClassWithDefaultConstructor() {
        assertTrue(Simple.class.createNew() instanceof Simple);
    }

    @Value
    @Accessors(fluent=true)
    public static class Complex {
        private Simple simple;
        public Complex(Simple simple) {
            this.simple = simple;
        }
    }
    
    @Test
    public void testClassWithOneConstructor() {
        val complex = Complex.class.createNew();
        assertTrue(complex          instanceof Complex);
        assertTrue(complex.simple() instanceof Simple);
    }
    
    // Add more tests
    
}
