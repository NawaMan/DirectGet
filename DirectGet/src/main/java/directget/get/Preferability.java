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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.val;
import lombok.experimental.ExtensionMethod;

// OK - I feel like am going to regret this but this closed design (of using Enums) makes things
//        much simpler and it might worth the thread off.
/**
 * This enum is used to specify the preferability of a providing.
 * 
 * @author nawaman
 */
@ExtensionMethod({ Extensions.class })
public enum Preferability {
    
    /** Only use when no other is preferred. */
    Default,
    /** Whatever */
    Normal,
    /** Use me first! */
    Dictate;
    
    private static Function<Scope, String> scopeToXRay = Scope::toXRayString;
    private static Function<ProvidingStackMap, String> stackToXRay = ProvidingStackMap::toXRayString;
    
    private static Function<String, String> addingTabIndentation = str -> str.replaceAll("\n", "\n\t");
    
    /**
     * @return {@code true} if the given preferability is the same as this
     *         preferability.
     */
    public boolean is(Preferability preferability) {
        return this == preferability;
    }
    
    /**
     * @return {@code true} if the given providing has the same preferability as
     *         this preferability.
     */
    public <T> boolean is(Providing<T> providing) {
        if (providing == null) {
            return false;
        }
        return is(providing.getPreferability());
    }
    
    /** */
    public static final Ref<DetermineProvidingListener> _Listener_ = Ref.of(DetermineProvidingListener.class,
            Named.supplier("NULL", () -> (DetermineProvidingListener) null));
    
    static final AtomicBoolean _ListenerEnabled_ = new AtomicBoolean(true);
    
    // TODO - Clean this up.
    /**  */
    @FunctionalInterface
    public static interface DetermineProvidingListener {
        
        /**  */
        public <T> void onDetermine(Ref<T> ref, String from, Providing<T> result, Supplier<String> stackTraceSupplier,
                Supplier<String> xraySupplier);
        
    }
    
    // The code in the following method is heavily duplicated.
    // That is intentional, if we are wondering where a providing came from,
    // debugging these method will give you that answer very quickly.
    // Ok, I am going to regret typing this too .... but
    // This logic is not intended or supposed to be changed often.
    /**
     * Determine the providing for Get.
     * 
     * @return the providing.
     */
    public static <T> Providing<T> determineProviding(Ref<T> ref, Scope parentScope, Scope currentScope,
            ProvidingStackMap stacks) {
        Optional<BiConsumer<String, Providing<T>>> alarm = Optional
                .ofNullable((!_ListenerEnabled_.get() || (ref == _Listener_)) ? null : Get.a(_Listener_))
                .map(listener -> (foundSource, foundProviding) -> {
                    listener.onDetermine(ref, foundSource, foundProviding, Preferability::callStackToString,
                            getXRayString(ref, parentScope, currentScope, stacks));
                });
        
        val refProviding = ref.getProviding();
        if (Dictate.is(refProviding)) {
            alarm.ifPresent(it -> it.accept("Ref", refProviding));
            return refProviding;
        }
        
        val parentProviding = (parentScope != null) ? parentScope.getProviding(ref) : null;
        if (Dictate.is(parentProviding)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProviding));
            return parentProviding;
        }
        
        val configProviding = currentScope.getProviding(ref);
        if (Dictate.is(configProviding)) {
            alarm.ifPresent(it -> it.accept("Config", configProviding));
            return configProviding;
        }
        
        val stackProviding = stacks.peek(ref);
        if (Dictate.is(stackProviding)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProviding));
            return stackProviding;
        }
        
        // At this point, non is dictate.
        
        if (Normal.is(stackProviding)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProviding));
            return stackProviding;
        }
        if (Normal.is(configProviding)) {
            alarm.ifPresent(it -> it.accept("Config", configProviding));
            return configProviding;
        }
        if (Normal.is(parentProviding)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProviding));
            return parentProviding;
        }
        if (Normal.is(refProviding)) {
            alarm.ifPresent(it -> it.accept("Ref", refProviding));
            return refProviding;
        }
        
        // At this point, non is normal.
        
        if (Default.is(stackProviding)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProviding));
            return stackProviding;
        }
        if (Default.is(configProviding)) {
            alarm.ifPresent(it -> it.accept("Config", configProviding));
            return configProviding;
        }
        if (Default.is(parentProviding)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProviding));
            return parentProviding;
        }
        if (Default.is(refProviding)) {
            alarm.ifPresent(it -> it.accept("Ref", refProviding));
            return refProviding;
        }
        
        return null;
    }
    
    private static String callStackToString() {
        val toString = Arrays.stream(Thread.currentThread().getStackTrace()).map(Objects::toString)
                .collect(Collectors.joining("\n\t"));
        return "\t" + toString;
    }
    
    private static <T> Supplier<String> getXRayString(Ref<T> ref, Scope parentScope, Scope currentScope,
            ProvidingStackMap stacks) {
        String parentXRayString  = parentScope._changeFrom(scopeToXRay);
        String currentXRayString = currentScope._changeFrom(scopeToXRay);
        String stackXRayString   = stacks._changeFrom(stackToXRay);
        return () -> {
        	String parentXRay  = parentXRayString._changeBy(addingTabIndentation);
            String currentXRay = currentXRayString._changeBy(addingTabIndentation);
            String stackXRay   = stackXRayString._changeBy(addingTabIndentation);
            return "{" + "\n\tParent:" + parentXRay + "\n\tConfig:" + currentXRay + "\n\tStack :" + stackXRay + "\n}";
        };
    }
    
}
