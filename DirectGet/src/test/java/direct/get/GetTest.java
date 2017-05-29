package direct.get;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GetTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testBasic() {
		ArrayList<String> list = AppSpace.get._a(ArrayList.class).orElse(null);
		assertNotNull(list);
	}
	
	@Test
	@SuppressWarnings("rawtypes")
	public void testRef() {
		List      theList = new ArrayList();
		Ref<List> aLIST   = Ref.of(List.class, ()->theList);
		assertTrue(AppSpace.get._a(aLIST).filter(list->list == theList).isPresent());
	}

}
