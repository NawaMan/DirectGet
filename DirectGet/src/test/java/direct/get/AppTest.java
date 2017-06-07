package direct.get;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Ignore;
import org.junit.Test;

import direct.get.exceptions.AppScopeAlreadyInitializedException;

// VERY IMPORTANT NOTE!!!
// These test cases are designed to be run alone.
// NEVER run them all at the same time OR with other tests.
public class AppTest {

	@Test
	@Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
	public void testDefaultInitialize() {
		// This test prove that without setting anything, the Get of the App scope is ready to use.
		App.Get().a(ArrayList.class);
	}

	@Test
	@Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
	public void testFirstInitialize() throws AppScopeAlreadyInitializedException {
		// This test prove that first initialize has no problem.
		App.initialize(null);
	}
	
	@Test(expected=AppScopeAlreadyInitializedException.class)
	@Ignore("This test case can only be run alone. So remove this ignore and run it alone.")
	public void testSecondInitialize() throws AppScopeAlreadyInitializedException {
		// This test prove that first initialize has no problem.
		try {
			App.initialize(null);
		} catch (AppScopeAlreadyInitializedException e) {
			fail("Oh no! Not from here.");
		}
		
		// ... but the second time will throws an exception.
		App.initialize(null);
	}
	
	// TODO - Should test if the config is really in effect.

}
