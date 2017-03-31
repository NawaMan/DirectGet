package direct.get;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class GetTest {

	@Test
	public void test() {
		@SuppressWarnings("unchecked")
		ArrayList<String> list = Get.a(ArrayList.class);
		assertNotNull(list);
	}

}
