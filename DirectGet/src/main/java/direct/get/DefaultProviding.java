package direct.get;

import java.util.Objects;

public class DefaultProviding {
	
	static class RefWithDefaultProvider<T> implements Ref<T> {
		
		private final Ref<T> ref;
		private final Provider<T> provider;
		
		RefWithDefaultProvider(final Ref<T> ref, Provider<T> provider) {
			this.ref = Objects.requireNonNull(ref);
			this.provider = Objects.requireNonNull(provider);
		}
		
		public Provider<T> getProvider() {
			return this.provider;
		}

		@Override
		public Class<T> getTargetClass() {
			return this.ref.getTargetClass();
		}
		
	}

}
