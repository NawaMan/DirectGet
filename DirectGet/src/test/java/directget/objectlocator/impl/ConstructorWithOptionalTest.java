package directget.objectlocator.impl;

import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import directget.get.Get;
import directget.get.Ref;
import directget.get.DefaultRefTest.Department;
import directget.get.DefaultRefTest.Executive;
import directget.get.DefaultRefTest.Manager;
import directget.get.DefaultRefTest.Nullable;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class ConstructorWithOptionalTest {
    
    private ObjectLocator locator = new ObjectLocator();
    
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
        assertNull(locator.get(Employee.class).departmentName());
    }
    
    
    public static class Salary {
        public Salary() {
            throw new RuntimeException("Too much");
        }
    }
    
    @ExtensionMethod({ Nulls.class })
    public static class Executive {
        private Optional<Salary> salary;
        public Executive(@Nullable Optional<Salary> salary) {
            this.salary = salary;
        }
        public Optional<Salary> salary() {
            return salary;
        }
    }
    
    @Test
    public void testThat_nullIsGivenToNullableOptionalParameterIfTheValueCannotBeObtainedDueToException() {
        // Since Department is an interface an no default is given, so its value can't be found.
        assertNull(locator.get(Executive.class).salary());
    }
    
}
