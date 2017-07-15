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
package directget.get.supportive;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
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
 * NOTE: This is intent for internal use only.
 * 
 * @author nawaman
 **/
public class directget_internal_utilities {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Function<Map, Map> newTreeMap = (Function<Map, Map>) TreeMap::new;
    
    @SuppressWarnings("javadoc")
    public static <T> T _or(T obj, T elseValue) {
        return (obj == null) ? elseValue : obj;
    }

    @SuppressWarnings("javadoc")
    public static <T> T _or(T obj, Supplier<? extends T> elseSupplier) {
        return (obj == null) ? elseSupplier.get() : obj;
    }

    @SuppressWarnings("javadoc")
    public static <F, T> T _changeFrom(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }

    @SuppressWarnings("javadoc")
    public static <F, T> T _changeBy(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }

    @SuppressWarnings("javadoc")
    public static <F, T> T _changeTo(F obj, Function<F, T> mapper) {
        return (obj == null) ? null : mapper.apply(obj);
    }

    @SuppressWarnings("javadoc")
    public static <V> void _do(V obj, Consumer<V> action) {
        if (obj != null) {
            action.accept(obj);
        }
    }

    @SuppressWarnings("javadoc")
    public static <K, V> Map<K, V> _toNonNullMap(Stream<V> stream,Function<V, K> keyMapper) {
        return stream.filter(Objects::nonNull).collect(toMap(keyMapper, p -> p));
    }

    @SuppressWarnings("javadoc")
    public static <K, V> Map<K, V> _toUnmodifiableSortedMap(Map<K, V> map) {
        @SuppressWarnings("unchecked")
        Map<K, V> newProvidingMap = _or(_changeBy(map, newTreeMap), emptyMap());
        return unmodifiableMap(newProvidingMap);
    }
    
    private static Function<Map.Entry<?, ?>, String> pairToString = each -> {
        val toString = each.getKey() + "=" + each.getValue();
        return toString;
    };

    @SuppressWarnings("javadoc")
    public static Stream<String> _toPairStrings(Map<?, ?> map) {
        return map.entrySet().stream().map(pairToString);
    }

    @SuppressWarnings("javadoc")
    public static String _toIndentLines(Stream<String> eachLines) {
        return eachLines.collect(Collectors.joining(",\n\t"));
    }

    @SuppressWarnings("javadoc")
    public static <T> List<T> _toList(Stream<T> stream) {
        return stream.collect(Collectors.toList());
    }

    @SuppressWarnings("javadoc")
    public static <T> Optional<T> _toNullable(T obj) {
        return Optional.ofNullable(obj);
    }

    @SuppressWarnings("javadoc")
    public static <T> List<T> _toUnmodifiableNonNullList(Collection<T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(collection.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @SuppressWarnings("javadoc")
    public static <T> List<T> _toUnmodifiableNonNullList(Stream<T> stream) {
        if (stream == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(stream.filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
    /**
     * Convert the given runnable to a supplier and in case of exception, assigned it in the exceptionHolder.
     * 
     * @param runnable
     *          the runnable.
     * @param exceptionHolder
     *          the exception holder.
     * @return {@code null}
     */
    public static <V> Supplier<V> _toSupplier(Runnable runnable, AtomicReference<RuntimeException> exceptionHolder) {
        return (Supplier<V>) (() -> {
            try {
                runnable.run();
            } catch (RuntimeException e) {
                if (exceptionHolder != null) {
                    exceptionHolder.set(e);
                } else {
                    throw e;
                }
            }
            return null;
        });
    }
    
}
