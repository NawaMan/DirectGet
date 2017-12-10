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
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import directget.get.exceptions.DefaultRefException;
import directget.get.supportive.RefTo;
import lombok.val;

/**
 * This annotation is used to mark a static field of Ref of the same class,
 *   so that its value is used as a default ref when Get.the(class).
 * 
 * @author NawaMan
 */
@Target(value=ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultRef {

    
    
    //== Utility class ==
    
    /**
     * Utility class.
     **/
    public static class Util {

        private static final String refToClassName = RefTo.class.getCanonicalName();
        
        @SuppressWarnings({ "rawtypes" })
        private static final ConcurrentHashMap<Class, Optional<RefTo>> defeaultRefs = new ConcurrentHashMap<>();

        /**
         * Returns the default reference of the given class.
         * 
         * @param targetClass the target class.
         * @return the default reference or {@code null} if non exist.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static <T> RefTo<T> defaultOf(Class<T> targetClass) {
            Optional<RefTo> refOpt = defeaultRefs.get(targetClass);
            if (refOpt == null) {
                for (val field : targetClass.getDeclaredFields()) {
                    if (!Modifier.isFinal(field.getModifiers()))
                        continue;
                    if (!Modifier.isPublic(field.getModifiers()))
                        continue;
                    if (!Modifier.isStatic(field.getModifiers()))
                        continue;
                    if (!Ref.class.isAssignableFrom(field.getType()))
                        continue;
                    val targetClassName  = targetClass.getName();
                    val expectedTypeName = refToClassName + "<" + targetClassName + ">";
                    val actualTypeName   = field.getGenericType().getTypeName();
                    if(!actualTypeName.equals(expectedTypeName))
                        continue;
                    
                    try {
                        refOpt = Optional.of((RefTo<T>)field.get(targetClass));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new DefaultRefException(e);
                    }
                }
                refOpt = (refOpt != null) ? refOpt :Optional.empty();
                defeaultRefs.put(targetClass, refOpt);
            }
            return refOpt.orElse(null);
        }
    }
    
}

