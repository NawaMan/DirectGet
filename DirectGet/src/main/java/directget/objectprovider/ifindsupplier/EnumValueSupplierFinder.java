package directget.objectprovider.ifindsupplier;

import static java.util.Arrays.stream;

import directget.objectprovider.CreationException;
import dssb.failable.Failable.Supplier;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ extensions.class })
public class EnumValueSupplierFinder implements IFindSupplier {

    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(Class<TYPE> theGivenClass) {
        if (!theGivenClass.isEnum()) 
            return null;
        
        val enumValue = findDefaultEnumValue(theGivenClass);
        return ()->enumValue;
    }
    
    private static <T> T findDefaultEnumValue(Class<T> theGivenClass) {
        T[] enumConstants = theGivenClass.getEnumConstants();
        if (enumConstants.length == 0)
            return null;
        return stream(enumConstants)
                .filter(value->checkDefaultEnumValue(theGivenClass, value))
                .findAny()
                .orElse(enumConstants[0]);
    }
    
    @SuppressWarnings("rawtypes")
    private static <T> boolean checkDefaultEnumValue(Class<T> theGivenClass, T value) {
        val name = ((Enum)value).name();
        try {
            return theGivenClass.getField(name).getAnnotations().hasAnnotation("Default");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new CreationException(theGivenClass, e);
        }
    }
    
}
