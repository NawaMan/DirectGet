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
package direct.get.exceptions;

/**
 * This exception is thrown when there is an attempt to initialize the Application
 *   Scope after it was aready been initialized.
 * 
 * @author nawaman
 */
public class AppScopeAlreadyInitializedException extends DirectGetException {

	private static final long serialVersionUID = 5582284564207362445L;

	/**
	 * Constructor.
	 */
	public AppScopeAlreadyInitializedException() {
        super();
    }
	
}
