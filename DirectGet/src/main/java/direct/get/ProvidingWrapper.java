package direct.get;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProvidingWrapper<T> implements Providing<T>{

	private final Ref<T> ref;
	private final Supplier<Providing<T>> actual;
	
	ProvidingWrapper(Ref<T> ref, Supplier<Providing<T>> actual) {
		this.ref    = Objects.requireNonNull(ref);
		this.actual = Objects.requireNonNull(actual);
	}

	private CurrentContext providing(Function<Providing<T>, CurrentContext> action) {
		Providing<T> actualProviding = this.actual.get();
		if (actualProviding == null) {
			throw new ProvidingException("No actual providing", ref);
		}
		CurrentContext context = action.apply(actualProviding);
		context = Objects.requireNonNull(context);
		return context;
	}
	
	@Override
	public <V extends T> CurrentContext with(V instance) {
		return providing(actualProviding -> actualProviding.with(instance));
	}

	@Override
	public <V extends T> CurrentContext withA(Class<V> targetClass) {
		return providing(actualProviding -> actualProviding.withA(targetClass));
	}

	@Override
	public <V extends T> CurrentContext withA(Ref<V> targetRef) {
		return providing(actualProviding -> actualProviding.withA(targetRef));
	}

	@Override
	public <V extends T> CurrentContext by(Supplier<V> supplier) {
		return providing(actualProviding -> actualProviding.by(supplier));
	}

	@Override
	public <V extends T> CurrentContext by(Function<Ref<T>, V> function) {
		return providing(actualProviding -> actualProviding.by(function));
	}


}
