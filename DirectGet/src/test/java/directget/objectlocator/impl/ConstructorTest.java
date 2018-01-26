package directget.objectlocator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Ignore;
import org.junit.Test;

import directget.objectlocator.impl.exception.CyclicDependencyDetectedException;
import dssb.utils.common.Nulls;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class ConstructorTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
    public static class Car {
        public String zoom() {
            return "FLASH!";
        }
    }
    
    @Test
    public void testThat_useDefaultConstructorToProvideTheValue() {
        assertEquals("FLASH!",  locator.get(Car.class).zoom());
    }
    
    // ==
    
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
    public void testOnlyConstructorIsTheDefaultConstructor() {
        assertEquals("FLASH!", locator.get(Driver.class).zoom());
    }
    
    public static class SuperCar extends Car {
        public String zoom() {
            return "SUPER FLASH!!!!";
        }
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
        assertEquals("Meh", locator.get(Person.class).zoom());
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Inject {
        
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
        assertEquals("FLASH!", locator.get(AnotherPerson.class).zoom());
    }
    
}
