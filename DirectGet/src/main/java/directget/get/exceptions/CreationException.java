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
 * This exception is thrown when creating an object fail.
 * 
 * @author NawaMan
 */
public class CreationException extends DirectGetRuntimeException {
    
    private static final long serialVersionUID = 5414890542605369904L;

    private final Class<?> clazz;
    
    /** Constructor */
    public CreationException(Class<?> clazz, Throwable cause) {
        super(clazz.getCanonicalName(), cause);
        this.clazz = clazz;
    }
    
    /** @return the target class with the problem. */
    public Class<?> getTargetClass() {
        return clazz;
    }
}
