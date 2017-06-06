package direct.get;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	public SessionBuilder by(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}

	/** Add the wrapper */
	public SessionBuilder By(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}

	/** Add the wrapper */
	public SessionBuilder using(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
	}

	/** Add the wrapper */
	public SessionBuilder Using(Function<Runnable, Runnable> wrapper) {
		return with(wrapper);
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
		public SessionBuilder by(Function<Runnable, Runnable> wrapper) {
			return with(wrapper);
		}

		/** Add the wrapper */
		public SessionBuilder using(Function<Runnable, Runnable> wrapper) {
			return with(wrapper);
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
				Function<Runnable, Runnable> wrapper = wrappers.get(i);
				Runnable wrapped = wrapper.apply(current);
				if (wrapped != null) {
					current = wrapped;
				}
			}
			current.run();
		}
		
	} 
	
}