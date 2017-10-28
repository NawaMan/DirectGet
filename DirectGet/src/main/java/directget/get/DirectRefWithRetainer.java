package directget.get;

import java.util.function.Supplier;

import directget.get.supportive.DirectRef;
import directget.get.supportive.retain.Retainer;
import directget.get.supportive.retain.RetainerBuilder;
import directget.get.supportive.retain.WithRetainer;
import lombok.val;

/**
 * A direct ref with retainer.
 * 
 * @author Nawa Man
 *
 * @param <T> the type of data this will be reference to.
 */
public class DirectRefWithRetainer<T> extends DirectRef<T> implements WithRetainer<T, DirectRefWithRetainer<T>> {
    
    /** Constructor. */
    public DirectRefWithRetainer(String name, Class<T> targetClass, Preferability preferability,
            Supplier<? extends T> factory) {
        super(name, targetClass, preferability, factory);
    }
    
    /** Returns the retainer. */
    public Retainer<T> getRetainer() {
        val supplier = getProviding().supplier;
        val retainer
            = ((supplier instanceof Retainer)
            ? ((Retainer<T>)supplier)
            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
        return retainer;
    }

    /** Change the supplier. */
    public DirectRefWithRetainer<T> __but(Supplier<T> newSupplier) {
        val supplier = getProviding().supplier;
        if (newSupplier == supplier) {
            return this;
        }
        return new DirectRefWithRetainer<>(this.getName(), this.getTargetClass(), this.getPreferability(), newSupplier);
    }
    
}
