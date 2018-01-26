package directget.objectlocator.impl;

import directget.objectlocator.api.ILocateObject;

@FunctionalInterface
public interface IBind<TYPE> {
    
    public TYPE get(ILocateObject objectLocator);
    
}
