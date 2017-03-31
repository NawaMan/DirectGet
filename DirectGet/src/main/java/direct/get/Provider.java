package direct.get;

import java.util.function.Function;

@FunctionalInterface
public interface Provider<T> extends Function<Class<T>, Object> {

}
