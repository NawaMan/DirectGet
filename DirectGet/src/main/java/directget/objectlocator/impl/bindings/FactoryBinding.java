package directget.objectlocator.impl.bindings;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.IBind;
import directget.objectlocator.impl.ICreateObject;
import lombok.val;

public class FactoryBinding<TYPE> implements IBind<TYPE> {
    
    private final ICreateObject<TYPE> factory;
    
    public FactoryBinding(ICreateObject<TYPE> factory) {
        this.factory = factory;
    }
    
    @Override
    public TYPE get(ILocateObject objectLocator) {
        val value = this.factory.create(objectLocator);
        return value;
    }
    
}
