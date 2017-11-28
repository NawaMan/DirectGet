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
import directget.get.supportive.RefOf;
import directget.get.supportive.Utilities;
import lombok.val;
import lombok.experimental.ExtensionMethod;

// OK - I feel like am going to regret this but this closed design (of using Enums) makes things
//        much simpler and it might worth the trade offs.
/**
 * This enum is used to specify the preferability of a provider
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Utilities.class })
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
     * @return {@code true} if the given preferability is the same as this
     *         preferability.
     */
    public boolean is(Preferability preferability) {
        return this == preferability;
    }
    
    /**
     * @return {@code true} if the given provider has the same preferability as
     *         this preferability.
     */
    public <T> boolean is(Provider<T> provider) {
        if (provider == null) {
            return false;
        }
        return is(provider.getPreferability());
    }
    
    /** */
    public static final RefOf<DetermineProviderListener> DefaultListener = Ref.ofValue(DetermineProviderListener.class, null);
    
    static final AtomicBoolean _ListenerEnabled_ = new AtomicBoolean(true);
    
    // TODO - Clean this up.
    /**  */
    @FunctionalInterface
    public static interface DetermineProviderListener {
        
        /**  */
        public <T> void onDetermine(Ref<T> ref, String from, Provider<T> result, Supplier<String> stackTraceSupplier,
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
     * @return the provider.
     */
    public static <T> Provider<T> determineProvider(Ref<T> ref, Scope parentScope, Scope currentScope,
            ProviderStackMap stacks) {
        // TODO - This code is terrible.
        Optional<BiConsumer<String, Provider<T>>> alarm = Optional
                .ofNullable(
                        (!_ListenerEnabled_.get() || (ref == DefaultListener) || currentScope.isInitializing.get())
                        ? null
                        : the(DefaultListener))
                .map(listener -> (foundSource, foundProvider) -> {
                    listener.onDetermine(ref, foundSource, foundProvider, Preferability::callStackToString,
                            getXRayString(ref, parentScope, currentScope, stacks));
                });
        
        val refProvider = ref.getProvider();
        if (Dictate.is(refProvider)) {
            alarm.ifPresent(it -> it.accept("Ref", refProvider));
            return refProvider;
        }
        
        val parentProvider = (parentScope != null) ? parentScope.getProvider(ref) : null;
        if (Dictate.is(parentProvider)) {
            alarm.ifPresent(it -> it.accept("Parent", parentProvider));
            return parentProvider;
        }
        
        val configProvider = currentScope.getProvider(ref);
        if (Dictate.is(configProvider)) {
            alarm.ifPresent(it -> it.accept("Config", configProvider));
            return configProvider;
        }
        
        val stackProvider = stacks.peek(ref);
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
        String parentXRayString  = parentScope ._changeFrom(scopeToXRay);
        String currentXRayString = currentScope._changeFrom(scopeToXRay);
        String stackXRayString   = stacks      ._changeFrom(stackToXRay);
        return () -> {
        	String parentXRay  = parentXRayString ._changeBy(addingTabIndentation);
            String currentXRay = currentXRayString._changeBy(addingTabIndentation);
            String stackXRay   = stackXRayString  ._changeBy(addingTabIndentation);
            return "{" + "\n\tParent:" + parentXRay + "\n\tConfig:" + currentXRay + "\n\tStack :" + stackXRay + "\n}";
        };
    }
    
}
