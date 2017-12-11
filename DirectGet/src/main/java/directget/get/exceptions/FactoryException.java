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
package directget.get.exceptions;

/**
 * This exception is thrown when a factory is unable to make something.
 * 
 * @author NawaMan
 */
public class FactoryException extends DirectGetRuntimeException {

    private static final long serialVersionUID = -1444549293673722463L;

    /** Constructor */
    public FactoryException() {
        super();
    }
    
    /**
     * Constructor 
     * 
     * @param message
     **/
    public FactoryException(String message) {
        super(message);
    }
    
    /**
     * Constructor 
     * 
     * @param message 
     * @param cause
     **/
    public FactoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor 
     * 
     * @param cause
     **/
    public FactoryException(Throwable cause) {
        super(cause);
    }
    
}
