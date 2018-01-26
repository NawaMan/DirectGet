package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Ignore;
import org.junit.Test;

import directget.objectlocator.api.ILocateObject;

public class DefaultLocatorTest {
    
    @Test
    public void testDefaultLocator() {
        assertTrue(ObjectLocator.class.isInstance(ILocateObject.defaultLocator().get()));
    }
    
}
