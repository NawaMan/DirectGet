package directget.get.supportive;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import directget.get.Get;
import directget.get.InjectedConstructor;
import directget.get.The;
import directget.get.exceptions.AbstractClassCreationException;
import directget.get.exceptions.CreationException;
import directget.get.run.Failable.Supplier;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This utility class can create an object using Get.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Utilities.class })
public class ObjectCreatator {
    
    // TODO - All @The to the InjectedConstructor ...
    //          that will get from the Ref in that target class with @DefaultRef

    @SuppressWarnings("rawtypes")
    private static final Map<Class, Supplier> suppliers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private static final Class injectClass = findInjectClass();
    
    /**
     * Find the {@code java.inject.Inject} class by name.
     * @return the class if found or {@code null} if not.
     */
    static Class<?> findInjectClass() {
        try { 
            return Class.forName("javax.inject.Inject");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Create an instance of the given class.
     * 
     * @param theGivenClass
     * @return the created value.
     */
    @SuppressWarnings("rawtypes")
    public static <T> T createNew(@NonNull Class<T> theGivenClass) throws CreationException {
        if (Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        try {
            Supplier supplier = suppliers.get(theGivenClass);
            if (supplier == null) {
                supplier = NewSupplierFor(theGivenClass);
                suppliers.put(theGivenClass, supplier);
            }

            val instance = supplier.get();
            return theGivenClass.cast(instance);
        } catch (Throwable e) {
            throw new CreationException(theGivenClass, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static <T> Supplier NewSupplierFor(Class<T> theGivenClass) throws NoSuchMethodException {
        val constructor = findConstructor(theGivenClass);
        Supplier supplier = ()->{
            val params   = getParameters(constructor);
            val instance = constructor.newInstance(params);
            return theGivenClass.cast(instance);
        };
        return supplier;
    }

    @SuppressWarnings("rawtypes")
    private static Object[] getParameters(Constructor constructor) {
        val params = new Object[constructor.getParameterCount()];
        val paramsArray = constructor.getParameters();
        for (int i = 0; i < paramsArray.length; i++) {
            val param      = paramsArray[i];
            val isThe      = param.getAnnotation(The.class) != null;
            val paramType  = param.getType();
            val paramValue = isThe? Get.the(paramType) : Get.a(paramType);
            params[i] = paramValue;
        }
        return params;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Constructor findConstructor(final java.lang.Class<T> clzz) throws NoSuchMethodException {
        Constructor foundConstructor = null;
        for(Constructor c : clzz.getConstructors()) {
            if (!Modifier.isPublic(c.getModifiers()))
                continue;
            
            if (c.getAnnotation(InjectedConstructor.class) != null) {
                foundConstructor = c;
                break;
            }
            if (injectClass.whenNotNull().map(c::getAnnotation).isPresent()) {
                foundConstructor = c;
                break;
            }
        }
        if (foundConstructor == null) {
            if (clzz.getConstructors().length == 1)
                 foundConstructor = clzz.getConstructors()[0];
            else foundConstructor = clzz.getConstructor();
        }
        
        foundConstructor.setAccessible(true);
        return foundConstructor;
    }
    
}
