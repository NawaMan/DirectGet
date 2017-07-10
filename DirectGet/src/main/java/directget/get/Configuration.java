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
import java.util.Collection;
import java.util.Map;
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
@ExtensionMethod({ utils.class })
public final class Configuration {
    
    private static final Function<Providing, Ref> byProvidingRef = Providing::getRef;
    
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
        this(providings._toNonNullMap(byProvidingRef));
    }
    
    private Configuration(Map<Ref, Providing> providings) {
        this.providings = providings._toUnmodifiableSortedMap();
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
        
        String pairs = providings._toPairStrings()._toIndentLines();
        String xRay = String.format("{\n\t%s\n}", pairs);
        return xRay;
    }
    
}
