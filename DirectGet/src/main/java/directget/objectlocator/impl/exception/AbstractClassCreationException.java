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

/**
 * This exception is thrown when there is an to create an instance of an abstract class .
 * 
 * @author NawaMan
 */
public class AbstractClassCreationException extends ObjectCreationException {

    private static final long serialVersionUID = 5751024581572983499L;

    /**
     * Constructor 
     * 
     * @param clazz  the class that this fail creation is attempted too.
     **/
    public AbstractClassCreationException(Class<?> clazz) {
        super(clazz);
    }
    
}
