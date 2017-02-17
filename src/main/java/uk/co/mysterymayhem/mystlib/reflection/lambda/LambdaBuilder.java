package uk.co.mysterymayhem.mystlib.reflection.lambda;

import sun.reflect.Reflection;
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
    private static final MethodHandles.Lookup TRUSTED_LOOKUP = LookupHelper.getTrustedLookup();

    public static <I> I buildFieldGetter(Class<I> functionalInterface, Field field) {
        try {
            MethodHandle getter = TRUSTED_LOOKUP.unreflectGetter(field);

            if (Modifier.isStatic(field.getModifiers())) {
                return LambdaBuilder.buildStaticFieldGetter(functionalInterface, getter);
            }
            else {
                return LambdaBuilder.buildInstanceFieldGetter(functionalInterface, getter);
            }
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    public static <I> I buildFieldSetter(Class<I> functionalInterface, Field field) {
        try {
            MethodHandle getter = TRUSTED_LOOKUP.unreflectSetter(field);

            if (Modifier.isStatic(field.getModifiers())) {
                return LambdaBuilder.buildStaticFieldSetter(functionalInterface, getter);
            }
            else {
                return LambdaBuilder.buildInstanceFieldSetter(functionalInterface, getter);
            }
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    public static <I_GET, I_Set> GetterSetterTuple<I_GET, I_Set> buildGetterSetterTuple(Class<I_GET> functionalInterfaceGetter, Class<I_Set> functionalInterfaceSetter, Field field) {
        return new GetterSetterTuple<>(
                LambdaBuilder.buildFieldGetter(functionalInterfaceGetter, field),
                LambdaBuilder.buildFieldSetter(functionalInterfaceSetter, field),
                field,
                true);
    }

    public static <I> I buildInstanceFieldGetter(Class<I> functionalInterface, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
        return LambdaBuilder.buildFieldLambda(functionalInterface, findInstanceFieldGetterHandle(declaringClass, fieldType, fieldNames));
    }

    @SuppressWarnings("unchecked")
    private static <I> I buildFieldLambda(Class<I> functionalInterface, MethodHandle fieldHandle) {
        MethodType methodType = fieldHandle.type();

        Class<?> declaringClass = TRUSTED_LOOKUP.revealDirect(fieldHandle).getDeclaringClass();

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterface);

        Class<?> contextForClassLoading;
        try {
            Class.forName(functionalInterface.getName(), false, declaringClass.getClassLoader());
            contextForClassLoading = declaringClass;
        } catch (ClassNotFoundException e) {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(fieldHandle);
            Field fieldToCall = methodHandleInfo.reflectAs(Field.class, TRUSTED_LOOKUP);
            boolean accessible = Reflection.verifyMemberAccess(interfaceMethod.getDeclaringClass(), fieldToCall.getDeclaringClass(), fieldToCall, fieldToCall.getModifiers());
            if (accessible) {
                contextForClassLoading = functionalInterface;
            }
            else {
                throw new LambdaBuildException(
                        "\n----------\n" +
                                "The classloader for " + declaringClass + " cannot access " + functionalInterface + " so an anonymous class with private access cannot be created.\n"
                                + "And " + functionalInterface + " cannot access " + fieldToCall + " so the lambda cannot be created.\n" +
                                "----------", e);
            }
        }

        CallSite metafactory = FieldLambdaMetafactory.metaFactory(
                TRUSTED_LOOKUP.in(contextForClassLoading),
                interfaceMethod.getName(),
                MethodType.methodType(functionalInterface),
                MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                fieldHandle,
                MethodType.methodType(methodType.returnType(), methodType.parameterArray())
        );

        try {
            // Can't use invokeExact because the cast to INTERFACE is a cast to Object. invokeExact needs an exact cast that matches the MethodHandle
            return (I)metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException("Creating instance of lambda failed", throwable);
        }
    }

    private static MethodHandle findInstanceFieldGetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
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

    private static Method findFunctionalInterfaceMethod(Class<?> clazz) {
        if (!clazz.isInterface()) {
            throw new LambdaBuildException(clazz + " is not a functional interface (not an interface)");
        }

        Method[] methods = clazz.getMethods();

        Method functionalMethod = null;

        for (Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())) {
                if (functionalMethod != null) {
                    throw new LambdaBuildException(clazz + " is not a functional interface (has more than one abstract method)");
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

    public static <I> I buildInstanceFieldSetter(Class<I> functionalInterface, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
        return LambdaBuilder.buildFieldLambda(functionalInterface, findInstanceFieldSetterHandle(declaringClass, fieldType, fieldNames));
    }

    private static MethodHandle findInstanceFieldSetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
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
     * Produces a Lambda that takes takes an object instance as the first parameter of its functional method
     *
     * @param functionalInterface Class object of a FunctionalInterface (or any interface class with a single abstract method)
     * @param method              The method the lambda should call on it's passed object
     * @param <I>
     * @return
     */
    public static <I> I buildInstanceMethodLambda(Class<I> functionalInterface, Method method) {
        validateInstanceMethod(method);

        try {
            MethodHandle methodHandle = TRUSTED_LOOKUP.unreflect(method);
            return LambdaBuilder.buildInstanceMethodLambda(functionalInterface, methodHandle);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }
    }

    private static void validateInstanceMethod(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new LambdaBuildException(method + " is not an instance method");
        }
    }

    @SuppressWarnings("unchecked")
    private static <I> I buildInstanceMethodLambda(Class<I> functionalInterface, MethodHandle methodHandle) {
        // Ensure that the MethodHandle is direct and is a an instance method
        validateInstanceMethodHandle(methodHandle);

        MethodType methodType = methodHandle.type();
        // First argument of a MethodHandle for an instance method is an instance of the class the method belongs to
        Class<?> declaringClass = methodType.parameterArray()[0];

//        if (Class.declaringClass.getClassLoader() == null && functionalInterface.getClassLoader() != null) {
//            // The class the method is declared in is loaded by the bootstrap classloader
//            // However, the interface being implemented  is not loaded by the bootstrap classloader
//            // Creation of the anonymous class within 'declaredClass' will fail as the bootstrap classloader won't be
//            // able to find 'functionalInterface'
//            //
//            // We will try to load the anonymous class with 'functionalInterface' as the
//            // "context for linkage, access control, protection domain, and class loader"
//            contextForClassLoading = functionalInterface;
//        }
//        else {
//            contextForClassLoading = declaringClass;
//        }

        // Ensure the class is actually a functional interface (interface with 1 abstract method) and retrieve its
        // functional method
        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterface);

        Class<?> contextForClassLoading = getContextForClassLoading(interfaceMethod, methodHandle);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    // Create a MethodHandles.Lookup that is 'within' the declaring class so that it has private access
                    // This is also the class that is internally passed to Unsafe::defineAnonymousClass as the
                    // "context for linkage, access control, protection domain, and class loader"
                    TRUSTED_LOOKUP.in(contextForClassLoading),
                    // Name of the method to be implemented
                    interfaceMethod.getName(),
                    // Pseudo-constructor parameters, return type is interface type, parameters define constructor's
                    // parameters as well as private final fields that will store the arguments passed in the constructor
                    MethodType.methodType(functionalInterface),
                    //
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(methodType.returnType(), methodType.parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        try {
            // Can't use invokeExact because the cast to INTERFACE is a cast to Object. invokeExact needs an exact cast
            // that matches the MethodHandle
            return (I)metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException("Creating instance of lambda failed", throwable);
        }
    }

    private static void validateInstanceMethodHandle(MethodHandle methodHandle) {
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

    // TODO: Allow passing a different class to use for anonymous class' context that will be checked in addition to the other two classes, this will allow protected access that is otherwise impossible currently
    private static Class<?> getContextForClassLoading(Method interfaceMethod, MethodHandle handleToCall) {
        MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(handleToCall);
        Class<?> declaringClass = methodHandleInfo.getDeclaringClass();
        Class<?> functionalInterface = interfaceMethod.getDeclaringClass();
        try {
            Class.forName(functionalInterface.getName(), false, declaringClass.getClassLoader());
            return declaringClass;
        } catch (ClassNotFoundException e) {
            Method methodToCall = methodHandleInfo.reflectAs(Method.class, TRUSTED_LOOKUP);
            boolean accessible = Reflection.verifyMemberAccess(interfaceMethod.getDeclaringClass(), methodToCall.getDeclaringClass(), methodToCall, methodToCall.getModifiers());
            if (accessible) {
                return functionalInterface;
            }
            else {
                throw new LambdaBuildException(
                        "\n----------\n" +
                                "The classloader for '" + declaringClass + "' cannot access '" + functionalInterface + "' so an anonymous class with private access cannot be created.\n"
                                + "And '" + functionalInterface + "' cannot access '" + methodToCall + "' so the lambda cannot be created.\n" +
                                "----------", e);
            }
        }
    }

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

    private static String informativeToStringOutput(MethodHandle methodHandle) {
        try {
            return TRUSTED_LOOKUP.revealDirect(methodHandle).toString();
        } catch (Exception e) {
            return "Non-direct MethodHandle: " + methodHandle;
        }
    }

    /**
     * Produces a Lambda that takes takes an object instance as the first parameter of its functional method and runs a method, specified by class, method type and name
     *
     * @param functionalInterface Class object of a FunctionalInterface (or any interface class with a single abstract method).
     * @param instanceClass       Class object of the object instance that the method that will be called is from.
     * @param methodDescriptor    MethodType that matches the method .
     * @param methodNames
     * @param <I>                 awdd.
     * @return adaw.
     */
    public static <I> I buildInstanceMethodLambda(
            Class<I> functionalInterface, Class<?> instanceClass, MethodType methodDescriptor, String... methodNames) {
        // Finds the instance method and creates a MethodHandle for it
        MethodHandle foundHandle = findInstanceMethodHandle(instanceClass, methodDescriptor, methodNames);
        return LambdaBuilder.buildInstanceMethodLambda(functionalInterface, foundHandle);
    }

    private static MethodHandle findInstanceMethodHandle(Class<?> instanceClass, MethodType methodType, String... methodNames) {
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

    public static <I> I buildInstanceMethodLambda(
            Class<I> functionalInterface, Class<?> instanceClass, MethodType interfaceMethodType, MethodType instanceMethodType, String... methodNames) {
        MethodHandle foundHandle = findInstanceMethodHandle(instanceClass, interfaceMethodType, methodNames);
        return LambdaBuilder.buildInstanceMethodLambda(functionalInterface, foundHandle);
    }

    public static <I> I buildStaticFieldGetter(Class<I> functionalInterface, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
        return LambdaBuilder.buildFieldLambda(functionalInterface, findStaticFieldGetterHandle(declaringClass, fieldType, fieldNames));
    }

    private static MethodHandle findStaticFieldGetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
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

    public static <I> I buildStaticFieldSetter(Class<I> functionalInterface, Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
        return LambdaBuilder.buildFieldLambda(functionalInterface, findStaticFieldSetterHandle(declaringClass, fieldType, fieldNames));
    }

    private static MethodHandle findStaticFieldSetterHandle(Class<?> declaringClass, Class<?> fieldType, String... fieldNames) {
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

    public static <I> I buildStaticMethodLambda(Class<I> functionalInterface, Class<?> declaringClass, MethodType methodDescriptor, String... methodNames) {
        MethodHandle foundHandle = findStaticMethodHandle(declaringClass, methodDescriptor, methodNames);
        return LambdaBuilder.buildStaticMethodLambda(functionalInterface, foundHandle);
    }

    private static MethodHandle findStaticMethodHandle(Class<?> clazz, MethodType methodType, String... methodNames) {
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

    public static <I> I buildStaticMethodLambda(Class<I> functionalInterface, MethodHandle methodHandle) {
        validateStaticMethodHandle(methodHandle);

        Class<?> declaringClass;
        try {
            MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(methodHandle);
            declaringClass = methodHandleInfo.getDeclaringClass();
        } catch (Exception e) {
            throw new LambdaBuildException(e);
        }

        MethodType type = methodHandle.type();

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterface);

        Class<?> contextForClassLoading = getContextForClassLoading(interfaceMethod, methodHandle);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    TRUSTED_LOOKUP.in(contextForClassLoading),
                    interfaceMethod.getName(),
                    MethodType.methodType(functionalInterface),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(type.returnType(), type.parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        try {
            // Can't use invokeExact because the cast to T is a cast to Object. invokeExact needs an exact cast that matches the MethodHandle
            return (I)metafactory.getTarget().invoke();
        } catch (Throwable throwable) {
            throw new LambdaBuildException(throwable);
        }
    }

    private static void validateStaticMethodHandle(MethodHandle methodHandle) {
        if (!isValidStaticMethod(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid static methodhandle");
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

    public static <I> I buildStaticMethodLambda(Class<I> functionalInterface, Method method) {
        validateStaticMethod(method);

        MethodHandle methodHandle;
        try {
            methodHandle = TRUSTED_LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }

        return LambdaBuilder.buildStaticMethodLambda(functionalInterface, methodHandle);
    }

    private static void validateStaticMethod(Method method) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new LambdaBuildException(method + " is not a static method");
        }
    }

    public static <T, I> InstanceBinder<T, I> getInstanceBinder(Class<I> functionalInterface, Class<? extends T> declaringClass, MethodType methodDescriptor, String... methodNames) {
        MethodHandle instanceMethodHandle = findInstanceMethodHandle(declaringClass, methodDescriptor, methodNames);
        return getInstanceBinder(functionalInterface, instanceMethodHandle);
    }

    public static <T, I> InstanceBinder<T, I> getInstanceBinder(Class<I> functionalInterface, MethodHandle methodHandle) {
        validateInstanceMethodHandle(methodHandle);
        MethodType methodType = methodHandle.type();
        Class<?> declaringClass = methodType.parameterArray()[0];

        Method interfaceMethod = findFunctionalInterfaceMethod(functionalInterface);

        Class<?> contextForClassLoading = getContextForClassLoading(interfaceMethod, methodHandle);

        CallSite metafactory;
        try {
            metafactory = LambdaMetafactory.metafactory(
                    TRUSTED_LOOKUP.in(contextForClassLoading),
                    interfaceMethod.getName(),
                    MethodType.methodType(functionalInterface, declaringClass),
                    MethodType.methodType(interfaceMethod.getReturnType(), interfaceMethod.getParameterTypes()),
                    methodHandle,
                    MethodType.methodType(methodType.returnType(), methodType.dropParameterTypes(0, 1).parameterArray())
            );
        } catch (LambdaConversionException e) {
            throw new LambdaBuildException(e);
        }

        return new InstanceBinder<>(metafactory.getTarget());
    }

    private static <I> I buildInstanceFieldGetter(Class<I> functionalInterface, MethodHandle fieldHandle) {
        validateGetFieldMethodHandle(fieldHandle);
        return LambdaBuilder.buildFieldLambda(functionalInterface, fieldHandle);
    }

    private static <I> I buildInstanceFieldSetter(Class<I> functionalInterface, MethodHandle fieldHandle) {
        validatePutFieldMethodHandle(fieldHandle);
        return LambdaBuilder.buildFieldLambda(functionalInterface, fieldHandle);
    }

    /**
     * Every call creates a new class, you should use getInstanceBinder instead to get an instance binder and use that to create bound lambda instances.
     *
     * @param functionalInterface
     * @param method
     * @param instance
     * @param <T>
     * @param <I>
     * @return
     * @throws LambdaBuildException
     */
    @Deprecated
    private static <T, I> I buildInstanceMethodBoundLambda(Class<I> functionalInterface, Method method, T instance) {
        InstanceBinder<T, I> instanceBoundMethodLambdaFactory = getInstanceBinder(functionalInterface, method);
        return instanceBoundMethodLambdaFactory.apply(instance);
    }

    public static <T, I> InstanceBinder<T, I> getInstanceBinder(Class<I> functionalInterface, Method method) {
        validateInstanceMethod(method);

        MethodHandle methodHandle;
        try {
            methodHandle = TRUSTED_LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new LambdaBuildException(e);
        }

        return getInstanceBinder(functionalInterface, methodHandle);
    }

    private static <I> I buildStaticFieldGetter(Class<I> functionalInterface, MethodHandle fieldHandle) {
        validateGetStaticMethodHandle(fieldHandle);
        return LambdaBuilder.buildFieldLambda(functionalInterface, fieldHandle);
    }

    private static <I> I buildStaticFieldSetter(Class<I> functionalInterface, MethodHandle fieldHandle) {
        validatePutStaticMethodHandle(fieldHandle);
        return LambdaBuilder.buildFieldLambda(functionalInterface, fieldHandle);
    }

    private static MethodHandle findInstanceMethodHandle(Object instance, MethodType methodType, String... methodNames) {
        return findInstanceMethodHandle(instance.getClass(), methodType, methodNames);
    }


//    public static <T, I> InstanceBinder<T, I> bind(Object obj, Class<I> boundFInterfaceClass, MethodType methodType, String... methodNames) {
//
////        InstanceBinder<T, I> instanceBinder = LambdaBuilder.getInstanceBinder(boundFInterfaceClass, obj.getClass(), methodType, methodNames);
//        return instanceBinder;
//    }
//
//    public static <T, I> I bind(Object function, Class<I> boundFInterfaceClass, T instance, MethodType methodType, String... methodNames) {
//        InstanceBinder<T, I> bind = LambdaBuilder.bind(function, boundFInterfaceClass, methodType, methodNames);
//        return bind.apply(instance);
//
//    }

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

    private static void validateGetFieldMethodHandle(MethodHandle methodHandle) {
        if (!isValidGETFIELD(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid GETFIELD methodhandle");
        }
    }

    private static void validateGetStaticMethodHandle(MethodHandle methodHandle) {
        if (!isValidGETSTATIC(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid GETSTATIC methodhandle");
        }
    }

    private static void validatePutFieldMethodHandle(MethodHandle methodHandle) {
        if (!isValidPUTFIELD(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid PUTFIELD methodhandle");
        }
    }

    private static void validatePutStaticMethodHandle(MethodHandle methodHandle) {
        if (!isValidPUTSTATIC(methodHandle)) {
            throw new LambdaBuildException(informativeToStringOutput(methodHandle) + " is not a valid PUTSTATIC methodhandle");
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

        /**
         * Alias for this::apply.
         * <p>
         * Creates a new instance of the lambda this InstanceBinder represents, with objectToBind as the constructor's argument
         *
         * @param objectToBind
         * @return
         */
        public INTERFACE createBoundLambda(INSTANCE objectToBind) {
            return this.apply(objectToBind);
        }

        @Override
        public INTERFACE apply(INSTANCE objectToBind) {
            try {
                return (INTERFACE)this.factory.invoke(objectToBind);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }
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

        public LambdaBuildException(Throwable cause) {
            super(cause);
        }

        public LambdaBuildException(String msg, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(msg, cause, enableSuppression, writableStackTrace);
        }

        LambdaBuildException(LambdaConversionException lambdaConversionException) {
            this("Conversion to lambda class failed", lambdaConversionException);
        }

        public LambdaBuildException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}

