//  ========================================================================
//  Copyright (c) 2017 Nawapunth Manusitthipol.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
package directget.get;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import directget.get.App;
import directget.get.exceptions.AppScopeAlreadyInitializedException;
import lombok.val;

// VERY IMPORTANT NOTE!!!
// These test cases are designed to be run alone.
// NEVER run them all at the same time OR with other tests.
public class AppTest {
    
    @Test
    @Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
    public void testDefaultInitialize() {
        // This test prove that without setting anything, the Get of the App
        // scope is ready to use.
        App.Get().a(ArrayList.class);
    }
    
    @Test
    @Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
    public void testFirstInitialize() throws AppScopeAlreadyInitializedException {
        // This test prove that first initialize has no problem.
        App.initialize(null);
    }
    
    @Test(expected = AppScopeAlreadyInitializedException.class)
    @Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
    public void testSecondInitialize() throws AppScopeAlreadyInitializedException {
        // This test prove that first initialize has no problem.
    	assertFalse(App.isInitialized());
        try {
            App.initialize(null);
        } catch (AppScopeAlreadyInitializedException e) {
            fail("Oh no! Not from here.");
        }
        
        assertTrue(App.isInitialized());
        // ... but the second time will throws an exception.
        App.initialize(null);
    }
    
    @Test
    @Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
    public void testSecondInitialize_onlyAbsent() throws AppScopeAlreadyInitializedException {
        // This test prove that first initialize has no problem.
    	assertFalse(App.isInitialized());
        App.initializeIfAbsent(null);
        
        assertTrue(App.isInitialized());
        // ... but the second time will throws an exception.
        App.initializeIfAbsent(null);
    }
    
    @Test
    @Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
    public void testInitialize_withConfiguration() throws AppScopeAlreadyInitializedException {
        val ref1 = Ref.of(String.class).with("Ref1");
        val ref2 = Ref.of(String.class).with("Ref2");

        assertEquals("Ref1", Get.the(ref1));
        assertEquals("Ref2", Get.the(ref2));
        
        val configuration = new Configuration(
                ref1.getProvider().butWith("Str1"),
                ref2
        );
        App.initializeIfAbsent(configuration);
        
        assertEquals("Str1", Get.the(ref1));
        assertEquals("Ref2", Get.the(ref2));
    }
    
}
