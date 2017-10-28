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
package directget.run.exceptions;

import java.util.function.Consumer;
import java.util.function.Supplier;

import directget.get.Get;
import directget.get.Ref;

/**
 * Handler of a problem.
 * 
 * @author nawaman
 */
public class ProblemHandler {
    
    /** This handler print a stacktrace. */
    public static final Ref<ProblemHandler> printStackTrace
            = Ref.of(ProblemHandler.class).by(()->new ProblemHandler(problem->problem.printStackTrace()));
    
    /** This handler ignore the problem. */
    public static final Ref<ProblemHandler> ignoreProblem
            = Ref.of(ProblemHandler.class).by(()->new ProblemHandler(problem->{}));
    
    /** Default problem handler. */
    public static final Ref<ProblemHandler> problemHandler
            = Ref.of(ProblemHandler.class).by(()->Get.a(printStackTrace));
    
    private final String name;
    
    private final Consumer<Throwable> action;
    
    
    /** Factory method to create 
     */
    public static Supplier<ProblemHandler> of(Consumer<Throwable> action) {
        return ()->new ProblemHandler(action);
    }
    
    /**
     * Construction with an action.
     * 
     * @param action the action.
     */
    public ProblemHandler(Consumer<Throwable> action) {
        this(null, action);
    }
    
    /**
     * Construction with a name and an action.
     * 
     * @param name   the name.
     * @param action the action.
     */
    public ProblemHandler(String name, Consumer<Throwable> action) {
        this.action = (action != null) ? action : problem->{};
        this.name   = (name   != null) ? name   : this.action .toString();
    }
    
    /** Handle the given problem. **/
    public final void handle(Throwable problem) throws ProblemHandledException {
        action.accept(problem);
        if(problem instanceof ProblemHandledException) {
            throw (ProblemHandledException)problem;
        }
        throw new ProblemHandledException(problem, this);
    }
    
    /** @return the name */
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "ProblemHandler(" + name + ")";
    }

}