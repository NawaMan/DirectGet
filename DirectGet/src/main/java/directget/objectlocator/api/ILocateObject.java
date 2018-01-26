package directget.objectlocator.api;

@FunctionalInterface
public interface ILocateObject {
    
    public <TYPE> TYPE get(Class<TYPE> theGivenClass)
            throws LocateObjectException;
    
}
