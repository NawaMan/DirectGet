package directget.get.supportive;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import directget.objectcreator.ObjectCreator;
import lombok.Value;
import lombok.experimental.Accessors;

public class ObjectCreatorTest {
    
    public static class Simple {}
    
    // Reconsider this.
//    @Test
//    public void testClassWithDefaultConstructor() {
//        assertTrue(Simple.class.createNew() instanceof Simple);
//    }
    
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
        Complex complex = new ObjectCreator().createNew(Complex.class);
        assertTrue(complex          instanceof Complex);
        assertTrue(complex.simple() instanceof Simple);
    }
    
    // Add more tests
    
}
