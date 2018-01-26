package directget.get;

import directget.objectlocator.api.ILocateObject;
import directget.objectlocator.impl.exception.ObjectCreationException;

public class GetObjectLocator implements ILocateObject {
    
    public static final GetObjectLocator instance = new GetObjectLocator();
    
    @Override
    public <TYPE> TYPE get(Class<TYPE> theGivenClass) throws ObjectCreationException {
        return Get.the(theGivenClass);
    }
    
}
