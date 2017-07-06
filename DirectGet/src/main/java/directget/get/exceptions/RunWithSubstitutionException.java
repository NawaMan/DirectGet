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
package directget.get.exceptions;

/**
 * This exception wrap the exception throw while trying to run with a
 * substitution.
 * 
 * @author nawaman
 */
public class RunWithSubstitutionException extends DirectGetRuntimeException {
    
    private static final long serialVersionUID = -6016449881081091295L;
    
    /** Default */
    public RunWithSubstitutionException(Throwable cause) {
        super(cause);
    }
    
}
