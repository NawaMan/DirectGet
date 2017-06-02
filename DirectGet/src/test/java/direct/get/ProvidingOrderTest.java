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
	// Get scope parent dictate
	// Get scope dictate
	// Get stack parent dictate
	// Get stack dictate
	// Get stack normal
	// Get scope normal
	// Get scope parent normal
	// Get parent normal - when included
	// Get stack default
	// Get scope default
	// Get scope parent default
	// Get parent default - when included
	// Ref default
	
	private final Ref<String> ref          = Ref.of(String.class, ()->"RefDefault");
	private final Ref<String> refNoDefault = Ref.of(String.class);
	
	private final Providing<String> getParentDictate   = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"GetParentDictate");
	private final Providing<String> scopeParentDictate = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"ScopeParentDictate");
	private final Providing<String> scopeDictate       = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"ScopeDictate");
	private final Providing<String> stackDictate       = new Providing.Basic<>(ref, PriorityLevel.Dictate, ()->"StackDictate");

	private final Providing<String> getParentNormal   = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"GetParentNormal");
	private final Providing<String> scopeParentNormal = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"ScopeParentNormal");
	private final Providing<String> scopeNormal       = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"ScopeNormal");
	private final Providing<String> stackNormal       = new Providing.Basic<>(ref, PriorityLevel.Normal, ()->"StackNormal");

	private final Providing<String> getParentDefault   = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"GetParentDefault");
	private final Providing<String> scopeParentDefault = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"ScopeParentDefault");
	private final Providing<String> scopeDefault       = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"ScopeDefault");
	private final Providing<String> stackDefault       = new Providing.Basic<>(ref, PriorityLevel.Default, ()->"StackDefault");

	private void doTest(
			Providing<String> _getParent,
			Providing<String> _scopeParent,
			Providing<String> _scope,
			Providing<String> _stack,
			String expected) {
		doTest(_getParent, _scopeParent, _scope, _stack, false, expected);
	}
	
	private void doTest(
			Providing<String> _getParent,
			Providing<String> _scopeParent,
			Providing<String> _scope,
			Providing<String> _stack,
			boolean isToInherit,
			String expected) {
		Scope appScope    = AppScope.current;
		Scope parentScope = appScope   .newSubScope(new Configuration(Collections.singletonMap(ref, _scopeParent)));
		Scope theScope    = parentScope.newSubScope(new Configuration(Collections.singletonMap(ref, _scope)));
		
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<AssertionError> assertErr = new AtomicReference<>();
		theScope.substitute(Arrays.asList(_getParent), ()->{
			try {
				@SuppressWarnings("rawtypes")
				List<Ref> list = isToInherit ? Arrays.asList(ref) : Collections.emptyList();
				theScope.runSubThread(list, ()->{
					theScope.substitute(Arrays.asList(_stack), ()->{
						String actual = theScope.get()._a(ref).orElse(null);
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
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
	}
	
	@Test
	public void test_forNormal_subHasPriority() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void test_forDefault_subHasPriority() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
	}
	
	@Test
	public void test_forDefault_hasPriority_thenNull() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDefault");
	}
	
	@Test
	public void test_forNormal_hasPriority_thenDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void test_forDictate_hasPriority_thenNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
	}
	
	@Test
	public void test_ifNonSpecified_refDefaultIsReturned() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
	}
	
	@Test
	public void test_refDefault_defaultConstructionIsCalledIsUsed() {
		Assert.assertEquals("", AppScope.current.get().a(refNoDefault));
	}
	
	@Test(expected=GetException.class)
	public void test_refDefault_withNoDefaultConstruction_exceptionIsThrown() {
		AppScope.current.get().a(Ref.of(List.class));
	}
	
	@Test
	public void test_refDefault_withNoDefaultConstruction_returnTheGivenResult() {
		List<String> theList = new ArrayList<>();
		Assert.assertEquals(theList, AppScope.current.get().a(Ref.of(List.class), theList));
	}
	
	@Test
	public void test_byDefault_parentHasNoEffect_so_refDefault() {
		Providing<String> _getParent   = scopeDictate;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
	}
	
	// == Characteristic tests ================================================
	
	//-- Default --
	
	@Test
	public void testRefDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
	}
	
	@Test
	public void testRefDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
	}
	
	@Test
	public void testScopeDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
	}
	
	@Test
	public void testScopeParentDefaultScopeDefaultStackDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
	}
	
	@Test
	public void testGetParentDefaultScopeParentDefaultScopeDefaultStackDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
	}
	
	@Test
	public void testGetParentDefaultScopeParentDefaultScopeDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDefault");
	}
	
	@Test
	public void testGetParentDefaultScopeParentDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDefault");
	}
	
	@Test
	public void testGetParentDefault() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
	}
	
	@Test
	public void testGetParentDefault_borrowSpecifyRefs_includeChecked_fromParent() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, true, "GetParentDefault");
	}
	
	@Test
	public void testGetParentDefault_borrowSpecifyRefs_excludeChecked_fromParent() {
		Providing<String> _getParent   = getParentDefault;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = null;
		doTest(_getParent, _scopeParent, _scope, _stack, false, "RefDefault");
	}
	
	//-- Normal --
	
	@Test
	public void testRefDefaultStackNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormalScopeParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormalScopeParentDefault() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeDefaultScopeParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeDefaultScopeParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeNormalScopeParentNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormalScopeParentormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormalScopeParentDefaultGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _scopeParent = scopeParentDefault;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeDefaultScopeParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeDefaultScopeParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeDefault;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeNormalScopeParentNormalGetParentNormal() {
		Providing<String> _getParent   = getParentNormal;
		Providing<String> _scopeParent = scopeParentNormal;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
	}
	
	//-- Dictate --
	
	@Test
	public void testRefDefaultStackDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = null;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
	}
	
	@Test
	public void testRefDefaultStackNormalScopeDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackNormal;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateScopeNormal() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateScopeDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = null;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeNormalScopeParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentDictate;
		Providing<String> _scope       = scopeNormal;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDefaultScopeDictateScopeParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentDictate;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDefault;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateScopeDictateScopeParentDictate() {
		Providing<String> _getParent   = null;
		Providing<String> _scopeParent = scopeParentDictate;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateScopeDictateScopeParentDictateGetParentDictate_includeParent() {
		Providing<String> _getParent   = getParentDictate;
		Providing<String> _scopeParent = scopeParentDictate;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, true, "ScopeParentDictate");
	}
	
	@Test
	public void testRefDefaultStackDictateScopeDictateScopeParentDictateGetParentDictate_excludeParent() {
		Providing<String> _getParent   = getParentDictate;
		Providing<String> _scopeParent = scopeParentDictate;
		Providing<String> _scope       = scopeDictate;
		Providing<String> _stack       = stackDictate;
		doTest(_getParent, _scopeParent, _scope, _stack, false, "ScopeParentDictate");
	}
	
}
