package directget.get;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import directget.get.supportive.retain.Retainer;
import directget.get.supportive.retain.RetainerBuilder;
import directget.get.supportive.retain.WithRetainer;
import lombok.val;

public class ProvidingWithRetainer<T> extends Providing<T> implements WithRetainer<T, ProvidingWithRetainer<T>> {

    
    public ProvidingWithRetainer(Ref<T> ref, Preferability preferability, Supplier<? extends T> supplier) {
        super(ref, preferability, supplier);
    }

    public Retainer<T> getRetainer() {
        val retainer
            = ((supplier instanceof Retainer)
            ? ((Retainer<T>)supplier)
            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
        return retainer;
    }

    public ProvidingWithRetainer<T> __but(Supplier<T> newSupplier) {
        if (newSupplier == supplier) {
            return this;
        }
        return new ProvidingWithRetainer<T>(ref, preferability, newSupplier);
    }
    
}