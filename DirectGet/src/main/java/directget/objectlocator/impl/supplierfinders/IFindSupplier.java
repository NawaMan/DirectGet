package directget.objectlocator.impl.supplierfinders;

import directget.objectlocator.api.ILocateObject;
import dssb.failable.Failable.Supplier;

@FunctionalInterface
public interface IFindSupplier {
    
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(
            Class<TYPE>   clss,
            ILocateObject objectLocator);
    
}
