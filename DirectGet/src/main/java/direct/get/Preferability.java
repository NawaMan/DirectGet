package direct.get;

import java.util.function.Consumer;
import java.util.function.Supplier;

// OK - I feel like am going to regret this but this closed design (of using Enums) makes things
//        much simpler and it might worth the thread off.
/**
 * This enum is used to specify the preferability of a providing.
 * 
 * @author nawaman
 */
public enum Preferability {
	
	/** Only use when no other is preferred. */
	Default,
	/** Whatever */
	Normal,
	/** Use me first! */
	Dictate;
	
	/** @return {@code true} if the given preferability is the same as this preferability. */
	public boolean is(Preferability preferability) {
		return this == preferability;
	}

	/** @return {@code true} if the given providing has the same preferability as this preferability. */
	public <T> boolean is(Providing<T> providing) {
		if(providing == null) {
			return false;
		}
		return is(providing.getPreferability());
	}
	
	// The following two method is heavily duplicate (both the inside and each others).
	// That is intentional, if we are wondering where a providing came from,
	//   debugging these method will give you that answer very quickly.
	// Ok, I am going to regret typing this too .... but
	// This logic is not intended or supposed to be changed often.
	
	/**
	 * Determine the providing for Get.
	 * 
	 * @return the providing.
	 * @see {@link direct.get.ProvidingOrderTest}
	 */
	public static <T> Providing<T> determineGetProviding(
			Ref<T>                 ref,
			Supplier<Providing<T>> scopeProvidingSupplier,
			Supplier<Providing<T>> stackProvidingSupplier) {
		Consumer<Supplier<String>> log = Get._Logger_.equals(ref) ? Get._Logger_.get() : Get.a(Get._Logger_);
		
		Providing<T> configProviding = scopeProvidingSupplier.get();
		if (Dictate.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}

		Providing<T> stackProviding = stackProvidingSupplier.get();
		if (Dictate.is(stackProviding)) {
			log.accept(()->"Get (" + ref + "): Stack: " + stackProviding);
			return stackProviding;
		}
		
		// At this point, non is dictate.
		
		if (Normal.is(stackProviding)) {
			log.accept(()->"Get (" + ref + "): Stack: " + stackProviding);
			return stackProviding;
		}
		if (Normal.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}
		
		// At this point, non is normal.
		
		if (Default.is(stackProviding)) {
			log.accept(()->"Get (" + ref + "): Stack: " + stackProviding);
			return stackProviding;
		}
		if (Default.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}
		
		return null;
	}

	/**
	 * Determine the providing for Space.
	 * 
	 * @return the providing.
	 * @see {@link direct.get.ProvidingOrderTest}
	 */
	public static <T> Providing<T> determineScopeProviding(
			Ref<T> ref,
			Supplier<Providing<T>> parentProvidingSupplier,
			Supplier<Providing<T>> configProvidingSupplier) {
		Consumer<Supplier<String>> log = Get._Logger_.equals(ref) ? Get._Logger_.get() : Get.a(Get._Logger_);
		
		Providing<T> parentProviding = parentProvidingSupplier.get();
		if (Dictate.is(parentProviding)) {
			log.accept(()->"Get (" + ref + "): Parent: " + parentProviding);
			return parentProviding;
		}
		Providing<T> configProviding = configProvidingSupplier.get();
		if (Dictate.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}
		
		// A this point, no dictate;

		if (Normal.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}
		if (Normal.is(parentProviding)) {
			log.accept(()->"Get (" + ref + "): Parent: " + parentProviding);
			return parentProviding;
		}
		
		// A this point, no normal;
		
		if (Default.is(configProviding)) {
			log.accept(()->"Get (" + ref + "): Scope: " + configProviding);
			return configProviding;
		}
		if (Default.is(parentProviding)) {
			log.accept(()->"Get (" + ref + "): Parent: " + parentProviding);
			return parentProviding;
		}
		
		return null;
	}
	
}
