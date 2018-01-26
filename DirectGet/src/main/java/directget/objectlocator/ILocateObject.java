package directget.objectlocator;

public interface ILocateObject {
    
    public <TYPE> TYPE locate(Class<TYPE> theGivenClass)
            throws CreationException;
    
}
