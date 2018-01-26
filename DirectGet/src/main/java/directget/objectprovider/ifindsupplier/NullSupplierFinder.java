package directget.objectprovider.ifindsupplier;

import static directget.objectprovider.ifindsupplier.common.NullSupplier;

import directget.objectprovider.annotations.DefaultToNull;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ extensions.class })
public class NullSupplierFinder implements IFindSupplier {
    
    public static final String ANNOTATION_NAME = DefaultToNull.class.getSimpleName();
    
    @SuppressWarnings("unchecked")
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(Class<TYPE> theGivenClass) {
        return theGivenClass.getAnnotations().hasAnnotation(ANNOTATION_NAME)
                ? (Supplier<TYPE, THROWABLE>)NullSupplier
                : null;
    }
    
}
