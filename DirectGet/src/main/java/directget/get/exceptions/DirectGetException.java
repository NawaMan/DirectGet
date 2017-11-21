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
 * General checked exception for DirectGet.
 * 
 * @author nawaman
 */
public abstract class DirectGetException extends Exception {
    
    private static final long serialVersionUID = -6611252364944586803L;
    
    /** Constructor */
    protected DirectGetException() {
        super();
    }
    
    /** Constructor */
    protected DirectGetException(String message) {
        super(message);
    }
    
    /** Constructor */
    protected DirectGetException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** Constructor */
    protected DirectGetException(Throwable cause) {
        super(cause);
    }
    
}
