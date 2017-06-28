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
package direct.get;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This class contains the providings for a scope.
 * 
 * @author nawaman
 **/
@SuppressWarnings({ "rawtypes", "unchecked" }) // OK, OK. I know is really bad
                                               // to put it here but
                                               // the alternative to have it on
                                               // every method is
                                               // just as bad if not worse.
@ExtensionMethod({ Extensions.class })
public final class Configuration {
    
    private static final Function<Map, Map> newTreeMap = (Function<Map, Map>) TreeMap::new;
    
    private final Map<Ref, Providing> providings;
    
    /** Default constructor. */
    public Configuration() {
        this((Map<Ref, Providing>) null);
    }
    
    /** Constructor. */
    public Configuration(Providing... providings) {
        this(Arrays.asList(providings));
    }
    
    /** Constructor. */
    public Configuration(Collection<Providing> providings) {
        this(providings.stream());
    }
    
    /** Constructor. */
    public Configuration(Stream<Providing> providings) {
        this(providings.filter(Objects::nonNull).collect(toMap(Providing::getRef, p -> p)));
    }
    
    private Configuration(Map<Ref, Providing> providings) {
        val newProvidingMap = providings._changeBy(newTreeMap)._or(emptyMap());
        this.providings = unmodifiableMap(newProvidingMap);
    }
    
    /** @return all the refs specified by this configuration. */
    public Stream<Ref> getRefs() {
        return providings.keySet().stream();
    }
    
    /** @return all the providings specified by this configuration. */
    public Stream<Providing> getProvidings() {
        return providings.values().stream();
    }
    
    /** @return the providing for the given ref. */
    public <T> Providing<T> getProviding(Ref<T> ref) {
        val providing = providings.get(ref);
        return providing;
    }
    
    /** @return {@code} if this configuration specified the providing for the given ref. */
    public <T> boolean hasProviding(Ref<T> ref) {
        val hasProviding = providings.containsKey(ref);
        return hasProviding;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        val toString = String.format("Configuration(%s)", providings.size());
        return toString;
    }
    
    /** Return the detail string representation of this object. */
    public String toXRayString() {
        val isEmpty = providings.isEmpty();
        if (isEmpty) {
            return "{\n}";
        }
        
        val pairs = providings._toPairStrings()._toIndentLines();
        val xRay = String.format("{\n\t%s\n}", pairs);
        return xRay;
    }
    
}
