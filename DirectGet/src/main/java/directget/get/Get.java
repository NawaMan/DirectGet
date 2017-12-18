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
import directget.get.supportive.RefTo;
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
    public static final RefTo<ThreadFactory> DefaultThreadFactory = Ref.to(ThreadFactory.class).defaultedTo(CounterThreadFactory.instance);
    
    /** The reference to the executor. */
    public static final RefTo<Executor> DefaultExecutor = Ref.to(Executor.class).defaultedToBy(()->GetThreadFactoryExecutor.instance);
    
    
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
     * Initialize the App scope with the given configuration if it has not yet been initialized.
     * 
     * @param theGivenConfiguration
     * @return {@code true} if the initialization actually happen with this call.
     */
    public static boolean initializeIfAbsent(Configuration theGivenConfiguration) {
        return App.initializeIfAbsent(theGivenConfiguration);
    }
    
    
    static <T> Provider<T> getProvider(Ref<T> ref) {
        return App.Get().getProvider(ref);
    }
    
    //-- the --
    
    /**
     * The optional value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the optional value associated with theGivenRef.
     **/
    public static <T> Optional<T> _the(Ref<T> theGivenRef) {
        val optValue = App.scope.get()._the(theGivenRef);
        return optValue;
    }
    
    /**
     * The value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the value associated with theGivenRef.
     **/
    public static <T> T the(Ref<T> theGivenRef) {
        val optValue = App.scope.get()._the(theGivenRef);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * Return the optional value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the optional value associated with the given targetClass.
     **/
    public static <T> Optional<T> _the(Class<T> targetClass) {
        val optValue = App.scope.get()._the(Ref.defaultOf(targetClass));
        return optValue;
    }
    
    /**
     * Return the value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the value associated with the given targetClass.
     **/
    public static <T> T the(Class<T> targetClass) {
        val optValue = App.scope.get()._the(targetClass);
        val value = optValue.orElse(null);
        return value;
    }
    
    //-- value --
    
    /**
     * The optional value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the optional value associated with theGivenRef.
     **/
    public static <T> Optional<T> _value(Ref<T> theGivenRef) {
        val optValue = App.scope.get()._the(theGivenRef);
        return optValue;
    }
    
    /**
     * The value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the value associated with theGivenRef.
     **/
    public static <T> T value(Ref<T> theGivenRef) {
        val optValue = App.scope.get()._the(theGivenRef);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * Return the optional value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the optional value associated with the given targetClass.
     **/
    public static <T> Optional<T> _value(Class<T> targetClass) {
        val optValue = App.scope.get()._the(Ref.defaultOf(targetClass));
        return optValue;
    }
    
    /**
     * Return the value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the value associated with the given targetClass.
     **/
    public static <T> T value(Class<T> targetClass) {
        val optValue = App.scope.get()._the(targetClass);
        val value = optValue.orElse(null);
        return value;
    }
    
    //-- From --

    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryRef 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> Optional<T> _from(Class<F> factoryRef) {
        val optValue = App.scope.get()._the(factoryRef);
        return optValue.map(Factory::make);
    }

    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryRef 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> T from(Class<F> factoryRef) {
        val optValue = App.scope.get()._the(factoryRef);
        return optValue.map(Factory::make).orElse(null);
    }

    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryRef 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> Optional<T> _from(Ref<F> factoryRef) {
        val optValue = App.scope.get()._the(factoryRef);
        return optValue.map(Factory::make);
    }

    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryRef 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> T from(Ref<F> factoryRef) {
        val optValue = App.scope.get()._the(factoryRef);
        return optValue.map(Factory::make).orElse(null);
    }
    
}
