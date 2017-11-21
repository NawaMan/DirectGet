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

import directget.get.Ref;

/**
 * This exception holds the exceptional result for Get.
 * 
 * @author nawaman
 */
public class GetException extends DirectGetRuntimeException {
    
    private static final long serialVersionUID = -5821727183532729001L;
    
    private final Ref<?> ref;
    
    /** Constructor */
    public GetException(Ref<?> ref, Throwable cause) {
        super(ref.toString(), cause);
        this.ref = ref;
    }
    
    /** @return the reference with the problem. */
    public Ref<?> getRef() {
        return ref;
    }
    
}
