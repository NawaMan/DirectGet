package directget.objectprovider;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import directget.get.exceptions.AbstractClassCreationException;
import directget.objectprovider.ifindsupplier.ConstructorSupplierFinder;
import directget.objectprovider.ifindsupplier.DefaultInterfaceSupplierFinder;
import directget.objectprovider.ifindsupplier.DefaultRefSupplierFinder;
import directget.objectprovider.ifindsupplier.DefautImplementationSupplierFinder;
import directget.objectprovider.ifindsupplier.EnumValueSupplierFinder;
import directget.objectprovider.ifindsupplier.FactoryMethodSupplierFinder;
import directget.objectprovider.ifindsupplier.IFindSupplier;
import directget.objectprovider.ifindsupplier.NullSupplierFinder;
import directget.objectprovider.ifindsupplier.SingletonFieldFinder;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This utility class can create an object using Get.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class })
public class ObjectProvider implements IProvideObject {
    
    // Stepping stone
    public static final ObjectProvider instance = new ObjectProvider();
    
    // TODO - Should create interface with all default method.
    // TODO - Should check for @NotNull
    
    @SuppressWarnings("rawtypes")
    private static final Supplier NullSupplier = ()->null;
    
    private final List<IFindSupplier> finders = Arrays.asList(
            new DefautImplementationSupplierFinder(),
            new NullSupplierFinder(),
            new EnumValueSupplierFinder(),
            new DefaultRefSupplierFinder(),
            new DefaultInterfaceSupplierFinder(),
            new SingletonFieldFinder(),
            new FactoryMethodSupplierFinder(),
            new ConstructorSupplierFinder()
    ).stream()
    .filter(Objects::nonNull)
    .collect(toList());
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, Supplier> suppliers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private static final ThreadLocal<Set<Class>> beingCreateds = ThreadLocal.withInitial(()->new HashSet<>());
    
    
    /**
     * Create an instance of the given class.
     * 
     * @param theGivenClass
     * @return the created value.
     * @throws CreationException when there is a problem creating the object.
     * @throws CyclicDependencyDetectedException when cyclic dependency is detected.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <TYPE> TYPE provide(Class<TYPE> theGivenClass) throws CreationException {
        val set = beingCreateds.get();
        if (set.contains(theGivenClass))
            throw new CyclicDependencyDetectedException(theGivenClass);
        
        try {
            set.add(theGivenClass);
            
            try {
                val supplier = getSupplierFor(theGivenClass);
                val instance = supplier.get();
                return theGivenClass.cast(instance);
            } catch (CreationException e) {
                throw e;
            } catch (Throwable e) {
                throw new CreationException(theGivenClass, e);
            }
        } finally {
            set.remove(theGivenClass);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> getSupplierFor(
            Class<TYPE> theGivenClass) {
        Supplier supplier = suppliers.get(theGivenClass);
        if (supplier.isNull()) {
            supplier = newSupplierFor(theGivenClass);
            supplier = supplier.or(NullSupplier);
            suppliers.put(theGivenClass, supplier);
        }
        return supplier;
    }
    
    @SuppressWarnings({ "rawtypes" })
    private <T> Supplier newSupplierFor(Class<T> theGivenClass) {
        for (val finder : finders) {
            val supplier = finder.find(theGivenClass);
            if (supplier.isNotNull())
                return supplier;
        }
        
        if (Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        // TODO - Should allow this to be configurable.
        return null;
    }
    
}
