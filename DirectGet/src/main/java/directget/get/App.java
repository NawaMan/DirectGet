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

import static directget.get.Get.the;
import static directget.get.supportive.Caller.trace;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.List;

import dssb.utils.common.Nulls;
import directget.get.ProposedConfiguration.ProposedConfigurationWithLastProvider;
import directget.get.exceptions.AppScopeAlreadyInitializedException;
import directget.get.supportive.Caller.Capture;
import directget.get.supportive.Provider;
import directget.get.supportive.RefTo;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This is the application scope.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class })
public final class App {
    
    /** Application mode - Default to TEST its the only one without main. */
    public static final RefTo<AppMode> mode = Ref.toValue(AppMode.TEST);
    

    /** List of Refs that will be protected (force 'Dictate' at initialize time) */
    public static final List<Ref<?>> PROTECTED_REFS = unmodifiableList(asList(
            Ref.objectFactory,
            App.mode
    ));
    
    /** The only instance of the Application scope. */
    public static final Scope scope = new Scope();
    
    /**
     * @return the get for the current thread that is associated with this scope. NOTE: capital 'G' is intentional.
     */
    public static GetInstance Get() {
        return scope.get();
    }
    
    // -- For testing only --
    
    /** Reset application configuration. */
    public static void reset() {
        if (!isInitialized())
            return;
        if (the(App.mode).isNot(AppMode.TEST))
            return;
        
        scope.reset();
    }
    
    /**
     * Initialize the application scope if it has yet to be initialized.
     * @return {@code true} if the initialization actually happen with this call.
     */
    public static boolean initialize() {
        val isAbsent = initializeIfAbsent(null);
        return isAbsent;
    }
    
    /**
     * Initialize the application scope. This method can only be run once.
     * 
     * @param config  the configuration.
     * @return the scope object.
     * @throws AppScopeAlreadyInitializedException 
     **/
    public static Scope initialize(Configuration config) throws AppScopeAlreadyInitializedException {
        scope.init(config);
        return scope;
    }
    
    /**
     * Initialize the application scope if it has yet to be initialized.
     * 
     * @param configuration  the configuration.
     * @return {@code true} if the initialization actually happen with this call.
     */
    public static boolean initializeIfAbsent(Configuration configuration) {
        val isAbsent = scope.initIfAbsent(configuration);
        return isAbsent;
    }
    
    /**
     * Initialize the application scope if it has yet to be initialized.
     * 
     * @param configuration  the configuration.
     * @param exitStatus     the exit status.
     */
    public static void initializeOrSystemHalt(Configuration configuration, int exitStatus) {
        val isAbsent = scope.initIfAbsent(configuration);
        if (isAbsent)
            return;

        System.err.println("Application is already initialized!");
        System.err.println("Initialized at: ");
        System.err.println(scope.getInitialzedStackTrace());
        System.err.println();
        System.err.println("Second attempt to initialize at: ");
        new NullPointerException().printStackTrace(System.err);
        System.err.println();
        System.exit(exitStatus);
    }
    
    /** @return {@code true} if the application scope has been initialized */
    public static boolean isInitialized() {
        val isInitialized = scope.isInitialized();
        return isInitialized;
    }
    
    /**
     * Ensure that the application mode is as specified.
     * If that is not the case the system will be force to halt.
     * 
     * @param appMode the application mode.
     */
    public static void ensureMode(AppMode appMode) {
        trace(Capture.Continue, caller->{
            ProposedConfiguration.instance
                .appMode(appMode)
                .orSystemHalt();
            return null;
        });
    }

    /**
     * Propose the provider.
     * 
     * @param provider the provider.
     * @return the proposed configuration.
     */
    public <T> ProposedConfigurationWithLastProvider propose(Provider<T> provider) {
        return ProposedConfiguration.instance.add(provider);
    }
    
    /** Private part */
    private App() {
        super();
    }
    
}
