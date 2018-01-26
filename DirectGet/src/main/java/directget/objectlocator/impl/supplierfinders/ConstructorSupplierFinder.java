package directget.objectlocator.impl.supplierfinders;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.annotations.Inject;
import directget.objectlocator.impl.exception.ObjectCreationException;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class ConstructorSupplierFinder extends MethodSupplierFinder implements IFindSupplier {
    
    public static final String INJECT = Inject.class.getSimpleName();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        val constructor = findConstructor(theGivenClass);
        if (constructor.isNotNull()) {
            val supplier = new Supplier() {
                public Object get() throws Throwable {
                    return callConstructor(theGivenClass, constructor, objectLocator);
                }
            };
            return (Supplier<TYPE, THROWABLE>) supplier;
        }
        return null;
    }
    
    @SuppressWarnings({ "rawtypes" })
    private <T> Constructor findConstructor(Class<T> clzz) {
        Constructor foundConstructor = findConstructorWithInject(clzz);
        if (foundConstructor.isNull()) {
            foundConstructor = hasOnlyOneConsructor(clzz)
                    ? getOnlyConstructor(clzz)
                    : getNoArgConstructor(clzz);
        }
        
        if (foundConstructor.isNull()
         || !Modifier.isPublic(foundConstructor.getModifiers()))
            return null;
        
        return foundConstructor;
    }
    
    @SuppressWarnings("rawtypes")
    private <T> Object callConstructor(Class<T> theGivenClass, Constructor constructor, ILocateObject objectLocator)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        val params   = getParameters(constructor, objectLocator);
        val instance = constructor.newInstance(params);
        // TODO - Change to use method handle later.
        return theGivenClass.cast(instance);
    }
    
    @SuppressWarnings({ "rawtypes" })
    private Object[] getParameters(Constructor constructor, ILocateObject objectLocator) {
        val paramsArray = constructor.getParameters();
        val params = new Object[paramsArray.length];
        for (int i = 0; i < paramsArray.length; i++) {
            val param             = paramsArray[i];
            val paramType         = param.getType();
            val parameterizedType = param.getParameterizedType();
            boolean isNullable    = param.getAnnotations().hasAnnotation("Nullable");
            Object paramValue     = getParameterValue(paramType, parameterizedType, isNullable, objectLocator);
            params[i] = paramValue;
        }
        return params;
    }
    
    public static <T> boolean hasOnlyOneConsructor(final java.lang.Class<T> clzz) {
        return clzz.getConstructors().length == 1;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> findConstructorWithInject(Class<T> clzz) {
        for(Constructor<?> constructor : clzz.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers()))
                continue;
            
            if (extensions.hasAnnotation(constructor.getAnnotations(), INJECT))
                return (Constructor<T>)constructor;
        }
        return null;
    }
    
    public static <T> Constructor<T> getNoArgConstructor(Class<T> clzz) {
        try {
            return clzz.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        } catch (SecurityException e) {
            throw new ObjectCreationException(clzz);
        }
    }
    
    public static <T> Constructor<?> getOnlyConstructor(Class<T> clzz) {
        return clzz.getConstructors()[0];
    }
    
}
