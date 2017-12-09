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

import static directget.get.Get.a;
import static directget.get.Get.the;
import static directget.get.Run.With;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;

import directget.get.Ref;
import directget.get.exceptions.FactoryException;
import directget.get.supportive.RefFor;
import directget.get.supportive.RefTo;
import lombok.Getter;
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
        RefTo<List> ref = Ref.of(List.class).defaultedTo(theList);
        assertTrue(ref._get().isPresent());
        assertTrue(ref._get().filter(list -> list == theList).isPresent());
    }
    
    @Test
    public void testRef_directWithGenericDefault() {
        RefTo<List<String>> ref = Ref.of(List.class, ()->new ArrayList<String>());
        
        assertTrue(ref.get().isEmpty());
        
        val list = ref.get();
        list.add("Hey");
        assertFalse(list.isEmpty());
        
        assertTrue(ref.get().isEmpty());

        RefTo<String> strRef = Ref.ofValue("Hello");
        assertEquals("Hello", Get.the(strRef));

        RefTo<String> strRef2 = Ref.of(String.class).defaultedToBy(()->"Hello");
        assertEquals("Hello", Get.the(strRef2));
        
        RefTo<Supplier<String>> supplierRef = Ref.ofValue(()->"Hello");
        assertEquals("Hello", Get.the(supplierRef).get());

        RefTo<Function<String, String>> funcRef = Ref.ofValue(name->"Hello " + name + "!");
        assertEquals("Hello Sir!", Get.the(funcRef).apply("Sir"));
        
        Run.With(funcRef.butProvidedWith(name -> "Hey " + name + "!"))
        .run(()->{
            assertEquals("Hey Sir!", Get.the(funcRef).apply("Sir"));
        });
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

    @Ignore("Thing break")
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
    
    @Ignore("Thing break")
    @Test
    public void test_substitute() {
        RefFor<Car> carRef = Ref.forClass(Car.class);
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

    @Ignore("Thing break")
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

    @Ignore("Thing break")
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
        RefTo<Person> personRef = Ref.of(Person.class);
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
        RefTo<Factory<Car>> carFactory = Ref.ofFactory(()->new Car());
        assertTrue(Get.from(carFactory) instanceof Car);
    }
    
    public static class CarFactory implements Factory<Car> {
        @Override
        public Car make() throws FactoryException {
            return new Car();
        }
    }
    
    @Test
    public void testA_forFactoryClass() {
        Ref<CarFactory> carFactory = Ref.forClass(CarFactory.class);
        assertTrue(Get.from(carFactory) instanceof Car);
    }
    
    @Test
    public void testFrom_forFactoryClass() {
        assertTrue(Get.from(CarFactory.class) instanceof Car);
    }
    
    public static class Greeting {
        
        @DefaultRef
        public static final RefTo<Greeting> ref = RefTo.ofValue(new Greeting("Hello"));
        
        @Getter
        private String greeting;
        
        public Greeting(String greeting) {
            this.greeting = greeting;
        }
        
    }
    
    @Test
    public void testDefaultRef() {
        val ref = Ref.defaultOf(Greeting.class);
        System.out.println(ref);
        assertEquals("Hello", the(ref).getGreeting());
        assertEquals("",      a(Greeting.class).getGreeting());

        assertEquals("Hello", the(Greeting.class).getGreeting());
    }
    
    public static class Greeter {

        @DefaultRef
        public static final RefTo<Greeter> ref = RefTo.of(Greeter.class).defaultedToA(Ref.forClass(Greeter.class));
        
        @Getter
        private Greeting greeting;
        
        public Greeter(@The Greeting greeting) {
            this.greeting = greeting;
        }
        
    }

    @Ignore("Thing break")
    @Test
    public void testDefaultRefUser() {
        assertEquals("Hello", the(Greeter.class).getGreeting().getGreeting());
    }
    
}
