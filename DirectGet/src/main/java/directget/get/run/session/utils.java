package directget.get.run.session;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class utils {
    
    static <T> T _or(T obj, Supplier<T> elseSupplier) {
        return (obj == null) ? elseSupplier.get() : obj;
    }
    
    static <T> List<T> _toUnmodifiableNonNullList(Collection<T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(collection.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
}
