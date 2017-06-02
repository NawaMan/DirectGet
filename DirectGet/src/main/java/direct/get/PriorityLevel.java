package direct.get;

import java.util.function.Supplier;

// OK - I feel like am going to regret this but this closed design makes things
//        much simpler and it might worth the thread off.
public enum PriorityLevel {
	
	Default, Normal, Dictate;
	
	public boolean is(PriorityLevel level) {
		return this == level;
	}
	
	public <T> boolean is(Providing<T> providing) {
		if(providing == null) {
			return false;
		}
		return is(providing.getLevel());
	}
	
	// The following two method is heavily duplicate (both the inside and each others).
	// That is intentional, if we are wondering where a providing came from,
	//   debugging these method will give you that answer very quickly.
	// Ok, I am going to regret typing this too .... but
	// This logic is not intended or supposed to be changed often.
	
	public static <T> Providing<T> determineGetProviding(
			Supplier<Providing<T>> parentProvidingSupplier,
			Supplier<Providing<T>> scopeProvidingSupplier,
			Supplier<Providing<T>> stackProvidingSupplier) {
		Providing<T> parentProviding = null;
		parentProviding = parentProvidingSupplier.get();
		if (Dictate.is(parentProviding)) {
			return parentProviding;
		}
		
		Providing<T> configProviding = scopeProvidingSupplier.get();
		if (Dictate.is(configProviding)) {
			return configProviding;
		}

		Providing<T> stackProviding = stackProvidingSupplier.get();
		if (Dictate.is(stackProviding)) {
			return stackProviding;
		}
		
		// At this point, non is dictate.
		
		if (Normal.is(stackProviding)) {
			return stackProviding;
		}
		if (Normal.is(configProviding)) {
			return configProviding;
		}
		if (Normal.is(parentProviding)) {
			return parentProviding;
		}
		
		// At this point, non is normal.
		
		if (Default.is(stackProviding)) {
			return stackProviding;
		}
		if (Default.is(configProviding)) {
			return configProviding;
		}
		if (Default.is(parentProviding)) {
			return parentProviding;
		}
		
		return null;
	}
	
	public static <T> Providing<T> determineScopeProviding(
			Supplier<Providing<T>> parentProvidingSupplier,
			Supplier<Providing<T>> configProvidingSupplier) {
		Providing<T> parentProviding = parentProvidingSupplier.get();
		if (Dictate.is(parentProviding)) {
			return parentProviding;
		}
		Providing<T> configProviding = configProvidingSupplier.get();
		if (Dictate.is(configProviding)) {
			return configProviding;
		}
		
		// A this point, no dictate;

		if (Normal.is(configProviding)) {
			return configProviding;
		}
		if (Normal.is(parentProviding)) {
			return parentProviding;
		}
		
		// A this point, no normal;
		
		if (Default.is(configProviding)) {
			return configProviding;
		}
		if (Default.is(parentProviding)) {
			return parentProviding;
		}
		
		return null;
	}
	
}
