package directget.get.supportive;

import java.util.function.Supplier;

import directget.get.Preferability;
import directget.get.Ref;
import directget.get.run.Named;
import lombok.NonNull;
import lombok.val;

/**
 * This class is an extension of Ref that add ability to modify few aspects of Ref without duplicating the whole things.
 * 
 * @author NawaMan
 *
 * @param <T> the data type T.
 */
public class RefWithSubstitute<T> {
    
    private final Ref<T> theRef;
    
    private final Supplier<Provider<T>> getCurrentProvider;
    
    /**
     * @param theRef
     * @param getCurrentProvider
     */
    public RefWithSubstitute(
            @NonNull Ref<T>                theRef,
            @NonNull Supplier<Provider<T>> getCurrentProvider) {
        this.theRef             = theRef;
        this.getCurrentProvider = getCurrentProvider;
    }
    
    private Provider<T> getCurrentProvider() {
        return getCurrentProvider.get();
    }

    //== For substitution =============================================================================================
    
    //-- Preference only --
    
    /** Create a provider that dictate the current. */
    public Provider<T> butDictate() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Dictate, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Normal preferability. */
    public Provider<T> butProvideNormally() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Normal, currentProvider.getSupplier());
    }

    /** Create a provider that provide the current with Default preferability. */
    public Provider<T> butDefault() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Default, currentProvider.getSupplier());
    }
    
    //-- but Preference + Value --
    
    /** Create a provider that dictate the given value. */
    public <V extends T> Provider<T> butDictatedTo(V value) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that dictate the value of the given ref. */
    public <V extends T> Provider<T> butDictatedToA(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that dictate the value of the given target class. */
    public <V extends T> Provider<T> butDictatedToA(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /** Create the provider that dictate the result of the given supplier. */
    public <V extends T> Provider<T> butDictatedBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Dictate, supplier);
    }
    
    /** Create the provider (normal preferability) the given value. */
    public <V extends T> Provider<T> butProvidedWith(V value) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     */
    public <V extends T> Provider<T> butProvidedWithA(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given target class.
     */
    public <V extends T> Provider<T> butProvidedWithA(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (normal preferability) the result of the given
     * supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Normal, supplier);
    }
    
    /** Create the provider (using the given preferability) the given value. */
    public <V extends T> Provider<T> butProvidedWith(Preferability preferability, V value) {
        return new Provider<>(this.theRef, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given ref.
     */
    public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Ref<V> ref) {
        return new Provider<>(this.theRef, preferability, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given target class.
     */
    public <V extends T> Provider<T> butProvidedWithA(Preferability preferability, Class<V> targetClass) {
        return new Provider<>(this.theRef, preferability, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider (using the given preferability) the result of the
     * given supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Preferability preferability, Supplier<V> supplier) {
        return new Provider<>(this.theRef, preferability, supplier);
    }
    
    /** Create the provider that default to the given value. */
    public <V extends T> Provider<T> butDefaultedTo(V value) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /** Create the provider that default to the value of the given ref. */
    public <V extends T> Provider<T> butDefaultedToA(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that default to the value of the given target class. */
    public <V extends T> Provider<T> butDefaultedToA(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(Ref.forClass(targetClass)));
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     */
    public <V extends T> Provider<T> butDefaultedToBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Normal, supplier);
    }
    
}
