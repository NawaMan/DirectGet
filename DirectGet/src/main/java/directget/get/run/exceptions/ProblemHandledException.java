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
package directget.get.run.exceptions;

import directget.get.Get;
import directget.get.Ref;
import directget.get.supportive.RefOf;

/**
 * This throwable indicates that there was some problem but it has been handled.
 * 
 * @author NawaMan
 **/
public class ProblemHandledException extends DirectRunRuntimeException {
    
    // TODO - Current time should be put in separate place.
    /** The ref to get current time. */
    public static RefOf<Long> currentTime = Ref.of(Long.class).defaultedToBy(System::currentTimeMillis);
    
    private static final long serialVersionUID = -5350585488754817001L;
    
    private final ProblemHandler handler;
    
    private final long timeMillis;
    
    /** Constructor */
    protected ProblemHandledException(Throwable problem, ProblemHandler handler) {
        super(problem);
        this.handler = handler;
        this.timeMillis = Get.the(currentTime);
    }
    
    /** @return the problem. */
    public Throwable getProblem() {
        return getCause();
    }
    
    /** @return the handler. */
    public ProblemHandler getHandler() {
        return handler;
    }
    
    /** @return the time - from {@code System.currentTimeMillis() }*/
    public long getTime() {
        return timeMillis;
    }
    
}
