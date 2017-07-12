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

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import directget.get.exceptions.GetException;
import directget.get.supportive.CounterThreadFactory;
import directget.get.supportive.GetThreadFactoryExecutor;
import directget.run.Named;
import directget.run.Named.Predicate;
import lombok.val;

/**
 * This class provide access to the application scope.
 * 
 * @author nawaman
 */
public final class Get {
    
    /** This predicate specifies that all of the references are to be inherited */
    @SuppressWarnings("rawtypes")
    public static final Predicate<Ref> INHERIT_ALL = Named.Predicate("InheritAll", ref -> true);
    
    /** This predicate specifies that none of the references are to be inherited */
    @SuppressWarnings("rawtypes")
    public static final Predicate<Ref> INHERIT_NONE = Named.Predicate("InheritNone", ref -> false);
    
    
    /** The reference to the thread factory. */
    public static final Ref<ThreadFactory> _ThreadFactory_ = Ref.of(ThreadFactory.class, CounterThreadFactory.instance);
    
    /** The reference to the executor. */
    public static final Ref<Executor> _Executor_ = Ref.of(Executor.class).by(()->GetThreadFactoryExecutor.instance);
    
    
    private Get() {
        
    }
    
    static <T> Providing<T> getProvider(Ref<T> ref) {
        return App.Get().getProviding(ref);
    }
    
    /** @return the optional value associated with the given ref. */
    public static <T> Optional<T> _a(Ref<T> ref) {
        val optValue = App.Get()._a(ref);
        return optValue;
    }
    
    /** @return the optional value associated with the given class. */
    public static <T> Optional<T> _a(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val optValue = _a(ref);
        return optValue;
    }
    
    /** @return the value associated with the given class. */
    public static <T> T a(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val value = a(ref);
        return value;
    }
    
    /** @return the value associated with the given ref. */
    public static <T> T a(Ref<T> ref) {
        val optValue = _a(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * @return the value associated with the given class or return the elseValue
     *         if no value associated with the class.
     */
    public static <T> T a(Class<T> clzz, T elseValue) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseValue);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the elseValue
     *         if no value associated with the ref.
     */
    public static <T> T a(Ref<T> ref, T elseValue) {
        try {
            val optValue = _a(ref);
            val value = optValue.orElse(elseValue);
            return value;
        } catch (GetException e) {
            return elseValue;
        }
    }
    
    /**
     * @return the value associated with the given class or return the from
     *         elseSupplier if no value associated with the class.
     */
    public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseSupplier);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the from
     *         elseSupplier if no value associated with the ref.
     */
    public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
        val optValue = _a(ref);
        val value = optValue.orElseGet(elseSupplier);
        return value;
    }
    
    /** @return the optional value associated with the given ref. */
    public static <T> Optional<T> _the(Ref<T> ref) {
        val optValue = App.Get()._the(ref);
        return optValue;
    }
    
    /** @return the optional value associated with the given class. */
    public static <T> Optional<T> _the(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val optValue = _the(ref);
        return optValue;
    }
    
    /** @return the value associated with the given class. */
    public static <T> T the(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val value = the(ref);
        return value;
    }
    
    /** @return the value associated with the given ref. */
    public static <T> T the(Ref<T> ref) {
        val optValue = _the(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * @return the value associated with the given class or return the elseValue
     *         if no value associated with the class.
     */
    public static <T> T the(Class<T> clzz, T elseValue) {
        val ref = Ref.forClass(clzz);
        val value = the(ref, elseValue);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the elseValue
     *         if no value associated with the ref.
     */
    public static <T> T the(Ref<T> ref, T elseValue) {
        try {
            val optValue = _the(ref);
            val value = optValue.orElse(elseValue);
            return value;
        } catch (GetException e) {
            return elseValue;
        }
    }
    
    /**
     * @return the value associated with the given class or return the from
     *         elseSupplier if no value associated with the class.
     */
    public static <T> T the(Class<T> clzz, Supplier<T> elseSupplier) {
        val ref = Ref.forClass(clzz);
        val value = the(ref, elseSupplier);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the from
     *         elseSupplier if no value associated with the ref.
     */
    public static <T> T the(Ref<T> ref, Supplier<T> elseSupplier) {
        val optValue = _the(ref);
        val value = optValue.orElseGet(elseSupplier);
        return value;
    }
    
}
