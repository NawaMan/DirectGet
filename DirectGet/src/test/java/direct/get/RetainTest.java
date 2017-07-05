package direct.get;

import static direct.get.Get.a;
import static direct.get.Get.the;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lombok.val;

public class RetainTest {
	
	private static class StringList extends ArrayList<String> {
		@Override
		public String toString() {
			return super.toString();
		}
	};
	
	public static final String orgName = "nawaman";
	
	public static final String newName = "nwman";
	
	static final Ref<StringList> logs= Ref.of(StringList.class, Retain.valueFrom(()->new StringList()).forCurrentThread());

	static final Ref<String> username = Ref.of(String.class, ()->{
		return orgName;
	});
	
	static final Ref<Integer> usernameLength = Ref.of(Integer.class, Retain.valueFrom(()->{
		a(logs).add("Calculate username length.");
		return a(username).length();
	}).forSame(username));
	
	@Test
	public void testRetainRef() {
		the(logs).clear();
		assertTrue(orgName.length() == a(usernameLength));
		assertEquals("[Calculate username length.]", the(logs).toString());
		
		assertTrue(orgName.length() == a(usernameLength));
		assertEquals("[Calculate username length.]", the(logs).toString());
		
		Run.with(username.providedWith(newName)).run(()->{
			assertTrue(newName.length() == a(usernameLength));
			assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
		});
		
		assertTrue(orgName.length() == Get.a(usernameLength));
		assertEquals("[Calculate username length., Calculate username length., Calculate username length.]", the(logs).toString());
	}
	
	@Test
	public void testRetain_thread() throws Throwable {
		the(logs).clear();
		
		val fork = new Fork();
		Run.onNewThread().joinWith(fork).start(()->{
			Thread.sleep(100);
			the(logs).add("log");
			assertEquals("[log]", the(logs).toString());
			
			Thread.sleep(100);
			the(logs).add("log");
			assertEquals("[log, log]", the(logs).toString());
		});

		Thread.sleep(100);
		the(logs).add("log");
		assertEquals("[log]", the(logs).toString());
		
		Thread.sleep(100);
		the(logs).add("log");
		assertEquals("[log, log]", the(logs).toString());
		fork.join();
	}
	
	@Test
	public void testRetain_globally() throws Throwable {
		Run
		.with(logs.dictatedBy(Retain.valueFrom(()->new StringList()).globally().always()))
		.run(()->{

			the(logs).clear();
			
			val fork = new Fork();
			Run
			.onNewThread()
			.joinWith(fork)
			.inherit(logs)
			.start(()->{
				Thread.sleep(100);
				the(logs).add("log");
			});
			
			Thread.sleep(100);
			the(logs).add("log");
			
			fork.join();
			
			assertEquals("[log, log]", the(logs).toString());
		});
	}

}
