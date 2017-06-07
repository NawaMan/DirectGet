package direct.get;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Thread.*;
import static direct.get.Run.*;

import org.junit.Test;

public class GetInstanceTest implements Named.User {
	
	private CountDownLatch latch = new CountDownLatch(1);
	
	private String orgText = "The Text";
	private String newText = "New Text!!!";
	
	private Ref<String> _text_ = Ref.of("TheText", String.class, Supplier("OrginalText",  ()->orgText));
	
	private Stream<Providing> provideNewText = Stream.of(new Providing<>(_text_, Preferability.Dictate, supplier("NewText",  ()->newText)));
	
	private Fork fork = new Fork();

	@Test
	@SuppressWarnings("unchecked")
	public void testBasic() {
		ArrayList<String> list = Get._a(ArrayList.class).orElse(null);
		assertNotNull(list);
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRef() {
		List      theList = new ArrayList();
		Ref<List> aLIST   = Ref.of("aList", List.class, ()->theList);
		assertTrue(App.Get()._a(aLIST).filter(list->list == theList).isPresent());
	}

	private void join() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRunSubstitution() {
		Run
		.with(_newText)
		.run(()->{
			assertEquals(newText, Get.a(_text_));
		});
	}
	
	private final Run.Wrapper _newText = runnable->()->{
		App.Get().substitute(provideNewText, runnable);
	};
	
	private final Run.Wrapper _verboseLogger = runnable->Named.runnable("VERBOSE", ()->{
		List providings = new ArrayList();
		Preferability.DetermineProvidingListener listener = new Preferability.DetermineProvidingListener() {
			@Override
			public <T> void onDetermine(Ref<T> ref, String from, Providing<T> result, Supplier<String> stackTraceSupplier,
					Supplier<String> xraySupplier) {
				System.out.println("Get(" + ref + ") = " + result + " from " + xraySupplier.get() + " on => {\n" + stackTraceSupplier.get() + "\n}");
			}
		};
		providings.add(new Providing(Preferability._Listener_, Preferability.Dictate, ()->listener));
		App.Get().substitute(providings.stream(), runnable);
	});
	
	private final Run.Wrapper _newEmptyThread = (Runnable runnable)->()->{
		App.Get().runNewThread(Get.INHERIT_NONE, fork.run(runnable));
	};
	
	private final Run.Wrapper _newThread = (Runnable runnable)->()->{
		App.Get().runNewThread(Get.INHERIT_ALL, fork.run(runnable));
	};
	
	@Test
	public void testRunNewThread_notInherit() throws Throwable {
		With(_newText)
		.using(_newEmptyThread).run(()->{
			assertEquals(orgText, Get.a(_text_));
		});
		fork.join();
	}
	
	@Test
	public void testRunNewThread_inherit() throws Throwable {
		With(_newText)
		.and.with(_verboseLogger)	// TODO - Create a separate test for this.
		.using(_newThread).run(()->{
			assertEquals(newText, Get.a(_text_));
		});
		fork.join();
	}

}
