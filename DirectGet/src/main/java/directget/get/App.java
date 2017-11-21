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
package directget.get;

import directget.get.exceptions.AppScopeAlreadyInitializedException;
import lombok.val;

/**
 * This is the application scope.
 * 
 * IMPORTANT NOTE: This class is complicated to test. So you should have a really really good reason to change it!!!
 * 
 * @author nawaman
 */
public final class App {
    
    /** The only instance of the Application scope. */
    public static final Scope scope = new Scope();
    
    /**
     * @return the get for the current thread that is associated with this scope. NOTE: capital 'G' is intentional.
     */
    public static GetInstance Get() {
        return scope.Get();
    }
    
    /**
     * Initialize the application scope. This method can only be run once.
     **/
    public static Scope initialize(Configuration config) throws AppScopeAlreadyInitializedException {
        scope.init(config);
        return scope;
    }
    
    /**
     * Initialize the application scope if it has yet to be initialized.
     * @return {@code true} if the initialization actually happen with this call.
     */
    public static boolean initializeIfAbsent(Configuration configuration) {
        val isAbsent = scope.initIfAbsent(configuration);
        return isAbsent;
    }
    
    /** @return {@code true} if the application scope has been initialized */
    public static boolean isInitialized() {
        val isInitialized = scope.hasBeenInitialized();
        return isInitialized;
    }
    
    /** Private part */
    private App() {
        super();
    }
    
}
