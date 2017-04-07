package direct.get;

import java.util.Objects;

@FunctionalInterface
public interface Ref<T> {
	
	public Class<T> getTargetClass();
	
	
	// == Basic implementation =========================================================================================
	
	public static <T> Ref<T> of(Class<T> targetClass) {
		return new Direct<>(targetClass);
	}
	
	public static class Direct<T> extends RefWithDefaultProvider<T> implements Ref<T> {
		
		public Direct(Class<T> targetClass) {
			super(targetClass, null);
		}
		
		public String toString() {
			return "Ref<" + this.getTargetClassName() + ">";
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
		
		final Class<T> targetClass;
		final String targetClassName;
		final Provider<T> provider;
		
		RefWithDefaultProvider(final Class<T> targetClass, Provider<T> provider) {
			this.targetClass = Objects.requireNonNull(targetClass);
			this.targetClassName = this.targetClass.getCanonicalName();
			this.provider = provider;
		}
		
		@Override
		public Class<T> getTargetClass() {
			return this.targetClass;
		}
		
		public String getTargetClassName() {
			return this.targetClassName;
		}
		
	}
	
}
