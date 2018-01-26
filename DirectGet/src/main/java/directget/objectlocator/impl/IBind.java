package directget.objectlocator.impl;

import directget.objectlocator.api.ILocateObject;
import dssb.failable.Failable.Supplier;

@FunctionalInterface
public interface IBind<TYPE> {
    
    public TYPE get(ILocateObject objectLocator);
    
}
