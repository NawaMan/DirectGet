package direct.get;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CurrentContext {

	public final class Providing<T> {
		
		private final Class<T> clzz;
		
		Providing(Class<T> clzz) {
			this.clzz = clzz;
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
			context.providers.put(clzz, optValue);
		}
		
		public <V extends T> CurrentContext with(V instance) {
			putProviding(optValue(c->instance));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext withA(Class<V> targetClass) {
			putProviding(optValue(c->Get.a(targetClass)));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext by(Supplier<V> supplier) {
			putProviding(optValue(c->supplier.get()));
			return CurrentContext.this;
		}
		
		public <V extends T> CurrentContext by(Function<Class<T>, V> function) {
			putProviding(optValue(c->function.apply(clzz)));
			return CurrentContext.this;
		}
		
	}

	public <T> Providing<T> provide(Class<T> clzz) {
		
		
		return new Providing<>(clzz);
	}
	
}
