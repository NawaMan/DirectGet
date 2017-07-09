package directget.get.supportive;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

class utils {
    
    public static <T> T _or(T obj, Supplier<? extends T> elseSupplier) {
        return (obj == null) ? elseSupplier.get() : obj;
    }
    
    public static Stream<String> _toPairStrings(Map<?, ?> map) {
        return map.entrySet().stream().map(pairToString);
    }
    
    private static Function<Map.Entry<?, ?>, String> pairToString = each -> {
        val toString = each.getKey() + "=" + each.getValue();
        return toString;
    };
    
    public static String _toIndentLines(Stream<String> eachLines) {
        return eachLines.collect(Collectors.joining(",\n\t"));
    }
    
}
