package directget.get.supportive;

import static directget.get.supportive.Caller.trace;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import directget.get.Ref;
import directget.get.Run;
import directget.get.supportive.Caller.Capture;
import lombok.val;

public class CallerTest {
    
    public static String methodBoss() {
        return trace(Capture.Continue, trace->{
            return "boss    : " + trace + "\n" + methodManager();
        });
    }
    
    public static String methodManager() {
        return trace(Capture.Pause, trace->{
            return "manager : " + trace + "\n" + methodEmployee();
        });
    }
    
    public static String methodEmployee() {
        return trace(Capture.Continue, trace->{
            return "employee: " + trace + "";
        });
    }
    
    @Test
    public void testCaller() {
        val expected
                = "boss    : CallerTest.java:39\n"
                + "manager : CallerTest.java:39\n" 
                + "employee: CallerTest.java:23";
        assertEquals(expected, methodBoss()
                                .replaceAll("directget\\.get\\.supportive\\.CallerTest\\..*\\(", "")
                                .replaceAll("\\)",                                               ""));
        // NOTE: I know this is fragile .. but it is very visual.
    }
    
    private static final Ref<String> hello = Ref.toValue("Hello");
    
    @Test
    public void testFieldCaller() {
        val callerTraceExpected = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)";
        assertEquals(callerTraceExpected, hello.getCallerTrace());
        assertEquals(callerTraceExpected, hello.getProvider().getCallerTrace());
        
        val ref_toStringExpected = "RefTo<:java.lang.String> at {\ndirectget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)\n}";
        assertEquals(ref_toStringExpected, hello.toString());
        
        val provider_toStringExpected = "Provider { \n" + 
                "Normal                                                           <--- preferability\n" + 
                "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45) <--- Ref<:class java.lang.String>\n" + 
                "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45) <--- Supplier(FromValue)\n" + 
                "}";
        assertEquals(provider_toStringExpected, hello.getProvider().toString());
    }
    
    private static final Ref<String> hi = Ref.toValue("Hi")
            .dictatedTo("Hey!");

    @Test
    public void testFieldCaller_withRefModification() {
        val expected = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:65)";
        assertEquals(expected, hi.getCallerTrace());
        assertEquals(expected, hi.getProvider().getCallerTrace());
    }

    @Test
    public void testFieldCaller_withSubstitution() {
        val hey = hello.butDictatedTo("Hey");
        val refExpected      = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)";
        val providerExpected = "directget.get.supportive.CallerTest.testFieldCaller_withSubstitution(CallerTest.java:76)";
        Run.with(hey).run(()->{
            assertEquals(refExpected,      hey.getRef().getCallerTrace());
            assertEquals(providerExpected, hey.getCallerTrace());
        });
    }

    @Test
    public void testFieldCaller_withSubstitution_andSubstitue() {
        val hey = hello.butDictatedTo("Hey");
        val ho  = hey.butWith("Ho");
        val refExpected      = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)";
        val providerExpected = "directget.get.supportive.CallerTest.testFieldCaller_withSubstitution_andSubstitue(CallerTest.java:88)";
        Run.with(hey).run(()->{
            assertEquals(refExpected,      ho.getRef().getCallerTrace());
            assertEquals(providerExpected, ho.getCallerTrace());
        });
    }

    @Test
    public void testFieldCaller_withSubstitution_withRetainer() {
        val hey = hello
                .butDictatedTo("Hey")
                .retained().singleton();
        val refExpected      = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)";
        val providerExpected = "directget.get.supportive.CallerTest.testFieldCaller_withSubstitution_withRetainer(CallerTest.java:101)";
        Run.with(hey).run(()->{
            assertEquals(refExpected,      hey.getRef().getCallerTrace());
            assertEquals(providerExpected, hey.getCallerTrace());
        });
    }
    
    private Ref<String> instanceRef = Ref.toValue("Instance");

    @Test
    public void testTrace_instanceField() {
        val hey = instanceRef;
        val refExpected      = "directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)";
        val trace = new Caller.Trace(instanceRef.getCallerTrace());
        assertEquals(CallerTest.class.getCanonicalName(), trace.className());
        assertEquals("<init>", trace.methodName());
        assertTrue(trace.isInstanceInit());
        assertEquals("CallerTest.java", trace.fileName());
        assertEquals(111, trace.lineNumber());
    }
    
}
