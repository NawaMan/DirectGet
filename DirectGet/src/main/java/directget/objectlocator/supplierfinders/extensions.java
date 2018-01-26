package directget.objectlocator.supplierfinders;

import static java.util.Arrays.stream;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

import lombok.val;

class extensions {

    /**
     * Check if the an annotation is has the simple name as the one given 
     * @param name  the name expected.
     * @return  the predicate to check if annotation is with the given name.
     **/
    public static Predicate<? super Annotation> named(String name) {
        return annotation->{
            val toString = annotation.toString();
            return toString.matches("^@.*(\\.|\\$)" + name + "\\(.*$");
        };
    }
    
    public static <T> boolean hasAnnotation(Annotation[] annotations, String name) {
        return stream(annotations)
                .anyMatch(named(name));
    }
    
    
}
