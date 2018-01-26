package directget.objectlocator.impl.supplierfinders;

import static java.util.Arrays.stream;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import directget.objectlocator.api.ILocateObject;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class FactoryMethodSupplierFinder extends MethodSupplierFinder implements IFindSupplier {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        val methodValue = findValueFromFactoryMethod(theGivenClass, objectLocator);
        if (methodValue.isNotNull())
            return (Supplier<TYPE, THROWABLE>)methodValue;
        
        return null;
    }
    
    @SuppressWarnings({ "rawtypes"})
    private <T> Supplier findValueFromFactoryMethod(Class<T> theGivenClass, ILocateObject objectLocator) {
        return (Supplier)stream(theGivenClass.getDeclaredMethods())
                .filter(method->Modifier.isStatic(method.getModifiers()))
                .filter(method->Modifier.isPublic(method.getModifiers()))
                .filter(method->extensions.hasAnnotation(method.getAnnotations(), "Default"))
                .map(method->FactoryMethodSupplierFinder.this.getFactoryMethodValue(theGivenClass, method, objectLocator))
                .findAny()
                .orElse(null);
    }
    
    @SuppressWarnings("rawtypes")
    private <T> Supplier getFactoryMethodValue(Class<T> theGivenClass, Method method, ILocateObject objectLocator) {
        val type = method.getReturnType();
        if (theGivenClass.isAssignableFrom(type))
            return (Supplier)(()->basicFactoryMethodCall(theGivenClass, method, objectLocator));
        
        if (Optional.class.isAssignableFrom(type)) {
            val parameterizedType = (ParameterizedType)method.getGenericReturnType();
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (theGivenClass.isAssignableFrom(actualType))
                return (Supplier)(()->optionalFactoryMethodCall(theGivenClass, method, objectLocator));
        }
        
        if (java.util.function.Supplier.class.isAssignableFrom(type)) {
            val parameterizedType = (ParameterizedType)method.getGenericReturnType();
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            val getMethod         = getGetMethod();
            
            if (theGivenClass.isAssignableFrom(actualType))
                return (Supplier)()->supplierFactoryMethodCall(theGivenClass, method, getMethod, objectLocator);
        }
        
        return null;
    }
    
    private static Method getGetMethod() {
        try {
            // TODO - Change to use MethodHandler.
            return java.util.function.Supplier.class.getMethod("get", new Class[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            // I am sure it is there.
            throw new RuntimeException(e);
        }
    }
    
    private <T> Object supplierFactoryMethodCall(
            Class<T> theGivenClass,
            Method method,
            Method getMethod,
            ILocateObject objectLocator) 
                    throws IllegalAccessException, InvocationTargetException {
        val params   = getMethodParameters(method, objectLocator);
        val result   = method.invoke(theGivenClass, params);
        val value    = getMethod.invoke(result);
        return value;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Object optionalFactoryMethodCall(Class<T> theGivenClass, Method method, ILocateObject objectLocator)
            throws IllegalAccessException, InvocationTargetException {
        val params = getMethodParameters(method, objectLocator);
        val value = method.invoke(theGivenClass, params);
        return ((Optional)value).orElse(null);
    }
    
    private <T> Object basicFactoryMethodCall(Class<T> theGivenClass, Method method, ILocateObject objectLocator)
            throws IllegalAccessException, InvocationTargetException {
        val params = getMethodParameters(method, objectLocator);
        val value = method.invoke(theGivenClass, params);
        return value;
    }
    
}
