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
import java.util.List;

import org.junit.Test;

import directget.get.Ref;

public class RefTest {
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_forClass() {
        Ref<List> ref1 = Ref.forClass(List.class);
        Ref<List> ref2 = Ref.forClass(List.class);
        assertEquals(ref1, ref2);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_direct() {
        Ref<List> ref1 = Ref.of(List.class);
        Ref<List> ref2 = Ref.of(List.class);
        assertNotEquals(ref1, ref2);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_directWithDefault() {
        List theList = new ArrayList();
        Ref<List> ref = Ref.of(List.class, theList);
        assertTrue(ref._get().isPresent());
        assertTrue(ref._get().filter(list -> list == theList).isPresent());
    }
    @Test
    public void testRef_directWithGenericDefault() {
        Ref<List<String>> ref = Ref.of((List<String>)null).defaultedToBy(()->new ArrayList<String>());
        assertTrue(ref.get().isEmpty());
    }
    
    
    static class Car {
        
        public String zoom() {
            return "FLASH!";
        }
        
    }
    
    @Test
    public void testDefaultValue() {
        Ref<Car> carRef = Ref.of(Car.class);
        assertEquals("FLASH!", Get.a(carRef).zoom());
    }
    
}
