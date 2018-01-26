package directget.objectlocator.impl.supplierfinders;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import directget.objectlocator.api.ILocateObject;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;


// TODO - Change this to composite to inherit
@ExtensionMethod({ Nulls.class, extensions.class })
public abstract class MethodSupplierFinder implements IFindSupplier {
    
    protected Object[] getMethodParameters(Method method, ILocateObject objectLocator) {
        val paramsArray = method.getParameters();
        val params = new Object[paramsArray.length];
        for (int i = 0; i < paramsArray.length; i++) {
            val param             = paramsArray[i];
            val paramType         = param.getType();
            val parameterizedType = param.getParameterizedType();
            boolean isNullable    = param.getAnnotations().hasAnnotation("Nullable");
            Object  paramValue    = getParameterValue(paramType, parameterizedType, isNullable, objectLocator);
            params[i] = paramValue;
        }
        return params;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Object getParameterValue(Class paramType, Type type, boolean isNullable, ILocateObject objectLocator) {
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType)type;
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (paramType == Supplier.class)
                return new Supplier() {
                    @Override
                    public Object get() throws Throwable {
                        return objectLocator.get(actualType);
                    }
                };
            
            if (paramType == java.util.function.Supplier.class)
                return new java.util.function.Supplier() {
                    @Override
                    public Object get() {
                        return objectLocator.get(actualType);
                    }
                };
            
            if (paramType == Optional.class)
                return getOptionalValueOrNullWhenFailAndNullable(isNullable, actualType, objectLocator);
        }
        
        if (isNullable)
            return getValueOrNullWhenFail(paramType, objectLocator);
        
        val paramValue = objectLocator.get(paramType);
        return paramValue;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object getOptionalValueOrNullWhenFailAndNullable(boolean isNullable, Class actualType, ILocateObject objectLocator) {
        try {
            val paramValue = objectLocator.get(actualType);
            return Optional.ofNullable(paramValue);
        } catch (Exception e) {
            return isNullable ? null : Optional.empty();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object getValueOrNullWhenFail(Class paramType, ILocateObject objectLocator) {
        try {
            return objectLocator.get(paramType);
        } catch (Exception e) {
            return null;
        }
    }
    
}
