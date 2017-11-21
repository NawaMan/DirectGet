package directget.get.run;

import java.util.function.Consumer;

class utils {
    
    public static <V> void _do(V obj, Consumer<V> action) {
        if (obj != null) {
            action.accept(obj);
        }
    }
    
}
