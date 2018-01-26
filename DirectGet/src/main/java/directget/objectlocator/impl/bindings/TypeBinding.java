package directget.objectlocator.impl.bindings;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.IBind;
import lombok.val;

public class TypeBinding<TYPE> implements IBind<TYPE> {
    
    private final Class<? extends TYPE> referedType;
    
    public TypeBinding(Class<? extends TYPE> referedType) {
        this.referedType = referedType;
    }
    
    @Override
    public TYPE get(ILocateObject objectLocator) {
        val value = (TYPE)objectLocator.get(referedType);
        return value;
    }
    
}
