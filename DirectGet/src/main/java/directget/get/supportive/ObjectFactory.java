//========================================================================
//Copyright (c) 2017 Nawapunth Manusitthipol.
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//
//  The Eclipse Public License is available at
//  http://www.eclipse.org/legal/epl-v10.html
//
//  The Apache License v2.0 is available at
//  http://www.opensource.org/licenses/apache2.0.php
//
//You may elect to redistribute this code under either of these licenses.
//========================================================================

package directget.get.supportive;

import directget.get.Ref;
import directget.get.exceptions.CreationException;
import directget.get.exceptions.GetException;
import lombok.val;

/**
 * Factory for a ref. It create an instance of a Ref.
 * 
 * @author NawaMan
 */
public interface ObjectFactory {
    
    /** A usable object factory that use Object creator to create an object. */
    public static final ObjectFactory instance = new ObjectFactory() {};
    
    
    
    /**
     * Create the value for the ref.
     * 
     * @param theRef
     * @return the created value.
     * @throws GetException
     */
    public default <T> T make(Ref<T> theRef) throws GetException {
        val clzz = theRef.getTargetClass();
        try {
            return ObjectCreator.createNew(clzz);
        } catch (CreationException cause) {
            throw new GetException(theRef, cause);
        }
    }
    
}