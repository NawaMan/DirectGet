package direct.get;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Providing<T> {

	public <V extends T> CurrentContext with(V instance);
	
	public <V extends T> CurrentContext withA(Class<V> targetClass);
	
	public <V extends T> CurrentContext withA(Ref<V> targetRef);
	
	public <V extends T> CurrentContext by(Supplier<V> supplier);
	
	public <V extends T> CurrentContext by(Function<Ref<T>, V> function);
	
}
