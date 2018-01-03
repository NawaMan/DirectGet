package directget.get.supportive;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import dssb.utils.common.Nulls;
import directget.get.Get;
import directget.get.InjectedConstructor;
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
@ExtensionMethod({ Nulls.class })
public class ObjectCreator {
    
    // TODO - Should create interface with all default method.
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, Supplier> suppliers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private static final Class injectClass = findClass("javax.inject.Inject");
    @SuppressWarnings("rawtypes")
    private static final Class nullableClass = findClass("javax.annotations.Nullable");
    
    /**
     * Find the {@code java.inject.Inject} class by name.
     * @return the class if found or {@code null} if not.
     */
    static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Create an instance of the given class.
     * 
     * @param theGivenClass
     * @return the created value.
     * @throws CreationException when there is a problem creating the object.
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object[] getParameters(Constructor constructor) {
        val params = new Object[constructor.getParameterCount()];
        val paramsArray = constructor.getParameters();
        for (int i = 0; i < paramsArray.length; i++) {
            val param             = paramsArray[i];
            val paramType         = param.getType();
            val parameterizedType = param.getParameterizedType();
            val isNullable         = (nullableClass != null) ? (param.getAnnotation(nullableClass) != null): false;
            val paramValue        = getParameterValue(paramType, parameterizedType, isNullable);
            params[i] = paramValue;
        }
        return params;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object getParameterValue(Class paramType, Type type, boolean isNullable) {
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType)type;
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (paramType == Supplier.class)
                return ((Supplier)()->Get.the(actualType));
            
            if (paramType == java.util.function.Supplier.class)
                return ((Supplier)()->Get.the(actualType)).gracefully();
            
            if (paramType == Optional.class)
                return getOptionalValueOrNullWhenFailAndNullable(isNullable, actualType);
        }
        
        if (isNullable)
            return getValueOrNullWhenFail(paramType);
        
        val paramValue = Get.the(paramType);
        return paramValue;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getOptionalValueOrNullWhenFailAndNullable(boolean isNullable, Class actualType) {
        try {
            val paramValue = Get.the(actualType);
            return Optional.ofNullable(paramValue);
        } catch (Exception e) {
            return isNullable ? null : Optional.empty();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getValueOrNullWhenFail(Class paramType) {
        try {
            return Get.the(paramType);
        } catch (Exception e) {
            return null;
        }
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
