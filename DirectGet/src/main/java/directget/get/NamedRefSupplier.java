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
package directget.get;

import directget.run.Named.Supplier;


/**
 * Supplier for a value of a ref.
 * 
 * @author nawaman
 **/
public class NamedRefSupplier<T> extends Supplier<T> {
    
    /** The name of the supplier. */
    public static final String NAME = "FromRef";
    /** The template for the name. */
    public static final String NAME_TEMPLATE = NAME + "(%s)";
    
    /** Constructor */
    public NamedRefSupplier(Ref<T> ref) {
        super(String.format(NAME_TEMPLATE, ref.toString()), () -> Get.a(ref));
    }
    
}