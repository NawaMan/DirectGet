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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.val;
/**
 * This class offer a natural way to run something.
 * 
 * @author nawaman
 **/
public class  Run {
	
	/** Add the wrapper */
	public static SessionBuilder with(Function<Runnable, Runnable> wrapper) {
		return new SessionBuilder().with(wrapper);
	}

	/** Add the wrapper */
	public static SessionBuilder With(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder with(Providing ... providings) {
		return with(Stream.of(providings));
	}

	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder With(Providing ... providings) {
		return with(Stream.of(providings));
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder with(Collection<Providing> providings) {
		return with(providings.stream());
	}

	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder With(Collection<Providing> providings) {
		return with(providings.stream());
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder with(Stream<Providing> providings) {
		return new SessionBuilder().with(runnable->()->{
			// TODO Think about how to default the scope.
			App.Get().substitute(providings, runnable);
		});
	}

	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder With(Stream<Providing> providings) {
		return with(providings);
	}

	/** Add the wrapper */
	public  SessionBuilder by(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}

	/** Add the wrapper */
	public  SessionBuilder By(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}

	/** Add the wrapper */
	public  SessionBuilder use(Function<Runnable, Runnable> wrapper) {
		return use(wrapper);
	}

	/** Add the wrapper */
	public  SessionBuilder Use(Function<Runnable, Runnable> wrapper) {
		return use(wrapper);
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder use(Providing ... providings) {
		return use(Stream.of(providings));
	}

	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder Use(Providing ... providings) {
		return use(providings);
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder use(Collection<Providing> providings) {
		return use(providings.stream());
	}

	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder Use(Collection<Providing> providings) {
		return use(providings);
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder use(Stream<Providing> providings) {
		return with(providings);
	}
	
	/** Add the wrapper */
	@SuppressWarnings("rawtypes")
	public static SessionBuilder Use(Stream<Providing> providings) {
		return new SessionBuilder().with(runnable->()->{
			// TODO Think about how to default the scope.
			App.Get().substitute(providings, runnable);
		});
	}
	
	/** Make the run to be run on a new thread. */
	public static NewThreadWrapper OnNewThread() {
		return new NewThreadWrapper();
	}

	/** Make the run to be run on a new thread. */
	public static NewThreadWrapper onNewThread() {
		return new NewThreadWrapper();
	}
	
	
	/** Alias type. */
	public static interface Wrapper extends Function<Runnable, Runnable> {}
	
	/** The wrapper for a new thread run. */
	public static class NewThreadWrapper extends SessionBuilder implements Wrapper {
		
		@SuppressWarnings("rawtypes")
		private final List<Ref> includedRefs = new ArrayList<>();
		@SuppressWarnings("rawtypes")
		private final List<Ref> excludedRefs = new ArrayList<>();
		
		private Boolean inheritMass = null;
		
		private Fork fork = null;
		
		/** Default constructor. */
		public NewThreadWrapper() {}
		
		/** Constructor with a fork. */
		public NewThreadWrapper(Fork fork) {
			this.fork = fork;
		}
		
		/** Join the runnable with using the given fork. */
		public NewThreadWrapper joinWith(Fork fork) {
			this.fork = fork;
			return this;
		}
		
		/** Set this run to inherit all refs from the parent thread. */
		public NewThreadWrapper inheritAll() {
			inheritMass = true;
			includedRefs.clear();
			excludedRefs.clear();
			return this;
		}
		
		/** Set this run to inherit no refs from the parent thread. */
		public NewThreadWrapper inheritNone() {
			inheritMass = false;
			includedRefs.clear();
			excludedRefs.clear();
			return this;
		}
		
		/** Specify what refs to be inherited - in case of inherit none. */
		@SuppressWarnings({ "rawtypes" })
		public NewThreadWrapper inherit(Ref ... refs) {
			List<Ref> list = Arrays.asList(refs);
			includedRefs.addAll(list);
			excludedRefs.removeAll(list);
			return this;
		}

		/** Specify what refs NOT to be inherited - in case of inherit all. */
		@SuppressWarnings({ "rawtypes" })
		public NewThreadWrapper notInherit(Ref ... refs) {
			List<Ref> list = Arrays.asList(refs);
			includedRefs.removeAll(list);
			excludedRefs.addAll(list);
			return this;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Runnable apply(Runnable runnable) {
			Predicate<Ref> predicate = Get.INHERIT_NONE;
			val isAll = Boolean.TRUE.equals(inheritMass);
			if (isAll) {
				if (excludedRefs.isEmpty()) {
					predicate = Get.INHERIT_ALL;
				} else {
					predicate = ref->!excludedRefs.contains(ref);
				}
			} else {
				val isNone = Boolean.TRUE.equals(inheritMass);
				if (isNone) {
					if (includedRefs.isEmpty()) {
						predicate = Get.INHERIT_NONE;
					} else {
						predicate = ref->includedRefs.contains(ref);
					}
				} else {
					predicate = ref->includedRefs.contains(ref);
				}
			}
			val checker = predicate;
			if (fork != null) {
				return ()->{
					App.Get().runNewThread(checker, fork.run(runnable));
				};
			} else {
				return ()->{
					App.Get().runNewThread(checker, runnable);
				};
			}
		}
		
	}
	
	/**
	 * This class make building a run a bit easier.
	 */
	public static class SessionBuilder {
		
		final List<Function<Runnable, Runnable>> wrappers = new ArrayList<>();
		
		
		/** Another way to chain the invocation */
		public final SessionBuilder and = this;
		
		/** Add the wrapper */
		public SessionBuilder with(Function<Runnable, Runnable> wrapper) {
			if (wrapper != null) {
				wrappers.add(wrapper);
			}
			return this;
		}
		
		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder with(Providing ... providings) {
			return with(Stream.of(providings));
		}

		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder with(Collection<Providing> providings) {
			return with(providings.stream());
		}

		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder with(Stream<Providing> providings) {
			return with(runnable->()->{
				// TODO Think about how to default the scope.
				App.Get().substitute(providings, runnable);
			});
		}

		/** Add the wrapper */
		public SessionBuilder by(Function<Runnable, Runnable> wrapper) {
			return with(wrapper);
		}

		/** Add the wrapper */
		public SessionBuilder use(Function<Runnable, Runnable> wrapper) {
			return with(wrapper);
		}
		
		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder use(Providing ... providings) {
			return use(Stream.of(providings));
		}

		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder use(Collection<Providing> providings) {
			return with(providings.stream());
		}

		/** Add the wrapper */
		@SuppressWarnings("rawtypes")
		public SessionBuilder use(Stream<Providing> providings) {
			return with(runnable->()->{
				// TODO Think about how to default the scope.
				App.Get().substitute(providings, runnable);
			});
		}
		
		/** Make the run to be run on a new thread. */
		public NewThreadWrapper onNewThread() {
			val newThreadWrapper = new NewThreadWrapper();
			newThreadWrapper.wrappers.addAll(this.wrappers);
			newThreadWrapper.wrappers.add(newThreadWrapper);
			return newThreadWrapper;
		}
		
		/** Make the run to be run on a new thread. */
		public NewThreadWrapper OnNewThread() {
			return onNewThread();
		}
		
		
		/** Build the session for later use. */
		public WrapperContext build() {
			return new WrapperContext(wrappers);
		}
		
		/** Run the session now. */
		public void run(Runnable runnable) {
			build()
			.run(runnable);
		}
		
		/** Run the session now. */
		public void start(Runnable runnable) {
			build()
			.run(runnable);
		}
		
	}
	
	/**
	 * The contains the wrappers so that we can run something within them.
	 */
	public static class WrapperContext {
		
		private final List<Function<Runnable, Runnable>> wrappers;

		WrapperContext(List<Function<Runnable, Runnable>> functions) {
			wrappers = functions == null
					? Collections.emptyList()
					: Collections.unmodifiableList(
							functions.stream()
							.filter(Objects::nonNull)
							.collect(Collectors.toList()));
		}
		
		/** Run something within this context. */
		public void start(Runnable runnable) {
			run(runnable);
		}
		
		/** Run something within this context. */
		public void run(Runnable runnable) {
			Runnable current = runnable;
			for (int i = wrappers.size(); i-->0;) {
				val wrapper = wrappers.get(i);
				val wrapped = wrapper.apply(current);
				if (wrapped != null) {
					current = wrapped;
				}
			}
			current.run();
		}
		
	}
	
}