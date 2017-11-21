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
package directget.get.run.session;

import static directget.get.run.session.utils._or;
import static directget.get.run.session.utils._toUnmodifiableNonNullList;

import java.util.List;
import java.util.function.Function;

import directget.get.run.Failable;

/**
 * The contains the wrappers so that we can run something within them.
 * 
 * @author NawaMan
 **/
public class WrapperContext {
    
    @SuppressWarnings("rawtypes")
    final Function<Failable.Runnable, Runnable> failHandler;
    final List<Function<Runnable, Runnable>> wrappers;
    
    @SuppressWarnings("rawtypes") 
    WrapperContext(Function<Failable.Runnable, Runnable> failHandler, List<Function<Runnable, Runnable>> functions) {
        this.failHandler = _or(failHandler, ()->runnable->runnable.gracefully());
        this.wrappers    = _toUnmodifiableNonNullList(functions);
    }
    
}