package directget.objectlocator.impl;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import directget.get.DefaultRefSupplierFinder;
import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.api.LocateObjectException;
import directget.objectlocator.impl.exception.AbstractClassCreationException;
import directget.objectlocator.impl.exception.CreationException;
import directget.objectlocator.impl.exception.CyclicDependencyDetectedException;
import directget.objectlocator.impl.supplierfinders.ConstructorSupplierFinder;
import directget.objectlocator.impl.supplierfinders.DefaultInterfaceSupplierFinder;
import directget.objectlocator.impl.supplierfinders.DefautImplementationSupplierFinder;
import directget.objectlocator.impl.supplierfinders.EnumValueSupplierFinder;
import directget.objectlocator.impl.supplierfinders.FactoryMethodSupplierFinder;
import directget.objectlocator.impl.supplierfinders.IFindSupplier;
import directget.objectlocator.impl.supplierfinders.NullSupplierFinder;
import directget.objectlocator.impl.supplierfinders.SingletonFieldFinder;
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
public class ObjectLocator implements ILocateObject {
    
    // Stepping stone
//    public static final ObjectLocator instance = new ObjectLocator();
    
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
    
    private ILocateObject parent;
    
    public ObjectLocator() {
        this.parent = null;
    }
    public ObjectLocator(ILocateObject parent) {
        this.parent = parent;
    }
    
    
    /**
     * Create an instance of the given class.
     * 
     * @param theGivenClass
     * @return the created value.
     * @throws LocateObjectException when there is a problem locating the object.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public <TYPE> TYPE get(Class<TYPE> theGivenClass) throws LocateObjectException {
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
        val parentLocator = (ILocateObject)this.parent.or(this);
        for (val finder : finders) {
            val supplier = finder.find(theGivenClass, parentLocator);
            if (supplier.isNotNull())
                return supplier;
        }
        
        if (Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        // TODO - Should allow this to be configurable.
        return null;
    }
    
}
