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

import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;

import lombok.val;
import lombok.experimental.ExtensionMethod;

@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtensionMethod({ Extensions.class })
class ProvidingStackMap extends TreeMap<Ref, Stack<Providing>> {
    
    private static final long serialVersionUID = -8113998773064688984L;
    
    private Supplier<Stack<Providing>> ensureValue(Ref ref) {
        return (Supplier<Stack<Providing>>) () -> {
            this.put((Ref) ref, new Stack<Providing>());
            return super.get(ref);
        };
    }
    
    @Override
    public Stack<Providing> get(Object ref) {
        val stack = super.get(ref)._or(ensureValue((Ref) ref));
        return stack;
    }
    
    public <T> Providing<T> peek(Ref<T> ref) {
        if (!containsKey(ref)) {
            return null;
        }
        Stack<Providing> stack = get(ref);
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }
    
    public String toXRayString() {
        val isEmpty = this.isEmpty();
        if (isEmpty) {
            return "{\n}";
        }
        
        val pairs = this._toPairStrings()._toIndentLines();
        val xRay = String.format("{\n\t%s\n}", pairs);
        return xRay;
    }
    
}