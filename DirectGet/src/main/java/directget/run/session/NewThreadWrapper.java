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
package directget.run.session;

import java.util.function.Predicate;

import directget.get.Get;
import directget.get.Ref;
import directget.run.Wrapper;
import lombok.val;

/**
 * Wrapper for running on the new thread.
 * 
 * @author NawaMan
 **/
public class NewThreadWrapper implements Wrapper {
    
    private final NewThreadSessionBuilder builder;
    
    NewThreadWrapper(NewThreadSessionBuilder builder) {
        this.builder = builder;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Runnable apply(Runnable runnable) {
        Predicate<Ref> predicate = Get.INHERIT_NONE;
        val isAll = Boolean.TRUE.equals(builder.inheritMass);
        if (isAll) {
            if (builder.excludedRefs.isEmpty()) {
                predicate = Get.INHERIT_ALL;
            } else {
                predicate = ref -> !builder.excludedRefs.contains(ref);
            }
        } else {
            val isNone = Boolean.TRUE.equals(builder.inheritMass);
            if (isNone) {
                if (builder.includedRefs.isEmpty()) {
                    predicate = Get.INHERIT_NONE;
                } else {
                    predicate = ref -> builder.includedRefs.contains(ref);
                }
            } else {
                predicate = ref -> {
                    return builder.includedRefs.contains(ref);
                };
            }
        }
        val checker = predicate;
        if (builder.fork != null) {
            return () -> {
                builder.get().runNewThread(checker, builder.fork.run(runnable));
            };
        } else {
            return () -> {
                builder.get().runNewThread(checker, runnable);
            };
        }
    }
    
}