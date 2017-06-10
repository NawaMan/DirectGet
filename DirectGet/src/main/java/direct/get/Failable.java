package direct.get;

/**
 * Failable actions.
 * 
 * @author nawaman
 */
public class Failable {
	
	private Failable() {
	}

	@FunctionalInterface
	@SuppressWarnings("javadoc")
	public static interface Runnable<T extends Throwable> {

	    public void run() throws T;
	    
	    public default java.lang.Runnable toRunnable() {
	    	return gracefully();
	    }

		public default java.lang.Runnable gracefully() {
	    	return ()->{
	    		try {
	    			run();
	    		} catch(Throwable t) {
	    			// TODO - Make this a dedicate exception.
	    			throw new RuntimeException(t);
	    		}
	    	};
		}

		public default java.lang.Runnable carelessly() {
	    	return ()->{
	    		try {
	    			run();
	    		} catch(Throwable t) {
	    		}
	    	};
		}
		
		// TODO - Add Handler - also with default (from Get().a(ProblemHandler))
	}
	
	@FunctionalInterface
	@SuppressWarnings("javadoc")
	public static interface Supplier<V, T extends Throwable> {

	    public V get() throws T;
	    
	    public default java.util.function.Supplier<V> toSupplier() {
	    	return gracefully();
	    }

		public default java.util.function.Supplier<V> gracefully() {
	    	return ()->{
	    		try {
	    			return get();
	    		} catch(Throwable t) {
	    			// TODO - Make this a dedicate exception.
	    			throw new RuntimeException(t);
	    		}
	    	};
		}

		public default java.util.function.Supplier<V> carelessly() {
	    	return ()->{
	    		try {
	    			return get();
	    		} catch(Throwable t) {
	    			return null;
	    		}
	    	};
		}
	}

	@FunctionalInterface
	@SuppressWarnings("javadoc")
	public static interface Consumer<V, T extends Throwable> {

	    public void accept(V value) throws T;
	    
	    public default java.util.function.Consumer<V> toConsumer() {
	    	return gracefully();
	    }

		public default java.util.function.Consumer<V> gracefully() {
	    	return v->{
	    		try {
	    			accept(v);
	    		} catch(Throwable t) {
	    			// TODO - Make this a dedicate exception.
	    			throw new RuntimeException(t);
	    		}
	    	};
		}

		public default java.util.function.Consumer<V> carelessly() {
	    	return v->{
	    		try {
	    			accept(v);
	    		} catch(Throwable t) {
	    		}
	    	};
		}
	}
	
}
