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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import direct.get.Get.Instance;
import direct.get.exceptions.AppScopeAlreadyInitializedException;

/***
 * Scope holds a configuration which specify providings.
 * 
 * @author nawaman
 */
public class Scope {

	private static final String APP_SCOPE_NAME = "AppScope";
	
	private static final Configuration DEF_CONFIG = new Configuration();

	private static final Object lock = new Object();
	

	/**
	 * The name of the scope.
	 * 
	 * This value is for the benefit of human who look at it.
	 * There is no use in the program in anyway (except debugging/logging/auditing purposes).
	 **/
	private final String name;
	
	private final Scope parentScope;
	
	private volatile Configuration config;
	
	final ThreadLocal<Get.Instance> threadGet;
	
	private volatile List<StackTraceElement> stackTraceAtCreation;
	
	// For AppScope only.
	Scope() {
		this.name        = APP_SCOPE_NAME;
		this.parentScope = null;
		this.config      = DEF_CONFIG;
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(this));
	}
	
	// For other scope.
	Scope(String name, Scope parentScope, Configuration config) {
		this.name        = Optional.ofNullable(name).orElse("Scope:" + this.getClass().getName());
		this.parentScope = parentScope;
		this.config      = Optional.ofNullable(config).orElseGet(Configuration::new);
		this.threadGet   = ThreadLocal.withInitial(()->new Get.Instance(this));
	}
	
	// -- For AppScope only ---------------------------------------------------
	void init(Configuration newConfig) throws AppScopeAlreadyInitializedException {
		if (config == DEF_CONFIG) {
			initIfAbsent(newConfig);
			return;
		}
		throw new AppScopeAlreadyInitializedException();
	}
	
	void ensureInitialized() {
		initIfAbsent(null);
	}
	
	boolean initIfAbsent(Configuration newConfig) {
		if (config == DEF_CONFIG) {
			synchronized (lock) {
				if (config == DEF_CONFIG) {
					config     = (newConfig == null) ? new Configuration() : newConfig;
					stackTraceAtCreation = Collections.unmodifiableList(Arrays.asList(new Throwable().getStackTrace()));
					return true;
				}
			}
		}
		return false;
	}
	
	boolean hasBeenInitialized() {
		return config != null;
	}
	
	/** @return the stacktrace when this scope is initialized. */
	public final Stream<StackTraceElement> getInitialzedStackTrace() {
		ensureInitialized();
		return stackTraceAtCreation.stream();
	}
	
	// -- For both types of Scope ------------------------------------------
	
	/** @return the name of the scope. */
	public String getName() {
		return name;
	}

	/** @return the name of the scope. */
	public Scope getParentScope() {
		return this.parentScope;
	}

	protected final Configuration getConfiguration() {
		ensureInitialized();
		return config;
	}
	
	protected final <T> Providing<T> getProviding(Ref<T> ref) {
		if (ref == null) {
			return null;
		}		

		return config.getProviding(ref);
	}
	
	/** @return the get for the current thread that is associated with this scope. NOTE: capital 'G' is intentional. */
	public Get.Instance Get() {
		return threadGet.get();
	}
	
	<T> Optional<T> doGet(Ref<T> ref) {
		Instance currentGet = this.Get();
		Providing<T> providing = currentGet.getProviding(ref);
		if (providing != null) {
			return Optional.ofNullable(providing.get());
		}
		// TODO - Move this to determineXXX
		return ref._get();
	}
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return name + "(" + config + ")";
	}
	
	/** Return the detail string representation of this object. */
	public final String toXRayString() {
		return name + "(" + config.toXRayString() + ")";
	}
	
	/**
	 * Create and return a new sub scope with the given configuration.
	 */
	public Scope newSubScope(Configuration config) {
		return new Scope(null, this, config);
	}
	
	/**
	 * Create and return a new sub scope with the given name and configuration.
	 */
	public Scope newSubScope(String name, Configuration config) {
		return new Scope(name, this, config);
	}
	
}
