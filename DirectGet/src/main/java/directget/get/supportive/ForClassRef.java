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

import directget.get.Preferability;
import directget.get.Providing;
import directget.get.Ref;
import lombok.val;

/**
 * This implements allow reference to a specific class. All instance of this
 * reference for the same class refer to the same thing.
 * 
 * @author NawaMan
 */
public final class ForClassRef<T> extends AbstractRef<T> implements Ref<T> {
    
    private final Providing<T> providing;
    
    /**
     * Constructor.
     * 
     * @param targetClass
     *          the target class.
     */
    public ForClassRef(Class<T> targetClass) {
        super(targetClass);
        this.providing = new Providing<>(this, Preferability.Default, () -> get());
    }
    
    /**
     * For ForClass ref to be equals, they have to point to the same target
     * class.
     **/
    @Override
    @SuppressWarnings("rawtypes")
    public final boolean equals(Object obj) {
        if (!(obj instanceof AbstractRef)) {
            return false;
        }
        val thisTargetClass = this.getTargetClass();
        val thatTargetClass = ((AbstractRef) obj).getTargetClass();
        return thisTargetClass == thatTargetClass;
    }
    
    @Override
    public Providing<T> getProviding() {
        return providing;
    }
    
}