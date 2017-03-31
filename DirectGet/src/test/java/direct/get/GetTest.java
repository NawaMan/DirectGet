package direct.get;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProvidedWithFixInstance() {
		List<String> orgList = Arrays.asList("Hello", "World");
		
		Get.a(CurrentContext.class)
			.provide(List.class).with(orgList);
		
		List<String> list = Get.a(List.class);
		assertEquals(orgList, list);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProvidedBySupplier() {
		Get.a(CurrentContext.class)
			.provide(List.class).by(()->Arrays.asList("Hello", "World"));
		
		List<String> list1 = Get.a(List.class);
		List<String> list2 = Get.a(List.class);
		assertEquals(list1, list2);
		assertNotSame(list1, list2);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProvidedByFunction() {
		Get.a(CurrentContext.class)
			.provide(List.class).by(clzz->Arrays.asList("Hello", "World"));
		
		List<String> list1 = Get.a(List.class);
		List<String> list2 = Get.a(List.class);
		assertEquals(list1, list2);
		assertNotSame(list1, list2);
	}

}
