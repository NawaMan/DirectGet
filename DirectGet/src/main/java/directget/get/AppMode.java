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
package directget.get;

/**
 * Application mode.
 * 
 * @author NawaMan
 */
public class AppMode {
    
    /** This application is run in production. */
    public static final AppMode PROD = new AppMode("PROD");
    /** This application is run in development mode. */
    public static final AppMode DEV = new AppMode("DEV");
    /** This application is run in test mode. */
    public static final AppMode TEST = new AppMode("TEST");
    
    
    private final String name;
    
    /**
     * Constructor.
     * 
     * @param name the name of the mode.
     */
    protected AppMode(String name) {
        this.name = name;
    }
    
    /**
     * Return the name.
     * 
     * @return the name of the mode.
     **/
    public String name() {
        return this.name;
    }
    
    
    /** Check if this mode is the same with the given mode.
     * 
     * @param theGivenMode
     * @return {@code true}
     */
    public boolean is(AppMode theGivenMode) {
        try {
            return this.name().equals(theGivenMode.name());
        } catch(NullPointerException e) {
            return false;
        }
    }

    /** Check if this mode is not the same with the given mode.
     * 
     * @param theGivenMode
     * @return {@code true}
     */
    public boolean isNot(AppMode theGivenMode) {
        try {
            return !this.name().equals(theGivenMode.name());
        } catch(NullPointerException e) {
            return true;
        }
    }
    
}
