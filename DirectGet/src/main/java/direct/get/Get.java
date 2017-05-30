package direct.get;

import java.util.Optional;
import java.util.function.Supplier;

import direct.get.exceptions.GetException;

public final class Get {
	
	private Get() {
		
	}
		
	public static <T> Optional<T> _a(Ref<T> ref) {
		return AppSpace.get._a(ref);
	}

	public static <T> T a(Class<T> clzz) {
		return a(Ref.forClass(clzz));
	}
	
	public static <T> T a(Ref<T> ref) {
		return _a(ref).orElse(null);
	}
	
	public static <T> T a(Class<T> clzz, T elseValue) {
		return a(Ref.forClass(clzz), elseValue);
	}
	
	public static <T> T a(Ref<T> ref, T elseValue) {
		try {
			return _a(ref).orElse(elseValue);
		} catch (GetException e) {
			return elseValue;
		}
	}
	
	public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
		return a(Ref.forClass(clzz), elseSupplier);
	}
	
	public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
		return _a(ref).orElseGet(elseSupplier);
	}
	
	public static <T> Optional<T> _a(Class<T> clzz) {
		return _a(Ref.forClass(clzz));
	}
	
	public static <T> GetInstance substitute(Providing<T> providing, Runnable runnable) {
		 return AppSpace.get.substitute(providing, runnable);
	}

	public static <T> WithSubstitution withSubstitution(Providing<T> providing) {
		return AppSpace.get.withSubstitution(providing);
	}
	
}
