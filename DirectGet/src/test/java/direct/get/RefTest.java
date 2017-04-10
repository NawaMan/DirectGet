package direct.get;

import static org.junit.Assert.*;

import java.awt.List;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class RefTest {

	@Test
	public void testDirectRef_mustEqualsForTargetClass() {
		Ref<List> ref1 = Ref.ofClass(List.class);
		Ref<List> ref2 = Ref.ofClass(List.class);
		assertEquals(ref1, ref2);
		assertEquals(ref1.toString(), ref2.toString());
		assertEquals(ref1.hashCode(), ref2.hashCode());
		
		Set<Ref<?>> set = new HashSet<>();
		assertEquals(0, set.size());
		set.add(ref1);
		assertEquals(1, set.size());
		set.add(ref2);
		assertEquals(1, set.size());
	}

}
