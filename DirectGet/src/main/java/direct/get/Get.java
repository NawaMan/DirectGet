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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import direct.get.exceptions.GetException;
import direct.get.exceptions.RunWithSubstitutionException;

/**
 * This class provide access to the application scope.
 * 
 * @author nawaman
 */
public final class Get {
	
	/** This predicate specifies that all of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_ALL = Predicate("InheritAll", ref->true);
	
	/** This predicate specifies that none of the references are to be inherited */
	@SuppressWarnings("rawtypes")
	public static final Predicate<Ref> INHERIT_NONE = Predicate("InheritNone", ref->false);
	
	private static AtomicInteger threadCount = new AtomicInteger(1);
	
	/** The reference to the thread factory. */
	public static final Ref<ThreadFactory> _ThreadFactory_ = Ref.of(ThreadFactory.class, runnable->{
		Thread thread = new Thread(runnable);
		thread.setName("Thread#" + threadCount.getAndIncrement());
		return thread;
	});
	
	/** The reference to the executor. */
	public static final Ref<Executor> _Executor_  = Ref.of(Executor.class, ()->(Executor)(runnable->{
		Thread newThread = Get.a(_ThreadFactory_).newThread(runnable);
		newThread.start();
	}));
	
	private Get() {
		
	}
	
	/** @return the optional value associated with the given ref.  */
	public static <T> Optional<T> _a(Ref<T> ref) {
		return App.Get()._a(ref);
	}
	
	/** @return the optional value associated with the given class.  */
	public static <T> Optional<T> _a(Class<T> clzz) {
		return _a(Ref.forClass(clzz));
	}
	
	/** @return the value associated with the given class.  */
	public static <T> T a(Class<T> clzz) {
		return a(Ref.forClass(clzz));
	}
	
	/** @return the value associated with the given ref.  */
	public static <T> T a(Ref<T> ref) {
		return _a(ref).orElse(null);
	}
	
	/** @return the value associated with the given class or return the elseValue if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, T elseValue) {
		return a(Ref.forClass(clzz), elseValue);
	}

	/** @return the value associated with the given ref or return the elseValue if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, T elseValue) {
		try {
			return _a(ref).orElse(elseValue);
		} catch (GetException e) {
			return elseValue;
		}
	}

	/** @return the value associated with the given class or return the from elseSupplier if no value associated with the class.  */
	public static <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
		return a(Ref.forClass(clzz), elseSupplier);
	}
	
	/** @return the value associated with the given ref or return the from elseSupplier if no value associated with the ref.  */
	public static <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
		return _a(ref).orElseGet(elseSupplier);
	}
	
	/**
	 * Substitute the given providings and run the runnable.
	 */
	@SuppressWarnings("rawtypes") 
	public static void substitute(Stream<Providing> providings, Runnable runnable) {
		 App.Get().substitute(providings, runnable);
	}
	
	/**
	 * Substitute the given providings and run the action.
	 */
	@SuppressWarnings("rawtypes")
	public <V, T extends Throwable> V substitute(Stream<Providing> providings, Computation<V, T> computation) throws T {
		return App.Get().substitute(providings, computation);
	}
	
	/**
	 * Create a sub thread with a get that inherits all substitution from the current Get
	 *   and run the runnable with it.
	 **/
	public static Thread newThread(Runnable runnable) {
		return App.Get().newThread(runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the given substitution from the current
	 *   Get and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
		return App.Get().newThread(refsToInherit, runnable);
	}

	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		return App.Get().newThread(refsToInherit, runnable);
	}
	
	/**
	 * Create and run a sub thread with a get that inherits all substitution from the current
	 *   Get and run the runnable with it.
	 **/
	public static void runNewThread(Runnable runnable) {
		App.Get().newThread(runnable).start();
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the providings of those given refs.
	 **/
	@SuppressWarnings("rawtypes") 
	public static void runNewThread(List<Ref> refsToInherit, Runnable runnable) {
		App.Get().runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Run the given runnable on a new thread that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test).
	 **/
	@SuppressWarnings("rawtypes") 
	public static void runNewThread(Predicate<Ref> refsToInherit, Runnable runnable) {
		App.Get().runNewThread(refsToInherit, runnable);
	}
	
	/**
	 * Create a sub thread with a get that inherits the substitution from the current Get
	 *   (all Ref that pass the predicate test) and run the runnable with it.
	 **/
	@SuppressWarnings("rawtypes")
	public static <V, T extends Throwable> CompletableFuture<V> runThread(
			Predicate<Ref> refsToInherit,
			Computation<V, T> computation) {
		return App.Get().runThread(refsToInherit, computation);
	}
	
	//== The implementation ===================================================
	
	/** Map of providing stack. */
	@SuppressWarnings("rawtypes")
	public static class ProvidingStackMap extends TreeMap<Ref, Stack<Providing>> {
		private static final long serialVersionUID = -8113998773064688984L;
		
		@Override
		public Stack<Providing> get(Object ref) {
			Stack<Providing> stack = super.get(ref);
			if (stack == null) {
				this.put((Ref)ref, new Stack<Providing>());
				stack = super.get(ref);
			}
			return stack;
		}
		/** Safely peek the stack of the value. */
		@SuppressWarnings("unchecked")
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
		/** Return the detail string representation of this object. */
		public String toXRayString() {
			return "{\n\t"
					+ entrySet().stream()
						.map(each->each.getKey() + "=" + each.getValue())
						.collect(Collectors.joining(",\n\t"))
					+ "\n}"; 
		}
	}
	
	/**
	 * Get is a service to allow access to other service.
	 * 
	 * @author nawaman
	 */
	public static final class Instance {
		
		private final Scope scope;
		
		private final ProvidingStackMap providingStacks = new ProvidingStackMap();
		
		Instance(Scope scope) {
			this.scope = scope;
		}
		
		/** @return the scope this Get is in. */
		public Scope getScope() {
			return this.scope;
		}
		
		@SuppressWarnings("rawtypes")
		Stream<Ref> getStackRefs() {
			return providingStacks.keySet().stream();
		}
		
		<T> Providing<T> getProviding(Ref<T> ref) {
			if (ref == null) {
				return null;
			}
			
			Providing<T> providing = Preferability.determineProviding(ref, scope.getParentScope(), scope, providingStacks);
			return providing;
		}

		/** @return the optional value associated with the given ref.  */
		public <T> Optional<T> _a(Ref<T> ref) {
			return scope.doGet(ref);
		}

		/** @return the optional value associated with the given class.  */
		public <T> Optional<T> _a(Class<T> clzz) {
			return _a(Ref.forClass(clzz));
		}
		
		/** @return the value associated with the given ref.  */
		public <T> T a(Class<T> clzz) {
			return a(Ref.forClass(clzz));
		}
		
		/** @return the value associated with the given class.  */
		public <T> T a(Ref<T> ref) {
			return _a(ref).orElse(null);
		}
		
		/** @return the value associated with the given class or return the elseValue if no value associated with the class.  */
		public <T> T a(Class<T> clzz, T elseValue) {
			return a(Ref.forClass(clzz), elseValue);
		}

		/** @return the value associated with the given ref or return the elseValue if no value associated with the ref.  */
		public <T> T a(Ref<T> ref, T elseValue) {
			try {
				return _a(ref).orElse(elseValue);
			} catch (GetException e) {
				return elseValue;
			}
		}

		/** @return the value associated with the given class or return the from elseSupplier if no value associated with the class.  */
		public <T> T a(Class<T> clzz, Supplier<T> elseSupplier) {
			return a(Ref.forClass(clzz), elseSupplier);
		}
		
		/** @return the value associated with the given ref or return the from elseSupplier if no value associated with the ref.  */
		public <T> T a(Ref<T> ref, Supplier<T> elseSupplier) {
			return _a(ref).orElseGet(elseSupplier);
		}
		
		// TODO - Make it array friendly.
		/**
		 * Substitute the given providings and run the runnable.
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public <T> void substitute(Stream<Providing> providings, Runnable runnable) {
			AtomicReference<RuntimeException> problem = new AtomicReference<RuntimeException>(null);
			try {
				substitute(providings, (Computation)(()->{
					try {
						runnable.run();
					} catch (RuntimeException e) {
						problem.set(e);
					}
					return null;
				}));
			} catch (Throwable t) {
				throw new RunWithSubstitutionException(t);
			}
			
			if (problem.get() != null) {
				throw problem.get();
			}
		}
		
		/**
		 * Substitute the given providings and run the action.
		 */
		@SuppressWarnings("rawtypes")
		synchronized public <V, T extends Throwable> V substitute(Stream<Providing> providings, Computation<V, T> computation) throws T {
			List<Ref> addedRefs = null;
			try {
				Iterable<Providing> iterable = ()->providings.iterator();
				for (Providing providing : iterable) {
					if (providing == null) {
						continue;
					}
					
					Ref ref = providing.getRef();
					providingStacks.get(ref).push(providing);
					if (addedRefs == null) {
						addedRefs = new ArrayList<>();
					}
					addedRefs.add(ref);
				}

				return computation.run();
			} finally {
				if (addedRefs != null) {
					addedRefs.forEach(ref->providingStacks.get(ref).pop());
				}
			}
		}
		
		/**
		 * Create a sub thread with a get that inherits all substitution from the current Get
		 *   and run the runnable with it.
		 **/
		public Thread newThread(Runnable runnable) {
			return newThread(INHERIT_NONE, runnable);
		}
		
		/**
		 * Create a sub thread with a get that inherits the given substitution from the current
		 *   Get and run the runnable with it.
		 **/
		@SuppressWarnings("rawtypes")
		public Thread newThread(List<Ref> refsToInherit, Runnable runnable) {
			return newThread(
					ref->refsToInherit.contains(ref),
					runnable);
		}
		
		/**
		 * Create and run a sub thread with a get that inherits all substitution from the current
		 *   Get and run the runnable with it.
		 **/
		public void runNewThread(Runnable runnable) {
			newThread(INHERIT_ALL, runnable).start();
		}
		
		/**
		 * Run the given runnable on a new thread that inherits the providings of those given refs.
		 **/
		@SuppressWarnings("rawtypes") 
		public void runNewThread(List<Ref> refsToInherit, Runnable runnable) {
			newThread(refsToInherit, runnable).start();
		}
		
		/**
		 * Run the given runnable on a new thread that inherits the substitution from the current Get
		 *   (all Ref that pass the predicate test).
		 **/
		@SuppressWarnings("rawtypes") 
		public void runNewThread(Predicate<Ref> refsToInherit, Runnable runnable) {
			newThread(refsToInherit, runnable).start();
		}
		
		/**
		 * Create a sub thread with a get that inherits the substitution from the current Get
		 *   (all Ref that pass the predicate test) and run the runnable with it.
		 **/
		@SuppressWarnings("rawtypes")
		public Thread newThread(Predicate<Ref> refsToInherit, Runnable runnable) {
			Get.Instance    newGet = new Get.Instance(scope);
			List<Providing> providings = prepareProvidings(refsToInherit);
			
			ThreadFactory newThread = a(_ThreadFactory_);
			return newThread.newThread(()->{
				scope.threadGet.set(newGet);
				List<Providing> providingsList = providings;
				newGet.substitute(providingsList.stream(), runnable);
			});
		}
		
		/**
		 * Create a sub thread with a get that inherits the substitution from the current Get
		 *   (all Ref that pass the predicate test) and run the runnable with it.
		 **/
		@SuppressWarnings("rawtypes")
		public <V, T extends Throwable> CompletableFuture<V> runThread(
				Predicate<Ref> refsToInherit,
				Computation<V, T> computation) {
			Get.Instance    newGet     = new Get.Instance(scope);
			List<Providing> providings = prepareProvidings(refsToInherit);
			
			Executor executor = a(_Executor_);
			return CompletableFuture.supplyAsync(()->{
				scope.threadGet.set(newGet);
				try {
					List<Providing> providingsList = providings;
					V result = newGet.substitute(providingsList.stream(), computation);
					return result;
				} catch (Throwable t) {
					throw new CompletionException(t);
				}
			}, executor);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private List<Providing> prepareProvidings(Predicate<Ref> refsToInherit) {
			Preferability._ListenerEnabled_.set(false);
			try {
				return 
					Optional.ofNullable(refsToInherit)
					.filter(test -> test != INHERIT_NONE)
					.map(test->(List)getStackRefs()
								.filter(test)
								.map(this::getProviding)
								.collect(Collectors.toList()))
					.orElse(Collections.emptyList());
			} finally {
				Preferability._ListenerEnabled_.set(true);
			}
		}
		
		/** Return the detail string representation of this object. */
		public final String toXRayString() {
			return "Get(" + scope + ")";
		}
		
	}
}
