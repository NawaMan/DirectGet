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
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import directget.get.App;
import directget.get.Configuration;
import directget.get.Preferability;
import directget.get.Ref;
import directget.get.Scope;
import directget.get.exceptions.GetException;
import directget.get.supportive.Provider;
import directget.get.supportive.RefTo;

public class ProviderOrderTest {

    public static final String NOT_IMPLEMENT_YET = "NOT_IMPLEMENT_YET";
    
    // The order
    // Get parent dictate - when included
    // Get scope parent dictate
    // Get scope dictate
    // Get stack parent dictate
    // Get stack dictate
    // Get stack normal
    // Get scope normal
    // Get scope parent normal
    // Get parent normal - when included
    // Get stack default
    // Get scope default
    // Get scope parent default
    // Get parent default - when included
    // Ref default
    
    private final RefTo<String> ref = Ref.toValue("RefDefault");
    private final RefTo<String> refNoDefault = Ref.to(String.class);
    
    private final Provider<String> getParentDictate = new Provider<>(ref, Preferability.Dictate,
            () -> "GetParentDictate");
    private final Provider<String> scopeParentDictate = new Provider<>(ref, Preferability.Dictate,
            () -> "ScopeParentDictate");
    private final Provider<String> scopeDictate = new Provider<>(ref, Preferability.Dictate, () -> "ScopeDictate");
    private final Provider<String> stackDictate = new Provider<>(ref, Preferability.Dictate, () -> "StackDictate");
    
    private final Provider<String> getParentNormal = new Provider<>(ref, Preferability.Normal,
            () -> "GetParentNormal");
    private final Provider<String> scopeParentNormal = new Provider<>(ref, Preferability.Normal,
            () -> "ScopeParentNormal");
    private final Provider<String> scopeNormal = new Provider<>(ref, Preferability.Normal, () -> "ScopeNormal");
    private final Provider<String> stackNormal = new Provider<>(ref, Preferability.Normal, () -> "StackNormal");
    
    private final Provider<String> getParentDefault = new Provider<>(ref, Preferability.Default,
            () -> "GetParentDefault");
    private final Provider<String> scopeParentDefault = new Provider<>(ref, Preferability.Default,
            () -> "ScopeParentDefault");
    private final Provider<String> scopeDefault = new Provider<>(ref, Preferability.Default, () -> "ScopeDefault");
    private final Provider<String> stackDefault = new Provider<>(ref, Preferability.Default, () -> "StackDefault");
    
    private void doTest(Provider<String> _getParent, Provider<String> _scopeParent, Provider<String> _scope,
            Provider<String> _stack, String expected) {
        doTest(_getParent, _scopeParent, _scope, _stack, false, expected);
    }
    
    private void doTest(Provider<String> _getParent, Provider<String> _scopeParent, Provider<String> _scope,
            Provider<String> _stack, boolean isToInherit, String expected) {
        Scope appScope = App.scope;
        Scope parentScope = appScope.newSubScope(new Configuration(_scopeParent));
        Scope theScope = parentScope.newSubScope(new Configuration(_scope));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<AssertionError> assertErr = new AtomicReference<>();
        theScope.get().substitute(Stream.of(_getParent), () -> {
            try {
                @SuppressWarnings("rawtypes")
                List<Ref> list = isToInherit ? Arrays.asList(ref) : Collections.emptyList();
                theScope.get().runAsync(list, () -> {
                    try {
                        theScope.get().substitute(Stream.of(_stack), () -> {
                            String actual = theScope.get().the(ref);
                            try {
                                assertEquals(expected, actual);
                            } catch (AssertionError e) {
                                assertErr.set(e);
                            } finally {
                                latch.countDown();
                            }
                        });
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        AssertionError err = assertErr.get();
        if (err != null) {
            throw err;
        }
    }
    
    // == Documented tests ====================================================
    
    @Test
    public void test_forDictate_superHasPriority() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
    }
    
    @Test
    public void test_forNormal_subHasPriority() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void test_forDefault_subHasPriority() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
    }
    
    @Test
    public void test_forDefault_hasPriority_thenNull() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDefault");
    }
    
    @Test
    public void test_forNormal_hasPriority_thenDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void test_forDictate_hasPriority_thenNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
    }
    
    @Test
    public void test_ifNonSpecified_refDefaultIsReturned() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
    }
    
    @Test(expected = GetException.class)
    public void test_refDefault_withNoDefaultConstruction_exceptionIsThrown() {
        App.scope.get().the(Ref.of(List.class));
    }
    
    @Test
    public void test_byDefault_parentHasNoEffect_so_refDefault() {
        Provider<String> _getParent = scopeDictate;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
    }
    
    // == Characteristic tests ================================================
    
    // -- Default --
    
    @Test
    public void testRefDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
    }
    
    @Test
    public void testRefDefaultStackDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
    }
    
    @Test
    public void testScopeDefaultStackDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
    }
    
    @Test
    public void testScopeParentDefaultScopeDefaultStackDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
    }
    
    @Test
    public void testGetParentDefaultScopeParentDefaultScopeDefaultStackDefault() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDefault");
    }
    
    @Test
    public void testGetParentDefaultScopeParentDefaultScopeDefault() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDefault");
    }
    
    @Test
    public void testGetParentDefaultScopeParentDefault() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDefault");
    }
    
    @Test
    public void testGetParentDefault() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, "RefDefault");
    }
    
    @Test
    public void testGetParentDefault_borrowSpecifyRefs_includeChecked_fromParent() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, true, "GetParentDefault");
    }
    
    @Test
    public void testGetParentDefault_borrowSpecifyRefs_excludeChecked_fromParent() {
        Provider<String> _getParent = getParentDefault;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = null;
        doTest(_getParent, _scopeParent, _scope, _stack, false, "RefDefault");
    }
    
    // -- Normal --
    
    @Test
    public void testRefDefaultStackNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormalScopeParentNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormalScopeParentDefault() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeDefaultScopeParentNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentNormal");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeDefaultScopeParentNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeNormalScopeParentNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormalScopeParentormalGetParentNormal() {
        Provider<String> _getParent = getParentNormal;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormalScopeParentDefaultGetParentNormal() {
        Provider<String> _getParent = getParentNormal;
        Provider<String> _scopeParent = scopeParentDefault;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeNormal");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeDefaultScopeParentNormalGetParentNormal() {
        Provider<String> _getParent = getParentNormal;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentNormal");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeDefaultScopeParentNormalGetParentNormal() {
        Provider<String> _getParent = getParentNormal;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeDefault;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeNormalScopeParentNormalGetParentNormal() {
        Provider<String> _getParent = getParentNormal;
        Provider<String> _scopeParent = scopeParentNormal;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackNormal");
    }
    
    // -- Dictate --
    
    @Test
    public void testRefDefaultStackDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = null;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
    }
    
    @Test
    public void testRefDefaultStackNormalScopeDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackNormal;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeDictate");
    }
    
    @Test
    public void testRefDefaultStackDictateScopeNormal() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
    }
    
    @Test
    public void testRefDefaultStackDictateScopeDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = null;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, "StackDictate");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeNormalScopeParentDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentDictate;
        Provider<String> _scope = scopeNormal;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
    }
    
    @Test
    public void testRefDefaultStackDefaultScopeDictateScopeParentDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentDictate;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDefault;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
    }
    
    @Test
    public void testRefDefaultStackDictateScopeDictateScopeParentDictate() {
        Provider<String> _getParent = null;
        Provider<String> _scopeParent = scopeParentDictate;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, "ScopeParentDictate");
    }
    
    @Test
    public void testRefDefaultStackDictateScopeDictateScopeParentDictateGetParentDictate_includeParent() {
        Provider<String> _getParent = getParentDictate;
        Provider<String> _scopeParent = scopeParentDictate;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, true, "ScopeParentDictate");
    }
    
    @Test
    public void testRefDefaultStackDictateScopeDictateScopeParentDictateGetParentDictate_excludeParent() {
        Provider<String> _getParent = getParentDictate;
        Provider<String> _scopeParent = scopeParentDictate;
        Provider<String> _scope = scopeDictate;
        Provider<String> _stack = stackDictate;
        doTest(_getParent, _scopeParent, _scope, _stack, false, "ScopeParentDictate");
    }
    
    // == Non default ref value =======================================================================================

    
    private static final String REF_DICTATE = "RefDictate";
    private static final RefTo<String> dictatedRef = Ref.to(String.class).dictatedTo(REF_DICTATE);
    
    @Test
    public void testRefDictate() {
        assertEquals(REF_DICTATE, the(dictatedRef));
        
        Run.with(dictatedRef.butDictatedTo("SubstitueDictate")).run(()->{
            assertEquals(REF_DICTATE, the(dictatedRef));
        });
    }
    
    private static final String REF_NORMAL = "RefNormal";
    private static final RefTo<String> normalRef = Ref.to(String.class).providedWith(REF_NORMAL);
    
    @Test
    public void testRefNormal() {
        assertEquals(REF_NORMAL, the(normalRef));
        
        Run.with(normalRef.butDictatedTo("SubstitueDictate")).run(()->{
            assertEquals("SubstitueDictate", the(normalRef));
        });

        Run.with(normalRef.butProvidedWith("SubstitueNormal")).run(()->{
            assertEquals("SubstitueNormal", the(normalRef));
        });
    }
    
}
