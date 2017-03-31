package direct.get.exceptions;

public class UnknownProviderException extends ProvidingException {
	
	private static final long serialVersionUID = 3484390985471205919L;
	
	public UnknownProviderException(Class<?> clzz) {
        super(clzz);
    }

}
