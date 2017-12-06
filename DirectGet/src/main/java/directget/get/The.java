//========================================================================
//Copyright (c) 2017 Nawapunth Manusitthipol.
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//
//  The Eclipse Public License is available at
//  http://www.eclipse.org/legal/epl-v10.html
//
//  The Apache License v2.0 is available at
//  http://www.opensource.org/licenses/apache2.0.php
//
//You may elect to redistribute this code under either of these licenses.
//========================================================================
package directget.get;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a parameter of an injected constructor
 *   so that the default ref of that class is used to get the value.
 * 
 * @author NawaMan
 */
@Target(value=ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface The {
    
}
