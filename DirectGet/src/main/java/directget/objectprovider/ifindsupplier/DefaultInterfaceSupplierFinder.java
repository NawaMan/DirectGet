package directget.objectprovider.ifindsupplier;

import static directget.objectprovider.ifindsupplier.common.NullSupplier;

import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class DefaultInterfaceSupplierFinder implements IFindSupplier {

    @SuppressWarnings("unchecked")
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(Class<TYPE> theGivenClass) {
        boolean isDefaultInterface
                =  theGivenClass.isInterface()
                && theGivenClass.getAnnotations().hasAnnotation("DefaultInterface");
        // TODO Implement this.
        return isDefaultInterface
                ? NullSupplier
                : null;
    }
    
}
