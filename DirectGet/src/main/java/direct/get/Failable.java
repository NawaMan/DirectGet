package direct.get;

import direct.get.exceptions.FailableException;

/**
 * Failable actions.
 * 
 * @author nawaman
 */
public class Failable {
    
    private Failable() {
    }
    
    /** Failable runnable.  */
    @FunctionalInterface
    public static interface Runnable<T extends Throwable> {
        
        /** Run this runnabe. */
        public void run() throws T;
        
        /** Change to regular runnable. */
        public default java.lang.Runnable toRunnable() {
            return gracefully();
        }

        /** Convert to a regular runnable and throw FailableException if there is an exception. */
        public default java.lang.Runnable gracefully() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        /** Convert to a regular runnable that completely ignore the exception throw from it. */
        public default java.lang.Runnable carelessly() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
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
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        public default java.util.function.Supplier<V> carelessly() {
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
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
            return v -> {
                try {
                    accept(v);
                } catch (Throwable t) {
                    throw new FailableException(t);
                }
            };
        }
        
        public default java.util.function.Consumer<V> carelessly() {
            return v -> {
                try {
                    accept(v);
                } catch (Throwable t) {
                }
            };
        }
    }
    
}
