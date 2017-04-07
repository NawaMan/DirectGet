package direct.get;

import java.util.Objects;

@FunctionalInterface
public interface Ref<T> {
	
	public Class<T> getTargetClass();
	
	
	// == Basic implementation =========================================================================================
	
	public static <T> Ref<T> of(Class<T> targetClass) {
		return new Direct<>(targetClass);
	}
	
	public static class Direct<T> implements Ref<T> {
		
		private final Class<T> targetClass;
		
		private final String targetClassName;
		
		public Direct(Class<T> targetClass) {
			this.targetClass = targetClass;
			this.targetClassName = this.targetClass.getCanonicalName();
		}
		
		@Override
		public Class<T> getTargetClass() {
			return this.targetClass;
		}
		
		public String toString() {
			return "Ref<" + this.targetClassName + ">";
		}
		
		@SuppressWarnings("rawtypes")
		public boolean equals(Object obj) {
			if (!(obj instanceof Ref.Direct)) {
				return false;
			}
			return this.targetClass == ((Ref.Direct)obj).targetClass;
		}
		
		@Override
		public int hashCode() {
			return this.targetClass.hashCode();
		}
		
	}
	
	//== With Default provider =========================================================================================
	
	static class RefWithDefaultProvider<T> implements Ref<T> {

		private final Class<T> targetClass;
		private final Provider<T> provider;
		
		RefWithDefaultProvider(final Class<T> targetClass, Provider<T> provider) {
			this.targetClass = Objects.requireNonNull(targetClass);
			this.provider = Objects.requireNonNull(provider);
		}
		
		public Provider<T> getProvider() {
			return this.provider;
		}

		@Override
		public Class<T> getTargetClass() {
			return this.targetClass;
		}
		
	}
	
	static class DirectWithDefaultProvider<T> implements Ref<T> {

		private final Class<T> targetClass;
		private final Provider<T> provider;
		
		DirectWithDefaultProvider(final Class<T> targetClass, Provider<T> provider) {
			this.targetClass = Objects.requireNonNull(targetClass);
			this.provider = Objects.requireNonNull(provider);
		}
		
		public Provider<T> getProvider() {
			return this.provider;
		}

		@Override
		public Class<T> getTargetClass() {
			return this.targetClass;
		}
		
	}
	
}
