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

import java.util.Collection;
import java.util.stream.Stream;

import directget.get.run.Failable;
import directget.get.run.Wrapper;
import directget.get.run.session.AsyncSessionBuilder;
import directget.get.run.session.SyncHandleProblemSessionBuilder;
import directget.get.run.session.SyncSessionBuilder;
import lombok.val;

// TODO - Record the stack when new thread.

/**
 * This class offer a natural way to run something.
 * 
 * @author NawaMan
 **/
public class Run {
    
    /**
     * Specify that the running should be done under the given scope .
     * 
     * @param scope 
     * @return the session builder.
     **/
    public static SyncSessionBuilder under(Scope scope) {
        return new SyncSessionBuilder().under(scope);
    }
    
    /**
     * Specify that the running should be done under the given scope.
     * 
     * @param scope
     * @return the session builder.
     **/
    public static SyncSessionBuilder Under(Scope scope) {
        return new SyncSessionBuilder().under(scope);
    }
    
    /**
     * Add the wrapper 
     * 
     * @param wrappers 
     * @return the session builder.
     **/
    public static SyncSessionBuilder with(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /**
     * Add the wrapper 
     * 
     * @param wrappers 
     * @return the session builder.
     **/
    public static SyncSessionBuilder With(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /**
     * Add the wrapper 
     * 
     * @param wrappers 
     * @return the session builder.
     **/
    public static SyncSessionBuilder with(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /**
     * Add the wrapper 
     * 
     * @param wrappers 
     * @return the session builder.
     **/
    public static SyncSessionBuilder With(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /**
     * Add the wrapper 
     * 
     * @param providers 
     * @return the session builder.
     **/
    public static SyncSessionBuilder With(Stream<Wrapper> providers) {
        return with(providers);
    }
    
    /**
     * Add the wrapper
     * 
     * @param wrappers 
     * @return a session builder.
     **/
    public static SyncSessionBuilder with(Stream<Wrapper> wrappers) {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.with(wrappers);
        return sessionBuilder;
    }
    
    /**
     * Mark that this run should handle the exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder HandleProblem() {
        return handleProblem();
    }
    
    /**
     * Mark that this run should handle the exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder handleProblem() {
        val sessionBuilder = new SyncHandleProblemSessionBuilder();
        sessionBuilder.handleProblem();
        return sessionBuilder;
    }
    
    /**
     * Mark that this run should handle the exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder HandleThenIgnoreProblem() {
        return handleThenIgnoreProblem();
    }
    
    /**
     * Mark that this run should ignore thrown exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder handleThenIgnoreProblem() {
        val sessionBuilder = new SyncHandleProblemSessionBuilder();
        sessionBuilder.handleThenIgnoreProblem();
        return sessionBuilder;
    }
    
    /** 
     * Mark that this run should ignore all the handled problem. 
     * 
     * @return a session builder.
     **/
    public static SyncSessionBuilder IgnoreHandledProblem() {
        return ignoreHandledProblem();
    }
    
    /**
     * Mark that this run should ignore thrown exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder IgnoreException() {
        return ignoreException();
    }
    
    /**
     * Mark that this run should ignore all the handled problem. 
     * 
     * @return a session builder.
     **/
    public static SyncSessionBuilder ignoreHandledProblem() {
        val sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.ignoreHandledProblem();
        return sessionBuilder;
    }
    
    /**
     * Mark that this run should ignore thrown exception. 
     * 
     * @return a session builder.
     **/
    public static SyncHandleProblemSessionBuilder ignoreException() {
        val sessionBuilder = new SyncHandleProblemSessionBuilder();
        sessionBuilder.ignoreException();
        return sessionBuilder;
    }
    
    /**
     * Make the run to be run on a new thread. 
     * 
     * @return a session builder.
     **/
    public static AsyncSessionBuilder Asynchronously() {
        return new SyncSessionBuilder().asynchronously();
    }
    
    /**
     * Make the run to be run on a new thread. 
     * 
     * @return a session builder.
     **/
    public static AsyncSessionBuilder asynchronously() {
        return new SyncSessionBuilder().asynchronously();
    }
    
    /**
     * Run the session now. 
     * 
     * @param runnable 
     * @throws T -- the throwable.
     **/
    public static <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.run(runnable);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @param supplier 
     * @return the result from the supplier.
     * @throws T -- the throwable.
     */
    public static <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        return sessionBuilder.run(supplier);
    }
    
}