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

import static directget.get.Run.With;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.Test;

import directget.get.Ref;
import directget.get.supportive.RefFor;
import directget.get.supportive.RefOf;
import lombok.val;

public class RefTest {
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testRef_forClass() {
        RefFor<List> ref1 = Ref.forClass(List.class);
        RefFor<List> ref2 = Ref.forClass(List.class);
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
        Ref<List> ref = Ref.ofValue(List.class, theList);
        assertTrue(ref._get().isPresent());
        assertTrue(ref._get().filter(list -> list == theList).isPresent());
    }
    
    public void testRef_directWithGenericDefault() {
        Ref<List<String>> ref = Ref.ofSupplier(List.class, ()->new ArrayList<String>());
        
        assertTrue(ref.get().isEmpty());
        
        val list = ref.get();
        list.add("Hey");
        assertFalse(ref.get().isEmpty());
        
        assertTrue(ref.get().isEmpty());
        
        // Bad for supplier of supplier
        RefOf<Supplier<String>> strRef = Ref.ofSupplier(Supplier.class, (Supplier<Supplier<String>>)()->{ return ()->"Hello"; });
        assertEquals("Hello", Get.the(strRef).get());
    }
    
    
    public static class Car {
        public String zoom() {
            return "FLASH!";
        }
    }
    
    @Test
    public void testDefaultValue() {
        RefFor<Car> carRef = Ref.forClass(Car.class);
        assertEquals("FLASH!", Get.a(carRef).zoom());
        assertEquals("FLASH!", Get.a(Car.class).zoom());
    }

    public static class Driver {
        private Car car;
        public Driver(Car car) {
            this.car = car;
        }
        public String zoom() {
            return car.zoom();
        }
    }
    
    @Test
    public void testOnlyConstructor() {
        RefFor<Driver> driverRef = Ref.forClass(Driver.class);
        assertEquals("FLASH!", Get.a(driverRef).zoom());
        assertEquals("FLASH!", Get.a(Driver.class).zoom());
    }
    
    public static class SuperCar extends Car {
        public String zoom() {
            return "SUPER FLASH!!!!";
        }
    }
    
    @Test
    public void test_substitute() {
        RefFor<Driver>   driverRef   = Ref.forClass(Driver.class);
        RefFor<Car>      carRef      = Ref.forClass(Car.class);
        assertEquals("SUPER FLASH!!!!", 
                With(carRef.butProvidedWithA(SuperCar.class))
                .run(()->
                    Get.a(Driver.class).zoom()
                )
        );
    }

    public static class Person {
        private Car car;
        public Person() {
            this(null);
        }
        public Person(Car car) {
            this.car = car;
        }
        public String zoom() {
            return (car != null) ? car.zoom() : "Meh";
        }
    }
    
    @Test
    public void testDefaultConstructor() {
        RefFor<Person> personRef = Ref.forClass(Person.class);
        assertEquals("Meh", Get.a(personRef).zoom());
        assertEquals("Meh", Get.a(Person.class).zoom());
    }

    public static class AnotherPerson {
        private Car car;
        public AnotherPerson() {
            this(null);
        }
        @Inject
        public AnotherPerson(Car car) {
            this.car = car;
        }
        public String zoom() {
            return (car != null) ? car.zoom() : "Meh";
        }
    }
    
    @Test
    public void testInjectConstructor() {
        RefFor<AnotherPerson> personRef = Ref.forClass(AnotherPerson.class);
        assertEquals("FLASH!", Get.a(personRef).zoom());
        assertEquals("FLASH!", Get.a(AnotherPerson.class).zoom());
    }

    public static class OneAnotherPerson {
        private Car car;
        public OneAnotherPerson() {
            this(null);
        }
        @InjectedConstructor
        public OneAnotherPerson(Car car) {
            this.car = car;
        }
        public String zoom() {
            return (car != null) ? car.zoom() : "Meh";
        }
    }
    
    @Test
    public void testInjectConstructorConstructor() {
        RefFor<OneAnotherPerson> personRef = Ref.forClass(OneAnotherPerson.class);
        assertEquals("FLASH!", Get.a(personRef).zoom());
        assertEquals("FLASH!", Get.a(OneAnotherPerson.class).zoom());
    }
    
    @Test
    public void testThe_forClass_useRefFactory() {
        RefFor<Person> personClassRef = Ref.forClass(Person.class);
        assertNotNull(Get.a(personClassRef));
    }
    
    @Test
    public void testThe_ofClass_returnNull() {
        RefOf<Person> personRef = Ref.of(Person.class);
        assertNull(Get.the(personRef));
    }
    
    @Test
    public void testA_forClass_useRefFactory() {
        RefFor<Person> personClassRef = Ref.forClass(Person.class);
        assertNotNull(Get.a(personClassRef));
        assertNotNull(Get.a(Person.class));
    }
    
    @Test
    public void testA_forFactory() {
        Ref<Factory<Car>> newCar = Ref.ofValue(Factory.class, ()->new Car());
        assertTrue(Get.a(newCar) instanceof Car);
    }
    
}
