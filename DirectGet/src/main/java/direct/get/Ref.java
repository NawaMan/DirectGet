package direct.get;

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
	
}
