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
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
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
    
    // TODO - Add default factory.
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
    private static final ThreadLocal<Set<Class>> beingCreateds = ThreadLocal.withInitial(()->new HashSet<>());
    
    @SuppressWarnings("unchecked")
    private static final List<IFindSupplier> noAdditionalSuppliers = (List<IFindSupplier>)EMPTY_LIST;
    
    private static final Bindings noBinding = new Bindings.Builder().build();
    
    private ILocateObject        parent;
    private List<IFindSupplier>  finders;
    private IHandleLocateFailure locateFailureHandler;
    
    private Bindings binidings;
    
    @SuppressWarnings("rawtypes")
    private Map<Class, Supplier> suppliers = new ConcurrentHashMap<Class, Supplier>();
    
    private List<IFindSupplier>  additionalSupplierFinders;
    
    public ObjectLocator() {
        this(null, null, null, null);
    }
    
    @SuppressWarnings("rawtypes")
    public ObjectLocator(
            ILocateObject        parent,
            List<IFindSupplier>  additionalSupplierFinders,
            Bindings             bingings,
            IHandleLocateFailure locateFailureHandler) {
        this.parent               = parent;
        this.finders              = combineFinders(additionalSupplierFinders);
        this.locateFailureHandler = locateFailureHandler;
        this.binidings            = bingings.or(noBinding);
        
        // Supportive
        this.additionalSupplierFinders = additionalSupplierFinders;
    }
    
    // TODO - Pipeable
    @Setter
    @AllArgsConstructor
    @Accessors(fluent=true,chain=true)
    public static class Builder {
        private ILocateObject        parent;
        private List<IFindSupplier>  additionalSupplierFinders;
        private Bindings             bingings;
        private IHandleLocateFailure locateFailureHandler;
        
        public Builder() {
            this(null, null, null, null);
        }
        
        public ObjectLocator build() {
            return new ObjectLocator(parent, additionalSupplierFinders, bingings, locateFailureHandler);
        }
    }
    
    private static List<IFindSupplier> combineFinders(List<IFindSupplier> additionalSupplierFinders) {
        val finderList = new ArrayList<IFindSupplier>();
        finderList.addAll(classLevelfinders);
        finderList.addAll(additionalSupplierFinders.or(noAdditionalSuppliers));
        finderList.addAll(elementLevelfinders);
        return unmodifiableList(finderList);
    }
    
    public ObjectLocator withNewCache() {
        return new ObjectLocator(parent, additionalSupplierFinders, binidings, locateFailureHandler);
    }
    
    public ObjectLocator withSharedCache() {
        return new ObjectLocator(parent, additionalSupplierFinders, binidings, locateFailureHandler);
    }
    
    public ObjectLocator wihtLocateFailureHandler(IHandleLocateFailure locateFailureHandler) {
        return new ObjectLocator(parent, additionalSupplierFinders, binidings, locateFailureHandler);
    }
    
    public ObjectLocator wihtBindings(Bindings binidings) {
        return new ObjectLocator(parent, additionalSupplierFinders, binidings, locateFailureHandler);
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
        val binding = this.binidings.getBinding(theGivenClass);
        if (binding.isNotNull())
            return ()->binding.get(this);
        
        if (ObjectLocator.class.isAssignableFrom(theGivenClass))
            return ()->this;
        
        val parentLocator = (ILocateObject)this.parent.or(this);
        for (val finder : finders) {
            val supplier = finder.find(theGivenClass, parentLocator);
            if (supplier.isNotNull())
                return supplier;
        }
        
        if (ILocateObject.class.isAssignableFrom(theGivenClass))
            return ()->this;
        
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
