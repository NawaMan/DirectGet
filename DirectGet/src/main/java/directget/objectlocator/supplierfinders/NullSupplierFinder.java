package directget.objectlocator.supplierfinders;

import static directget.objectlocator.supplierfinders.common.NullSupplier;

import directget.objectlocator.ILocateObject;
import directget.objectlocator.annotations.DefaultToNull;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
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
