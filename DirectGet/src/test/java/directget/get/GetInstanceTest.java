//  ========================================================================
//  Copyright (c) 2017 The Direct Solution Software Builder.
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static directget.get.Preferability.Dictate;
import static directget.get.Run.*;
import static java.lang.Thread.*;

import org.junit.Test;

import directget.get.App;
import directget.get.Fork;
import directget.get.Get;
import directget.get.Named;
import directget.get.Preferability;
import directget.get.Providing;
import directget.get.Ref;
import directget.get.Run;
import lombok.val;

public class GetInstanceTest implements Named.User {
    
    private CountDownLatch latch = new CountDownLatch(1);
    
    private String orgText = "The Text";
    private String newText = "New Text!!!";
    
    private Ref<String> _text_ = Ref.of("TheText", String.class, supplier("OrginalText", () -> orgText));
    
    private Stream<Providing> provideNewText = Stream
            .of(new Providing<>(_text_, Dictate, supplier("NewText", () -> newText)));
    
    @Test
    public void testBasic() {
    	StringBuffer buffer = Get._a(StringBuffer.class).orElse(null);
        assertNotNull(buffer);
    }
    
    @Test
    public void testRef() {
    	StringBuffer theBuffer = new StringBuffer();
        Ref<StringBuffer> aBuffer = Ref.of("aList", StringBuffer.class, () -> theBuffer);
        assertTrue(App.Get()._a(aBuffer).filter(buffer -> buffer == theBuffer).isPresent());
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
            assertEquals(newText, Get.a(_text_));
        });
    }
    
    private final Run.Wrapper _newText = runnable -> () -> {
        App.Get().substitute(provideNewText, runnable);
    };
    
    private final Run.Wrapper _verboseLogger = runnable -> Named.runnable("VERBOSE", () -> {
        List providings = new ArrayList();
        Preferability.DetermineProvidingListener listener = new Preferability.DetermineProvidingListener() {
            @Override
            public <T> void onDetermine(Ref<T> ref, String from, Providing<T> result,
                    Supplier<String> stackTraceSupplier, Supplier<String> xraySupplier) {
                String str = "Get(" + ref + ") = " + result + "\nXRay " + xraySupplier.get() + " on => "
                        + Thread.currentThread().toString() + " {\n" + stackTraceSupplier.get() + "\n}";
                System.out.println(str);
            }
        };
        providings.add(new Providing(Preferability._Listener_, Preferability.Dictate, () -> listener));
        App.Get().substitute(providings.stream(), runnable);
    });
    
    @Test
    public void testRunNewThread_notInherit() throws Throwable {
        val fork = new Fork();
        
        With(_newText)
        .onNewThread()
        .inheritNone()
        .joinWith(fork)
        .run(()->{
            assertEquals(orgText, Get.a(_text_));
        });
        
        fork.join();
    }
    
    @Test
    public void testRunNewThread_inherit() throws Throwable {
        val fork = new Fork();
        
        Run.with(_text_.providedWith(newText))
        .and.with(_verboseLogger)
        .and.onNewThread()
        .inheritAll()
        .joinWith(fork)
        .start(()->{
        	assertEquals(newText, Get.a(_text_));
        });
        
        fork.join();
    }
    
}
