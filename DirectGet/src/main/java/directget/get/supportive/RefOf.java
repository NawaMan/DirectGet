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
package directget.get.supportive;

import static directget.get.supportive.Caller.trace;

import directget.get.Preferability;
import directget.get.Ref;
import directget.get.supportive.Caller.Capture;
import lombok.val;

/**
 * This implements allow reference to a specific class. All instance of this
 * reference of the same class refer to the same thing.
 * 
 * @author NawaMan
 * @param <T>  the type of the data this ref is refering to.
 */
public final class RefOf<T> extends Ref<T> {
    
    private final Provider<T> provider;
    
    /**
     * Constructor.
     * 
     * @param targetClass
     *          the target class.
     */
    public RefOf(Class<T> targetClass) {
        super(targetClass);
        
        this.provider = trace(Capture.Continue, caller->{
            return new Provider<>(this, Preferability.Default, () -> getDefaultValue());
        });
    }
    
    /**
     * For ForClass ref to be equals, they have to point to the same target
     * class.
     **/
    @Override
    @SuppressWarnings("rawtypes")
    public final boolean equals(Object obj) {
        if (!(obj instanceof Ref)) {
            return false;
        }
        val thisTargetClass = this.getTargetClass();
        val thatTargetClass = ((Ref) obj).getTargetClass();
        return thisTargetClass == thatTargetClass;
    }
    
    @Override
    public Provider<T> getProvider() {
        return provider;
    }
    
}