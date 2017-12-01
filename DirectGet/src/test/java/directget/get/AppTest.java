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
    public void testDefaultInitialize() {
        App.reset();
        try {
            // This test prove that without setting anything, the Get of the App
            // scope is ready to use.
            App.Get().a(ArrayList.class);
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testFirstInitialize() throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
            // This test prove that first initialize has no problem.
            App.initialize(null);
        } finally {
            App.reset();
        }
    }
    
    @Test(expected = AppScopeAlreadyInitializedException.class)
    public void testSecondInitialize() throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
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
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testSecondInitialize_onlyAbsent() throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
            // This test prove that first initialize has no problem.
        	assertFalse(App.isInitialized());
            App.initializeIfAbsent(null);
            
            assertTrue(App.isInitialized());
            // ... but the second time will throws an exception.
            App.initializeIfAbsent(null);
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testInitialize_withConfiguration() throws AppScopeAlreadyInitializedException {
        App.reset();
        try {
            val ref1 = Ref.of(String.class).defaultedTo("Ref1");
            val ref2 = Ref.of(String.class).defaultedTo("Ref2");
            
            val configuration = new Configuration(
                    ref1.getProvider().butNormal().butWith("Str1"),
                    ref2
            );
            App.initializeIfAbsent(configuration);
            
            assertEquals("Str1", Get.the(ref1));
            assertEquals("Ref2", Get.the(ref2));
        } finally {
            App.reset();
        }
    }
    
    @Test
    public void testInitializeGet() {
        val ref = Ref.of(String.class).defaultedTo("Ref1");
        assertEquals("Ref1", Get.the(ref));
        
        assertTrue(App.isInitialized());
        assertTrue(App.scope
                .getConfiguration()
                .getProvider(Ref.refFactory)
                .getPreferability()
                .is(Preferability.Dictate));
    }
    
}
