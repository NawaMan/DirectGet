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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import directget.get.exceptions.GetException;
import directget.get.exceptions.RunWithSubstitutionException;
import directget.get.supportive.Provider;
import directget.get.supportive.ProviderStackMap;
import directget.get.supportive.Utilities;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * Get is a service to allow access to other service.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Utilities.class })
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
    
    /** @return the optional value associated with the given ref. */
    public <T> Optional<T> _a(Ref<T> ref) {
        val optValue = scope.doGet(ref);
        return optValue;
    }
    
    /** @return the optional value associated with the given class. */
    public <T> Optional<T> _a(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val optValue = _a(ref);
        return optValue;
    }
    
    /** @return the value associated with the given ref. */
    public <T> T a(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val value = a(ref);
        return value;
    }
    
    /** @return the value associated with the given class. */
    public <T> T a(Ref<T> ref) {
        val optValue = _a(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * @return the value associated with the given class or return the elseValue
     *         if no value associated with the class.
     */
    public <T> T a(Class<T> clzz, T elseValue) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseValue);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the elseValue
     *         if no value associated with the ref.
     */
    public <T> T a(Ref<T> ref, T elseValue) {
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
    public <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseSupplier);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the from
     *         elseSupplier if no value associated with the ref.
     */
    public <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
        val optValue = _a(ref);
        val value = optValue.orElseGet(elseSupplier);
        return value;
    }

    /** @return the optional value associated with the given ref. */
    public <T> Optional<T> _the(Ref<T> ref) {
        val optValue = scope.doGet(ref);
        return optValue;
    }
    
    /** @return the optional value associated with the given class. */
    public <T> Optional<T> _the(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val optValue = _the(ref);
        return optValue;
    }
    
    /** @return the value associated with the given ref. */
    public <T> T the(Class<T> clzz) {
        val ref = Ref.forClass(clzz);
        val value = a(ref);
        return value;
    }
    
    /** @return the value associated with the given class. */
    public <T> T the(Ref<T> ref) {
        val optValue = _a(ref);
        val value = optValue.orElse(null);
        return value;
    }
    
    /**
     * @return the value associated with the given class or return the elseValue
     *         if no value associated with the class.
     */
    public <T> T the(Class<T> clzz, T elseValue) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseValue);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the elseValue
     *         if no value associated with the ref.
     */
    public <T> T the(Ref<T> ref, T elseValue) {
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
    public <T> T the(Class<T> clzz, Supplier<T> elseSupplier) {
        val ref = Ref.forClass(clzz);
        val value = a(ref, elseSupplier);
        return value;
    }
    
    /**
     * @return the value associated with the given ref or return the from
     *         elseSupplier if no value associated with the ref.
     */
    public <T> T the(Ref<T> ref, Supplier<T> elseSupplier) {
        val optValue = _a(ref);
        val value = optValue.orElseGet(elseSupplier);
        return value;
    }
    
    /**
     * Substitute the given providers and run the runnable.
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
     * Run the given runnable asynchronously and inherits the providers of
     * those given refs.
     **/
    public void runAsync(
            @SuppressWarnings("rawtypes") List<Ref> refsToInherit,
            Runnable runnable) {
        runAsync(refsToInherit::contains, runnable);
    }
    
    /**
     * Run the given runnable asynchronously and inherits the substitution
     * from the current Get (all Ref that pass the predicate test).
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
                = refsToInherit._toNullable()
                    .filter(notInteritNone)
                    .map(filterRefs)
                    .orElse(emptyProviderList.get());
            return (List<Provider>) list;
        } finally {
            Preferability._ListenerEnabled_.set(true);
        }
    }
    
    /** Return the detail string representation of this object. */
    public final String toXRayString() {
        String toString = "Get(" + scope + ")";
        return toString;
    }
    
}