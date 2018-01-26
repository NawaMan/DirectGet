package directget.objectlocator.impl;

import org.junit.Ignore;
import org.junit.Test;

import directget.objectlocator.impl.exception.CyclicDependencyDetectedException;

public class CyclicDependencyDetectionTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    public static class Cyclic1 {
        
        public Cyclic1(Cyclic1 another) {
        }
    }
    
    @Ignore("Entanglement")
    @Test(expected=CyclicDependencyDetectedException.class)
    public void testThat_whenDefaultConstructorAskForItself_expectCyclicDependencyDetectedException() {
        locator.get(Cyclic1.class);
    }
    
}
