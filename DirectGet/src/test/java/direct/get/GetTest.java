package direct.get;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class GetTest {

	@Test
	public void testGetBasic() {
		ArrayList<String> list = Get.a(ArrayList.class);
		assertNotNull(list);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProvidedWithAnotherType() {
		Get.a(CurrentContext.class)
			.provide(List.class).withA(ArrayList.class);
		
		List<String> list = Get.a(List.class);
		assertTrue(list instanceof List);
	}

}
