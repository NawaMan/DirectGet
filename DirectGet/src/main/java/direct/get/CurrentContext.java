package direct.get;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CurrentContext {

	public final class Providing<T> {
		
		private final Ref<T> ref;
		
		Providing(Ref<T> ref) {
			this.ref = ref;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private Optional optValue(Function function) {
			Provider provider = c->function.apply(c);
			Optional optional = Optional.of(provider);
			return optional;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void putProviding(Optional optValue) {
			Get get = Get.a(Get.class);
			Context context = get.getContext();
			context.providers.put(ref, optValue);
		}
		
		public <V extends T> CurrentContext with(V instance) {
			putProviding(optValue(c->instance));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext withA(Class<V> targetClass) {
			return withA(Ref.ofClass(targetClass));
		}
		
		public <V extends T> CurrentContext withA(Ref<V> targetRef) {
			putProviding(optValue(c->Get.a(targetRef)));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext by(Supplier<V> supplier) {
			putProviding(optValue(c->supplier.get()));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext by(Function<Ref<T>, V> function) {
			putProviding(optValue(c->function.apply(ref)));
			return CurrentContext.this;
		}
		
	}

	public <T> Providing<T> provide(Class<T> targetClass) {
		// Use ProvidingWrapper to hide plug this current context leak
		return new Providing<>(Ref.ofClass(targetClass));
		
	}

	public <T> Providing<T> provide(Ref<T> ref) {
		// Use ProvidingWrapper to hide plug this current context leak
		return new Providing<>(ref);
		
	}
	
}
