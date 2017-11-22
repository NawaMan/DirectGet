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
import directget.get.run.session.SyncNoCheckExceptionSessionBuilder;
import directget.get.run.session.SyncSessionBuilder;
import lombok.val;

/**
 * This class offer a natural way to run something.
 * 
 * @author NawaMan
 **/
public class Run {
    
    /** Specify that the running should be done under the given scope */
    public static SyncSessionBuilder under(Scope scope) {
        return new SyncSessionBuilder().under(scope);
    }
    
    /** Specify that the running should be done under the given scope */
    public static SyncSessionBuilder Under(Scope scope) {
        return new SyncSessionBuilder().under(scope);
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder with(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder With(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder with(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder With(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder With(Stream<Wrapper> providers) {
        return with(providers);
    }
    
    /** Add the wrapper */
    public static SyncSessionBuilder with(Stream<Wrapper> wrappers) {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.with(wrappers);
        return sessionBuilder;
    }
    
    /** Mark that this run should handle the exception. */
    public static SyncNoCheckExceptionSessionBuilder HandleProblem() {
        return handleProblem();
    }
    
    /** Mark that this run should handle the exception. */
    public static SyncNoCheckExceptionSessionBuilder handleProblem() {
        val sessionBuilder = new SyncNoCheckExceptionSessionBuilder();
        sessionBuilder.handleProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should handle the exception. */
    public static SyncNoCheckExceptionSessionBuilder HandleThenIgnoreProblem() {
        return handleThenIgnoreProblem();
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SyncNoCheckExceptionSessionBuilder handleThenIgnoreProblem() {
        val sessionBuilder = new SyncNoCheckExceptionSessionBuilder();
        sessionBuilder.handleThenIgnoreProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore all the handled problem. */
    public static SyncSessionBuilder IgnoreHandledProblem() {
        return ignoreHandledProblem();
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SyncNoCheckExceptionSessionBuilder IgnoreException() {
        return ignoreException();
    }
    
        /** Mark that this run should ignore all the handled problem. */
    public static SyncSessionBuilder ignoreHandledProblem() {
        val sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.ignoreHandledProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SyncNoCheckExceptionSessionBuilder ignoreException() {
        val sessionBuilder = new SyncNoCheckExceptionSessionBuilder();
        sessionBuilder.ignoreException();
        return sessionBuilder;
    }
    
    /** Make the run to be run on a new thread. */
    public static AsyncSessionBuilder Asynchronously() {
        return new SyncSessionBuilder().asynchronously();
    }
    
    /** Make the run to be run on a new thread. */
    public static AsyncSessionBuilder asynchronously() {
        return new SyncSessionBuilder().asynchronously();
    }
    
    /** Run the session now. */
    public static <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        sessionBuilder.run(runnable);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @throws T
     */
    public static <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        SyncSessionBuilder sessionBuilder = new SyncSessionBuilder();
        return sessionBuilder.run(supplier);
    }
    
}