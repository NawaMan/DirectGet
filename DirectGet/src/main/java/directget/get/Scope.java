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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import directget.get.supportive.Provider;
import dssb.callerid.impl.CallerId;
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
    
    final ThreadLocal<GetInstance> threadGet;
    
    private volatile Configuration config;
    
    private volatile List<StackTraceElement> stackTraceAtCreation = null;
    
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
    
    // -- For testing only --

    /** Reset application scope configuration. */
    void reset() {
        if (parentScope == null) {
            if (!CallerId.instance.isLocalCall())
                return;
            
            config = DEFAULT_CONFIG;
            stackTraceAtCreation = null;
            ProposedConfiguration.instance.reset();
        }
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
        boolean isInitializedHere = false;
        if (config == DEFAULT_CONFIG) {
            synchronized (lock) {
                val proposedConfiguration = ProposedConfiguration.instance;
                try {
                    if (config == DEFAULT_CONFIG) {
                        isInitializing.set(true);
                        try {
                            config = forceDictate((newConfig != null) ? newConfig : proposedConfiguration.getConfiguration());
                            stackTraceAtCreation = unmodifiableList(asList(new Throwable().getStackTrace()));
                            isInitializedHere = true;
                        } finally {
                            isInitializing.set(false);
                        }
                    }
                } finally {
                    proposedConfiguration.onInitialized();
                }
            }
        }
        return isInitializedHere;
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
    
    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return name + "(" + config + ")";
    }
    
    /**
     * Return the detail string representation of this object.
     * 
     * @return the XRay string.
     **/
    public final String toXRayString() {
        return name + "(" + config.toXRayString() + ")";
    }
    
    /**
     * Create and return a new sub scope with the given configuration. 
     * 
     * @param configuration  the configuration.
     * @return the new sub scope.
     **/
    public Scope newSubScope(Configuration configuration) {
        val subScope = new Scope(null, this, configuration);
        return subScope;
    }
    
    /**
     * Create and return a new sub scope with the given name and configuration.
     * 
     * @param name
     * @param configuration
     * @return the new sub scope.
     */
    public Scope newSubScope(String name, Configuration configuration) {
        val subScope = new Scope(name, this, configuration);
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
