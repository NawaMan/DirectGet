package direct.get;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RefTest {
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRef_forClass() {
		Ref<List> ref1 = Ref.forClass(List.class);
		Ref<List> ref2 = Ref.forClass(List.class);
		assertEquals(ref1, ref2);
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRef_direct() {
		Ref<List> ref1 = Ref.of(List.class);
		Ref<List> ref2 = Ref.of(List.class);
		assertNotEquals(ref1, ref2);
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRef_directWithDefault() {
		List      theList = new ArrayList();
		Ref<List> ref     = Ref.of(List.class, ()->theList);
		assertTrue(ref._get().isPresent());
		assertTrue(ref._get().filter(list->list == theList).isPresent());
	}
	
}
