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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import directget.get.run.Named;
import directget.get.run.Named.Predicate;
import directget.get.supportive.CounterThreadFactory;
import directget.get.supportive.GetThreadFactoryExecutor;
import directget.get.supportive.Provider;
import directget.get.supportive.RefTo;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This class provide access to the application scope.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class })
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
    
    static <T> Provider<T> getProvider(Ref<T> ref) {
        return App.Get().getProvider(ref);
    }
    
    //-- the --
    
    /**
     * The value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the value associated with theGivenRef.
     **/
    public static <T> T the(Ref<T> theGivenRef) {
        val value = App.scope.get().the(theGivenRef);
        return value;
    }
    
    /**
     * Return the value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the value associated with the given targetClass.
     **/
    public static <T> T the(Class<T> targetClass) {
        val value = App.scope.get().the(targetClass);
        return value;
    }
    
    //-- value --
    
    /**
     * The value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the value associated with theGivenRef.
     **/
    public static <T> T valueOf(Ref<T> theGivenRef) {
        val value = App.scope.get().the(theGivenRef);
        return value;
    }
    
    /**
     * Return the value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the value associated with the given targetClass.
     **/
    public static <T> T valueOf(Class<T> targetClass) {
        val value = App.scope.get().the(targetClass);
        return value;
    }
    
    //-- From --

    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryClass 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> T from(Class<F> factoryClass) {
        T value = App.scope.get().the(factoryClass).whenNotNull().map(Factory::make).orElse(null);
        return value;
    }
    
    /**
     * Return the optional value of object create from the factory.
     * 
     * @param factoryRef 
     * @return the optional value of object create from the factory.
     **/
    public static <T, F extends Factory<T>> T from(Ref<F> factoryRef) {
        T value = App.scope.get().the(factoryRef).whenNotNull().map(Factory::make).orElse(null);
        return value;
    }
    
}
