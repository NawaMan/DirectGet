package direct.get;

import java.util.function.Predicate;
import java.util.function.Supplier;


/**
 * This class offer a way to make it easy to debug lambda by adding name to them.
 * 
 * @author nawaman
 **/
// https://stackoverflow.com/questions/42876840/namingtostring-lambda-expressions-for-debugging-purpose/42876841#42876841
// https://stackoverflow.com/questions/23704355/creating-string-representation-of-lambda-expression/23705160#23705160
public class Named {
	
	/** The ready to use object. */
	public static Named.User instance = new User() {};
	
	private Named() {
		
	}
	
	/** Add name to the given predicate. */
	public static <T> Predicate<T> predicate(String name, Predicate<T> check) {
		return instance.predicate(name, check);
	}
	
	/** Add name to the given predicate. */
	public static <T> Predicate<T> Predicate(String name, Predicate<T> check) {
		return instance.predicate(name, check);
	}
	
	/** Add name to the given supplier. */
	public static <T> Supplier<T> supplier(String name, Supplier<T> supplier) {
		return instance.supplier(name, supplier);
	}
	
	/** Add name to the given supplier. */
	public static <T> Supplier<T> Supplier(String name, Supplier<T> supplier) {
		return instance.supplier(name, supplier);
	}
	
	/** Add name to the given runnable. */
	public static Runnable runnable(String name, Runnable runnable) {
		return instance.runnable(name, runnable);
	}
	
	/** Add name to the given runnable. */
	public static Runnable Runnable(String name, Runnable runnable) {
		return instance.runnable(name, runnable);
	}
	
	/**
	 * This interface make it possible to the user of the class to use these 
	 *   method without static import.
	 **/
	public static interface User {

		/** Add name to the given predicate. */
		public default <T> Predicate<T> predicate(String name, Predicate<T> check) {
			return new Predicate<T>() {
				@Override
				public boolean test(T t) {
					return check.test(t);
				}
				@Override
				public String toString() {
					return "Predicate(" + name + ")";
				}
			};
		}

		/** Add name to the given supplier. */
		public default Runnable runnable(String name, Runnable runnable) {
			return new Runnable() {
				@Override
				public void run() {
					runnable.run();
				}
				@Override
				public String toString() {
					return "Runnable(" + name + ")";
				}
			};
		}
		
		/** Add name to the given runnable. */
		public default <T> Supplier<T> supplier(String name, Supplier<T> supplier) {
			return new Supplier<T>() {
				@Override
				public T get() {
					return supplier.get();
				}
				@Override
				public String toString() {
					return "Supplier(" + name + ")";
				}
			};
		}
		
		/** Add name to the given predicate. */
		public default <T> Predicate<T> Predicate(String name, Predicate<T> check) {
			return instance.predicate(name, check);
		}
		
		/** Add name to the given supplier. */
		public default <T> Supplier<T> Supplier(String name, Supplier<T> supplier) {
			return instance.supplier(name, supplier);
		}
		
		/** Add name to the given runnable. */
		public default Runnable Runnable(String name, Runnable runnable) {
			return instance.runnable(name, runnable);
		}
		
	}
	
}
