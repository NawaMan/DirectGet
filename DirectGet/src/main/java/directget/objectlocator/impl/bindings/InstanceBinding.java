package directget.objectlocator.impl.bindings;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.IBind;

public class InstanceBinding<TYPE> implements IBind<TYPE> {
    
    private final TYPE instance;
    
    public InstanceBinding(TYPE instance) {
        this.instance = instance;
    }
    
    @Override
    public TYPE get(ILocateObject objectLocator) {
        return this.instance;
    }
    
}
