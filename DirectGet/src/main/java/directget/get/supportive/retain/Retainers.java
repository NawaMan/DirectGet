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
package directget.get.supportive.retain;

import directget.get.Ref;
import directget.get.supportive.Provider;
import dssb.callerid.impl.CallerId;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Easy ways to create retainers.
 * 
 * @author NawaMan
 */
@UtilityClass
public class Retainers {
    
    /**
     * Returns a RefTo with retainer for the given ref.
     * 
     * @param ref 
     * @return the retainer.
     **/
    public <T> RefToWithRetainer<T> retained(Ref<T> ref) {
        if (ref instanceof RefToWithRetainer)
            return (RefToWithRetainer<T>)ref;
        
        val name          = ref.getName();
        val targetClass   = ref.getTargetClass();
        val preferability = ref.getProvider().getPreferability();
        val supplier      = ref.getProvider().getProvider().getSupplier();
        return new RefToWithRetainer<>(name, targetClass, preferability, supplier);
    }
    
    /**
     * Returns a RefTo with retainer for the given ref.
     * 
     * @param provider  the provider.
     * 
     * @return the retained provider.
     **/
    public <T> ProviderWithRetainer<T> retained(Provider<T> provider) {
        return CallerId.instance.trace(caller->{
            val ref           = provider.getRef();
            val preferability = provider.getPreferability();
            val supplier      = provider.getSupplier();
            return new ProviderWithRetainer<T>(ref, preferability, supplier);
        });
    }
    
    /**
     * Returns a RefTo with retainer for the given ref.
     * 
     * @param provider  the provider.
     * 
     * @return the provider with the singleton retainer. */
    public <T> ProviderWithRetainer<T> singleton(Provider<T> provider) {
        return CallerId.instance.trace(caller->{
            return retained(provider).globally().forAlways();
        });
    }
    
}
