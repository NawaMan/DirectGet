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
package directget.get.supportive;

import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;

import directcommon.common.Nulls;
import directget.get.Ref;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * StackMap for Provider.
 * 
 * @author NawaMan
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtensionMethod({ Utilities.class, Nulls.class })
public class ProviderStackMap extends TreeMap<Ref, Stack<Provider>> {
    
    private static final long serialVersionUID = -8113998773064688984L;
    
    private Supplier<Stack<Provider>> ensureValue(Ref ref) {
        return (Supplier<Stack<Provider>>) () -> {
            this.put((Ref) ref, new Stack<Provider>());
            return super.get(ref);
        };
    }
    
    @Override
    public Stack<Provider> get(Object ref) {
        Stack<Provider> stack = super.get(ref).orGet(ensureValue((Ref) ref));
        return stack;
    }
    
    /**
     * Peek the top of the stack for the ref.
     * 
     * @param ref
     *          the ref.
     * @return the provider.
     */
    public <T> Provider<T> peek(Ref<T> ref) {
        if (!containsKey(ref)) {
            return null;
        }
        Stack<Provider> stack = get(ref);
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }
    
    /**
     * Returns the X-Ray status of the stack.
     * 
     * @return the X-Ray.
     */
    public String toXRayString() {
        val isEmpty = this.isEmpty();
        if (isEmpty) {
            return "{\n}";
        }
        
        String pairs = this._toPairStrings()._toIndentLines();
        String xRay = String.format("{\n\t%s\n}", pairs);
        return xRay;
    }
    
}