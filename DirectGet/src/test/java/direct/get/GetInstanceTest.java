package direct.get;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GetInstanceTest {

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
		assertTrue(AppScope.get._a(aLIST).filter(list->list == theList).isPresent());
	}

}
