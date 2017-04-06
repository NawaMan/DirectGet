package direct.get.exceptions;

import direct.get.Ref;

public class UnknownProviderException extends ProvidingException {
	
	private static final long serialVersionUID = 3484390985471205919L;
	
	public UnknownProviderException(Ref<?> ref) {
        super(ref);
    }

}
