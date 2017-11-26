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

import java.lang.reflect.InvocationTargetException;

import directget.get.Ref;
import directget.get.exceptions.GetException;
import lombok.val;

/**
 * Factory for a ref.
 * 
 * @param <T> the data type of the ref.
 * 
 * @author NawaMan
 */
public class RefFactory {
    
    /**
     * Create the value for the ref.
     * 
     * @param theRef
     * @return the created value.
     * @throws GetException
     */
    public <T> T make(Ref<T> theRef) throws GetException {
        try {
            // TODO cache this and make it support @Inject
            val clzz      = theRef.getTargetClass();
            val constuctor = clzz.getConstructor();
            constuctor.setAccessible(true);
            val instance = constuctor.newInstance();
            return instance;
        } catch (InstantiationException
               | IllegalAccessException
               | NoSuchMethodException
               | SecurityException
               | IllegalArgumentException
               | InvocationTargetException e) {
            throw new GetException(theRef, e);
        }
    }
    
}