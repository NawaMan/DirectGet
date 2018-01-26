package directget.objectlocator.impl;

import directget.objectlocator.api.ILocateObject;

@FunctionalInterface
public interface ICreateObject<TYPE> {
    
    public TYPE create(ILocateObject objectLocator);
    
}
