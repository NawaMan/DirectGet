package directget.objectlocator.impl.supplierfinders;

import static directget.objectlocator.impl.supplierfinders.common.NullSupplier;

import directget.objectlocator.api.ILocateObject;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class, extensions.class })
public class DefaultInterfaceSupplierFinder implements IFindSupplier {

    @SuppressWarnings("unchecked")
    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        boolean isDefaultInterface
                =  theGivenClass.isInterface()
                && theGivenClass.getAnnotations().hasAnnotation("DefaultInterface");
        // TODO Implement this.
        return isDefaultInterface
                ? NullSupplier
                : null;
    }
    
}
