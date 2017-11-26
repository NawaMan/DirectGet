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

import static java.util.Arrays.stream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import directget.get.supportive.HasProvider;
import directget.get.supportive.Provider;
import directget.get.supportive.Utilities;
import lombok.val;
import lombok.experimental.ExtensionMethod;

// TODO - Convenient method to create configuration from different sources.

/**
 * This class contains the providers for a scope.
 * 
 * @author NawaMan
 **/
@SuppressWarnings({ "rawtypes", "unchecked" }) // OK, OK. I know is really bad
                                               // to put it here but
                                               // the alternative to have it on
                                               // every method is
                                               // just as bad if not worse.
@ExtensionMethod({ Utilities.class })
public final class Configuration {
    
    private final Map<Ref, Provider> providers;
    
    /** Default constructor. */
    public Configuration() {
        this((Map<Ref, Provider>) null);
    }
    
    

    /** Constructor. */
    public Configuration(HasProvider ... hasProviders) {
        this(Arrays.asList(hasProviders).stream().map(HasProvider::getProvider));
    }
    
    /** Constructor. */
    public Configuration(Provider... providers) {
        this(Arrays.asList(providers));
    }
    
    /** Constructor. */
    public Configuration(Collection<Provider> providers) {
        this(providers.stream());
    }
    
    /** Constructor. */
    public Configuration(Stream<Provider> providers) {
        this(toMap(providers));
    }
    
    private static Map<Ref, Provider> toMap(Stream<Provider> providers) {
        val theMap = new TreeMap<Ref, Provider>();
        providers
        .filter(Objects::nonNull)
        .forEach(provider->{
            val ref = provider.getRef();
            if (theMap.containsKey(ref)) {
                val thisPreferability = theMap.get(ref).getPreferability();
                val thatPreferability = provider.getPreferability();
                if (thisPreferability.compareTo(thatPreferability) >= 0)
                    return;
                
                theMap.put(ref, provider);
            }
            
            theMap.put(ref, provider);
        });
        return theMap;
    }
    
    // TODO - Merge and cascade configurations.
    
    private Configuration(Map<Ref, Provider> providers) {
        this.providers = providers._toUnmodifiableSortedMap();
    }
    
    /** @return all the refs specified by this configuration. */
    public Stream<Ref> getRefs() {
        return providers.keySet().stream();
    }
    
    /** @return all the providers specified by this configuration. */
    public Stream<Provider> getProviders() {
        return providers.values().stream();
    }
    
    /** @return the provider for the given ref. */
    public <T> Provider<T> getProvider(Ref<T> ref) {
        val provider = providers.get(ref);
        return provider;
    }
    
    /** @return {@code} if this configuration specified the provider for the given ref. */
    public <T> boolean hasProvider(Ref<T> ref) {
        val hasProvider = providers.containsKey(ref);
        return hasProvider;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        val toString = String.format("Configuration(%s)", providers.size());
        return toString;
    }
    
    /** Return the detail string representation of this object. */
    public String toXRayString() {
        val isEmpty = providers.isEmpty();
        if (isEmpty) {
            return "{\n}";
        }
        
        String pairs = providers._toPairStrings()._toIndentLines();
        String xRay = String.format("{\n\t%s\n}", pairs);
        return xRay;
    }
    
    /**
     * Combine the configurations.
     * 
     * If there are duplicate Ref, the one in the earlier configuration get precedence.
     * 
     * @param configurations the configurations.
     * @return the configuration.
     */
    public static Configuration combineOf(Configuration ... configurations) {
        return new Configuration(stream(configurations).flatMap(Configuration::getProviders));
    }
    
}
