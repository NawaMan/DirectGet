package directget.get.run;

import directget.get.Get;
import directget.get.run.exceptions.ProblemHandler;

/**
 * Failable with handler.
 * 
 * @author NawaMan
 */
public class HandledFailable {
    
    private HandledFailable() {
    }
    
    /**
     * Failable actions.
     * 
     * @author NawaMan
     * 
     * @param <T>
     *            the throwable.
     */
    public interface Runnable<T extends Throwable> extends dssb.failable.Failable.Runnable<T> {
        
        /**
         * Create a HandledFailableRunnable from the given runnable.
         * 
         * @param runnable
         *            the runnable.
         * @return the HandledFailableRunnable.
         */
        public static <T extends Throwable> Runnable<T> of(dssb.failable.Failable.Runnable<T> runnable) {
            return () -> runnable.run();
        }
        
        /**
         * Convert to a regular runnable that handle the problem using {@code ProblemHandler}.
         * 
         * @return Java's Runnable.
         **/
        public default java.lang.Runnable handledly() {
            return () -> {
                try {
                    run();
                } catch (Throwable t) {
                    Get.the(ProblemHandler.problemHandler).handle(t);
                }
            };
        }
        
    }
    
    /**
     * Failable actions.
     * 
     * @author NawaMan
     * 
     * @param <V>
     *            the value supplied.
     * @param <T>
     *            the throwable.
     */
    public interface Supplier<V, T extends Throwable> extends dssb.failable.Failable.Supplier<V, T> {
        
        /**
         * Create a HandledFailableSupplier from the given runnable.
         * 
         * @param supplier
         *            the supplier.
         * @return the HandledFailableRunnable.
         */
        public static <V, T extends Throwable> Supplier<V, T> of(
                dssb.failable.Failable.Supplier<V, T> supplier) {
            return () -> supplier.get();
        }
        
        /**
         * Convert to a regular runnable that handle the problem using {@code ProblemHandler}.
         * 
         * @return Java's Runnable.
         **/
        public default java.util.function.Supplier<V> handledly() {
            return () -> {
                try {
                    return get();
                } catch (Throwable t) {
                    Get.the(ProblemHandler.problemHandler).handle(t);
                    return null;
                }
            };
        }
        
    }
}
