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

package directget.get.supportive;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import directget.get.Get;
import directget.get.InjectedConstructor;
import directget.get.Ref;
import directget.get.exceptions.GetException;
import directget.get.run.Failable.Supplier;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * Factory for a ref.
 * 
 * @param <T> the data type of the ref.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Utilities.class })
public class RefFactory {
    
    @SuppressWarnings("rawtypes")
    private Map<Class, Supplier> suppliers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("rawtypes")
    private Class injectClass = null;
    
    /**
     * Constructor.
     */
    public RefFactory() {
        injectClass = findInjectClass();
    }
    
    /**
     * Find the {@code java.inject.Inject} class by name.
     * @return the class if found or {@code null} if not.
     */
    public static Class<?> findInjectClass() {
        try { 
            return Class.forName("javax.inject.Inject");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Create the value for the ref.
     * 
     * @param theRef
     * @return the created value.
     * @throws GetException
     */
    @SuppressWarnings("rawtypes")
    public <T> T make(Ref<T> theRef) throws GetException {
        try {
            val clzz = theRef.getTargetClass();
            Supplier supplier = suppliers.get(clzz);
            if (supplier != null) {
                val instance = supplier.get();
                return clzz.cast(instance);
            }

            val constructor = findConstructor(clzz);
            supplier = ()->{
                val params   = getParameters(constructor);
                val instance = constructor.newInstance(params);
                return clzz.cast(instance);
            };
            
            suppliers.put(clzz, supplier);
            val instance = supplier.get();
            return clzz.cast(instance);
        } catch (InstantiationException
               | IllegalAccessException
               | NoSuchMethodException
               | SecurityException
               | IllegalArgumentException
               | InvocationTargetException e) {
            throw new GetException(theRef, e);
        } catch (Throwable e) {
            throw new GetException(theRef, e);
        }
    }

    @SuppressWarnings("rawtypes")
    private Object[] getParameters(Constructor constructor) {
        val params = new Object[constructor.getParameterCount()];
        val paramsArray = constructor.getParameters();
        for (int i = 0; i < paramsArray.length; i++) {
            val param = paramsArray[i];
            val paramRef = Ref.forClass(param.getType());
            val paramValue = Get.the(paramRef);
            params[i] = paramValue;
        }
        return params;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> Constructor findConstructor(final java.lang.Class<T> clzz) throws NoSuchMethodException {
        Constructor foundConstructor = null;
        for(Constructor c : clzz.getConstructors()) {
            if (c.getAnnotation(InjectedConstructor.class) != null) {
                foundConstructor = c;
                break;
            }
            if (injectClass.whenNotNull().map(c::getAnnotation).isPresent()) {
                foundConstructor = c;
                break;
            }
        }
        if (foundConstructor == null) {
            if (clzz.getConstructors().length == 1)
                 foundConstructor = clzz.getConstructors()[0];
            else foundConstructor = clzz.getConstructor();
        }
        
        foundConstructor.setAccessible(true);
        return foundConstructor;
    }
    
}