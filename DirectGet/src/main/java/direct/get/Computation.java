package direct.get;

/**
 * Classes implementing this interface can perform a computation.
 *
 * @param <V> the return type.
 * 
 * @author nawaman
 */
@FunctionalInterface
public interface Computation<V, T extends Throwable> {
	
	/**
	 * Perform the computation.
	 **/
	public V run() throws T;
	
}
