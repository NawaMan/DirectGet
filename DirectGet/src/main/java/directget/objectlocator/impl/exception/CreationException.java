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
package directget.objectlocator.impl.exception;

import directget.objectlocator.api.LocateObjectException;

/**
 * This exception is thrown when creating an object fail.
 * 
 * @author NawaMan
 */
public class CreationException extends LocateObjectException {
    
    private static final long serialVersionUID = 5414890542605369904L;
    
    /**
     * Constructor 
     * 
     * @param clazz  the class that this fail creation is attempted too.
     **/
    public CreationException(Class<?> clazz) {
        this(clazz, null);
    }
    
    /**
     * Constructor 
     * 
     * @param clazz 
     * @param cause
     **/
    public CreationException(Class<?> clazz, Throwable cause) {
        super(clazz, cause);
    }
    
}
