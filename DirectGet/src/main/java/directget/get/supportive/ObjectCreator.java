package directget.get.supportive;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import directget.get.Get;
import directget.get.Ref;
import directget.get.exceptions.AbstractClassCreationException;
import directget.get.exceptions.CreationException;
import directget.get.exceptions.CyclicDependencyDetectedException;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

/**
 * This utility class can create an object using Get.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class, ObjectCreator.extensions.class })
public class ObjectCreator {
    
    // TODO - Should create interface with all default method.
    // TODO - Should check for @NotNull
    
    private static Predicate<? super Field> isFieldStatic = field->Modifier.isStatic(field.getModifiers());
    private static Predicate<? super Field> isFieldPublic = field->isPublic(field.getModifiers());
    
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
    
    /** Check if the an annotation is an Nullable annotation */
    public static Predicate<? super Annotation> anNullableAnnotation
            = annotation->{
                val toString = annotation.toString();
                return toString.matches("^@.*\\.Nullal\\(.*$");
            };
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class, Supplier>    suppliers     = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    private static final ThreadLocal<Set<Class>> beingCreateds = ThreadLocal.withInitial(()->new HashSet<>());
    
    
    /**
     * Find the {@code java.inject.Inject} class by name.
     * @return the class if found or {@code null} if not.
     */
    static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Create an instance of the given class.
     * 
     * @param theGivenClass
     * @return the created value.
     * @throws CreationException when there is a problem creating the object.
     * @throws CyclicDependencyDetectedException when cyclic dependency is detected.
     */
    @SuppressWarnings("rawtypes")
    public static <T> T createNew(@NonNull Class<T> theGivenClass)
            throws CreationException, CyclicDependencyDetectedException {
        val set = beingCreateds.get();
        if (set.contains(theGivenClass))
            throw new CyclicDependencyDetectedException(theGivenClass);
        
        try {
            set.add(theGivenClass);
            
            try {
                Supplier supplier = suppliers.get(theGivenClass);
                if (supplier.isNull()) {
                    supplier = newSupplierFor(theGivenClass);
                    if (supplier.isNotNull())
                        suppliers.put(theGivenClass, supplier);
                }
                
                val instance = supplier.get();
                return theGivenClass.cast(instance);
            } catch (CyclicDependencyDetectedException e) {
                throw e;
            } catch (Throwable e) {
                throw new CreationException(theGivenClass, e);
            }
        } finally {
            set.remove(theGivenClass);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Supplier newSupplierFor(Class<T> theGivenClass) throws NoSuchMethodException {
        if (theGivenClass.getAnnotations().hasAnnotation("DefaultImplementation")) {
            val defaultImplementationClass = findDefaultImplementation(theGivenClass);
            if (defaultImplementationClass.isNotNull())
                return ()->Get.the(defaultImplementationClass);
            return ()->null;
        }
        
        if (theGivenClass.isInterface()
         && theGivenClass.getAnnotations().hasAnnotation("DefaultInterface")) {
            // TODO Implement this.
            return ()->null;
        }
        
        if (theGivenClass.getAnnotations().hasAnnotation("DefaultToNull"))
            return ()->null;
        
        if (theGivenClass.isEnum()) {
            val enumValue = findDefaultEnumValue(theGivenClass);
            return ()->enumValue;
        }
        
        val defaultRef = Ref.findDefaultRefOf(theGivenClass);
        if (defaultRef.isNotNull())
            return ()->defaultRef.asSupplier().get();
        
        val fieldValue = findValueFromSingletonField(theGivenClass);
        if (fieldValue.isNotNull())
            return fieldValue;
        
        val methodValue = findValueFromFactoryMethod(theGivenClass);
        if (methodValue.isNotNull())
            return methodValue;
        
        val constructor = findConstructor(theGivenClass);
        if (constructor.isNotNull()) {
            val supplier = (Supplier)(()->{
                val params   = getParameters(constructor);
                val instance = constructor.newInstance(params);
                // TODO - Change to use method handle later.
                return theGivenClass.cast(instance);
            });
            return supplier;
        }
        
        if (Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    private static <T> Class findDefaultImplementation(Class<T> theGivenClass) {
        return stream(theGivenClass.getAnnotations())
            .map(Object::toString)
            .map(toString->toString.replaceAll("^(.*\\(value=)(.*)(\\))$", "$2"))
            .map(ObjectCreator::findClass)
            .filter(Objects::nonNull)
            .filter(theGivenClass::isAssignableFrom)
            .findAny()
            .orElse(null);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Supplier findValueFromSingletonField(Class<T> theGivenClass) {
        return (Supplier)stream(theGivenClass.getDeclaredFields())
                .filter(isFieldStatic)
                .filter(isFieldPublic)
                .filter(field->ObjectCreator.extensions.hasAnnotation(field.getAnnotations(), "Default"))
                .map(field->{
                    val type = field.getType();
                    if (theGivenClass.isAssignableFrom(type))
                        return (Supplier)(()->field.get(theGivenClass));
                    
                    if (Optional.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)field.getGenericType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        
                        if (theGivenClass.isAssignableFrom(actualType))
                            return (Supplier)(()->((Optional)field.get(theGivenClass)).orElse(null));
                    }
                    
                    if (java.util.function.Supplier.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)field.getGenericType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        
                        if (theGivenClass.isAssignableFrom(actualType))
                            return (Supplier)()->((java.util.function.Supplier)field.get(theGivenClass)).get();
                    }
                    
                    return null;
                })
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T> Supplier findValueFromFactoryMethod(Class<T> theGivenClass) {
        return (Supplier)stream(theGivenClass.getDeclaredMethods())
                .filter(method->Modifier.isStatic(method.getModifiers()))
                .filter(method->Modifier.isPublic(method.getModifiers()))
                .filter(method->ObjectCreator.extensions.hasAnnotation(method.getAnnotations(), "Default"))
                .map(method->{
                    val type = method.getReturnType();
                    if (theGivenClass.isAssignableFrom(type)) {
                        return (Supplier)(()->{
                            val params = ObjectCreator.getMethodParameters(method);
                            val value = method.invoke(theGivenClass, params);
                            return value;
                        });
                    }
                    
                    if (Optional.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)method.getGenericReturnType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        
                        if (theGivenClass.isAssignableFrom(actualType)) {
                            return (Supplier)(()->{
                                val params = ObjectCreator.getMethodParameters(method);
                                val value = method.invoke(theGivenClass, params);
                                return ((Optional)value).orElse(null);
                            });
                        }
                    }
                    
                    if (java.util.function.Supplier.class.isAssignableFrom(type)) {
                        val parameterizedType = (ParameterizedType)method.getGenericReturnType();
                        val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
                        val getMethod         = ObjectCreator.getGetMethod();
                        
                        if (theGivenClass.isAssignableFrom(actualType)) {
                            return (Supplier)()->{
                                val params   = ObjectCreator.getMethodParameters(method);
                                val result   = method.invoke(theGivenClass, params);
                                val value    = getMethod.invoke(result);
                                return value;
                            };
                        }
                    }
                    
                    return null;
                })
                .findAny()
                .orElse(null);
    }
    
    private static Method getGetMethod() {
        try {
            // TODO - Change to use MethodHandler.
            return java.util.function.Supplier.class.getMethod("get", new Class[0]);
        } catch (NoSuchMethodException | SecurityException e) {
            // I am sure it is there.
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static <T> T findDefaultEnumValue(Class<T> theGivenClass) {
        T[] enumConstants = theGivenClass.getEnumConstants();
        if (enumConstants.length == 0)
            return null;
        return stream(enumConstants)
                .filter(value->{
                    val name = ((Enum)value).name();
                    try {
                        return ObjectCreator.extensions.hasAnnotation(theGivenClass.getField(name).getAnnotations(), "Default");
                    } catch (NoSuchFieldException | SecurityException e) {
                        throw new CreationException(theGivenClass, e);
                    }
                })
                .findAny()
                .orElse(enumConstants[0]);
    }
    
    @SuppressWarnings({ "rawtypes" })
    private static Object[] getParameters(Constructor constructor) {
        val paramsArray = constructor.getParameters();
        val params = new Object[paramsArray.length];
        for (int i = 0; i < paramsArray.length; i++) {
            val param             = paramsArray[i];
            val paramType         = param.getType();
            val parameterizedType = param.getParameterizedType();
            boolean isNullable    = param.getAnnotations().hasAnnotation("Nullable");
            Object paramValue     = getParameterValue(paramType, parameterizedType, isNullable);
            params[i] = paramValue;
        }
        return params;
    }
    
    private static Object[] getMethodParameters(Method method) {
        val paramsArray = method.getParameters();
        val params = new Object[paramsArray.length];
        for (int i = 0; i < paramsArray.length; i++) {
            val param             = paramsArray[i];
            val paramType         = param.getType();
            val parameterizedType = param.getParameterizedType();
            boolean isNullable    = param.getAnnotations().hasAnnotation("Nullable");
            Object  paramValue    = getParameterValue(paramType, parameterizedType, isNullable);
            params[i] = paramValue;
        }
        return params;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object getParameterValue(Class paramType, Type type, boolean isNullable) {
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType)type;
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (paramType == Supplier.class)
                return ((Supplier)()->Get.the(actualType));
            
            if (paramType == java.util.function.Supplier.class)
                return ((Supplier)()->Get.the(actualType)).gracefully();
            
            if (paramType == Optional.class)
                return getOptionalValueOrNullWhenFailAndNullable(isNullable, actualType);
        }
        
        if (isNullable)
            return getValueOrNullWhenFail(paramType);
        
        val paramValue = Get.the(paramType);
        return paramValue;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getOptionalValueOrNullWhenFailAndNullable(boolean isNullable, Class actualType) {
        try {
            val paramValue = Get.the(actualType);
            return Optional.ofNullable(paramValue);
        } catch (Exception e) {
            return isNullable ? null : Optional.empty();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object getValueOrNullWhenFail(Class paramType) {
        try {
            return Get.the(paramType);
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings({ "rawtypes" })
    private static <T> Constructor findConstructor(Class<T> clzz) throws NoSuchMethodException {
        Constructor foundConstructor = findConstructorWithInject(clzz);
        if (foundConstructor.isNull()) {
            if (clzz.hasOnlyOneConsructor())
                 foundConstructor = getOnlyConstructorOf(clzz);
            else {
                try {
                    foundConstructor = getNoArgConstructorOf(clzz);
                } catch (NoSuchMethodException e) {
                    // Do nothing .. let it fall back.
                }
            }
        }
        
        if (foundConstructor.isNull() || !Modifier.isPublic(foundConstructor.getModifiers()))
            return null;
        
        return foundConstructor;
    }
    
    private static <T> Constructor<T> getNoArgConstructorOf(Class<T> clzz)
            throws NoSuchMethodException {
        return clzz.getConstructor();
    }
    
    private static <T> Constructor<?> getOnlyConstructorOf(Class<T> clzz) {
        return clzz.getConstructors()[0];
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> findConstructorWithInject(Class<T> clzz) {
        for(Constructor<?> constructor : clzz.getConstructors()) {
            if (!Modifier.isPublic(constructor.getModifiers()))
                continue;
            
            if (constructor.getAnnotations().hasAnnotation("Inject"))
                return (Constructor<T>)constructor;
        }
        return null;
    }
    
    @UtilityClass
    static class extensions {
    
        public static <T> boolean hasAnnotation(Annotation[] annotations, String name) {
            return stream(annotations)
                    .anyMatch(named(name));
        }
        
        public static <T> boolean hasOnlyOneConsructor(final java.lang.Class<T> clzz) {
            return clzz.getConstructors().length == 1;
        }
        
    }
    
}
