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
public class Utilities {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Function<Map, Map> newTreeMap = (Function<Map, Map>) TreeMap::new;
    
    /** Returns elseValue if the given object is null. **/
    public static <T> T _or(T theGivenObject, T elseValue) {
        return (theGivenObject == null) ? elseValue : theGivenObject;
    }
    
    /** Extension method to create optional of the given object. **/
    public static <T> Optional<T> whenNotNull(T theGivenObject) {
        return Optional.ofNullable(theGivenObject);
    }
    
    /** Returns the result of elseSupplier if the given object is null. **/
    public static <T> T _or(T theGivenObject, Supplier<? extends T> elseSupplier) {
        return (theGivenObject == null) ? elseSupplier.get() : theGivenObject;
    }
    
    /** Returns mapped result using the mapperFunction if the given object is not null. **/
    public static <F, T> T _changeFrom(F theGivenObject, Function<F, T> mapperFunction) {
        return (theGivenObject == null) ? null : mapperFunction.apply(theGivenObject);
    }
    
    /** Returns mapped result using the mapperFunction if the given object is not null. **/
    public static <F, T> T _changeBy(F theGivenObject, Function<F, T> mapperFunction) {
        return (theGivenObject == null) ? null : mapperFunction.apply(theGivenObject);
    }
    
    /** Returns mapped result using the mapperFunction if the given object is not null. **/
    public static <F, T> T _changeTo(F theGivenObject, Function<F, T> mapperFunction) {
        return (theGivenObject == null) ? null : mapperFunction.apply(theGivenObject);
    }
    
    /** Proceed to perform the action if theGivenObject is not null. Returns theGivenObject. **/
    public static <V> V _do(V theGivenObject, Consumer<V> theAction) {
        if (theGivenObject != null)
            theAction.accept(theGivenObject);
        return theGivenObject;
    }
    
    /** Convert non-null elements in the given stream to map using the keyMapper to create the key. **/
    public static <K, V> Map<K, V> _toNonNullMap(Stream<V> theGivenStream, Function<V, K> theKeyMapper) {
        return theGivenStream.filter(Objects::nonNull).collect(toMap(theKeyMapper, p -> p));
    }
    
    /** Returns the unmodifiedable sorted map from the given map. **/
    public static <K, V> Map<K, V> _toUnmodifiableSortedMap(Map<K, V> theGivenMap) {
        @SuppressWarnings("unchecked")
        Map<K, V> newProvidingMap = _or(_changeBy(theGivenMap, newTreeMap), emptyMap());
        return unmodifiableMap(newProvidingMap);
    }
    
    private static Function<Map.Entry<?, ?>, String> pairToString = each -> {
        val toString = each.getKey() + "=" + each.getValue();
        return toString;
    };
    
    /** Returns the stream of string that each element is the `${key}=${value}` of each entry of the given map. */
    public static Stream<String> _toPairStrings(Map<?, ?> theGivenMap) {
        return theGivenMap.entrySet().stream().map(pairToString);
    }
    
    /** Returns the indented lines of the given lines. */
    public static String _toIndentLines(Stream<String> eachLines) {
        return eachLines.collect(Collectors.joining(",\n\t"));
    }
    
    /** Collect the given stream into a list. */
    public static <T> List<T> _toList(Stream<T> theGivenStream) {
        return theGivenStream.collect(Collectors.toList());
    }
    
    /** Returns the nullable optional of the given object. */
    public static <T> Optional<T> _toNullable(T theGivenObject) {
        return Optional.ofNullable(theGivenObject);
    }
    
    /** Collects the non-null elements of the given collection into a list. **/
    public static <T> List<T> _toUnmodifiableNonNullList(Collection<T> collection) {
        if (collection == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(collection.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
    /** Collects the non-null elements of the given stream into a list. **/
    public static <T> List<T> _toUnmodifiableNonNullList(Stream<T> stream) {
        if (stream == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(stream.filter(Objects::nonNull).collect(Collectors.toList()));
    }
    
    /** Convert the given runnable to a supplier and in case of exception, assigned it in the exceptionHolder. */
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
