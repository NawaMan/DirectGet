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

import static directget.get.Preferability.Dictate;
import static directget.get.Run.With;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Test;

import directcommon.common.Nulls;
import directget.get.run.Fork;
import directget.get.run.Named;
import directget.get.run.Wrapper;
import directget.get.supportive.Provider;
import directget.get.supportive.RefTo;
import lombok.val;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ Nulls.class })
public class GetInstanceTest implements Named.User {
    
    private CountDownLatch latch = new CountDownLatch(1);
    
    private String orgText = "The Text";
    private String newText = "New Text!!!";
    
    private RefTo<String> _text_ = Ref.to("TheText", String.class).defaultedTo(orgText);
    
    private Stream<Provider> provideNewText = Stream
            .of(new Provider<>(_text_, Dictate, supplier("NewText", () -> newText)));
    
    @Test
    public void testBasic() {
    	StringBuffer buffer = Get.the(StringBuffer.class);
        assertNotNull(buffer);
    }
    
    @Test
    public void testRef() {
    	StringBuffer theBuffer = new StringBuffer();
    	RefTo<StringBuffer> aBuffer = Ref.to("aList", StringBuffer.class).defaultedTo(theBuffer);
        assertTrue(App.Get().the(aBuffer).whenNotNull().filter(buffer -> buffer == theBuffer).isPresent());
    }
    
    private void join() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testRunSubstitution() {
        Run.with(_newText).run(() -> {
            assertEquals(newText, Get.the(_text_));
        });
    }
    
    private final Wrapper _newText = runnable -> () -> {
        App.Get().substitute(provideNewText, runnable);
    };
    
    private final Wrapper _verboseLogger = runnable -> Named.runnable("VERBOSE", () -> {
        List providers = new ArrayList();
        Preferability.DetermineProviderListener listener = new Preferability.DetermineProviderListener() {
            @Override
            public <T> void onDetermine(Ref<T> ref, String from, Provider<T> result,
                    Supplier<String> stackTraceSupplier, Supplier<String> xraySupplier) {
                String str = "Get(" + ref + ") = " + result + "\nXRay " + xraySupplier.get() + " on => "
                        + Thread.currentThread().toString() + " {\n" + stackTraceSupplier.get() + "\n}";
                System.out.println(str);
            }
        };
        providers.add(new Provider(Preferability.DefaultListener, Preferability.Dictate, () -> listener));
        App.Get().substitute(providers.stream(), runnable);
    });
    
    @Test
    public void testRunNewThread_notInherit() throws Throwable {
        val fork = new Fork();
        
        With(_newText)
        .asynchronously()
        .inheritNone()
        .joinWith(fork)
        .run(()->{
            assertEquals(orgText, Get.the(_text_));
        });
        
        fork.join();
    }
    
    @Test
    public void testRunNewThread_inherit() throws Throwable {
        val fork = new Fork();
        
        Run
        .with(_text_.butProvidedWith(newText))
        .with(_verboseLogger)
        .asynchronously()
        .inheritAll()
        .joinWith(fork)
        .run(()->{
            assertEquals(newText, Get.the(_text_));
        });
        
        fork.join();
    }
    
}
