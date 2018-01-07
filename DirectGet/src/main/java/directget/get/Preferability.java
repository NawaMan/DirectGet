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

import static directget.get.Get.the;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import directget.get.supportive.Provider;
import directget.get.supportive.ProviderStackMap;
import directget.get.supportive.RefTo;
import dssb.utils.common.Nulls;
import lombok.val;
import lombok.experimental.ExtensionMethod;

// OK - I feel like am going to regret this but this closed design (of using Enums) makes things
//        much simpler and it might worth the trade offs.
/**
 * This enum is used to specify the preferability of a provider
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class })
public enum Preferability {
    
    /** Only use when no other is preferred. */
    Default,
    /** Whatever */
    Normal,
    /** Use me first! */
    Dictate;
    
    private static Function<Scope, String> scopeToXRay = Scope::toXRayString;
    private static Function<ProviderStackMap, String> stackToXRay = ProviderStackMap::toXRayString;
    
    private static Function<String, String> addingTabIndentation = str -> str.replaceAll("\n", "\n\t");
    
    /**
     * Check if this is the same with the preferability.
     * 
     * @param theGivenPreferability 
     * @return {@code true} if the given preferability is the same as this preferability.
     */
    public boolean is(Preferability theGivenPreferability) {
        return this == theGivenPreferability;
    }
    
    /**
     * Returns if the given provider has the same preferability.
     * 
     * @param theGivenProvider 
     * @return {@code true} if the given provider has the same preferability as this preferability.
     */
    public <T> boolean is(Provider<T> theGivenProvider) {
        if (theGivenProvider == null) {
            return false;
        }
        return is(theGivenProvider.getPreferability());
    }
    
    /** */
    public static final RefTo<DetermineProviderListener> DefaultListener = Ref.to(DetermineProviderListener.class);
    
    static final AtomicBoolean _ListenerEnabled_ = new AtomicBoolean(true);
    
    // TODO - Clean this up.
    /**  */
    @FunctionalInterface
    public static interface DetermineProviderListener {
        
        /**
         * Listen to when the provider is determined.
         * 
         * @param theRef 
         * @param from 
         * @param theResultProvider 
         * @param stackTraceSupplier 
         * @param xraySupplier
         **/
        public <T> void onDetermine(
                Ref<T> theRef, 
                String from, 
                Provider<T> theResultProvider, 
                Supplier<String> stackTraceSupplier,
                Supplier<String> xraySupplier);
        
    }
    
    // The code in the following method is heavily duplicated.
    // That is intentional, if we are wondering where a provider came from,
    // debugging these method will give you that answer very quickly.
    // Ok, I am going to regret typing this too .... but
    // This logic is not intended or supposed to be changed often.
    /**
     * Determine the provider for Get.
     * 
     * @param theRef 
     * @param parentScope 
     * @param currentScope 
     * @param stacks 
     * 
     * @return the provider.
     */
    public static <T> Provider<T> determineProvider(Ref<T> theRef, Scope parentScope, Scope currentScope,
            ProviderStackMap stacks) {
        // TODO - This code is terrible.
        Optional<BiConsumer<String, Provider<T>>> alarm = Optional
                .ofNullable(
                        (!_ListenerEnabled_.get() || (theRef == DefaultListener) || currentScope.isInitializing.get())
                        ? null
                        : the(DefaultListener))
                .map(listener -> (foundSource, foundProvider) -> {
                    listener.onDetermine(theRef, foundSource, foundProvider, Preferability::callStackToString,
                            getXRayString(theRef, parentScope, currentScope, stacks));
                });
        
        val refProvider = theRef.getProvider();
        if (Dictate.is(refProvider)) {
            alarm.ifPresent(it -> it.accept("Ref", refProvider));
            return refProvider;
        }
        
        val parentProvider = (parentScope != null) ? parentScope.getProvider(theRef) : null;
        if (Dictate.is(parentProvider)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProvider));
            return parentProvider;
        }
        
        val configProvider = currentScope.getProvider(theRef);
        if (Dictate.is(configProvider)) {
            alarm.ifPresent(it -> it.accept("Config", configProvider));
            return configProvider;
        }
        
        val stackProvider = stacks.peek(theRef);
        if (Dictate.is(stackProvider)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProvider));
            return stackProvider;
        }
        
        // At this point, non is dictate.
        
        if (Normal.is(stackProvider)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProvider));
            return stackProvider;
        }
        if (Normal.is(configProvider)) {
            alarm.ifPresent(it -> it.accept("Config", configProvider));
            return configProvider;
        }
        if (Normal.is(parentProvider)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProvider));
            return parentProvider;
        }
        if (Normal.is(refProvider)) {
            alarm.ifPresent(it -> it.accept("Ref", refProvider));
            return refProvider;
        }
        
        // At this point, non is normal.
        
        if (Default.is(stackProvider)) {
            alarm.ifPresent(it -> it.accept("Stack", stackProvider));
            return stackProvider;
        }
        if (Default.is(configProvider)) {
            alarm.ifPresent(it -> it.accept("Config", configProvider));
            return configProvider;
        }
        if (Default.is(parentProvider)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProvider));
            return parentProvider;
        }
        if (Default.is(refProvider)) {
            alarm.ifPresent(it -> it.accept("Ref", refProvider));
            return refProvider;
        }
        
        return null;
    }
    
    private static String callStackToString() {
        val toString = Arrays.stream(Thread.currentThread().getStackTrace())
                .map(Objects::toString)
                .collect(Collectors.joining("\n\t"));
        return "\t" + toString;
    }
    
    private static <T> Supplier<String> getXRayString(Ref<T> ref, Scope parentScope, Scope currentScope,
            ProviderStackMap stacks) {
        String parentXRayString  = parentScope .mapFrom(scopeToXRay);
        String currentXRayString = currentScope.mapFrom(scopeToXRay);
        String stackXRayString   = stacks      .mapFrom(stackToXRay);
        return () -> {
        	String parentXRay  = parentXRayString .mapBy(addingTabIndentation);
            String currentXRay = currentXRayString.mapBy(addingTabIndentation);
            String stackXRay   = stackXRayString  .mapBy(addingTabIndentation);
            return "{" + "\n\tParent:" + parentXRay + "\n\tConfig:" + currentXRay + "\n\tStack :" + stackXRay + "\n}";
        };
    }
    
}
