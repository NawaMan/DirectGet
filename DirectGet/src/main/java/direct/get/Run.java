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

import static direct.get.App.Get;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
	
	
	/** Alias type. */
	public static interface Wrapper extends Function<Runnable, Runnable> {}
	
	/**
	 * This class make building a run a bit easier.
	 */
	public static class SessionBuilder {
		
		private final List<Function<Runnable, Runnable>> wrappers = new ArrayList<>();
		
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
		
		/** Build the session for later use. */
		public WrapperContext build() {
			return new WrapperContext(wrappers);
		}
		
		/** Run the session now. */
		public void run(Runnable runnable) {
			build().run(runnable);
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