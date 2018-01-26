package directget.objectlocator.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dssb.utils.common.Nulls;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

// TODO - Pipeable
@ExtensionMethod({ Nulls.class })
public class Bindings {
    
    @SuppressWarnings("rawtypes")
    private final Map<Class, IBind> bindings;
    
    Bindings(@SuppressWarnings("rawtypes") Map<Class, IBind> bindings) {
        this.bindings = Collections.unmodifiableMap(new HashMap<>(bindings));
    }
    
    @SuppressWarnings("unchecked")
    public <TYPE> IBind<TYPE> getBinding(Class<TYPE> clzz) {
        return (IBind<TYPE>)this.bindings.get(clzz);
    }
    
    public static class Builder {
        
        private final Map<Class, IBind> bindings = new HashMap<>();
        
        public <TYPE> Builder bind(@NonNull Class<TYPE> clzz, IBind<TYPE> binding) {
            if (binding.isNotNull())
                this.bindings.put(clzz, binding);
            return this;
        }
        
        public Bindings build() {
            return new Bindings(bindings);
        }
        
    }
    
}
