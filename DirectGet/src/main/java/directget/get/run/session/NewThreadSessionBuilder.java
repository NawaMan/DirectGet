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
package directget.get.run.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import directget.get.Ref;
import directget.get.run.Failable;
import directget.get.run.Fork;

/**
 * The wrapper for a new thread run.
 * 
 * @author NawaMan
 **/
public class NewThreadSessionBuilder extends SessionBuilder<NewThreadSessionBuilder> {
    
    @SuppressWarnings("rawtypes")
    final List<Ref> includedRefs = new ArrayList<>();
    @SuppressWarnings("rawtypes")
    final List<Ref> excludedRefs = new ArrayList<>();
    
    Boolean inheritMass = null;
    
    Fork fork = null;
    
    NewThreadWrapper newThreadwrapper = new NewThreadWrapper(this);
    
    /** Default constructor. */
    public NewThreadSessionBuilder() {
    }
    
    /** Constructor with a fork. */
    public NewThreadSessionBuilder(Fork fork) {
        this.fork = fork;
    }
    
    /** Join the runnable with using the given fork. */
    public NewThreadSessionBuilder joinWith(Fork fork) {
        this.fork = fork;
        return this;
    }
    
    /** Set this run to inherit all refs from the parent thread. */
    public NewThreadSessionBuilder inheritAll() {
        inheritMass = true;
        includedRefs.clear();
        excludedRefs.clear();
        return this;
    }
    
    /** Set this run to inherit no refs from the parent thread. */
    public NewThreadSessionBuilder inheritNone() {
        inheritMass = false;
        includedRefs.clear();
        excludedRefs.clear();
        return this;
    }
    
    /** Specify what refs to be inherited - in case of inherit none. */
    @SuppressWarnings({ "rawtypes" })
    public NewThreadSessionBuilder inherit(Ref... refs) {
        List<Ref> list = Arrays.asList(refs);
        includedRefs.addAll(list);
        excludedRefs.removeAll(list);
        return this;
    }
    
    /** Specify what refs NOT to be inherited - in case of inherit all. */
    @SuppressWarnings({ "rawtypes" })
    public NewThreadSessionBuilder notInherit(Ref... refs) {
        List<Ref> list = Arrays.asList(refs);
        includedRefs.removeAll(list);
        excludedRefs.addAll(list);
        return this;
    }
    
    /** Build the session for later use. */
    public NewThreadWrapperContext build() {
        return new NewThreadWrapperContext(failHandler, wrappers);
    }
    
    /** Run the given supplier and return a value. */
    public <R, T extends Throwable> CompletableFuture<R> run(Failable.Supplier<R, T> supplier) {
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