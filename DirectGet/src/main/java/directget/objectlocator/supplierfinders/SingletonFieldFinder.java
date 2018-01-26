package directget.objectlocator.supplierfinders;

import static java.util.Arrays.stream;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.Optional;

import directget.objectlocator.ILocateObject;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class SingletonFieldFinder implements IFindSupplier {

    @SuppressWarnings({ "unchecked" })
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        val fieldValue = findValueFromSingletonField(theGivenClass);
        if (fieldValue.isNotNull())
            return (Supplier<TYPE, THROWABLE>) fieldValue;
        
        return null;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Supplier findValueFromSingletonField(Class<T> theGivenClass) {
        return (Supplier)stream(theGivenClass.getDeclaredFields())
                .filter(field->Modifier.isStatic(field.getModifiers()))
                .filter(field->Modifier.isPublic(field.getModifiers()))
                .filter(field->extensions.hasAnnotation(field.getAnnotations(), "Default"))
                .map(field->{
                    val type = field.getType();
                    if (theGivenClass.isAssignableFrom(type))
                        return (Supplier)(()->field.get(theGivenClass));
                    
                    if (Optional.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)field.getGenericType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        
                        if (theGivenClass.isAssignableFrom(actualType))
                            return (Supplier)(()->((Optional)field.get(theGivenClass)).orElse(null));
                    }
                    
                    if (java.util.function.Supplier.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)field.getGenericType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        
                        if (theGivenClass.isAssignableFrom(actualType))
                            return (Supplier)()->((java.util.function.Supplier)field.get(theGivenClass)).get();
                    }
                    
                    return null;
                })
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
    
}
