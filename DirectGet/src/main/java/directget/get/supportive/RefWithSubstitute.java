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
    
    /**
     * Create a provider that dictate the current. 
     * 
     * @return a new provider pretty much like this one but dictate.
     **/
    public Provider<T> butDictate() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Dictate, currentProvider.getSupplier());
    }

    /**
     * Create a provider that provide the current with Normal preferability. 
     * 
     * @return a new provider pretty much like this one but provide.
     **/
    public Provider<T> butProvideNormally() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Normal, currentProvider.getSupplier());
    }

    /**
     * Create a provider that provide the current with Default preferability.
     * 
     * @return a new provider pretty much like this one but default.
     **/
    public Provider<T> butDefault() {
        val currentProvider = getCurrentProvider();
        return new Provider<>(this.theRef, Preferability.Default, currentProvider.getSupplier());
    }
    
    //-- but Preference + Value --
    
    /**
     * Create a provider that dictate the given value. 
     * 
     * @param value  the given value.
     * @return a new provider pretty much like this one but dictate to the given value.
     **/
    public <V extends T> Provider<T> butDictatedTo(V value) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider that dictate the value of the given ref. 
     * 
     * @param ref  the given ref.
     * @return a new provider pretty much like this one but dictate to a value of the given ref.
     **/
    public <V extends T> Provider<T> butDictatedToThe(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider that dictate the value of the given target class. 
     * 
     * @param targetClass  the target class.
     * @return a new provider pretty much like this one but dictate to the value of the given target class.
     **/
    public <V extends T> Provider<T> butDictatedToThe(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Dictate, new Named.RefSupplier<V>(Ref.of(targetClass)));
    }
    
    /**
     * Create the provider that dictate the result of the given supplier. 
     * 
     * @param supplier  the supplier.
     * @return a new provider pretty much like this one but dictate to the value got from the supplier.
     **/
    public <V extends T> Provider<T> butDictatedBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Dictate, supplier);
    }
    
    /**
     * Create the provider (normal preferability) the given value. 
     * 
     * @param value  the given value.
     * @return a new provider pretty much like this one but provided with the given value.
     **/
    public <V extends T> Provider<T> butProvidedWith(V value) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given ref.
     * 
     * @param ref  the given ref.
     * @return  a new provider pretty much like this one but provided with the value from the given ref.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (normal preferability) the value of the given target class.
     * 
     * @param targetClass  the target class.
     * @return  a new provider pretty much like this one but provided with the value from the given target class. 
     */
    public <V extends T> Provider<T> butProvidedWithThe(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(Ref.of(targetClass)));
    }
    
    /**
     * Create the provider (normal preferability) the result of the given supplier.
     * 
     * @param supplier  the supplier.
     * @return   a new provider pretty much like this one but provided with the value got from the supplier.
     */
    public <V extends T> Provider<T> butProvidedBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Normal, supplier);
    }
    
    /**
     * Create the provider (using the given preferability) the given value. 
     * 
     * @param preferability 
     * @param value 
     * @return   a new provider pretty much like this one but with the given perferaability and the value.
     **/
    public <V extends T> Provider<T> butProvidedWith(Preferability preferability, V value) {
        return new Provider<>(this.theRef, preferability, new Named.ValueSupplier<T>(value));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given ref.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Preferability preferability, Ref<V> ref) {
        return new Provider<>(this.theRef, preferability, new Named.RefSupplier<V>(ref));
    }
    
    /**
     * Create the provider (using the given preferability) the value of the
     * given target class.
     */
    public <V extends T> Provider<T> butProvidedWithThe(Preferability preferability, Class<V> targetClass) {
        return new Provider<>(this.theRef, preferability, new Named.RefSupplier<V>(Ref.of(targetClass)));
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
    public <V extends T> Provider<T> butDefaultedToThe(Ref<V> ref) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(ref));
    }
    
    /** Create the provider that default to the value of the given target class. */
    public <V extends T> Provider<T> butDefaultedToThe(Class<V> targetClass) {
        return new Provider<>(this.theRef, Preferability.Normal, new Named.RefSupplier<V>(Ref.of(targetClass)));
    }
    
    /**
     * Create the provider that default to the result of the given supplier.
     */
    public <V extends T> Provider<T> butDefaultedToBy(Supplier<V> supplier) {
        return new Provider<>(this.theRef, Preferability.Normal, supplier);
    }
    
}
