package directget.get.supportive;

import java.util.ArrayList;
import java.util.List;

import directcommon.common.Nulls;
import directget.get.run.Failable;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * @author manusitn
 *
 */
@ExtensionMethod({ Nulls.class })
public class Caller {
    
    /**
     * The enum indicating whether or not the capture should be continued or it should be paused.
     */
    public static enum Capture {
        /** Pause */    Pause,
        /** Continue */ Continue;
    }
    
    public static class Trace {
        private final String traceString;
        private int   classNameEnded  = -1;
        private int   methodNameEnded = -1;
        private int   fileNameEnded   = -1;
        public Trace(String traceString) {
            this.traceString = traceString;
        }
        // directget.get.supportive.CallerTest.<clinit>(CallerTest.java:45)
        //                                    ^        ^               ^
        //                                    |        |               +-- FileNameEnded
        //                                    |        +------------------ MethodEnded
        //                                    +--------------------------- ClassEnded
        private void process() {
            if (classNameEnded != -1)
                return;
            synchronized (this) {
                if (classNameEnded != -1)
                    return;
                methodNameEnded = traceString.indexOf('(');
                classNameEnded  = traceString.lastIndexOf('.', methodNameEnded);
                fileNameEnded   = traceString.lastIndexOf(':');
            }
        }
        public String className() {
            process();
            return traceString.substring(0, classNameEnded);
        }
        public String methodName() {
            process();
            return traceString.substring(classNameEnded + 1, methodNameEnded);
        }
        public boolean isClassInit() {
            return methodName().equals("<clinit>");
        }
        public boolean isInstanceInit() {
            return methodName().equals("<init>");
        }
        public String fileName() {
            process();
            return traceString.substring(methodNameEnded + 1, fileNameEnded);
        }
        public String lineNumberString() {
            process();
            return traceString.substring(fileNameEnded, traceString.length() - 1);
        }
        public int lineNumber() {
            return Integer.parseInt(lineNumberString());
        }
    }
    
    
    private static ThreadLocal<List<String>> callerTrace = ThreadLocal.withInitial(()->new ArrayList<>(10));
    static {
        callerTrace.get().add(null);
    }
    
    private static String last() {
        val list = callerTrace.get();
        if (list.isEmpty()) {
            callerTrace.get().add(null);
            return null;
        }
        return list.get(0);
    }
    
    /**
     * Trace the caller.
     * 
     * @param continueCapture  should the tracing be continue to the body?
     * @param body             the code to run if we want to pass on the parsing.
     * @return  the value returned by the body.
     * @throws T  the exception thrown by the value.
     */
    public static <V, T extends Throwable> V trace(Capture continueCapture, Failable.Function<String, V, T> body) throws T {
        String  trace   = last();
        boolean isAdded = false;
        if (continueCapture == Capture.Pause) {
            callerTrace.get().add(0, null);
        } else {
            if (trace.isNull()) {
                val stackTrace = Thread.currentThread().getStackTrace();
                val length     = stackTrace.length;
                val index      = Math.min(length - 1, 3);
                trace = stackTrace[index].toString();
    
                val list = callerTrace.get();
                list.set(0, trace);
                isAdded = true;
            }
        }
        try {
            return body.apply(trace);
        } finally {
            if (continueCapture == Capture.Pause) {
                callerTrace.get().remove(0);
            } else {
                if (isAdded) 
                    callerTrace.get().set(0, null);
            }
        }
    }
    
}
