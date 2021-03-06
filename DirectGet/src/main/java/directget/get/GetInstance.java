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

import static directget.get.Get.DefaultExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import dssb.utils.common.Nulls;
import directget.get.exceptions.RunWithSubstitutionException;
import directget.get.supportive.Provider;
import directget.get.supportive.ProviderStackMap;
import directget.get.utilities;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * Get is a service to allow access to other service.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ utilities.class, Nulls.class })
public final class GetInstance {
    
    private final Scope scope;
    
    private final ProviderStackMap providerStacks = new ProviderStackMap();
    
    
    GetInstance(Scope scope) {
        this.scope = scope;
    }
    
    /** @return the scope this Get is in. */
    public Scope getScope() {
        return this.scope;
    }
    
    @SuppressWarnings("rawtypes")
    Stream<Ref> getStackRefs() {
        return providerStacks.keySet().stream();
    }
    
    <T> Provider<T> getProvider(Ref<T> ref) {
        if (ref == null) {
            return null;
        }
        
        val provider = Preferability.determineProvider(ref, scope.getParentScope(), scope, providerStacks);
        return provider;
    }

    //-- the --
    
    /**
     * The value associated with theGivenRef.
     * 
     * @param theGivenRef 
     * @return the value associated with theGivenRef.
     **/
    public <T> T the(Ref<T> theGivenRef) {
        val optValue = scope.doGetThe(theGivenRef);
        val value    = optValue.orElse(null);
        return value;
    }
    
    /**
     * Return the value associated with the given targetClass.
     * 
     * @param targetClass 
     * @return the value associated with the given targetClass.
     **/
    public <T> T the(Class<T> targetClass) {
        val ref      = Ref.defaultOf(targetClass);
        val optValue = scope.doGetThe(ref);
        val value    = optValue.orElse(null);
        return value;
    }
    
    //-- Substitute --
    
    /**
     * Substitute the given providers and run the runnable.
     * 
     * @param providers the provider to be substitute.
     * @param runnable  the runnable body.
     */
    @SuppressWarnings({ "rawtypes" })
    public void substitute(Stream<Provider> providers, Runnable runnable) {
        val problemHolder = new AtomicReference<RuntimeException>(null);
        try {
            substitute(providers, runnable._toSupplier(problemHolder));
        } catch (Throwable t) {
            throw new RunWithSubstitutionException(t);
        }
        
        val theProblem = problemHolder.get();
        if (theProblem != null) {
            throw theProblem;
        }
    }
    
    /**
     * Substitute the given providers and run the action.
     * 
     * @param providers the provider to be substitute.
     * @param supplier  the supplier body.
     * @return the result of the computation.
     */
    @SuppressWarnings("rawtypes")
    synchronized public <V> V substitute(Stream<Provider> providers, Supplier<V> supplier) {
        List<Ref> substitutedRefs = null;
        try {
            substitutedRefs = substituteProviders(providers, substitutedRefs);
            val result = supplier.get();
            return result;
        } finally {
            resetSubstitution(substitutedRefs);
        }
    }

    @SuppressWarnings("rawtypes")
    private List<Ref> substituteProviders(Stream<Provider> providers, List<Ref> addedRefs) {
        Iterable<Provider> iterable = () -> providers.iterator();
        for (Provider provider : iterable) {
            if (provider == null) {
                continue;
            }
            
            val ref = provider.getRef();
            val stack = providerStacks.get(ref);
            stack.push(provider);
            if (addedRefs == null) {
                addedRefs = new ArrayList<>();
            }
            addedRefs.add(ref);
        }
        return addedRefs;
    }
    
    @SuppressWarnings("rawtypes")
    private void resetSubstitution(List<Ref> addedRefs) {
        if (addedRefs != null) {
            addedRefs.forEach(ref -> {
                val stack = providerStacks.get(ref);
                stack.pop();
            });
        }
    }
    
    /**
     * Run the given runnable asynchronously and inherits the providers of those given refs.
     * 
     * @param refsToInherit the list of Ref to inherit.
     * @param runnable      the runnable body.
     **/
    public void runAsync(@SuppressWarnings("rawtypes") List<Ref> refsToInherit, Runnable runnable) {
        runAsync(refsToInherit::contains, runnable);
    }
    
    /**
     * Run the given runnable asynchronously and inherits the substitution
     * from the current Get (all Ref that pass the predicate test).
     * 
     * @param refsToInherit  the list of Ref to inherit.
     * @param runnable       the runnable body.
     **/
    @SuppressWarnings("rawtypes")
    public void runAsync(Predicate<Ref> refsToInherit, Runnable runnable) {
        val newGet = new GetInstance(scope);
        val providers = prepareProviders(refsToInherit);
        
        val newExecutor = the(DefaultExecutor);
        newExecutor.execute(() -> {
            scope.threadGet.set(newGet);
            val providersList = providers;
            newGet.substitute(providersList.stream(), runnable);
        });
    }
    
    @SuppressWarnings("rawtypes")
    private static final Predicate<Predicate<Ref>> notInteritNone = test -> test != Get.INHERIT_NONE;
    
    @SuppressWarnings("rawtypes")
    private static final Supplier<List<Provider>> emptyProviderList = ()->new ArrayList<Provider>();
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List<Provider> prepareProviders(Predicate<Ref> refsToInherit) {
        Preferability._ListenerEnabled_.set(false);
        try {
            Function<Predicate<Ref>, List> filterRefs = test -> {
                return (List) getStackRefs()
                        .filter(test)
                        .map(this::getProvider)
                        ._toList();
            };
            List<Provider> list
                = refsToInherit.whenNotNull()
                    .filter(notInteritNone)
                    .map(filterRefs)
                    .orElse(emptyProviderList.get());
            return (List<Provider>) list;
        } finally {
            Preferability._ListenerEnabled_.set(true);
        }
    }
    
    /**
     * Return the detail string representation of this object.
     * 
     * @return the detail string representation of this object
     **/
    public final String toXRayString() {
        String toString = "Get(" + scope + ")";
        return toString;
    }
    
}