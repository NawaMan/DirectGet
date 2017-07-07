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
package directget.get;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import directget.get.exceptions.FailableException;
import directget.get.exceptions.ProblemHandledException;
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
    public static SameThreadSessionBuilder with(Function<Runnable, Runnable> wrapper) {
        return new SameThreadSessionBuilder().with(wrapper);
    }
    
    /** Add the wrapper */
    public static SameThreadSessionBuilder With(Function<Runnable, Runnable> wrapper) {
        return with(wrapper);
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder with(Providing... providings) {
        return with(Stream.of(providings));
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder With(Providing... providings) {
        return with(Stream.of(providings));
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder with(Collection<Providing> providings) {
        return with(providings.stream());
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder With(Collection<Providing> providings) {
        return with(providings.stream());
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder with(Stream<Providing> providings) {
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.with(runnable -> () -> {
            sessionBuilder.get().substitute(providings, runnable);
        });
        return sessionBuilder;
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder With(Stream<Providing> providings) {
        return with(providings);
    }
    
    /** Add the wrapper */
    public SameThreadSessionBuilder by(Function<Runnable, Runnable> wrapper) {
        return with(wrapper);
    }
    
    /** Add the wrapper */
    public SameThreadSessionBuilder By(Function<Runnable, Runnable> wrapper) {
        return with(wrapper);
    }
    
    /** Add the wrapper */
    public SameThreadSessionBuilder use(Function<Runnable, Runnable> wrapper) {
        return use(wrapper);
    }
    
    /** Add the wrapper */
    public SameThreadSessionBuilder Use(Function<Runnable, Runnable> wrapper) {
        return use(wrapper);
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder use(Providing... providings) {
        return use(Stream.of(providings));
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder Use(Providing... providings) {
        return use(providings);
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder use(Collection<Providing> providings) {
        return use(providings.stream());
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder Use(Collection<Providing> providings) {
        return use(providings);
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder use(Stream<Providing> providings) {
        return with(providings);
    }
    
    /** Add the wrapper */
    @SuppressWarnings("rawtypes")
    public static SameThreadSessionBuilder Use(Stream<Providing> providings) {
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.with(runnable -> () -> {
            sessionBuilder.get().substitute(providings, runnable);
        });
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore all the handled problem. */
    public static SameThreadSessionBuilder IgnoreHandledProblem() {
        return ignoreHandledProblem();
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SameThreadSessionBuilder IgnoreException() {
        return ignoreException();
    }
    
        /** Mark that this run should ignore all the handled problem. */
    public static SameThreadSessionBuilder ignoreHandledProblem() {
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.ignoreHandledProblem();
        return sessionBuilder;
    }
    
    /** Mark that this run should ignore thrown exception. */
    public static SameThreadSessionBuilder ignoreException() {
        val sessionBuilder = new SameThreadSessionBuilder();
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
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.run(runnable);
    }
    
    /** Run the session now. */
    public static <T extends Throwable> void start(Failable.Runnable<T> runnable) throws T {
        val sessionBuilder = new SameThreadSessionBuilder();
        sessionBuilder.run(runnable);
    }
    
    /**
     * Run the given supplier and return a value.
     * 
     * @throws T
     */
    public static <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
        val sessionBuilder = new SameThreadSessionBuilder();
        return sessionBuilder.run(supplier);
    }
    
    
    
    
    /** Alias type. */
    public static interface Wrapper extends Function<Runnable, Runnable> {
    }
    
    /** Wrapper for running on the new thread. */
    public static class NewThreadWrapper implements Wrapper {
        
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
    
    /** Builder for RunSession. **/
    public static abstract class SessionBuilder<SB extends SessionBuilder<SB>> {
        
        @SuppressWarnings("rawtypes")
        Function<Failable.Runnable, Runnable> failHandler = runnable->runnable.gracefully();
        final List<Function<Runnable, Runnable>> wrappers = new ArrayList<>();
        
        private Scope scope = App.scope;
        
        /** Another way to chain the invocation */
        public final SessionBuilder<SB> and = this;
        
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
        @SuppressWarnings("unchecked")
        public SB with(Function<Runnable, Runnable> wrapper) {
            if (wrapper != null) {
                wrappers.add(wrapper);
            }
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB with(Providing... providings) {
            with(Stream.of(providings));
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB with(Collection<Providing> providings) {
            with(providings.stream());
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB with(Stream<Providing> providings) {
            with(runnable -> () -> {
                get().substitute(providings, runnable);
            });
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings("unchecked")
        public SB by(Function<Runnable, Runnable> wrapper) {
            with(wrapper);
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings("unchecked")
        public SB use(Function<Runnable, Runnable> wrapper) {
            with(wrapper);
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB use(Providing... providings) {
            use(Stream.of(providings));
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB use(Collection<Providing> providings) {
            with(providings.stream());
            return (SB) this;
        }
        
        /** Add the wrapper */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public SB use(Stream<Providing> providings) {
            with(runnable -> () -> {
                get().substitute(providings, runnable);
            });
            return (SB) this;
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
        @SuppressWarnings("unchecked")
        public SB ignoreException() {
            failHandler = Failable.Runnable::carelessly;
            return (SB)this;
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
    
    /**
     * This class make building a run a bit easier.
     */
    public static class SameThreadSessionBuilder extends SessionBuilder<SameThreadSessionBuilder> {
        
        /** Another way to chain the invocation */
        public final SameThreadSessionBuilder and = this;
        
        /** Default constructor. */
        public SameThreadSessionBuilder() {
            super(null);
        }
        
        /** Constructor that take a scope. */
        public SameThreadSessionBuilder(Scope scope) {
            super(scope);
        }
        
        /** Build the session for later use. */
        public SameThreadWrapperContext build() {
            return new SameThreadWrapperContext(failHandler, wrappers);
        }
        
        /**
         * Run the given supplier and return a value.
         * 
         * @throws T
         */
        public <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
            return build().run(supplier);
        }
        
        /**
         * Run the given supplier and return a value.
         * 
         * @throws T
         */
        public <R, T extends Throwable> R start(Failable.Supplier<R, T> supplier) throws T {
            return build().run(supplier);
        }
        
        /** Run the session now. */
        public <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
            build().run(runnable);
        }
        
        /** Run the session now. */
        public <T extends Throwable> void start(Failable.Runnable<T> runnable) throws T {
            build().run(runnable);
        }
        
    }
    
    /** The wrapper for a new thread run. */
    public static class NewThreadSessionBuilder extends SessionBuilder<NewThreadSessionBuilder> {
        
        @SuppressWarnings("rawtypes")
        private final List<Ref> includedRefs = new ArrayList<>();
        @SuppressWarnings("rawtypes")
        private final List<Ref> excludedRefs = new ArrayList<>();
        
        private Boolean inheritMass = null;
        
        private Fork fork = null;
        
        private NewThreadWrapper newThreadwrapper = new NewThreadWrapper(this);
        
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
        
        /** Run the given supplier and return a value. */
        public <R, T extends Throwable> CompletableFuture<R> start(Failable.Supplier<R, T> supplier) {
            return build().run(supplier);
        }
        
        /** Run the session now. */
        public <T extends Throwable> CompletableFuture<Void> run(Failable.Runnable<T> runnable) {
            return build().run(()->{
                runnable.run();
                return null;
            });
        }
        
        /** Run the session now. */
        public <T extends Throwable> CompletableFuture<Void> start(Failable.Runnable<T> runnable) throws T {
            return build().run(()->{
                runnable.run();
                return null;
            });
        }
    }
    
    /** The contains the wrappers so that we can run something within them. */
    public static class WrapperContext {
        
        @SuppressWarnings("rawtypes")
        final Function<Failable.Runnable, Runnable> failHandler;
        final List<Function<Runnable, Runnable>> wrappers;
        
        WrapperContext(@SuppressWarnings("rawtypes") Function<Failable.Runnable, Runnable> failHandler, List<Function<Runnable, Runnable>> functions) {
            this.failHandler = Optional.ofNullable(failHandler).orElse(Failable.Runnable::gracefully);
            this.wrappers = functions == null ? Collections.emptyList()
                    : Collections
                            .unmodifiableList(functions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        
        /** Run something within this context. */
        public <T extends Throwable> void start(Failable.Runnable<T> runnable) throws T {
            run(runnable);
        }
        
        /** Run something within this context. */
        @SuppressWarnings("unchecked")
        public <T extends Throwable> void run(Failable.Runnable<T> runnable) throws T {
            Runnable current = failHandler.apply(runnable);
            for (int i = wrappers.size(); i-- > 0;) {
                val wrapper = wrappers.get(i);
                val wrapped = wrapper.apply(current);
                if (wrapped != null) {
                    current = wrapped;
                }
            }
            try {
                current.run();
            } catch (FailableException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException)cause;
                }
                
                throw (T)cause;
            }
        }
        
    }
    
    /** The contains the wrappers so that we can run something within them. */
    public static class SameThreadWrapperContext extends WrapperContext {
        
        @SuppressWarnings("rawtypes")
        SameThreadWrapperContext(
                Function<Failable.Runnable, Runnable> failHandler,
                List<Function<Runnable, Runnable>> functions) {
            super(failHandler, functions);
        }
        
        /** Run the given supplier and return a value. */
        public <R, T extends Throwable> R run(Failable.Supplier<R, T> supplier) throws T {
            val result = new AtomicReference<R>();
            val runnable = (Failable.Runnable<T>) () -> {
                val theResult = supplier.get();
                result.set(theResult);
            };
            super.run(runnable);
            return result.get();
        }
        
    }
    
    /** The contains the wrappers so that we can run something within them. */
    public static class NewThreadWrapperContext extends WrapperContext {
        
        @SuppressWarnings("rawtypes")
        NewThreadWrapperContext(Function<Failable.Runnable, Runnable> failHandler, List<Function<Runnable, Runnable>> functions) {
            super(failHandler, functions);
        }
        
        /** Run the given supplier and return a value. */
        @SuppressWarnings("unchecked")
        public <R, T extends Throwable> CompletableFuture<R> run(Failable.Supplier<R, T> supplier) {
            CompletableFuture<R> future = new CompletableFuture<R>();
            val runnable = (Failable.Runnable<T>) () -> {
                try {
                    val theResult = supplier.get();
                    future.complete(theResult);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            };
            Runnable current = failHandler.apply(runnable);
            for (int i = wrappers.size(); i-- > 0;) {
                val wrapper = wrappers.get(i);
                val wrapped = wrapper.apply(current);
                if (wrapped != null) {
                    current = wrapped;
                }
            }
            try {
                current.run();
            } catch (FailableException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException)cause;
                }
                
                future.completeExceptionally((T)cause);
            }
            return future;
        }
        
    }
    
}