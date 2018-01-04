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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import directget.get.Ref;
import directget.get.run.Fork;
import directget.get.run.HandledFailable;
import dssb.failable.Failable;

/**
 * The wrapper for a new thread run.
 * 
 * @author NawaMan
 **/
public class AsyncSessionBuilder extends SessionBuilder<AsyncSessionBuilder> {
    
    @SuppressWarnings("rawtypes")
    final List<Ref> includedRefs = new ArrayList<>();
    @SuppressWarnings("rawtypes")
    final List<Ref> excludedRefs = new ArrayList<>();
    
    Boolean inheritMass = null;
    
    Fork fork = null;
    
    AsyncWrapper asyncWrapper = new AsyncWrapper(this);
    
    /** Default constructor. */
    public AsyncSessionBuilder() {
    }
    
    /** Constructor with a fork. */
    public AsyncSessionBuilder(Fork fork) {
        this.fork = fork;
    }
    
    /** Join the runnable with using the given fork. */
    public AsyncSessionBuilder joinWith(Fork fork) {
        this.fork = fork;
        return this;
    }
    
    /** Set this run to inherit all refs from the parent thread. */
    public AsyncSessionBuilder inheritAll() {
        inheritMass = true;
        includedRefs.clear();
        excludedRefs.clear();
        return this;
    }
    
    /** Set this run to inherit no refs from the parent thread. */
    public AsyncSessionBuilder inheritNone() {
        inheritMass = false;
        includedRefs.clear();
        excludedRefs.clear();
        return this;
    }
    
    /** Specify what refs to be inherited - in case of inherit none. */
    @SuppressWarnings({ "rawtypes" })
    public AsyncSessionBuilder inherit(Ref... refs) {
        List<Ref> list = Arrays.asList(refs);
        includedRefs.addAll(list);
        excludedRefs.removeAll(list);
        return this;
    }
    
    /** Specify what refs NOT to be inherited - in case of inherit all. */
    @SuppressWarnings({ "rawtypes" })
    public AsyncSessionBuilder notInherit(Ref... refs) {
        List<Ref> list = Arrays.asList(refs);
        includedRefs.removeAll(list);
        excludedRefs.addAll(list);
        return this;
    }
    
    /** Build the session for later use. */
    public AsyncWrapperContext build() {
        return new AsyncWrapperContext(failHandler, wrappers);
    }
    
    /** Run the given supplier and return a value. */
    public <R, T extends Throwable> CompletableFuture<R> run(HandledFailable.Supplier<R, T> supplier) {
        return build().run(supplier);
    }
    
    /** Run the session now. */
    public <T extends Throwable> CompletableFuture<Void> run(Failable.Runnable<T> runnable) {
        return build().run(()->{
            runnable.run();
            return null;
        });
    }
}