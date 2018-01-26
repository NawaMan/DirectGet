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
import directget.objectprovider.CyclicDependencyDetectedException;
import directget.objectprovider.ObjectProvider;
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
        assertEquals("FLASH!", Get.the(AnotherPerson.class).zoom());
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
    
    public static class Cyclic1 {
        
        public Cyclic1(Cyclic1 another) {
        }
    }
    
    @Ignore("Entanglement")
    @Test(expected=CyclicDependencyDetectedException.class)
    public void testThat_whenDefaultConstructorAskForItself_expectCyclicDependencyDetectedException() {
        // TODO - We will get the right answer if we call ObjectProvider.instance.provide(...)
        //Get.the(Cyclic1.class);
        ObjectProvider.instance.provide(Cyclic1.class);
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
//        Get.the(Cyclic2.class);
        ObjectProvider.instance.provide(Cyclic1.class);
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
    
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultImplementation {
        
        public String value();
        
    }
    
    
    @DefaultImplementation("directget.get.TheClass2")
    public static interface TheInterface2 {
        
        public String getText();
        
    }
    
    public static class TheInterface2User {
        
        private TheInterface2 i2;
        
        public TheInterface2User(TheInterface2 i2) {
            this.i2 = i2;
        }
        
        public String getText() {
            return this.i2.whenNotNull().map(TheInterface2::getText).orElse("I am TheInterface2User.");
        }
        
    }
    
    @Test
    public void testThat_whenAnnotatedWithDefaultImplementation_findTheClassAndUseItsDefaultAsThis() {
        assertTrue(Get.the(TheInterface2.class) instanceof TheClass2);
        assertEquals(TheClass2.TEXT, Get.the(TheInterface2User.class).getText());
    }
    
    
    @DefaultImplementation("directget.get.TheClassThatDoesNotExist")
    public static interface TheInterface3 {
        
        public String getText();
        
    }
    
    public static class TheInterface3User {
        
        public static final String TEXT = "I am TheInterface3User.";
        
        private TheInterface3 i3;
        
        public TheInterface3User(TheInterface3 i3) {
            this.i3 = i3;
        }
        
        public String getText() {
            return this.i3.whenNotNull().map(TheInterface3::getText).orElse(TEXT);
        }
        
    }
    
    @Test
    public void testThat_whenAnnotatedWithDefaultImplementation_findTheClassAndUseItsDefaultAsThis_nullWhenNotExist() {
        assertEquals(TheInterface3User.TEXT, Get.the(TheInterface3User.class).getText());
    }
    // TODO - multiple proposals ... cmbine to map or list
    
    
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
    
    
    public static enum MyEnum1 { Value1, Value2; }
    
    @Test
    public void testThat_enumValue_isTheFirstValue() {
        assertEquals(MyEnum1.Value1, Get.the(MyEnum1.class));
    }
    
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Default {
        
    }
    
    public static enum MyEnum2 { Value1, @Default Value2; }
    
    @Test
    public void testThat_enumValueWithDefaultAnnotation() {
        assertEquals(MyEnum2.Value2, Get.the(MyEnum2.class));
    }
    
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DefaultToNull {
        
    }
    
    @DefaultToNull
    public static class NullValue {
        
    }
    
    @Test
    public void testThat_classAnnotatedWithDefaultToNull_hasDefaultValueOfNull() {
        assertNull(Get.the(NullValue.class));
    }
    
    
    public static class BasicSingleton {
        @Default
        public static final BasicSingleton instance = new BasicSingleton("instance");
        
        private String string;
        
        private BasicSingleton(String string) {
            this.string = string;
        }
        
        @Default
        public static BasicSingleton newInstance() {
            return new BasicSingleton("factory");
        }
    }
    
    @Test
    public void testThat_singletonClassWithDefaultAnnotationHasTheInstanceAsTheValue_withFieldMorePreferThanFactory() {
        assertEquals(BasicSingleton.instance, Get.the(BasicSingleton.class));
        assertEquals("instance", Get.the(BasicSingleton.class).string);
    }
    
    
    public static class OptionalSingleton {
        @Default
        public static final Optional<OptionalSingleton> instance = Optional.of(new OptionalSingleton());
        
        private OptionalSingleton() {}
    }
    
    @Test
    public void testThat_optionalSingletonClassWithDefaultAnnotationHasTheInstanceAsTheValue() {
        OptionalSingleton value = Get.the(OptionalSingleton.class);
        assertEquals(OptionalSingleton.instance.get(), value);
    }
    
    
    public static class EmptyOptionalSingleton {
        @Default
        public static final Optional<EmptyOptionalSingleton> instance = Optional.empty();
        
        private EmptyOptionalSingleton() {}
    }
    
    @Test
    public void testThat_optionalSingletonClassWithDefaultAnnotationHasTheInstanceAsTheValue_empty() {
        EmptyOptionalSingleton value = Get.the(EmptyOptionalSingleton.class);
        assertEquals(EmptyOptionalSingleton.instance.orElse(null), value);
        assertNull(value);
    }
    
    
    public static class SupplierSingleton {
        
        private static int counter = 0;
        
        private static final SupplierSingleton secretInstance = new SupplierSingleton();
        
        @Default
        public static final Supplier<SupplierSingleton> instance = ()->{
            counter++;
            return secretInstance;
        };
        
        private SupplierSingleton() {}
    }
    
    @Test
    public void testThat_supplierSingletonClassWithDefaultAnnotationHasResultOfThatInstanceAsValue() {
        int prevCounter = SupplierSingleton.counter;
        
        assertEquals(SupplierSingleton.secretInstance, Get.the(SupplierSingleton.class));
        assertEquals(prevCounter + 1, SupplierSingleton.counter);
        
        assertEquals(SupplierSingleton.secretInstance, Get.the(SupplierSingleton.class));
        assertEquals(prevCounter + 2, SupplierSingleton.counter);
    }
    
    
    public static class BasicFactoryMethod {
        
        private static int counter = 0;
        
        public static final BasicFactoryMethod instance = new BasicFactoryMethod("instance");
        
        private String string;
        
        private BasicFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static BasicFactoryMethod newInstance() {
            counter++;
            return new BasicFactoryMethod("factory");
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheInstanceAsTheValue() {
        int prevCounter = BasicFactoryMethod.counter;
        
        assertEquals("factory", Get.the(BasicFactoryMethod.class).string);
        assertEquals(prevCounter + 1, BasicFactoryMethod.counter);
        
        assertEquals("factory", Get.the(BasicFactoryMethod.class).string);
        assertEquals(prevCounter + 2, BasicFactoryMethod.counter);
    }
    
    public static class OptionalFactoryMethodFactoryMethod {
        
        private static int counter = 0;
        private String string;
        
        private OptionalFactoryMethodFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static Optional<OptionalFactoryMethodFactoryMethod> newInstance() {
            counter++;
            return Optional.of(new OptionalFactoryMethodFactoryMethod("factory"));
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheResultAsTheValue_optonal() {
        int prevCounter = OptionalFactoryMethodFactoryMethod.counter;
        
        assertEquals("factory", Get.the(OptionalFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 1, OptionalFactoryMethodFactoryMethod.counter);
        
        assertEquals("factory", Get.the(OptionalFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 2, OptionalFactoryMethodFactoryMethod.counter);
    }
    
    public static class SupplierFactoryMethodFactoryMethod {
        
        private static int counter = 0;
        private String string;
        
        private SupplierFactoryMethodFactoryMethod(String string) {
            this.string = string;
        }
        
        @Default
        public static Supplier<SupplierFactoryMethodFactoryMethod> newInstance() {
            return ()->{
                counter++;
                return new SupplierFactoryMethodFactoryMethod("factory");
            };
        }
    }
    
    @Test
    public void testThat_classWithFactoryMethodDefaultAnnotationHasTheResultAsTheValue_supplier() {
        int prevCounter = SupplierFactoryMethodFactoryMethod.counter;
        
        assertEquals("factory", Get.the(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 1, SupplierFactoryMethodFactoryMethod.counter);
        
        assertEquals("factory", Get.the(SupplierFactoryMethodFactoryMethod.class).string);
        assertEquals(prevCounter + 2, SupplierFactoryMethodFactoryMethod.counter);
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
