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

/**
 * Base implementation of references.
 * 
 * @author nawaman
 **/
public abstract class AbstractRef<T> implements Ref<T> {
    
    private final Class<T> targetClass;
    
    private final String targetClassName;
    
    AbstractRef(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.targetClassName = this.targetClass.getCanonicalName();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getName() {
        return this.targetClassName;
    }
    
    /** {@inheritDoc} */
    @Override
    public final Class<T> getTargetClass() {
        return this.targetClass;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Ref<" + this.targetClassName + ">";
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return this.targetClass.hashCode();
    }
    
}