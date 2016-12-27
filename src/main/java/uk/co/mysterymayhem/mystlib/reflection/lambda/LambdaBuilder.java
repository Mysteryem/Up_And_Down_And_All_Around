package uk.co.mysterymayhem.mystlib.reflection.lambda;

import net.minecraft.util.Tuple;
import uk.co.mysterymayhem.mystlib.reflection.LookupHelper;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;

/**
 * <p>Class for building lambda classes and creating instances of them at runtime.</p>
 * <p>Lambdas are either on par or faster than MethodHandles, but can be more complicated to create at runtime and
 * cannot perform get/set field instructions</p>
 * <p>Lambdas also have the additional benefits that any checked exceptions thrown by the method they implement are
 * automatically wrapped (so you don't have to deal with try/catch blocks whenever you invoke them) and that if you
 * do need to store them in a collection or pass them as an argument, they are expressed as a specific interface</p>
 * <p>
 * <p>Based on my own benchmarks with JMH, static (static final) MethodHandles (using invokeExact) and static Lambdas
 * operate at effectively the same speed as normal compiled code. However, dynamic (non-'static final') MethodHandles
 * and dynamic Lambdas perform worse.</p>
 * <p>Given an instance method that took no arguments and returned an int and consisted solely of "return 0;", dynamic Lambdas
 * performed at about twice the speed of dynamic MethodHandles</p>
 * <p>Both Lambdas and MethodHandles outperform the standard Reflection API in static cases</p>
 * <p>MethodHandles are between slightly faster than and the same speed as the standard Reflection API in dynamic cases (java 8+)</p>
 * <p>There is a third way of calling methods/accessing fields reflexively, and that's using the MethodHandleProxies class.
 * Unfortunately, the proxies it produces are even slower than the standard Reflection API</p>
 * <p>As</p>
 * Created by Mysteryem on 2016-12-02.
 */
@SuppressWarnings("WeakerAccess")
public class LambdaBuilder {

    // LambdaMetaFactory requires a lookup with private access for some reason, even if the method we're going to
    // implement in a lambda is public.
    static final MethodHandles.Lookup TRUSTED_LOOKUP = LookupHelper.getTrustedLookup();

    private static boolean isValidInstanceMethod(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_invokeInterface
                    || referenceKind == MethodHandleInfo.REF_invokeSpecial
                    || referenceKind == MethodHandleInfo.REF_invokeVirtual;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidStaticMethod(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_invokeStatic;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidINVOKESPECIAL(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_invokeSpecial;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidGETSTATIC(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_getStatic;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidPUTSTATIC(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_putStatic;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidGETFIELD(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_getField;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static boolean isValidPUTFIELD(MethodHandle handle) {
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handle);
            int referenceKind = methodHandleInfo.getReferenceKind();
            return referenceKind == MethodHandleInfo.REF_putField;
        } catch (Exception e) {
            //likely case is that it's not a direct methodhandle (which we can't use anyway)
            return false;
        }
    }

    private static String informativeToStringOutput(MethodHandle methodHandle) {
        try {
            return TRUSTED_LOOKUP.revealDirect(methodHandle).toString();
        } catch (Exception e) {
            return "Non-direct MethodHandle: " + methodHandle;
        }
    }

    private static void validateGetFieldMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidGETFIELD(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid GETFIELD methodhandle");
        }
    }

    private static void validatePutFieldMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidPUTFIELD(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid PUTFIELD methodhandle");
        }
    }

    private static void validateGetStaticMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidGETSTATIC(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid GETSTATIC methodhandle");
        }
    }

    private static void validatePutStaticMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidPUTSTATIC(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid PUTSTATIC methodhandle");
        }
    }

    private static void validateInstanceMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidInstanceMethod(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid instance methodhandle");
        }

        MethodType methodType = methodHandle.type();
        // First argument is the class of the instance the method is called on
        if (methodType.parameterCount() < 1) {
            throw new LambdaBuildException("The first argument of MethodHandles used for creating instance method" +
                    " lambdas must have their first parameter equal the class the method is from");
        }
    }

    private static void validateInstanceMethod(Method method) throws LambdaBuildException {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new LambdaBuildException(method + " is not an instance method");
        }
    }

    private static void validateStaticMethodHandle(MethodHandle methodHandle) throws LambdaBuildException {
        if (!isValidStaticMethod(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid static methodhandle");
        }
    }

    private static void validateStaticMethod(Method method) throws LambdaBuildException {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new LambdaBuildException(method + " is not a static method");
        }
    }

    @SuppressWarnings("unchecked")
    private static <INTERFACE> INTERFACE buildFieldLambda(Class<? super INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        MethodType methodType = methodHandle.type();

        Class<?> instanceClass = TRUSTED_LOOKUP.revealDirect(methodHandle).getDeclaringClass();

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterfaceClass);

        CallSite metafactory = FieldLambdaMetafactory.metaFactory(
                TRUSTED_LOOKUP.in(instanceClass),
                interfaceMethod.getName(),
                MethodType.methodType(functionalInterfaceClass),
                MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                methodHandle,
                MethodType.methodType(methodType.returnType(), methodType.parameterArray())
        );

        try {
            // Can't use invokeExact because the cast to INTERFACE is a cast to Object. invokeExact needs an exact cast that matches the MethodHandle
            return (INTERFACE) metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException("Creating instance of lambda failed", throwable);
        }
    }

    public static <INTERFACE> INTERFACE buildInstanceFieldGetterLambda(Class<INTERFACE> functionalInterfaceClass, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, findInstanceFieldGetterHandle(declaringClass, fieldType, fieldNames));
    }

    public static <INTERFACE> INTERFACE buildInstanceFieldSetterLambda(Class<INTERFACE> functionalInterfaceClass, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, findInstanceFieldSetterHandle(declaringClass, fieldType, fieldNames));
    }

    public static <INTERFACE> INTERFACE buildStaticFieldGetterLambda(Class<INTERFACE> functionalInterfaceClass, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, findStaticFieldGetterHandle(declaringClass, fieldType, fieldNames));
    }

    public static <INTERFACE> INTERFACE buildStaticFieldSetterLambda(Class<INTERFACE> functionalInterfaceClass, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, findStaticFieldSetterHandle(declaringClass, fieldType, fieldNames));
    }

    public static <INTERFACE> INTERFACE buildFieldGetterLambda(Class<INTERFACE> functionalInterfaceClass, Field field) throws LambdaBuildException {
        try {
            MethodHandle getter = TRUSTED_LOOKUP.unreflectGetter(field);

            if (Modifier.isStatic(field.getModifiers())) {
                return LambdaBuilder.<INTERFACE>buildStaticGetterLambda(functionalInterfaceClass, getter);
            }
            else {
                return LambdaBuilder.<INTERFACE>buildInstanceGetterLambda(functionalInterfaceClass, getter);
            }
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    public static <INTERFACE> INTERFACE buildFieldSetterLambda(Class<INTERFACE> functionalInterfaceClass, Field field) throws LambdaBuildException {
        try {
            MethodHandle getter = TRUSTED_LOOKUP.unreflectSetter(field);

            if (Modifier.isStatic(field.getModifiers())) {
                return LambdaBuilder.<INTERFACE>buildStaticSetterLambda(functionalInterfaceClass, getter);
            }
            else {
                return LambdaBuilder.<INTERFACE>buildInstanceSetterLambda(functionalInterfaceClass, getter);
            }
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    public static <I_GETTER, I_SETTER> Tuple<I_GETTER, I_SETTER> buildGetterSetterTuple(Class<I_GETTER> functionalInterfaceClassGetter, Class<I_SETTER> functionalInterfaceClassSetter, Field field) throws LambdaBuildException {
        return new Tuple<>(
                LambdaBuilder.<I_GETTER>buildFieldGetterLambda(functionalInterfaceClassGetter, field),
                LambdaBuilder.<I_SETTER>buildFieldSetterLambda(functionalInterfaceClassSetter, field));
    }

    private static <INTERFACE> INTERFACE buildInstanceGetterLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validateGetFieldMethodHandle(methodHandle);
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, methodHandle);
    }

    private static <INTERFACE> INTERFACE buildInstanceSetterLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validatePutFieldMethodHandle(methodHandle);
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, methodHandle);
    }

    private static <INTERFACE> INTERFACE buildStaticGetterLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validateGetStaticMethodHandle(methodHandle);
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, methodHandle);
    }

    private static <INTERFACE> INTERFACE buildStaticSetterLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validatePutStaticMethodHandle(methodHandle);
        return LambdaBuilder.<INTERFACE>buildFieldLambda(functionalInterfaceClass, methodHandle);
    }

    @SuppressWarnings("unchecked")
    private static <INTERFACE> INTERFACE buildInstanceMethodLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validateInstanceMethodHandle(methodHandle);

        MethodType methodType = methodHandle.type();
        Class<?> clazz = methodType.parameterArray()[0];

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterfaceClass);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    TRUSTED_LOOKUP.in(clazz),
                    interfaceMethod.getName(),
                    MethodType.methodType(functionalInterfaceClass),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(methodType.returnType(), methodType.parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        try {
            // Can't use invokeExact because the cast to INTERFACE is a cast to Object. invokeExact needs an exact cast that matches the MethodHandle
            return (INTERFACE) metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException("Creating instance of lambda failed", throwable);
        }
    }

    /**
     * Produces a Lambda that takes takes an object instance as the first parameter of its functional method
     *
     * @param functionalInterfaceClass Class object of a FunctionalInterface (or any interface class with a single abstract method)
     * @param method                   The method the lambda should call on it's passed object
     * @param <INTERFACE>
     * @return
     */
    public static <INTERFACE> INTERFACE buildInstanceMethodLambda(Class<INTERFACE> functionalInterfaceClass, Method method) throws LambdaBuildException {
        validateInstanceMethod(method);

        try {
            MethodHandle methodHandle = TRUSTED_LOOKUP.unreflect(method);
            return LambdaBuilder.<INTERFACE>buildInstanceMethodLambda(functionalInterfaceClass, methodHandle);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    /**
     * Produces a Lambda that takes takes an object instance as the first parameter of its functional method and runs a method, specified by class, method type and name
     *
     * @param functionalInterfaceClass Class object of a FunctionalInterface (or any interface class with a single abstract method).
     * @param instanceClass            Class object of the object instance that the method that will be called is from.
     * @param methodDescriptor         MethodType that matches the method .
     * @param methodNames
     * @param <INTERFACE>              awdd.
     * @return adaw.
     */
    public static <INTERFACE> INTERFACE buildInstanceMethodLambda(
            Class<INTERFACE> functionalInterfaceClass, Class<?> instanceClass, MethodType methodDescriptor, String... methodNames) throws LambdaBuildException {
        MethodHandle foundHandle = findInstanceMethodHandle(instanceClass, methodDescriptor, methodNames);
        return LambdaBuilder.<INTERFACE>buildInstanceMethodLambda(functionalInterfaceClass, foundHandle);
    }

    /**
     * Every call creates a new class, you should use getInstanceBinder instead to get an instance binder and use that to create bound lambda instances.
     *
     * @param functionalInterfaceClass
     * @param method
     * @param instance
     * @param <INSTANCE>
     * @param <INTERFACE>
     * @return
     * @throws LambdaBuildException
     */
    @Deprecated
    private static <INSTANCE, INTERFACE> INTERFACE buildBoundInstanceMethodLambda(Class<INTERFACE> functionalInterfaceClass, Method method, INSTANCE instance) throws LambdaBuildException {
        InstanceBinder<INSTANCE, INTERFACE> instanceBoundMethodLambdaFactory = getInstanceBinder(functionalInterfaceClass, method);
        return instanceBoundMethodLambdaFactory.apply(instance);
    }

    public static <INSTANCE, INTERFACE> InstanceBinder<INSTANCE, INTERFACE> getInstanceBinder(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validateInstanceMethodHandle(methodHandle);
        MethodType methodType = methodHandle.type();
        Class<?> instanceClass = methodType.parameterArray()[0];

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterfaceClass);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    TRUSTED_LOOKUP.in(instanceClass),
                    interfaceMethod.getName(),
                    MethodType.methodType(functionalInterfaceClass, instanceClass),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(methodType.returnType(), methodType.dropParameterTypes(0, 1).parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        return new InstanceBinder<>(metafactory.getTarget());
    }

    public static <T, INTERFACE> InstanceBinder<T, INTERFACE> getInstanceBinder(Class<INTERFACE> functionalInterfaceClass, Method method) throws LambdaBuildException {
        validateInstanceMethod(method);

        MethodHandle methodHandle;
        try {
            methodHandle = TRUSTED_LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }

        return getInstanceBinder(functionalInterfaceClass, methodHandle);
    }

    public static <INTERFACE> INTERFACE buildStaticMethodLambda(Class<INTERFACE> functionalInterfaceClass, MethodHandle methodHandle) throws LambdaBuildException {
        validateStaticMethodHandle(methodHandle);

        Class<?> clazz;
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(methodHandle);
            clazz = methodHandleInfo.getDeclaringClass();
        } catch (Exception e) {
            throw new LambdaBuildException(e);
        }

        MethodType type = methodHandle.type();

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterfaceClass);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    TRUSTED_LOOKUP.in(clazz),
                    interfaceMethod.getName(),
                    MethodType.methodType(functionalInterfaceClass),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(type.returnType(), type.parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        try {
            // Can't use invokeExact because the cast to T is a cast to Object. invokeExact needs an exact cast that matches the MethodHandle
            return (INTERFACE) metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException(throwable);
        }
    }

    public static <INTERFACE> INTERFACE buildStaticMethodLambda(Class<INTERFACE> functionalInterfaceClass, Method method) throws LambdaBuildException {
        validateStaticMethod(method);

        MethodHandle methodHandle = null;
        try {
            methodHandle = TRUSTED_LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }

        return LambdaBuilder.<INTERFACE>buildStaticMethodLambda(functionalInterfaceClass, methodHandle);
    }

    private static Method findFunctionalInterfaceMethod(Class<?> clazz) throws LambdaBuildException {
        if (!clazz.isInterface()) {
            throw new LambdaBuildException(clazz + " is not a functional interface (not an interface)");
        }

        Method[] methods = clazz.getMethods();

        Method functionalMethod = null;

        for (Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())) {
                if (functionalMethod != null) {
                    throw new LambdaBuildException(clazz + " is not a functional interface (more than one abstract method");
                }
                else {
                    functionalMethod = method;
                }
            }
        }

        if (functionalMethod == null) {
            throw new LambdaBuildException(clazz + " is not a functional interface (has no abstract methods)");
        }

        return functionalMethod;
    }

    private static MethodHandle findInstanceMethodHandle(Object instance, MethodType methodType, String... methodNames) throws LambdaBuildException {
        return findInstanceMethodHandle(instance.getClass(), methodType, methodNames);
    }

    private static MethodHandle findInstanceMethodHandle(Class<?> instanceClass, MethodType methodType, String... methodNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String methodName : methodNames) {
            try {
                return TRUSTED_LOOKUP.findVirtual(instanceClass, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find handle for instance method %s in %s with descriptor matching %s", Arrays.toString(methodNames), instanceClass, methodType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    private static MethodHandle findStaticMethodHandle(Class<?> clazz, MethodType methodType, String... methodNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String methodName : methodNames) {
            try {
                return TRUSTED_LOOKUP.findStatic(clazz, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find handle for static method %s in %s with descriptor matching %s", Arrays.toString(methodNames), clazz, methodType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    private static MethodHandle findStaticFieldGetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String fieldName : fieldNames) {
            try {
                return TRUSTED_LOOKUP.findStaticGetter(declaringClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find getter handle for static field %s in %s with type %s", Arrays.toString(fieldNames), declaringClass, fieldType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    private static MethodHandle findStaticFieldSetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String fieldName : fieldNames) {
            try {
                return TRUSTED_LOOKUP.findStaticSetter(declaringClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find setter handle for static field %s in %s with type %s", Arrays.toString(fieldNames), declaringClass, fieldType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    private static MethodHandle findInstanceFieldGetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String fieldName : fieldNames) {
            try {
                return TRUSTED_LOOKUP.findGetter(declaringClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find getter handle for instance field %s in %s with type %s", Arrays.toString(fieldNames), declaringClass, fieldType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    private static MethodHandle findInstanceFieldSetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) throws LambdaBuildException {
        ReflectiveOperationException mostRecentException = null;

        for (String fieldName : fieldNames) {
            try {
                return TRUSTED_LOOKUP.findSetter(declaringClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                mostRecentException = e;
            }
        }
        String extraInfo = String.format("Failed to find setter handle for instance field %s in %s with type %s", Arrays.toString(fieldNames), declaringClass, fieldType);
        throw new LambdaBuildException(extraInfo, mostRecentException);
    }

    /**
     * Exception wrapper for dealing with exceptions thrown by build methods
     */
    @SuppressWarnings("WeakerAccess")
    public static final class LambdaBuildException extends RuntimeException {
        public LambdaBuildException() {
        }

        public LambdaBuildException(String msg) {
            super(msg);
        }

        public LambdaBuildException(String msg, Throwable cause) {
            super(msg, cause);
        }

        public LambdaBuildException(Throwable cause) {
            super(cause);
        }

        public LambdaBuildException(String msg, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(msg, cause, enableSuppression, writableStackTrace);
        }

        LambdaBuildException(LambdaConversionException lambdaConversionException) {
            this("Conversion to lambda class failed", lambdaConversionException);
        }
    }

    /**
     * @param <INSTANCE>
     * @param <INTERFACE>
     */
    @SuppressWarnings("unchecked")
    public static final class InstanceBinder<INSTANCE, INTERFACE> implements Function<INSTANCE, INTERFACE> {
        private final MethodHandle factory;

        InstanceBinder(MethodHandle factory) {
            this.factory = factory;
        }

        @Override
        public INTERFACE apply(INSTANCE object) {
            try {
                return (INTERFACE) factory.invoke(object);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
    }
}

