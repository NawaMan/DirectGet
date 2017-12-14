package directget.get.supportive;

import static directget.get.supportive.Caller.trace;
import static org.junit.Assert.*;

import org.junit.Test;

import directget.get.Ref;
import directget.get.Run;
import directget.get.supportive.Caller.Capture;
import lombok.val;

public class CallerUtilTest {
    
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
                = "boss    : directget.get.supportive.CallerUtilTest.testCaller(CallerUtilTest.java:39)\n"
                + "manager : directget.get.supportive.CallerUtilTest.testCaller(CallerUtilTest.java:39)\n" 
                + "employee: directget.get.supportive.CallerUtilTest.lambda$1(CallerUtilTest.java:23)";
        assertEquals(expected, methodBoss());
        // NOTE: I know this is fragile .. but it is very visual.
    }
    
    private static final Ref<String> hello = Ref.toValue("Hello");
    
    @Test
    public void testFieldCaller() {
        val callerTraceExpected = "directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43)";
        assertEquals(callerTraceExpected, hello.getCallerTrace());
        assertEquals(callerTraceExpected, hello.getProvider().getCallerTrace());
        
        val ref_toStringExpected = "RefTo<:java.lang.String>@(directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43))";
        assertEquals(ref_toStringExpected, hello.toString());
        
        val provider_toStringExpected = "Provider (Normal:RefTo<:java.lang.String>@(directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43))): Supplier(FromValue)@(directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43))";
        assertEquals(provider_toStringExpected, hello.getProvider().toString());
    }
    
    private static final Ref<String> hi = Ref.toValue("Hi")
            .dictatedTo("Hey!");

    @Test
    public void testFieldCaller_withRefModification() {
        val expected = "directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:59)";
        assertEquals(expected, hi.getCallerTrace());
        assertEquals(expected, hi.getProvider().getCallerTrace());
    }

    @Test
    public void testFieldCaller_withSubstitution() {
        val hey = hello.butDictatedTo("Hey");
        val refExpected      = "directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43)";
        val providerExpected = "directget.get.supportive.CallerUtilTest.testFieldCaller_withSubstitution(CallerUtilTest.java:70)";
        Run.with(hey).run(()->{
            assertEquals(refExpected,      hey.getRef().getCallerTrace());
            assertEquals(providerExpected, hey.getCallerTrace());
        });
    }

    @Test
    public void testFieldCaller_withSubstitution_andSubstitue() {
        val hey = hello.butDictatedTo("Hey");
        val ho  = hey.butWith("Ho");
        val refExpected      = "directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43)";
        val providerExpected = "directget.get.supportive.CallerUtilTest.testFieldCaller_withSubstitution_andSubstitue(CallerUtilTest.java:82)";
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
        val refExpected      = "directget.get.supportive.CallerUtilTest.<clinit>(CallerUtilTest.java:43)";
        val providerExpected = "directget.get.supportive.CallerUtilTest.testFieldCaller_withSubstitution_withRetainer(CallerUtilTest.java:95)";
        Run.with(hey).run(()->{
            assertEquals(refExpected,      hey.getRef().getCallerTrace());
            assertEquals(providerExpected, hey.getCallerTrace());
        });
    }
    
}
