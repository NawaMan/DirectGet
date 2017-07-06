package directget.get;

import static directget.get.Get.a;
import static directget.get.Get.the;
import static directget.get.Retain.retain;
import static directget.get.Run.OnNewThread;
import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import directget.get.Fork;
import directget.get.Get;
import directget.get.Ref;
import directget.get.Retain;
import directget.get.Run;
import lombok.val;

public class RetainTest {
    
    private static class StringList extends ArrayList<String> {
        @Override
        public String toString() {
            return super.toString();
        }
    };
    
    public static final String orgName = "nawaman";
    
    public static final String anotherName = new String("nawaman".getBytes());
    
    public static final String newName = "nwman";
    
    static final Ref<StringList> logs = Ref.of(StringList.class, retain(() -> new StringList()).forCurrentThread());
    
    static final Ref<String> username = Ref.of(String.class, () -> {
        return orgName;
    });
    
    static final Ref<Integer> usernameLength = Ref.of(Integer.class, Retain.valueOf(()->{
        a(logs).add("Calculate username length.");
        return a(username).length();
    }).forSame(username));
    
    @Test
    public void testRetainRef_same() {
        the(logs).clear();
        assertTrue(orgName.length() == a(usernameLength));
        assertEquals("[Calculate username length.]", the(logs).toString());
        
        assertTrue(orgName.length() == a(usernameLength));
        assertEquals("[Calculate username length.]", the(logs).toString());
        
        Run.with(username.providedWith(anotherName)).run(() -> {
            assertTrue(anotherName.length() == a(usernameLength));
            assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
            
            assertTrue(anotherName.length() == a(usernameLength));
            assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
        });
        
        Run.with(username.providedWith(newName)).run(() -> {
            assertTrue(newName.length() == a(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
            
            assertTrue(newName.length() == a(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
        });
        
        assertTrue(orgName.length() == Get.a(usernameLength));
        assertEquals(
                "[Calculate username length., Calculate username length., Calculate username length., Calculate username length.]",
                the(logs).toString());
    }
    
    @Test
    public void testRetainRef_equal() {
        With(usernameLength.providedBy(Retain.valueOf(()->{
            a(logs).add("Calculate username length.");
            return a(username).length();
        }).forEquivalent(username))).run(() -> {
            the(logs).clear();
            assertTrue(orgName.length() == a(usernameLength));
            assertEquals("[Calculate username length.]", the(logs).toString());
            
            assertTrue(orgName.length() == a(usernameLength));
            assertEquals("[Calculate username length.]", the(logs).toString());
            
            Run.with(username.providedWith(anotherName)).run(() -> {
                assertTrue(anotherName.length() == a(usernameLength));
                assertEquals("[Calculate username length.]", the(logs).toString());
                
                assertTrue(anotherName.length() == a(usernameLength));
                assertEquals("[Calculate username length.]", the(logs).toString());
            });
            
            Run.with(username.providedWith(newName)).run(() -> {
                assertTrue(newName.length() == a(usernameLength));
                assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
                
                assertTrue(newName.length() == a(usernameLength));
                assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
            });
            
            assertTrue(orgName.length() == Get.a(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_always() throws Throwable {
        With(logs.dictatedBy(Retain.valueOf(() -> new StringList()).always()))
        .run(()->{
            the(logs).clear();
            
            the(logs).add("log");
            assertEquals("[log]", the(logs).toString());
            
            the(logs).add("log");
            assertEquals("[log, log]", the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_never() throws Throwable {
        With(logs.dictatedBy(Retain.valueOf(() -> new StringList()).globally().never()))
        .run(()->{
            the(logs).clear();
            
            the(logs).add("log");
            assertEquals("[]", the(logs).toString());
            
            the(logs).add("log");
            assertEquals("[]", the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_thread() throws Throwable {
        the(logs).clear();
        
        val fork = new Fork();
        OnNewThread()
        .joinWith(fork)
        .start(() -> {
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
        With(logs.dictatedBy(Retain.valueOf(() -> new StringList()).globally().always()))
        .run(()->{
            the(logs).clear();
            
            val fork = new Fork();
            Run.onNewThread().joinWith(fork).inherit(logs).start(() -> {
                Thread.sleep(10);
                the(logs).add("log");
            });
            
            the(logs).add("log");
            
            fork.join();
            
            assertEquals("[log, log]", the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_time() throws Throwable {
        With(logs.dictatedBy(Retain.valueOf(() -> new StringList())
        .forTime(200, TimeUnit.MILLISECONDS)))
        .run(() -> {
            the(logs).clear();
            
            the(logs).add("log");
            assertEquals("[log]", the(logs).toString());
            
            Thread.sleep(100);
            
            the(logs).add("log");
            assertEquals("[log, log]", the(logs).toString());
            
            Thread.sleep(200);
            
            assertEquals("[]", the(logs).toString());
            
            the(logs).add("log");
            assertEquals("[log]", the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_expire() throws Throwable {
        With(logs.dictatedBy(Retain.valueOf(StringList::new).globally().expireAfter(200, TimeUnit.MILLISECONDS)))
        .run(()->{
            the(logs).clear();
            
            the(logs).add("log");
            assertEquals("[log]", the(logs).toString());
            
            Thread.sleep(100);
            
            the(logs).add("log");
            assertEquals("[log, log]", the(logs).toString());
            
            Thread.sleep(1000);
            
            assertEquals("[]", the(logs).toString());
            
            the(logs).add("log");
            assertEquals("[log]", the(logs).toString());
        });
    }
    
}