package directget.objectlocator.impl;

@FunctionalInterface
public interface IHandleLocateFailure {
    
    public <T> T handle(Class<T> theGivenClass);
    
}
