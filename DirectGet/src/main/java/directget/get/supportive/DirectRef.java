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
package directget.get.supportive;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import directget.get.DirectRefWithRetainer;
import directget.get.Preferability;
import directget.get.Providing;
import directget.get.Ref;
import directget.get.supportive.retain.Retainer;
import directget.get.supportive.retain.RetainerBuilder;
import lombok.val;

/**
 * This reference implementation allows multiple references to a class to
 * mean different things.
 * 
 * @author nawaman
 **/
public class DirectRef<T> extends AbstractRef<T> implements Ref<T> {
    
    static final AtomicLong id = new AtomicLong();

    /**
     * The new id.
     * 
     * @return the new id.
     */
    public static long getNewId() {
        return  id.incrementAndGet();
    }
    
    private final String name;
    
    private final Providing<T> providing;
    
    /**
     * Constructor.
     * 
     * @param name
     *          the ref name.
     * @param targetClass
     *          the target class.
     * @param preferability
     *          the preferability.
     * @param factory
     *          the value factory.
     */
    public DirectRef(String name, Class<T> targetClass, Preferability preferability, Supplier<? extends T> factory) {
        super(targetClass);
        val prefer = (preferability != null) ? preferability : Preferability.Default;
        
        this.name = Optional.ofNullable(name).orElse(targetClass.getName() + "#" + id.getAndIncrement());
        this.providing = (factory == null) 
                ? null 
                : new Providing<>(this, prefer, factory);
    }
    
    /** @return the name of the reference */
    public String getName() {
        return this.name;
    }
    
    /** @return the default object. */
    @Override
    public final T get() {
        if (providing == null) {
            return super.get();
        } else {
            return providing.get();
        }
    }
    
    /** @return the optional default object. */
    @Override
    public final Optional<T> _get() {
        return Optional.ofNullable(get());
    }
    
    @Override
    public Providing<T> getProviding() {
        return this.providing;
    }
    
    /**
     * Returns the preferability.
     * 
     * @return preferability
     */
    public Preferability getPreferability() {
        return (this.providing != null) ? this.providing.getPreferability() : Preferability.Default;
    }
    
    /**
     * For Direct ref to be equals, they have to be the same object.
     **/
    @Override
    public final boolean equals(Object obj) {
        return this == obj;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "Ref<" + this.name + ":" + this.getTargetClass().getName() + ">";
    }
    
    public DirectRefWithRetainer<T> retained() {
        return new DirectRefWithRetainer<>(name, getTargetClass(), getPreferability(), providing.getSupplier());
       }

    
    //== Wither =======================================================================================================
//    
//    DirectRef<T> but(Providing<T> newProviding) {
//        return new DirectRef<T>(name, getTargetClass(), Preferability.Normal, newProviding.getSupplier());
//    }
//    
//    /**
//     * @return the new providing similar to this one except with the value.
//     **/
//    public DirectRef<T> with(T value) {
//        return but(providing.butWith(value));
//    }
//    
//    /**
//     * @return the new providing similar to this one except with the value.
//     **/
//    public DirectRef<T> withA(Ref<T> ref) {
//        return but(providing.butWithA(ref));
//    }
//    
//    /**
//     * @return the new providing similar to this one except the supplied by the given supplier.
//     **/
//    @SuppressWarnings("unchecked")
//    public DirectRef<T> by(Supplier<? extends T> supplier) {
//        if (providing == null) {
//            return new DirectRef<>(name, getTargetClass(), getPreferability(), supplier);
//        }
//        
//        return but(providing.butBy((Supplier<T>)supplier));
//    }
////    
//    Retainer<T> getRetainer() {
//        val supplier = providing.getSupplier();
//        val retainer
//            = ((supplier instanceof Retainer)
//            ? ((Retainer<T>)supplier)
//            : (Retainer<T>)new RetainerBuilder<T>(supplier).globally().always());
//        return retainer;
//    }
//    
//    DirectRef<T> but(Retainer<T> newRetainer) {
//        return new DirectRef<T>(name, getTargetClass(), Preferability.Normal, newRetainer);
//    }
//
//    /** @return the new ref similar to this one except that it retains globally. **/
//    public DirectRef<T> globally() {
//        return but(getRetainer().butGlobally());
//    }
//
//    /** @return the new ref similar to this one except that it retains locally. **/
//    public DirectRef<T> locally() {
//        return but(getRetainer().butLocally());
//    }
//    
//    /** @return the new ref similar to this one except that it always retains its value. **/
//    public DirectRef<T> always() {
//        return but(getRetainer().butAlways());
//    }
//    
//    /** @return the new ref similar to this one except that it never retains its value. **/
//    public DirectRef<T> never() {
//        return but(getRetainer().butNever());
//    }
//    
//    /** @return the new ref similar to this one except that it retains its value with in current thread. **/
//    public DirectRef<T> forCurrentThread() {
//        return but(getRetainer().forCurrentThread());
//    }
//
//    /** @return the new ref similar to this one except that it retains its value follow the give reference value ('same' rule). **/
//    public <R> DirectRef<T> forSame(Ref<R> ref) {
//        return but(getRetainer().forSame(ref));
//    }
//
//    /** @return the new ref similar to this one except that it retains its value follow the give reference value ('equivalent' rule). **/
//    public <R> DirectRef<T> forEquivalent(Ref<R> ref) {
//        return but(getRetainer().forEquivalent(ref));
//    }
//    
//    /** @return the new ref similar to this one except that it retains its value for a given time period (in millisecond). **/
//    public <R> DirectRef<T> forTime(long time) {
//        return but(getRetainer().forTime(null, time));
//    }
//
//    /** @return the new ref similar to this one except that it retains its value for a given time period. **/
//    public <R> DirectRef<T> forTime(long time, TimeUnit unit) {
//        return but(getRetainer().forTime(null, time, unit));
//    }
//    
//    /** @return the new ref similar to this one except that it retains its value for a given time period (in millisecond). **/
//    public <R> DirectRef<T> forTime(Long startMilliseconds, long time) {
//        return but(getRetainer().forTime(startMilliseconds, time));
//    }
//
//    /** @return the new ref similar to this one except that it retains its value for a given time period. **/
//    public <R> DirectRef<T> forTime(Long startMilliseconds, long time, TimeUnit unit) {
//        return but(getRetainer().forTime(startMilliseconds, time, unit));
//    }
    
}