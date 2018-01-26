package directget.objectprovider;

public interface IProvideObject {
    
    public <TYPE> TYPE provide(Class<TYPE> theGivenClass)
            throws CreationException;
    
}
