package directget.get;

import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import directget.get.exceptions.FactoryException;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import directget.objectlocator.impl.ObjectLocator;
import directget.objectlocator.impl.ConstructorTest.Car;
import directget.objectlocator.impl.ConstructorTest.Driver;
import directget.objectlocator.impl.ConstructorTest.SuperCar;
import directget.objectlocator.impl.exception.CyclicDependencyDetectedException;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

// This test involve the default value got when no substitution or configuration is made.
@ExtensionMethod({ Nulls.class })
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
        assertEquals("FLASH!", Get.the(Driver.class).zoom());
    }
    
    public static class SuperCar extends Car {
        public String zoom() {
            return "SUPER FLASH!!!!";
        }
    }
    
    @Test
    public void test_substituteOverideTheDefault() {
        RefOf<Car> carRef = Ref.of(Car.class);
        assertEquals("SUPER FLASH!!!!", 
                With(carRef.butProvidedWithThe(SuperCar.class))
                .run(()->
                    Get.the(Driver.class).zoom()
                )
        );
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Inject {
        
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
    public void testThe_ofClass_returnNull() {
        RefTo<Person> personRef = Ref.to(Person.class);
        assertNull(Get.the(personRef));
    }
    
    @Test
    public void testA_forFactory() {
        RefTo<Factory<Car>> carFactory = Ref.toFactory(()->new Car());
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
        Ref<CarFactory> carFactory = Ref.of(CarFactory.class);
        assertTrue(Get.from(carFactory) instanceof Car);
    }
    
    @Test
    public void testFrom_forFactoryClass() {
        assertTrue(Get.from(CarFactory.class) instanceof Car);
    }
    
    public static class GreetingWithRefWithValue {
        @Ref.DefaultRef
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
    
    
    public static class GreetingWithRefNoValue {
        @Ref.DefaultRef
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
    
    public static class Cyclic2 {
        
        @Ref.DefaultRef
        public static final RefTo<Cyclic2> instance = Ref.toValueOf(Cyclic2.class);
        
        public Cyclic2() {
        }
    }
    
    @Ignore("Entanglement")
    @Test(expected=CyclicDependencyDetectedException.class)
    public void testThat_whenDefaultRefGetValueFromTheClassItSelf_expectCyclicDependencyDetectedException() {
        // TODO - We will get the right answer if we call ObjectProvider.instance.provide(...)
        Get.the(Cyclic2.class);
    }
    
    public static interface TheInterface1 {
        
        @Ref.DefaultRef
        public static final RefTo<TheInterface1> instance
                = Ref.toValueOf(TheClass1.class);
    }
    
    public static class TheClass1
            implements TheInterface1 {
        
    }
    
    public void testThat_whenAskForSuperClassWithDefaultRef_getTheValueOfTheDefaultRef() {
        assertTrue(Get.the(TheInterface1.class) instanceof TheClass1);
    }
    
    public static class SuperClass {
        
        @Ref.DefaultRef
        public static final RefTo<TheInterface1> instance
                = Ref.toValueOf(TheClass1.class);
        
        @Inject
        public SuperClass() {
            
        }
    }
    
    public static class SubClass extends SuperClass {
        
    }
    
    public void testThat_defaultRef_isMorePreferableThanIbjectedConstructor() {
        assertTrue(Get.the(SuperClass.class) instanceof SubClass);
    }
    
    public static interface Department {
        public String name();
    }
    @ExtensionMethod({ Nulls.class })
    public static class Employee {
        private Department department;
        public Employee(Optional<Department> department) {
            this.department = department.orElse(null);
        }
        public String departmentName() {
            return department.whenNotNull().map(Department::name).orElse(null);
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Nullable {
        
    }
    
    @ExtensionMethod({ Nulls.class })
    public static class Manager {
        private Department department;
        public Manager(@Nullable Department department) {
            this.department = department;
        }
        public String departmentName() {
            return department.whenNotNull().map(Department::name).orElse(null);
        }
    }
    
    @Test
    public void testThat_nullIsGivenToNullableParameterIfTheValueCannotBeObtained() {
        // Since Department is an interface an no default is given, so its value can't be found.
        assertNull(Get.the(Manager.class).departmentName());
        
        // With default given, now we can get the value.
        val specialDepartment = Ref.of(Department.class).butDefaultedTo((Department)()->"Special");
        assertEquals("Special", With(specialDepartment).run(()->Get.the(Manager.class).departmentName()));
    }
    
    @ExtensionMethod({ Nulls.class })
    public static class Executive {
        private Optional<Department> department;
        public Executive(@Nullable Optional<Department> department) {
            this.department = department;
        }
        public Optional<Department> department() {
            return department;
        }
    }
    
    // TODO - Check if the test is lieing.
    @Test
    public void testThat_nullIsGivenToNullableOptionalParameterIfTheValueCannotBeObtainedDueToException() {
        // Since Department is an interface an no default is given, so its value can't be found.
        assertNull(Get.the(Executive.class).department());
        
        // With default given as no, now we can get the value as Optiona.empty.
        val nullDepartment = Ref.of(Department.class).butDefaultedTo(null);
        assertTrue(With(nullDepartment).run(()->Get.the(Executive.class).department().equals(Optional.empty())));
        
        // With default given, now we can get the value.
        val secretDepartment = Ref.of(Department.class).butDefaultedTo((Department)()->"Secret");
        assertEquals("Secret", With(secretDepartment).run(()->Get.the(Manager.class).departmentName()));
    }
    
}
