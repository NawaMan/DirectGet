//  ========================================================================
//  Copyright (c) 2017 The Direct Solution Software Builder.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
package directget.get;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;

/**
 * This class provide commonly use extension methods or the ones that help
 * documenting the code.
 * 
 * @author nawaman
 **/
class Extensions {
    
    public static <T> T _or(T obj, T elseValue) {
        return (obj == null) ? elseValue : obj;
    }
    
    public static <T> T _or(T obj, Supplier<? extends T> elseSupplier) {
        return (obj == null) ? elseSupplier.get() : obj;
    }
    
    public static <F, T> T _changeFrom(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }
    
    public static <F, T> T _changeBy(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }
    
    public static <F, T> T _changeTo(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }
    
    public static <V> void _do(V obj, Consumer<V> action) {
        if (obj != null) {
            action.accept(obj);
        }
    }
    
    private static Function<Map.Entry<?, ?>, String> pairToString = each -> {
        val toString = each.getKey() + "=" + each.getValue();
        return toString;
    };
    
    public static Stream<String> _toPairStrings(Map<?, ?> map) {
        return map.entrySet().stream().map(pairToString);
    }
    
    public static String _toIndentLines(Stream<String> eachLines) {
        return eachLines.collect(Collectors.joining(",\n\t"));
    }
    
    public static <T> List<T> _toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }
    
    public static <T> Optional<T> _toNullable(T obj) {
        return Optional.ofNullable(obj);
    }
    
    public static <T> List<T> _toUnmodifiableNonNullList(Collection<T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(collection.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
    public static <T> List<T> _toUnmodifiableNonNullList(Stream<T> stream) {
        if (stream == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(stream.filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
}
