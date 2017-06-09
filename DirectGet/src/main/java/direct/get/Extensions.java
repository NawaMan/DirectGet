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
package direct.get;

import java.util.function.Function;
import java.util.function.Supplier;

class Extensions {
	
	public static <T> T orElse(T obj, T elseValue) {
		return (obj == null) ? elseValue : obj;
	}
	
	public static <T> T orElse(T obj, Supplier<T> elseSupplier) {
		return (obj == null) ? elseSupplier.get() : obj;
	}
	
	public static <F, T> T mapFrom(F obj, Function<F, T> mapper) {
		return (obj == null) ? null : mapper.apply(obj);
	} 
	
	public static <F, T> T mapBy(F obj, Function<F, T> mapper) {
		return (obj == null) ? null : mapper.apply(obj);
	} 
	
}
