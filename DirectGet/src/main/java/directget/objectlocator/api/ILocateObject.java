package directget.objectlocator.api;

import java.util.Optional;

import lombok.val;

@FunctionalInterface
public interface ILocateObject {
    
    public <TYPE> TYPE get(Class<TYPE> theGivenClass)
            throws LocateObjectException;
    
    
    
    public static Optional<ILocateObject> defaultLocator() {
        val locatorClass = utils.findClass("directget.objectlocator.impl.ObjectLocator");
        if ((locatorClass == null)
         || !ILocateObject.class.isAssignableFrom(locatorClass)) 
            Optional.empty();
        
        try {
            val locator = ILocateObject.class.cast(locatorClass.newInstance());
            return Optional.of(locator);
            
        } catch (InstantiationException | IllegalAccessException e) {
            return Optional.empty();
        }
    }
    
}
