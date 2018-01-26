package directget.objectlocator.impl.supplierfinders;

import static directget.objectlocator.impl.supplierfinders.common.NullSupplier;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.annotations.DefaultToNull;
import dssb.failable.Failable.Supplier;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ extensions.class })
public class NullSupplierFinder implements IFindSupplier {
    
    public static final String ANNOTATION_NAME = DefaultToNull.class.getSimpleName();
    
    @SuppressWarnings("unchecked")
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        return theGivenClass.getAnnotations().hasAnnotation(ANNOTATION_NAME)
                ? (Supplier<TYPE, THROWABLE>)NullSupplier
                : null;
    }
    
}
