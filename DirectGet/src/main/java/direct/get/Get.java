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

import static direct.get.Named.Predicate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;
import lombok.val;

// TODO - substitue and xxxThread shouw be moved to Run.

/**
 * This class provide access to the application scope.
 * 
 * @author nawaman
 */
public final class Get {
	
	/** This predicate specifies that all of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_ALL = Named.Predicate("InheritAll", ref->true);
	
	/** This predicate specifies that none of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_NONE = Named.Predicate("InheritNone", ref->false);
	
	private static AtomicInteger threadCount = new AtomicInteger(1);
	
	/** The reference to the thread factory. */
	public static final Ref<ThreadFactory> _ThreadFactory_ = Ref.of(ThreadFactory.class, runnable->{
		val thread = new Thread(runnable);
		thread.setName("Thread#" + threadCount.getAndIncrement());
		return thread;
	});
	
	/** The reference to the executor. */
	public static final Ref<Executor> _Executor_  = Ref.of(Executor.class, ()->(Executor)(runnable->{
		val newThread = Get.a(_ThreadFactory_).newThread(runnable);
		newThread.start();
	}));
	
	private Get() {
		
	}
	
	/** @return the optional value associated with the given ref.  */
	public static <T> Optional<T> _a(Ref<T> ref) {
		val optValue = App.Get()._a(ref);
		return optValue;
	}
	
	/** @return the optional value associated with the given class.  */
	public static <T> Optional<T> _a(Class<T> clzz) {
		val ref      = Ref.forClass(clzz);
		val optValue = _a(ref);
		return optValue;
	}
	
	/** @return the value associated with the given class.  */
	public static <T> T a(Class<T> clzz) {
		val ref   = Ref.forClass(clzz);
		val value = a(ref);
		return value;
	}
	
	/** @return the value associated with the given ref.  */
	public static <T> T a(Ref<T> ref) {
		val optValue = _a(ref);
		val value    = optValue.orElse(null);
		return value;
	}
	
	/** @return the value associated with the given class or return the elseValue if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, T elseValue) {
		val ref   = Ref.forClass(clzz);
		val value = a(ref, elseValue);
		return value;
	}

	/** @return the value associated with the given ref or return the elseValue if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, T elseValue) {
		try {
			val optValue = _a(ref);
			val value    = optValue.orElse(elseValue);
			return value;
		} catch (GetException e) {
			return elseValue;
		}
	}

	/** @return the value associated with the given class or return the from elseSupplier if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
		val ref   = Ref.forClass(clzz);
		val value = a(ref, elseSupplier);
		return value;
	}
	
	/** @return the value associated with the given ref or return the from elseSupplier if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
		val optValue = _a(ref);
		val value    = optValue.orElseGet(elseSupplier);
		return value;
	}
	
	// TODO - Lots of the below should be moved to Run.
	
	/**
	 * Substitute the given providings and run the runnable.
	 */
	@SuppressWarnings("rawtypes") 
	public static void substitute(Stream<Providing> providings, Runnable runnable) {
		val get = App.Get();
		get.substitute(providings, runnable);
	}
	
	/**
	 * Substitute the given providings and run the action.
	 */
	@SuppressWarnings("rawtypes")
	public <V> V substitute(Stream<Providing> providings, Supplier<V> action) {
		val get    = App.Get();
		val result = get.substitute(providings, action);
		return result;
	}
	
	/**
	 * Create a sub thread with a get that inherits all substitution from the current Get
	 *   and run the runnable with it.
	 **/
	public static <V> Thread newThread(Runnable runnable) {
		val get    = App.Get();
		val thread = get.newThread(runnable);
		return thread;
	}
	
	/**
	 * Create a sub thread with a get that inherits the given substitution from the current
	 *   Get and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static <V> Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
		val get    = App.Get();
		val thread = get.newThread(refsToInherit, runnable);
		return thread;
	}

	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static <V> Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		val get    = App.Get();
		val thread = get.newThread(refsToInherit, runnable);
		return thread;
	}
	
	/**
	 * Create and run a sub thread with a get that inherits all substitution from the current
	 *   Get and run the runnable with it.
	 **/
	public static <V> void runNewThread(Runnable runnable) {
		val get    = App.Get();
		val thread = get.newThread(runnable);
		thread.start();
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the providings of those given refs.
	 **/
	@SuppressWarnings("rawtypes") 
	public static <V> void runNewThread(List<Ref> refsToInherit, Runnable runnable) {
		val get = App.Get();
		get.runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test).
	 **/
	@SuppressWarnings("rawtypes") 
	public static <V> void runNewThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		val val = App.Get();
		val.runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static <V> CompletableFuture<V> runThread(
			Predicate<Ref> refsToInherit,
			Supplier<V>    action) {
		val get    = App.Get();
		val future = get.runThread(refsToInherit, action);
		return future;
	}
	
}
