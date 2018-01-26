package directget.get;

import directget.objectlocator.ILocateObject;
import directget.objectlocator.supplierfinders.IFindSupplier;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class DefaultRefSupplierFinder implements IFindSupplier {

    @Override
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   theGivenClass,
            ILocateObject objectLocator) {
        val defaultRef = Ref.findDefaultRefOf(theGivenClass);
        return defaultRef.isNotNull()
                ? ()->defaultRef.asSupplier().get()
                : null;
    }
    
}
