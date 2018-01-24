package directget.objectcreator;

import static java.util.Arrays.stream;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import directget.get.annotations.Inject;
import directget.get.exceptions.AbstractClassCreationException;
import directget.get.exceptions.CyclicDependencyDetectedException;
import dssb.failable.Failable.Supplier;
import dssb.utils.common.Nulls;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.ExtensionMethod;

/**
 * This utility class can create an object using Get.
 * 
 * @author NawaMan
 */
@ExtensionMethod({ Nulls.class, ObjectCreator.extensions.class })
public class ObjectCreator {
    
    // Stepping stone
    public static final ObjectCreator instance = new ObjectCreator();
    
    
    // TODO - Should create interface with all default method.
    // TODO - Should check for @NotNull
    
    private static final String INJECT = Inject.class.getSimpleName();

    private static final Supplier NullSupplier = ()->null;


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
    public <T> T createNew(@NonNull Class<T> theGivenClass)
            throws CreationException, CyclicDependencyDetectedException {
        val set = beingCreateds.get();
        if (set.contains(theGivenClass))
            throw new CyclicDependencyDetectedException(theGivenClass);
        
        try {
            set.add(theGivenClass);
            
            try {
                val supplier = getSupplierFor(theGivenClass);
                val instance = supplier.get();
                return theGivenClass.cast(instance);
            } catch (CyclicDependencyDetectedException e) {
                throw e;
            } catch (CreationException e) {
                throw e;
            } catch (Throwable e) {
                throw new CreationException(theGivenClass, e);
            }
        } finally {
            set.remove(theGivenClass);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    <TYPE, THROWABLE extends Throwable> Supplier<TYPE, THROWABLE> getSupplierFor(
            Class<TYPE> theGivenClass) {
        Supplier supplier = suppliers.get(theGivenClass);
        if (supplier.isNull()) {
            supplier = newSupplierFor(theGivenClass);
            supplier = supplier.or(NullSupplier);
            suppliers.put(theGivenClass, supplier);
        }
        return supplier;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <T> Supplier newSupplierFor(Class<T> theGivenClass) {
        if (theGivenClass.getAnnotations().hasAnnotation("DefaultImplementation")) {
            val defaultImplementationClass = findDefaultImplementation(theGivenClass);
            if (defaultImplementationClass.isNotNull()) {
                return new Supplier() {
                    @Override
                    public Object get() throws Throwable {
                        return getValueOf(defaultImplementationClass);
                    }
                };
            }
            return NullSupplier;
        }
        
        if (theGivenClass.isInterface()
         && theGivenClass.getAnnotations().hasAnnotation("DefaultInterface")) {
            // TODO Implement this.
            return NullSupplier;
        }
        
        if (theGivenClass.getAnnotations().hasAnnotation("DefaultToNull"))
            return NullSupplier;
        
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
            val supplier = new Supplier() {
                public Object get() throws Throwable {
                    return callConstructor(theGivenClass, constructor);
                }
            };
            return supplier;
        }
        
        if (Modifier.isAbstract(theGivenClass.getModifiers()))
            throw new AbstractClassCreationException(theGivenClass);
        
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    private <T> Object callConstructor(Class<T> theGivenClass, Constructor constructor)
            throws InstantiationException, IllegalAccessException, InvocationTargetException {
        val params   = getParameters(constructor);
        val instance = constructor.newInstance(params);
        // TODO - Change to use method handle later.
        return theGivenClass.cast(instance);
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
                .filter(field->Modifier.isStatic(field.getModifiers()))
                .filter(field->Modifier.isPublic(field.getModifiers()))
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
    
    @SuppressWarnings({ "rawtypes"})
    private <T> Supplier findValueFromFactoryMethod(Class<T> theGivenClass) {
        return (Supplier)stream(theGivenClass.getDeclaredMethods())
                .filter(method->Modifier.isStatic(method.getModifiers()))
                .filter(method->Modifier.isPublic(method.getModifiers()))
                .filter(method->ObjectCreator.extensions.hasAnnotation(method.getAnnotations(), "Default"))
                .map(method->ObjectCreator.this.getFactoryMethodValue(theGivenClass, method))
                .findAny()
                .orElse(null);
    }
    
    @SuppressWarnings("rawtypes")
    private <T> Supplier getFactoryMethodValue(Class<T> theGivenClass, Method method) {
        val type = method.getReturnType();
        if (theGivenClass.isAssignableFrom(type))
            return (Supplier)(()->basicFactoryMethodCall(theGivenClass, method));
        
        if (Optional.class.isAssignableFrom(type)) {
            val parameterizedType = (ParameterizedType)method.getGenericReturnType();
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (theGivenClass.isAssignableFrom(actualType))
                return (Supplier)(()->optionalFactoryMethodCall(theGivenClass, method));
        }
        
        if (java.util.function.Supplier.class.isAssignableFrom(type)) {
            val parameterizedType = (ParameterizedType)method.getGenericReturnType();
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            val getMethod         = getGetMethod();
            
            if (theGivenClass.isAssignableFrom(actualType))
                return (Supplier)()->supplierFactoryMethodCall(theGivenClass, method, getMethod);
        }
        
        return null;
    }
    
    private <T> Object supplierFactoryMethodCall(
            Class<T> theGivenClass,
            Method method,
            Method getMethod) 
                    throws IllegalAccessException, InvocationTargetException {
        val params   = getMethodParameters(method);
        val result   = method.invoke(theGivenClass, params);
        val value    = getMethod.invoke(result);
        return value;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> Object optionalFactoryMethodCall(Class<T> theGivenClass, Method method)
            throws IllegalAccessException, InvocationTargetException {
        val params = getMethodParameters(method);
        val value = method.invoke(theGivenClass, params);
        return ((Optional)value).orElse(null);
    }
    
    private <T> Object basicFactoryMethodCall(Class<T> theGivenClass, Method method)
            throws IllegalAccessException, InvocationTargetException {
        val params = getMethodParameters(method);
        val value = method.invoke(theGivenClass, params);
        return value;
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
    
    private static <T> T findDefaultEnumValue(Class<T> theGivenClass) {
        T[] enumConstants = theGivenClass.getEnumConstants();
        if (enumConstants.length == 0)
            return null;
        return stream(enumConstants)
                .filter(value->checkDefaultEnumValue(theGivenClass, value))
                .findAny()
                .orElse(enumConstants[0]);
    }
    
    @SuppressWarnings("rawtypes")
    private static <T> boolean checkDefaultEnumValue(Class<T> theGivenClass, T value) {
        val name = ((Enum)value).name();
        try {
            return ObjectCreator.extensions.hasAnnotation(theGivenClass.getField(name).getAnnotations(), "Default");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new CreationException(theGivenClass, e);
        }
    }
    
    @SuppressWarnings({ "rawtypes" })
    private Object[] getParameters(Constructor constructor) {
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
    
    private Object[] getMethodParameters(Method method) {
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
    private Object getParameterValue(Class paramType, Type type, boolean isNullable) {
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType)type;
            val actualType        = (Class)parameterizedType.getActualTypeArguments()[0];
            
            if (paramType == Supplier.class)
                return new Supplier() {
                    @Override
                    public Object get() throws Throwable {
                        return getValueOf(actualType);
                    }
                };
            
            if (paramType == java.util.function.Supplier.class)
                return new java.util.function.Supplier() {
                    @Override
                    public Object get() {
                        return getValueOf(actualType);
                    }
                };
            
            if (paramType == Optional.class)
                return getOptionalValueOrNullWhenFailAndNullable(isNullable, actualType);
        }
        
        if (isNullable)
            return getValueOrNullWhenFail(paramType);
        
        val paramValue = getValueOf(paramType);
        return paramValue;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object getOptionalValueOrNullWhenFailAndNullable(boolean isNullable, Class actualType) {
        try {
            val paramValue = getValueOf(actualType);
            return Optional.ofNullable(paramValue);
        } catch (Exception e) {
            return isNullable ? null : Optional.empty();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object getValueOrNullWhenFail(Class paramType) {
        try {
            return getValueOf(paramType);
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings({ "rawtypes" })
    private <T> Constructor findConstructor(Class<T> clzz) {
        Constructor foundConstructor = clzz.findConstructorWithInject();
        if (foundConstructor.isNull()) {
            foundConstructor = clzz.hasOnlyOneConsructor()
                    ? clzz.getOnlyConstructor()
                    : clzz.getNoArgConstructor();
        }
        
        if (foundConstructor.isNull()
         || !Modifier.isPublic(foundConstructor.getModifiers()))
            return null;
        
        return foundConstructor;
    }
    
    protected <T> T getValueOf(Class<T> clzz) {
        return Get.the(clzz);
    }
    
    static class extensions {
    
        public static <T> boolean hasAnnotation(Annotation[] annotations, String name) {
            return stream(annotations)
                    .anyMatch(named(name));
        }
        
        public static <T> boolean hasOnlyOneConsructor(final java.lang.Class<T> clzz) {
            return clzz.getConstructors().length == 1;
        }
        
        @SuppressWarnings("unchecked")
        public static <T> Constructor<T> findConstructorWithInject(Class<T> clzz) {
            for(Constructor<?> constructor : clzz.getConstructors()) {
                if (!Modifier.isPublic(constructor.getModifiers()))
                    continue;
                
                if (hasAnnotation(constructor.getAnnotations(), INJECT))
                    return (Constructor<T>)constructor;
            }
            return null;
        }
        
        public static <T> Constructor<T> getNoArgConstructor(Class<T> clzz) {
            try {
                return clzz.getConstructor();
            } catch (NoSuchMethodException e) {
                return null;
            } catch (SecurityException e) {
                throw new CreationException(clzz);
            }
        }
        
        public static <T> Constructor<?> getOnlyConstructor(Class<T> clzz) {
            return clzz.getConstructors()[0];
        }
        
    }
    
}
