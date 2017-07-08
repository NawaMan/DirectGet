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
package directget.run;

import java.util.Collection;
import java.util.stream.Stream;

import directget.get.Scope;
import directget.run.session.NewThreadSessionBuilder;
import directget.run.session.SameThreadNoCheckExceptionSessionBuilder;
import directget.run.session.SameThreadSessionBuilder;
import lombok.val;

/**
 * This class offer a natural way to run something.
 * 
 * @author nawaman
 **/
public class Run {
    
    /** Specify that the running should be done under the given scope */
    public static SameThreadSessionBuilder under(Scope scope) {
        return new SameThreadSessionBuilder().under(scope);
    }
    
    /** Specify that the running should be done under the given scope */
    public static SameThreadSessionBuilder Under(Scope scope) {
        return new SameThreadSessionBuilder().under(scope);
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder with(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder With(Wrapper... wrappers) {
        return with(Stream.of(wrappers));
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder with(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder With(Collection<Wrapper> wrappers) {
        return with(wrappers.stream());
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder With(Stream<Wrapper> providings) {
        return with(providings);
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder with(Stream<Wrapper> wrappers) {
        SameThreadSessionBuilder sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.with(wrappers);
        return sessionBuilder;
    }
    
    /** Mark that this run should handle the exception. */
    public static SameThreadNoCheckExceptionSessionBuilder HandleProblem() {
        return handleProblem();
    }
    
    /** Mark that this run should handle the exception. */
    public static SameThreadNoCheckExceptionSessionBuilder handleProblem() {
        val sessionBuilder = new SameThreadNoCheckExceptionSessionBuilder();
        sessionBuilder.handleProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should handle the exception. */
    public static SameThreadNoCheckExceptionSessionBuilder HandleThenIgnoreProblem() {
        return handleThenIgnoreProblem();
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SameThreadNoCheckExceptionSessionBuilder handleThenIgnoreProblem() {
        val sessionBuilder = new SameThreadNoCheckExceptionSessionBuilder();
        sessionBuilder.handleThenIgnoreProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore all the handled problem. */
    public static SameThreadSessionBuilder IgnoreHandledProblem() {
        return ignoreHandledProblem();
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SameThreadNoCheckExceptionSessionBuilder IgnoreException() {
        return ignoreException();
    }
    
        /** Mark that this run should ignore all the handled problem. */
    public static SameThreadSessionBuilder ignoreHandledProblem() {
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.ignoreHandledProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SameThreadNoCheckExceptionSessionBuilder ignoreException() {
        val sessionBuilder = new SameThreadNoCheckExceptionSessionBuilder();
        sessionBuilder.ignoreException();
        return sessionBuilder;
    }
    
    /** Make the run to be run on a new thread. */
    public static NewThreadSessionBuilder OnNewThread() {
        return new SameThreadSessionBuilder().onNewThread();
    }
    
    /** Make the run to be run on a new thread. */
    public static NewThreadSessionBuilder onNewThread() {
        return new SameThreadSessionBuilder().onNewThread();
    }
    
    /** Run the session now. */
    public static <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
        SameThreadSessionBuilder sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.run(runnable);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @throws T
     */
    public static <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        SameThreadSessionBuilder sessionBuilder = new SameThreadSessionBuilder();
        return sessionBuilder.run(supplier);
    }
    
}