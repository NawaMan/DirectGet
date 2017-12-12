package directget.get;

import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotations.Nullable;
import javax.inject.Inject;

import org.junit.Test;

import directcommon.common.Nulls;
import directget.get.exceptions.FactoryException;
import directget.get.supportive.RefOf;
import directget.get.supportive.RefTo;
import lombok.val;
import lombok.experimental.ExtensionMethod;

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
        assertEquals("FLASH!", Get.the(Driver.class).zoom());
    }
    
    public static class SuperCar extends Car {
        public String zoom() {
            return "SUPER FLASH!!!!";
        }
    }
    
    @Test
    public void test_substitute() {
        RefOf<Car> carRef = Ref.of(Car.class);
        assertEquals("SUPER FLASH!!!!", 
                With(carRef.butProvidedWithThe(SuperCar.class))
                .run(()->
                    Get.the(Driver.class).zoom()
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
        assertEquals("Meh", Get.the(Person.class).zoom());
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
        assertEquals("FLASH!", Get.the(AnotherPerson.class).zoom());
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
        assertEquals("FLASH!", Get.the(OneAnotherPerson.class).zoom());
    }
    
    @Test
    public void testThe_ofClass_useObjectFactory() {
        assertNotNull(Get.the(Person.class));
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

    public static class GreetingWithRefWithValueFromClass {
        @Ref.Default
        public static final RefTo<GreetingWithRefWithValueFromClass> instance = Ref.toValueOf(GreetingWithRefWithValueFromClass.class);
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
    
    @Test
    public void testThat_OptionalEmptyIsGivenIfTheValueCannotBeObtained() {
        // Since Department is an interface an no default is given, so its value can't be found.
        assertNull(Get.the(Employee.class).departmentName());
        
        // With default given, now we can get the value.
        val mainDepartment = Ref.of(Department.class).butDefaultedTo((Department)()->"Main");
        assertEquals("Main", With(mainDepartment).run(()->Get.the(Employee.class).departmentName()));
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
    

    @ExtensionMethod({ Nulls.class })
    public static class Company {
        private Supplier<Integer> revenueSupplier;
        public Company(Supplier<Integer> revenueSupplier) {
            this.revenueSupplier = revenueSupplier;
        }
        public int revenue() {
            return revenueSupplier.get();
        }
    }
    
    @Test
    public void testThat_withSupplierAsParameter_aSupplierToGetIsGiven() {
        val company = Get.the(Company.class);
        
        assertEquals(0, company.revenue());

        // Notice that company is created once but the revenue returns different value as it is substitue.
        val revenueCalculator = Ref.of(Integer.class).butDefaultedTo(10000);
        assertEquals(10000, With(revenueCalculator).run(()->company.revenue()).intValue());
        
        assertEquals(0, company.revenue());
    }
}