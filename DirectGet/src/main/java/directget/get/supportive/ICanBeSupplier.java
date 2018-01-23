package directget.get.supportive;

import java.util.function.Supplier;

public interface ICanBeSupplier<T> {
    
    public Supplier<T> asSupplier();

}
