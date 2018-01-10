//  ========================================================================
//  Copyright (c) 2017 Nawapunth Manusitthipol.
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class utilities {
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final Function<Map, Map> newTreeMap = (Function<Map, Map>) TreeMap::new;
    
    public <K, V> Map<K, V> _toUnmodifiableSortedMap(Map<K, V> theGivenMap) {
        @SuppressWarnings("unchecked")
        Map<K, V> newProviderMap = Nulls.or(Nulls.mapTo(theGivenMap, newTreeMap), emptyMap());
        return unmodifiableMap(newProviderMap);
    }
    
    private static Function<Map.Entry<?, ?>, String> pairToString = each -> {
        val toString = each.getKey() + "=" + each.getValue();
        return toString;
    };
    
    public Stream<String> _toPairStrings(Map<?, ?> theGivenMap) {
        return theGivenMap.entrySet().stream().map(pairToString);
    }
    
    public String _toIndentLines(Stream<String> eachLines) {
        return eachLines.collect(Collectors.joining(",\n\t"));
    }
    
    public <T> List<T> _toList(Stream<T> theGivenStream) {
        return theGivenStream.collect(Collectors.toList());
    }
    
    public <V> Supplier<V> _toSupplier(Runnable runnable, AtomicReference<RuntimeException> exceptionHolder) {
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
