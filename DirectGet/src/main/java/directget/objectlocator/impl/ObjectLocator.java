package directget.objectlocator.impl;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.api.LocateObjectException;
import directget.objectlocator.impl.exception.AbstractClassCreationException;
import directget.objectlocator.impl.exception.CyclicDependencyDetectedException;
import directget.objectlocator.impl.exception.ObjectCreationException;
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
    private static final Supplier NoSupplier = ()->null;
    
    
    private static final List<IFindSupplier> classLevelfinders = Arrays.asList(
            new DefautImplementationSupplierFinder(),
            new NullSupplierFinder(),
            new EnumValueSupplierFinder(),
            new DefaultInterfaceSupplierFinder()
    );
    private static final List<IFindSupplier> elementLevelfinders = Arrays.asList(
            new SingletonFieldFinder(),
            new FactoryMethodSupplierFinder(),
            new ConstructorSupplierFinder()
    );
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, Supplier> sharedSuppliers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private static final ThreadLocal<Set<Class>> beingCreateds = ThreadLocal.withInitial(()->new HashSet<>());
    
    @SuppressWarnings("unchecked")
    private static final List<IFindSupplier> emptyList = (List<IFindSupplier>)EMPTY_LIST;
    
    private ILocateObject        parent;
    private List<IFindSupplier>  finders;
    private IHandleLocateFailure locateFailureHandler;
    
    @SuppressWarnings("rawtypes")
    private Map<Class, Supplier> suppliers;
    
    private boolean              useShareSupplierCache;
    private List<IFindSupplier>  additionalSupplierFinders;
    
    public ObjectLocator() {
        this(null, true, null, null);
    }
    
    @SuppressWarnings("rawtypes")
    public ObjectLocator(
            ILocateObject        parent,
            boolean              useShareSupplierCache,
            List<IFindSupplier>  additionalSupplierFinders,
            IHandleLocateFailure locateFailureHandler) {
        this.parent               = parent;
        this.finders              = combineFinders(additionalSupplierFinders);
        this.locateFailureHandler = locateFailureHandler;
        this.suppliers            = useShareSupplierCache ? sharedSuppliers : new ConcurrentHashMap<Class, Supplier>();
        
        // Supportive
        this.useShareSupplierCache     = useShareSupplierCache;
        this.additionalSupplierFinders = additionalSupplierFinders;
    }
    
    private static List<IFindSupplier> combineFinders(List<IFindSupplier> additionalSupplierFinders) {
        val finderList = new ArrayList<IFindSupplier>();
        finderList.addAll(classLevelfinders);
        finderList.addAll(additionalSupplierFinders.or(emptyList));
        finderList.addAll(elementLevelfinders);
        return unmodifiableList(finderList);
    }
    
    public ObjectLocator withNewCache() {
        return new ObjectLocator(parent, false, additionalSupplierFinders, locateFailureHandler);
    }
    
    public ObjectLocator withSharedCache() {
        return new ObjectLocator(parent, true, additionalSupplierFinders, locateFailureHandler);
    }
    
    public ObjectLocator wihtLocateFailureHandler(IHandleLocateFailure locateFailureHandler) {
        return new ObjectLocator(parent, useShareSupplierCache, additionalSupplierFinders, locateFailureHandler);
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
            } catch (ObjectCreationException e) {
                throw e;
            } catch (Throwable e) {
                throw new ObjectCreationException(theGivenClass, e);
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
            supplier = supplier.or(NoSupplier);
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
        
        return ()->handleLoateFailure(theGivenClass);
    }
    
    private <T> Object handleLoateFailure(Class<T> theGivenClass) {
        if (this.locateFailureHandler.isNotNull()) {
            return callHandler(theGivenClass);
        } else {
            return defaultHandling(theGivenClass);
        }
    }
    private <T> Object callHandler(Class<T> theGivenClass) {
        T value = this.locateFailureHandler.handle(theGivenClass);
        return value;
    }
    private <T> Object defaultHandling(Class<T> theGivenClass) {
        if (theGivenClass.isInterface()
         || Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        return null;
    }
    
}
