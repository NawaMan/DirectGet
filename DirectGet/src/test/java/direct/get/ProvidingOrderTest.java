package direct.get;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

public class ProvidingOrderTest {

	// The order
	// Get parent dictate
	// Get space parent dictate
	// Get space dictate
	// Get stack parent dictate
	// Get stack dictate
	// Get stack normal
	// Get space normal
	// Get space parent normal
	// Get parent normal
	// Get stack default
	// Get space default
	// Get space parent default
	// Get parent default
	// Ref default
	
	private final Ref<String> ref = Ref.of(String.class, ()->"RefDefault");
	
	private final Providing<String> getParentDictate   = new Providing.Basic<>(ref, ProvidingLevel.Dictate, ()->"GetParentDictate");
	private final Providing<String> spaceParentDictate = new Providing.Basic<>(ref, ProvidingLevel.Dictate, ()->"SpaceParentDictate");
	private final Providing<String> spaceDictate       = new Providing.Basic<>(ref, ProvidingLevel.Dictate, ()->"SpaceDictate");
	private final Providing<String> stackDictate       = new Providing.Basic<>(ref, ProvidingLevel.Dictate, ()->"StackDictate");

	private final Providing<String> getParentNormal   = new Providing.Basic<>(ref, ProvidingLevel.Normal, ()->"GetParentNormal");
	private final Providing<String> spaceParentNormal = new Providing.Basic<>(ref, ProvidingLevel.Normal, ()->"SpaceParentNormal");
	private final Providing<String> spaceNormal       = new Providing.Basic<>(ref, ProvidingLevel.Normal, ()->"SpaceNormal");
	private final Providing<String> stackNormal       = new Providing.Basic<>(ref, ProvidingLevel.Normal, ()->"StackNormal");

	private final Providing<String> getParentDefault   = new Providing.Basic<>(ref, ProvidingLevel.Default, ()->"GetParentDefault");
	private final Providing<String> spaceParentDefault = new Providing.Basic<>(ref, ProvidingLevel.Default, ()->"SpaceParentDefault");
	private final Providing<String> spaceDefault       = new Providing.Basic<>(ref, ProvidingLevel.Default, ()->"SpaceDefault");
	private final Providing<String> stackDefault       = new Providing.Basic<>(ref, ProvidingLevel.Default, ()->"StackDefault");
	
	private void doTest(
			Providing<String> _getParent,
			Providing<String> _spaceParent,
			Providing<String> _space,
			Providing<String> _stack,
			String expected) {
		RefSpace appSpace    = AppSpace.current;
		RefSpace parentSpace = appSpace   .newSubSpace(new Configuration(Collections.singletonMap(ref, _spaceParent)));
		RefSpace theSpace    = parentSpace.newSubSpace(new Configuration(Collections.singletonMap(ref, _space)));
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<AssertionError> assertErr = new AtomicReference<>();
		theSpace.substitute(_getParent, ()->{
			theSpace.runSubThread(()->{
				theSpace.substitute(_stack, ()->{
					String actual = theSpace.get()._a(ref).orElse(null);
					try {
						assertEquals(expected, actual);
					} catch (AssertionError e) {
						assertErr.set(e);
					} finally {
						latch.countDown();
					}
				});
			});
		});
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		AssertionError err = assertErr.get();
		if (err != null) {
			throw err;
		}
	}
	
	@Test
	public void testRefDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "RefDefault");
	}
	
	@Test
	public void testRefDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDefault");
	}
	
	@Test
	public void testSpaceDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDefault");
	}
	
	@Test
	public void testSpaceParentDefaultSpaceDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDefault");
	}
	
	@Test
	public void testGetParentDefaultSpaceParentDefaultSpaceDefaultStackDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDefault");
	}
	
	@Test
	public void testGetParentDefaultSpaceParentDefaultSpaceDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDefault");
	}
	
	@Test
	public void testGetParentDefaultSpaceParentDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentDefault");
	}
	
	@Test
	public void testGetParentDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "GetParentDefault");
	}

}
