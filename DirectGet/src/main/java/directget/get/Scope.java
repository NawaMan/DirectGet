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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import directget.get.supportive.Provider;
import lombok.val;

/***
 * Scope holds a configuration which specify providers.
 * 
 * @author NawaMan
 */
public class Scope {
    
    private static final String APP_SCOPE_NAME = "AppScope";
    
    private static final Configuration DEFAULT_CONFIG = new Configuration();
    
    private static final Object lock = new Object();
    
    final AtomicBoolean isInitializing = new AtomicBoolean(false);
    
    /**
     * The name of the scope.
     * 
     * This value is for the benefit of human who look at it. There is no use in
     * the program in anyway (except debugging/logging/auditing purposes).
     **/
    private final String name;
    
    private final Scope parentScope;
    
    private volatile Configuration config;
    
    final ThreadLocal<GetInstance> threadGet;
    
    private volatile List<StackTraceElement> stackTraceAtCreation;
    
    // For AppScope only.
    Scope() {
        this.name = APP_SCOPE_NAME;
        this.parentScope = null;
        this.config = DEFAULT_CONFIG;
        this.threadGet = ThreadLocal.withInitial(() -> new GetInstance(this));
    }
    
    // For other scope.
    Scope(String name, Scope parentScope, Configuration config) {
        this.name = Optional.ofNullable(name).orElse("Scope:" + this.getClass().getName());
        this.parentScope = parentScope;
        this.config = Optional.ofNullable(config).orElseGet(Configuration::new);
        this.threadGet = ThreadLocal.withInitial(() -> new GetInstance(this));
    }
    
    // -- For AppScope only ---------------------------------------------------
    void init(Configuration newConfig) throws AppScopeAlreadyInitializedException {
        if (config == DEFAULT_CONFIG) {
            initIfAbsent(newConfig);
            return;
        }
        throw new AppScopeAlreadyInitializedException();
    }
    
    void ensureInitialized() {
        initIfAbsent(null);
    }
    
    boolean initIfAbsent(Configuration newConfig) {
        if (config == DEFAULT_CONFIG) {
            synchronized (lock) {
                if (config == DEFAULT_CONFIG) {
                    isInitializing.set(true);
                    try {
                        config = forceDictate((newConfig == null) ? new Configuration() : newConfig);
                        stackTraceAtCreation = Collections.unmodifiableList(Arrays.asList(new Throwable().getStackTrace()));
                        return true;
                    } finally {
                        isInitializing.set(false);
                    }
                }
            }
        }
        return false;
    }
    
    boolean isInitialized() {
        return config != DEFAULT_CONFIG;
    }
    
    /** @return the stacktrace when this scope is initialized. */
    public final Stream<StackTraceElement> getInitialzedStackTrace() {
        ensureInitialized();
        return stackTraceAtCreation.stream();
    }
    
    // -- For both types of Scope ------------------------------------------
    
    /** @return the name of the scope. */
    public String getName() {
        return name;
    }
    
    /** @return the name of the scope. */
    public Scope getParentScope() {
        return this.parentScope;
    }
    
    protected final Configuration getConfiguration() {
        ensureInitialized();
        return config;
    }
    
    protected final <T> Provider<T> getProvider(Ref<T> ref) {
        if (ref == null) {
            return null;
        }
        
        return config.getProvider(ref);
    }
    
    /**
     * @return the get for the current thread that is associated with this
     *         scope. NOTE: capital 'G' is intentional.
     */
    public GetInstance get() {
        return threadGet.get();
    }
    
    <T> Optional<T> doGetThe(Ref<T> ref) {
        initIfAbsent(null);
        
        val currentGet = this.get();
        val provider = currentGet.getProvider(ref);
        if (provider != null) {
            return Optional.ofNullable(provider.get());
        }
        
        return Optional.empty();
    }
    
    <T> Optional<T> doGetA(Ref<T> ref) {
        val optValue = doGetThe(ref);
        if (optValue.isPresent())
             return optValue;
        else return ref._get();
    }
    
    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return name + "(" + config + ")";
    }
    
    /** Return the detail string representation of this object. */
    public final String toXRayString() {
        return name + "(" + config.toXRayString() + ")";
    }
    
    /** Create and return a new sub scope with the given configuration. */
    public Scope newSubScope(Configuration config) {
        val subScope = new Scope(null, this, config);
        return subScope;
    }
    
    /**
     * Create and return a new sub scope with the given name and configuration.
     */
    public Scope newSubScope(String name, Configuration config) {
        val subScope = new Scope(name, this, config);
        return subScope;
    }

    @SuppressWarnings("rawtypes")
    private static Configuration forceDictate(final Configuration config) {
        List<Provider> providers = new ArrayList<>();
        App.PROTECTED_REFS.forEach(ref->{
            createDictates(config, ref, providers);
        });
        return Configuration.combineOf(config, new Configuration(providers));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void createDictates(Configuration config, Ref ref, List<Provider> providers) {
        if (config.getProvider(ref) == null) {
            providers.add(ref.butDictate());
        } else {
            val provider = config.getProvider(ref);
            if (!provider.getPreferability().is(Preferability.Dictate))
                providers.add(provider.butDictate());
        }
    }
    
}
