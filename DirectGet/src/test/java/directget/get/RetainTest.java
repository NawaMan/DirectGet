//  ========================================================================
//  Copyright (c) 2017 Nawapunth Manusitthipol.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
package directget.get;

import static directget.get.Get.the;
import static directget.get.Run.Asynchronously;
import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import directget.get.run.Fork;
import directget.get.supportive.RefTo;
import directget.get.supportive.retain.Retainers;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Retainers.class })
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
    
    static final RefTo<StringList> logs = Ref.to(StringList.class).defaultedToBy(StringList::new).retained().forCurrentThread();
    
    static final RefTo<String> username = Ref.toValue(orgName);
    
    static final RefTo<Integer> usernameLength = Ref.to(Integer.class).defaultedToBy(()->{
        the(logs).add("Calculate username length.");
        return the(username).length();
    }).retained().forSame(username);
    
    @Test
    public void testRetainRef_same() {
        the(logs).clear();
        assertTrue(orgName.length() == the(usernameLength));
        assertEquals("[Calculate username length.]", the(logs).toString());
        
        assertTrue(orgName.length() == the(usernameLength));
        assertEquals("[Calculate username length.]", the(logs).toString());
        
        Run.with(username.butProvidedWith(anotherName)).run(() -> {
            assertTrue(anotherName.length() == the(usernameLength));
            assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
            
            assertTrue(anotherName.length() == the(usernameLength));
            assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
        });
        
        Run.with(username.butProvidedWith(newName)).run(() -> {
            assertTrue(newName.length() == the(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
            
            assertTrue(newName.length() == the(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
        });
        
        assertTrue(orgName.length() == Get.the(usernameLength));
        assertEquals(
                "[Calculate username length., Calculate username length., Calculate username length., Calculate username length.]",
                the(logs).toString());
    }
    
    @Test
    public void testRetainRef_equal() {
        With(usernameLength
            .butProvidedBy(()->{
                the(logs).add("Calculate username length.");
                return the(username).length();
            })
            .retained().forEquivalent(username)
        )
        .run(() -> {
            the(logs).clear();
            assertTrue(orgName.length() == the(usernameLength));
            assertEquals("[Calculate username length.]", the(logs).toString());
            
            assertTrue(orgName.length() == the(usernameLength));
            assertEquals("[Calculate username length.]", the(logs).toString());
            
            Run.with(username.butProvidedWith(anotherName)).run(() -> {
                assertTrue(anotherName.length() == the(usernameLength));
                assertEquals("[Calculate username length.]", the(logs).toString());
                
                assertTrue(anotherName.length() == the(usernameLength));
                assertEquals("[Calculate username length.]", the(logs).toString());
            });
            
            Run.with(username.butProvidedWith(newName)).run(() -> {
                assertTrue(newName.length() == the(usernameLength));
                assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
                
                assertTrue(newName.length() == the(usernameLength));
                assertEquals("[Calculate username length., Calculate username length.]", the(logs).toString());
            });
            
            assertTrue(orgName.length() == the(usernameLength));
            assertEquals("[Calculate username length., Calculate username length., Calculate username length.]",
                    the(logs).toString());
        });
    }
    
    @Test
    public void testRetain_always() throws Throwable {
        With(logs.butDictatedBy(StringList::new).retained().forAlways())
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
        With(logs.butDictatedBy(StringList::new).retained().globally().forNever())
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
        Asynchronously()
        .joinWith(fork)
        .run(() -> {
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
        With(logs.butDictatedBy(StringList::new).retained().globally().forAlways())
        .run(()->{
            the(logs).clear();
            
            val fork = new Fork();
            Asynchronously()
            .joinWith(fork)
            .inherit(logs)
            .run(() -> {
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
        With(logs.butDictatedBy(StringList::new).retained().forTime(200, TimeUnit.MILLISECONDS))
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
    public void testRetain_but() {
        val counter = Ref.to(AtomicInteger.class).defaultedToBy(()->new AtomicInteger()).retained().globally().forAlways();
        val ref     = Ref.to(String.class).defaultedToBy(()->"Value#" + the(counter).getAndIncrement());
        assertEquals("Value#0", the(ref).toString());
        assertEquals("Value#1", the(ref).toString());
        
        Asynchronously().run(()->{
            assertEquals("Value#2", the(ref).toString());
        });
        
        With(counter.butDictate().retained().forCurrentThread())
        .run(()->{
            assertEquals("Value#0", the(ref).toString());
        });
        
        assertEquals("Value#3", the(ref).toString());
    }
    
    
}
