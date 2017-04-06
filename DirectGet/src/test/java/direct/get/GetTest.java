package direct.get;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

public class GetTest {

	@Test
	public void testGetBasic() {
		@SuppressWarnings("unchecked")
		ArrayList<String> list = (ArrayList<String>)Get.a(ArrayList.class);
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
	
	@Test
	public void testDifferentThredHasDifferentGet() throws InterruptedException {
		Get thisGet = Get.a(Get.class);
		AtomicReference<Get> getRef = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);
		
		new Thread(()->{
			getRef.set(Get.a(Get.class));
			latch.countDown();
		}).start();
		
		latch.await();
		Assert.assertNotEquals(thisGet,  getRef.get());
	}
	
	@Test
	public void testGlobalGetAreSameEveryThread() throws InterruptedException {
		Get thisGlobalGet = Get.a(Get.$GLOBAL_GET);
		AtomicReference<Get> globalGetRef = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);
		
		new Thread(()->{
			globalGetRef.set(Get.a(Get.$GLOBAL_GET));
			latch.countDown();
		}).start();
		
		latch.await();
		Assert.assertSame(thisGlobalGet,  globalGetRef.get());
	}
	
	@Test
	public void testByDefaultGlobalGetIsParentOfEveryGet() throws InterruptedException {
		Get thisParentGet = Get.a(Get.class).getParentGet().get();
		AtomicReference<Get> thatParentGetRef = new AtomicReference<>();
		CountDownLatch latch = new CountDownLatch(1);
		
		new Thread(()->{
			thatParentGetRef.set(Get.a(Get.class).getParentGet().get());
			latch.countDown();
		}).start();
		
		latch.await();
		
		Get globalGet = Get.a(Get.$GLOBAL_GET);
		Assert.assertSame(thisParentGet,          globalGet);
		Assert.assertSame(thatParentGetRef.get(), globalGet);
	}

}
