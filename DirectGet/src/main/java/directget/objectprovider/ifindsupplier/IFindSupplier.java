package directget.objectprovider.ifindsupplier;

import dssb.failable.Failable.Supplier;

public interface IFindSupplier {
    
    public <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> find(Class<TYPE> clss);
    
}
