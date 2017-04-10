package direct.get;

import java.util.Objects;

@FunctionalInterface
public interface Ref<T> {
	
	public Class<T> getTargetClass();
	
	public default Provider<T> getDefaultProvider() {
		return null;
	}

	public static <T> Ref<T> of(Class<T> targetClass, Provider<T> provider) {
		return new Ref<T>() {
			@Override
			public Class<T> getTargetClass() {
				return targetClass;
			}
			@Override
			public Provider<T> getDefaultProvider() {
				return provider;
			}
		};
	}
	
	// == Class base implementation =========================================================================================
	
	public static <T> Ref<T> ofClass(Class<T> targetClass) {
		return new OfClass<>(targetClass, null);
	}
	
	public static <T> Ref<T> ofClass(Class<T> targetClass, Provider<T> provider) {
		return new OfClass<>(targetClass, provider);
	}
	
	public static class OfClass<T> implements Ref<T> {
		
		final Class<T> targetClass;
		final String targetClassName;
		final Provider<T> provider;
		
		public OfClass(final Class<T> targetClass, Provider<T> provider) {
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
		
		
		public String toString() {
			return "Ref<" + this.getTargetClassName() + ">";
		}

		@SuppressWarnings("rawtypes")
		public boolean equals(Object obj) {
			if (!(obj instanceof Ref.OfClass)) {
				return false;
			}
			return this.targetClass == ((Ref.OfClass)obj).targetClass;
		}
		
		@Override
		public int hashCode() {
			return this.targetClass.hashCode();
		}
		
	}
	
}
