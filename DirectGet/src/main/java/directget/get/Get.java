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

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import directget.get.run.Named;
import directget.get.run.Named.Predicate;
import directget.get.supportive.CounterThreadFactory;
import directget.get.supportive.GetThreadFactoryExecutor;
import directget.get.supportive.Provider;
import directget.get.supportive.RefFor;
import directget.get.supportive.RefOf;
import lombok.val;

/**
 * This class provide access to the application scope.
 * 
 * @author NawaMan
 */
public final class Get {
    
    /** This predicate specifies that all of the references are to be inherited */
    @SuppressWarnings("rawtypes")
    public static final Predicate<Ref> INHERIT_ALL = Named.Predicate("InheritAll", ref -> true);
    
    /** This predicate specifies that none of the references are to be inherited */
    @SuppressWarnings("rawtypes")
    public static final Predicate<Ref> INHERIT_NONE = Named.Predicate("InheritNone", ref -> false);
    
    
    /** The reference to the thread factory. */
    public static final RefOf<ThreadFactory> DefaultThreadFactory = Ref.of(ThreadFactory.class).defaultedTo(CounterThreadFactory.instance);
    
    /** The reference to the executor. */
    public static final RefOf<Executor> DefaultExecutor = Ref.of(Executor.class).defaultedToBy(()->GetThreadFactoryExecutor.instance);
    
    
    private Get() {
        
    }
    
    /**
     * Initialize the App scope.
     * 
     * @return {@code true} if the initialization actually happen with this call.
     **/
    public static boolean initialize() {
        return App.initializeIfAbsent(new Configuration());
    }
    
    /**
     * Initialize the App scope with the given configuration.
     * 
     * @param theGivenConfiguration
     * @throws AppScopeAlreadyInitializedException
     */
    public static void initialize(Configuration theGivenConfiguration) throws AppScopeAlreadyInitializedException {
        App.initialize(theGivenConfiguration);
    }

    /**
     * Initialize the App scope with the given configuration if it has not yet been iniialized.
     * 
     * @param theGivenConfiguration
     * @return {@code true} if the initialization actually happen with this call.
     * @throws AppScopeAlreadyInitializedException
     */
    public static boolean initializeIfAbsent(Configuration configuration) {
        return App.initializeIfAbsent(configuration);
    }
    
    
    static <T> Provider<T> getProvider(Ref<T> ref) {
        return App.Get().getProvider(ref);
    }
    
    //-- a --
    
    /** @return the optional value associated with the given ref. */
    public static <T> Optional<T> _a(RefFor<T> ref) {
        val optValue = App.scope.get()._a(ref);
        return optValue;
    }
    
    /** @return the optional value associated with the given class. */
    public static <T> Optional<T> _a(Class<T> clzz) {
        val optValue = App.scope.get()._a(clzz);
        return optValue;
    }
    
    /** @return the value associated with the given ref. */
    public static <T> T a(Class<T> clzz) {
        val optValue = App.scope.get().a(clzz);
        return optValue;
    }
    
    /** @return the value associated with the given class. */
    public static <T> T a(RefFor<T> ref) {
        val optValue = App.scope.get().a(ref);
        return optValue;
    }
    
    //-- the --
    
    /** @return the optional value associated with the given ref. */
    public static <T> Optional<T> _the(RefOf<T> ref) {
        val optValue = App.scope.get()._the(ref);
        return optValue;
    }
    
    /** @return the value associated with the given ref. */
    public static <T> T the(RefOf<T> ref) {
        val optValue = App.scope.get()._the(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    /** @return the optional value associated with the given targetClass. */
    public static <T> Optional<T> _the(Class<T> targetClass) {
        val optValue = App.scope.get()._the(Ref.defaultOf(targetClass));
        return optValue;
    }
    
    /** @return the value associated with the given targetClass. */
    public static <T> T the(Class<T> targetClass) {
        val optValue = App.scope.get()._the(targetClass);
        val value = optValue.orElse(null);
        return value;
    }
    
    //-- any --
    
    // TODO - Any = the(<defaultRef of targetClass>).or(a(<targetClass>))
    
    /** @return the optional value associated with the given ref. */
    public static <T> Optional<T> _any(Ref<T> ref) {
        val optValue = App.scope.get()._any(ref);
        return optValue;
    }
    
    /** @return the value associated with the given ref. */
    public static <T> T any(Ref<T> ref) {
        val optValue = App.scope.get()._any(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    //-- From --

    /** @return the optional value of object create from the factory. */
    public static <T, F extends Factory<T>> Optional<T> _from(Class<F> factoryRef) {
        val optValue = App.scope.get()._a(factoryRef);
        return optValue.map(Factory::make);
    }

    /** @return the optional value of object create from the factory. */
    public static <T, F extends Factory<T>> T from(Class<F> factoryRef) {
        val optValue = App.scope.get()._a(factoryRef);
        return optValue.map(Factory::make).orElse(null);
    }

    /** @return the optional value of object create from the factory. */
    public static <T, F extends Factory<T>> Optional<T> _from(Ref<F> factoryRef) {
        val optValue = App.scope.get()._any(factoryRef);
        return optValue.map(Factory::make);
    }

    /** @return the optional value of object create from the factory. */
    public static <T, F extends Factory<T>> T from(Ref<F> factoryRef) {
        val optValue = App.scope.get()._any(factoryRef);
        return optValue.map(Factory::make).orElse(null);
    }
    
}
