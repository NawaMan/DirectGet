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
 * This exception is thrown when a there is problem getting default reference of a class.
 * 
 * @author NawaMan
 */
public class DefaultRefException extends DirectGetRuntimeException {

    private static final long serialVersionUID = -1444549293673722463L;

    /** Constructor */
    public DefaultRefException() {
        super();
    }
    
    /** Constructor */
    public DefaultRefException(String message) {
        super(message);
    }
    
    /** Constructor */
    public DefaultRefException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** Constructor */
    public DefaultRefException(Throwable cause) {
        super(cause);
    }
    
}
