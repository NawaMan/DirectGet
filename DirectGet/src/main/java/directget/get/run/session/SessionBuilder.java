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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import directget.get.App;
import directget.get.GetInstance;
import directget.get.Providing;
import directget.get.Scope;
import directget.get.run.Failable;
import directget.get.run.Wrapper;
import directget.get.run.exceptions.ProblemHandledException;
import lombok.val;

/**
 * Builder for RunSession.
 * 
 * @author NawaMan
 **/
public abstract class SessionBuilder<SB extends SessionBuilder<SB>> {
    
    @SuppressWarnings("rawtypes")
    Function<Failable.Runnable, Runnable> failHandler = runnable->runnable.gracefully();
    List<Function<Runnable, Runnable>>    wrappers    = new ArrayList<>();
    
    private Scope scope = App.scope;
    
    /** Default constructor. */
    public SessionBuilder() {
        this(null);
    }
    
    /** Constructor that take a scope. */
    public SessionBuilder(Scope scope) {
        this.scope = Optional.ofNullable(scope).orElse(App.scope);
    }
    
    /** @return the get instance of the current scope. */
    public GetInstance get() {
        return scope.Get();
    }
    
    /** Specify that the running should be done under the given scope */
    @SuppressWarnings("unchecked")
    public SB under(Scope scope) {
        this.scope = Optional.ofNullable(scope).orElse(App.scope);
        return (SB) this;
    }
    
    /** Add the wrapper */
    @SuppressWarnings({ "unchecked" })
    public SB with(Wrapper... wrappers) {
        with(Stream.of(wrappers));
        return (SB) this;
    }
    
    /** Add the wrapper */
    @SuppressWarnings({ "unchecked" })
    public SB with(Collection<Wrapper> wrappers) {
        with(wrappers.stream());
        return (SB) this;
    }
    
    /** Add the wrapper */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SB with(Stream<Wrapper> wrappers) {
        val providings = new ArrayList<Providing>();
        wrappers.forEach(wrapper->{
            if (wrapper instanceof Providing) {
                providings.add((Providing)wrapper);
            } else {
                this.wrappers.add(wrapper);
            }
        });
        if (!providings.isEmpty()) {
            this.wrappers.add(runnable->()->{
                scope.Get().substitute(providings.stream(), runnable);
            });
        }
        return (SB) this;
    }
    
    private SameThreadNoCheckExceptionSessionBuilder toSameThreadNoCheckExceptionSessionBuilder() {
        val builder = new SameThreadNoCheckExceptionSessionBuilder();
        builder.failHandler = this.failHandler;
        builder.wrappers    = new ArrayList<>(this.wrappers);
        return builder;
    }
    
    /** Mark that this run should handle the exception. */
    public SameThreadNoCheckExceptionSessionBuilder handleProblem() {
        val builder
                = (this instanceof SameThreadNoCheckExceptionSessionBuilder)
                ? (SameThreadNoCheckExceptionSessionBuilder)this
                : toSameThreadNoCheckExceptionSessionBuilder();
        builder.failHandler = Failable.Runnable::handledly;
        return builder;
    }
    
    /** Mark that this run should ignore thrown exception. */
    public SameThreadNoCheckExceptionSessionBuilder handleThenIgnoreProblem() {
        val builder
                = (this instanceof SameThreadNoCheckExceptionSessionBuilder)
                ? (SameThreadNoCheckExceptionSessionBuilder)this
                : toSameThreadNoCheckExceptionSessionBuilder();
        
        builder.failHandler = runnable->((Failable.Runnable<Throwable>)()->{
            try {
                runnable.handledly().run();
            } catch (ProblemHandledException e) {
            }
        }).gracefully();
        return builder;
    }
    
    /** Mark that this run should ignore all the handled problem. */
    @SuppressWarnings("unchecked")
    public SB ignoreHandledProblem() {
        failHandler = runnable->((Failable.Runnable<Throwable>)()->{
            try {
                runnable.run();
            } catch (ProblemHandledException e) {
            }
        }).gracefully();
        return (SB)this;
    }
    
    /** Mark that this run should ignore thrown exception. */
    public SameThreadNoCheckExceptionSessionBuilder ignoreException() {
        val builder
                = (this instanceof SameThreadNoCheckExceptionSessionBuilder)
                ? (SameThreadNoCheckExceptionSessionBuilder)this
                : toSameThreadNoCheckExceptionSessionBuilder();
        builder.failHandler = Failable.Runnable::carelessly;
        return builder;
    }
    
    /** Make the run to be run on a new thread. */
    public NewThreadSessionBuilder onNewThread() {
        val newThreadSessionBuilder = new NewThreadSessionBuilder();
        newThreadSessionBuilder.wrappers.addAll(this.wrappers);
        newThreadSessionBuilder.wrappers.add(newThreadSessionBuilder.newThreadwrapper);
        return newThreadSessionBuilder;
    }
    
    /** Build the session for later use. */
    public WrapperContext build() {
        return new SameThreadWrapperContext(failHandler, wrappers);
    }
    
}
