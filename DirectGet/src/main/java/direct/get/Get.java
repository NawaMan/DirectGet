package direct.get;

import direct.get.exceptions.GetException;

public class Get {

	public static <T> T a(Class<T> clzz) {
		try {
			return clzz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new GetException(e);
		}
	}
	
}
