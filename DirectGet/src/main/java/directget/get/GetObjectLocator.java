package directget.get;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.exception.CreationException;

public class GetObjectLocator implements ILocateObject {
    
    public static final GetObjectLocator instance = new GetObjectLocator();
    
    @Override
    public <TYPE> TYPE get(Class<TYPE> theGivenClass) throws CreationException {
        return Get.the(theGivenClass);
    }
    
}
