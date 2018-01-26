package directget.objectprovider.ifindsupplier;

import static directget.objectprovider.ifindsupplier.common.NullSupplier;
import static java.util.Arrays.stream;

import java.util.Objects;

import directget.get.Get;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class DefautImplementationSupplierFinder implements IFindSupplier {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(Class<TYPE> theGivenClass) {
        if (theGivenClass.getAnnotations().hasAnnotation("DefaultImplementation")) {
            val defaultImplementationClass = findDefaultImplementation(theGivenClass);
            if (defaultImplementationClass.isNotNull()) {
                return new Supplier() {
                    @Override
                    public Object get() throws Throwable {
                        return getValueOf(defaultImplementationClass);
                    }
                };
            }
            return NullSupplier;
        }
        
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    private static <T> Class findDefaultImplementation(Class<T> theGivenClass) {
        return stream(theGivenClass.getAnnotations())
            .map(Object::toString)
            .map(toString->toString.replaceAll("^(.*\\(value=)(.*)(\\))$", "$2"))
            .map(DefautImplementationSupplierFinder::findClass)
            .filter(Objects::nonNull)
            .filter(theGivenClass::isAssignableFrom)
            .findAny()
            .orElse(null);
    }
    
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
    
    protected <T> T getValueOf(Class<T> clzz) {
        return Get.the(clzz);
    }
    
}
