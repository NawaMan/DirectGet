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
package directget.get.run.session;

import static dssb.utils.common.Nulls.orGet;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import directget.get.run.HandledFailable;

/**
 * The contains the wrappers so that we can run something within them.
 * 
 * @author NawaMan
 **/
public class WrapperContext {
    
    @SuppressWarnings("rawtypes")
    final Function<HandledFailable.Runnable, Runnable> failHandler;
    final List<Function<Runnable, Runnable>> wrappers;
    
    @SuppressWarnings("rawtypes") 
    WrapperContext(Function<HandledFailable.Runnable, Runnable> failHandler, List<Function<Runnable, Runnable>> functions) {
        this.failHandler = orGet(failHandler, ()->runnable->runnable.gracefully());
        this.wrappers    = toUnmodifiableNonNullList(functions);
    }
    
    private static <T> List<T> toUnmodifiableNonNullList(Collection<T> collection) {
        if (collection == null) {
            return emptyList();
        }
        return unmodifiableList(collection.stream()
                .filter(Objects::nonNull)
                .collect(toList()));
    }
    
}