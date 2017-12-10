package directget.get;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import directget.get.exceptions.CreationException;
import directget.get.exceptions.GetException;
import directget.get.supportive.ObjectCreatator;
import directget.get.supportive.RefFactory;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import lombok.Getter;
import lombok.val;

// This test involve the default value got when no substitution or configuration is made.
public class DefaultRefTest {
    
    public static class Car {
        public String zoom() {
            return "FLASH!";
        }
    }
    
    @Test
    public void testThat_theOfClassRefIsTheDefaultRef() {
        assertEquals(Ref.of(Car.class), Ref.defaultOf(Car.class));
    }
    
    @Test
    public void testThat_useDefaultConstructorToProvideTheValue() {
        assertEquals("FLASH!", Get.the(Car.class).zoom());
    }

//    public static class Driver {
//        private Car car;
//        public Driver(Car car) {
//            this.car = car;
//        }
//        public String zoom() {
//            return car.zoom();
//        }
//    }
//
//    @Ignore("Thing break")
//    @Test
//    public void testOnlyConstructor() {
//        RefOf<Driver> driverRef = Ref.of(Driver.class);
//        assertEquals("FLASH!", Get.a(driverRef).zoom());
//        assertEquals("FLASH!", Get.a(Driver.class).zoom());
//    }
//    
//    public static class SuperCar extends Car {
//        public String zoom() {
//            return "SUPER FLASH!!!!";
//        }
//    }
//    
//    @Ignore("Thing break")
//    @Test
//    public void test_substitute() {
//        RefOf<Car> carRef = Ref.of(Car.class);
//        assertEquals("SUPER FLASH!!!!", 
//                With(carRef.butProvidedWithA(SuperCar.class))
//                .run(()->
//                    Get.a(Driver.class).zoom()
//                )
//        );
//    }
//
//    public static class Person {
//        private Car car;
//        public Person() {
//            this(null);
//        }
//        public Person(Car car) {
//            this.car = car;
//        }
//        public String zoom() {
//            return (car != null) ? car.zoom() : "Meh";
//        }
//    }
//    
//    @Test
//    public void testDefaultConstructor() {
//        RefOf<Person> personRef = Ref.of(Person.class);
//        assertEquals("Meh", Get.a(personRef).zoom());
//        assertEquals("Meh", Get.a(Person.class).zoom());
//    }
//
//    public static class AnotherPerson {
//        private Car car;
//        public AnotherPerson() {
//            this(null);
//        }
//        @Inject
//        public AnotherPerson(Car car) {
//            this.car = car;
//        }
//        public String zoom() {
//            return (car != null) ? car.zoom() : "Meh";
//        }
//    }
//
//    @Ignore("Thing break")
//    @Test
//    public void testInjectConstructor() {
//        RefOf<AnotherPerson> personRef = Ref.of(AnotherPerson.class);
//        assertEquals("FLASH!", Get.a(personRef).zoom());
//        assertEquals("FLASH!", Get.a(AnotherPerson.class).zoom());
//    }
//
//    public static class OneAnotherPerson {
//        private Car car;
//        public OneAnotherPerson() {
//            this(null);
//        }
//        @InjectedConstructor
//        public OneAnotherPerson(Car car) {
//            this.car = car;
//        }
//        public String zoom() {
//            return (car != null) ? car.zoom() : "Meh";
//        }
//    }
//
//    @Ignore("Thing break")
//    @Test
//    public void testInjectConstructorConstructor() {
//        RefOf<OneAnotherPerson> personRef = Ref.of(OneAnotherPerson.class);
//        assertEquals("FLASH!", Get.a(personRef).zoom());
//        assertEquals("FLASH!", Get.a(OneAnotherPerson.class).zoom());
//    }
//    
//    @Test
//    public void testThe_forClass_useRefFactory() {
//        RefOf<Person> personClassRef = Ref.of(Person.class);
//        assertNotNull(Get.a(personClassRef));
//    }
//    
//    @Test
//    public void testThe_ofClass_returnNull() {
//        RefTo<Person> personRef = Ref.to(Person.class);
//        assertNull(Get.the(personRef));
//    }
//    
//    @Test
//    public void testA_forClass_useRefFactory() {
//        RefOf<Person> personClassRef = Ref.of(Person.class);
//        assertNotNull(Get.a(personClassRef));
//        assertNotNull(Get.a(Person.class));
//    }
//    
//    @Test
//    public void testA_forFactory() {
//        RefTo<Factory<Car>> carFactory = Ref.toFactory(()->new Car());
//        assertTrue(Get.from(carFactory) instanceof Car);
//    }
//    
//    public static class CarFactory implements Factory<Car> {
//        @Override
//        public Car make() throws FactoryException {
//            return new Car();
//        }
//    }
//    
//    @Test
//    public void testA_forFactoryClass() {
//        Ref<CarFactory> carFactory = Ref.of(CarFactory.class);
//        assertTrue(Get.from(carFactory) instanceof Car);
//    }
//    
//    @Test
//    public void testFrom_forFactoryClass() {
//        assertTrue(Get.from(CarFactory.class) instanceof Car);
//    }
    
    public static class GreetingWithRefWithValue {
        @Ref.Default
        public static final RefTo<GreetingWithRefWithValue> instance = RefTo.toValue(new GreetingWithRefWithValue("Hello"));
        private final String greeting;
        public GreetingWithRefWithValue(String greeting) {
            this.greeting = greeting;
        }
        public String getGreeting() {
            return greeting;
        }
    }
    
    @Test
    public void testThat_theDefaultRefIsTheOneAnnotatedWithRefDefault() {
        assertEquals(GreetingWithRefWithValue.instance.getRef(), Ref.defaultOf(GreetingWithRefWithValue.class));
        
        assertEquals("Hello", Get.the(GreetingWithRefWithValue.instance)            .getGreeting());
        assertEquals("Hello", Get.the(GreetingWithRefWithValue.class)               .getGreeting());
        assertEquals("Hello", Get.the(Ref.defaultOf(GreetingWithRefWithValue.class)).getGreeting());
    }
    
    @Test
    public void testThat_whenGetA_theValueIsFrom_ofClassRef() {
        Run
        .with(Ref.of(GreetingWithRefWithValue.class).butDefaultedTo(new GreetingWithRefWithValue("Hi")))
        .run(()->{
            assertEquals("Hi", Get.a(GreetingWithRefWithValue.class).getGreeting());
        });
    }
    
    
    public static class GreetingWithRefNoValue {
        @Ref.Default
        public static final RefTo<GreetingWithRefNoValue> instance = Ref.to(GreetingWithRefNoValue.class);
        private final String greeting;
        public GreetingWithRefNoValue(String greeting) {
            this.greeting = greeting;
        }
        public String getGreeting() {
            return greeting;
        }
    }
    
    @Test
    public void testThat_whenDefaultRefGivenWithoutValue_nullIsReturned() {
        assertNull(Get.the(GreetingWithRefNoValue.class));
        assertNull(Get.the(GreetingWithRefNoValue.instance));
    }
    
    @Test
    public void testThat_whenGetA_theValueIsFrom_ofClassRef_byPassingTheDefaultRef() {
        Run
        .with(Ref.of(GreetingWithRefNoValue.class).butDefaultedTo(new GreetingWithRefNoValue("Hi")))
        .run(()->{
            assertEquals("Hi", Get.a(GreetingWithRefNoValue.class).getGreeting());
        });

        assertEquals("", Get.a(GreetingWithRefNoValue.class).getGreeting());
    }
    

    public static class GreetingWithRefWithValueFromClass {
        @Ref.Default
        public static final RefTo<GreetingWithRefWithValueFromClass> instance = Ref.toA(GreetingWithRefWithValueFromClass.class);
        private final String greeting;
        public GreetingWithRefWithValueFromClass() {
            this("Hey!");
        }
        public GreetingWithRefWithValueFromClass(String greeting) {
            this.greeting = greeting;
        }
        public String getGreeting() {
            return greeting;
        }
    }
    
    @Test
    public void testThat_whenDefaultRefGivenWithValueFromClass_theValueIsFromObjectFactory() {
        assertEquals("Hey!", Get.the(GreetingWithRefWithValueFromClass.class).getGreeting());
    }
  
}
