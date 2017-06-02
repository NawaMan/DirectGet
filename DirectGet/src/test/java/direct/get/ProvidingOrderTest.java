package direct.get;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import direct.get.exceptions.GetException;

public class ProvidingOrderTest {
	
	public static final String NOT_IMPLEMENT_YET = "NOT_IMPLEMENT_YET";

	// The order
	// Get parent dictate - when included
	// Get space parent dictate
	// Get space dictate
	// Get stack parent dictate
	// Get stack dictate
	// Get stack normal
	// Get space normal
	// Get space parent normal
	// Get parent normal - when included
	// Get stack default
	// Get space default
	// Get space parent default
	// Get parent default - when included
	// Ref default
	
	private final Ref<String> ref          = Ref.of(String.class, ()->"RefDefault");
	private final Ref<String> refNoDefault = Ref.of(String.class);
	
	private final Providing<String> getParentDictate   = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"GetParentDictate");
	private final Providing<String> spaceParentDictate = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"SpaceParentDictate");
	private final Providing<String> spaceDictate       = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"SpaceDictate");
	private final Providing<String> stackDictate       = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"StackDictate");

	private final Providing<String> getParentNormal   = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"GetParentNormal");
	private final Providing<String> spaceParentNormal = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"SpaceParentNormal");
	private final Providing<String> spaceNormal       = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"SpaceNormal");
	private final Providing<String> stackNormal       = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"StackNormal");

	private final Providing<String> getParentDefault   = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"GetParentDefault");
	private final Providing<String> spaceParentDefault = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"SpaceParentDefault");
	private final Providing<String> spaceDefault       = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"SpaceDefault");
	private final Providing<String> stackDefault       = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"StackDefault");

	private void doTest(
			Providing<String> _getParent,
			Providing<String> _spaceParent,
			Providing<String> _space,
			Providing<String> _stack,
			String expected) {
		doTest(_getParent, _spaceParent, _space, _stack, false, expected);
	}
	
	private void doTest(
			Providing<String> _getParent,
			Providing<String> _spaceParent,
			Providing<String> _space,
			Providing<String> _stack,
			boolean isToInherit,
			String expected) {
		RefSpace appSpace    = AppSpace.current;
		RefSpace parentSpace = appSpace   .newSubSpace(new Configuration(Collections.singletonMap(ref, _spaceParent)));
		RefSpace theSpace    = parentSpace.newSubSpace(new Configuration(Collections.singletonMap(ref, _space)));
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<AssertionError> assertErr = new AtomicReference<>();
		theSpace.substitute(Arrays.asList(_getParent), ()->{
			try {
				@SuppressWarnings("rawtypes")
				List<Ref> list = isToInherit ? Arrays.asList(ref) : Collections.emptyList();
				theSpace.runSubThread(list, ()->{
					theSpace.substitute(Arrays.asList(_stack), ()->{
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
			} catch (Throwable e) {
				latch.countDown();
			}
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
	
	// == Documented tests ====================================================

	@Test
	public void test_forDictate_superHasPriority() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDictate");
	}
	
	@Test
	public void test_forNormal_subHasPriority() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void test_forDefault_subHasPriority() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDefault");
	}
	
	@Test
	public void test_forDefault_hasPriority_thenNull() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDefault");
	}
	
	@Test
	public void test_forNormal_hasPriority_thenDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void test_forDictate_hasPriority_thenNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDictate");
	}
	
	@Test
	public void test_ifNonSpecified_refDefaultIsReturned() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "RefDefault");
	}
	
	@Test
	public void test_refDefault_defaultConstructionIsCalledIsUsed() {
		Assert.assertEquals("", AppSpace.current.get().a(refNoDefault));
	}
	
	@Test(expected=GetException.class)
	public void test_refDefault_withNoDefaultConstruction_exceptionIsThrown() {
		AppSpace.current.get().a(Ref.of(List.class));
	}
	
	@Test
	public void test_refDefault_withNoDefaultConstruction_returnTheGivenResult() {
		List<String> theList = new ArrayList<>();
		Assert.assertEquals(theList, AppSpace.current.get().a(Ref.of(List.class), theList));
	}
	
	@Test
	public void test_byDefault_parentHasNoEffect_so_refDefault() {
		Providing<String> _getParent   = spaceDictate;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, "RefDefault");
	}
	
	// == Characteristic tests ================================================
	
	//-- Default --
	
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
		doTest(_getParent, _spaceParent, _space, _stack, "RefDefault");
	}
	
	@Test
	//@Ignore(NOT_IMPLEMENT_YET)
	public void testGetParentDefault_borrowSpecifyRefs_includeChecked_fromParent() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, true, "GetParentDefault");
	}
	
	@Test
	//@Ignore(NOT_IMPLEMENT_YET)
	public void testGetParentDefault_borrowSpecifyRefs_excludeChecked_fromParent() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _spaceParent, _space, _stack, false, "RefDefault");
	}
	
	//-- Normal --
	
	@Test
	public void testRefDefaultStackNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormalSpaceParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormalSpaceParentDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceDefaultSpaceParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceDefaultSpaceParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceNormalSpaceParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormalSpaceParentormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormalSpaceParentDefaultGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _spaceParent = spaceParentDefault;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceDefaultSpaceParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceDefaultSpaceParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceDefault;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceNormalSpaceParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _spaceParent = spaceParentNormal;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "StackNormal");
	}
	
	//-- Dictate --
	
	@Test
	public void testRefDefaultStackDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = null;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDictate");
	}
	
	@Test
	public void testRefDefaultStackNormalSpaceDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateSpaceNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateSpaceDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = null;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceNormalSpaceParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentDictate;
		Providing<String> _space       = spaceNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultSpaceDictateSpaceParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentDictate;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateSpaceDictateSpaceParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _spaceParent = spaceParentDictate;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, "SpaceParentDictate");
	}
	
	@Test
	//@Ignore(NOT_IMPLEMENT_YET)
	public void testRefDefaultStackDictateSpaceDictateSpaceParentDictateGetParentDictate_includeParent() {
		Providing<String> _getParent   = getParentDictate;
		Providing<String> _spaceParent = spaceParentDictate;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, true, "SpaceParentDictate");
	}
	
	@Test
	//@Ignore(NOT_IMPLEMENT_YET)
	public void testRefDefaultStackDictateSpaceDictateSpaceParentDictateGetParentDictate_excludeParent() {
		Providing<String> _getParent   = getParentDictate;
		Providing<String> _spaceParent = spaceParentDictate;
		Providing<String> _space       = spaceDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _spaceParent, _space, _stack, false, "SpaceParentDictate");
	}
	
}
