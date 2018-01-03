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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import dssb.utils.common.Nulls;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class RefTest {
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_ofClass() {
        RefOf<List> ref1 = Ref.of(List.class);
        RefOf<List> ref2 = Ref.of(List.class);
        assertEquals(ref1, ref2);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_toClass() {
        Ref<List> ref1 = Ref.to(List.class);
        Ref<List> ref2 = Ref.to(List.class);
        assertNotEquals(ref1, ref2);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_toClassWithDefault() {
        List theList = new ArrayList();
        RefTo<List> ref = Ref.to(List.class).defaultedTo(theList);
        assertTrue(ref.getDefaultValue().isNotNull());
        assertTrue(ref.getDefaultValue().whenNotNull().filter(list -> list == theList).isPresent());
    }
    
    @Test
    public void testRef_toClassWithGenericDefault() {
        RefTo<List<String>> ref = Ref.to(List.class, ()->new ArrayList<String>());
        
        assertTrue(ref.getDefaultValue().isEmpty());
        
        val list = ref.getDefaultValue();
        list.add("Hey");
        assertFalse(list.isEmpty());
        
        assertTrue(ref.getDefaultValue().isEmpty());

        RefTo<String> strRef = Ref.toValue("Hello");
        assertEquals("Hello", Get.the(strRef));

        RefTo<String> strRef2 = Ref.to(String.class).defaultedToBy(()->"Hello");
        assertEquals("Hello", Get.the(strRef2));
        
        RefTo<Supplier<String>> supplierRef = Ref.toValue(()->"Hello");
        assertEquals("Hello", Get.the(supplierRef).get());

        RefTo<Function<String, String>> funcRef = Ref.toValue(name->"Hello " + name + "!");
        assertEquals("Hello Sir!", Get.the(funcRef).apply("Sir"));
        
        Run.With(funcRef.butProvidedWith(name -> "Hey " + name + "!"))
        .run(()->{
            assertEquals("Hey Sir!", Get.the(funcRef).apply("Sir"));
        });
    }
    
    @Test
    public void testProvidedUsing() {
        RefTo<String>  theString = Ref.toValue("Hello");
        RefTo<Integer> theLength = Ref.to(Integer.class).defaultedUsing(theString, String::length);
        
        assertEquals(5, theLength.value().intValue());
    }
}
