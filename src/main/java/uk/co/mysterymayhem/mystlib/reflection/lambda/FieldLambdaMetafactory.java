package uk.co.mysterymayhem.mystlib.reflection.lambda;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;
import sun.misc.Unsafe;
import uk.co.mysterymayhem.mystlib.MystLib;
import uk.co.mysterymayhem.mystlib.reflection.LookupHelper;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Mysteryem on 2016-12-23.
 */
public class FieldLambdaMetafactory {
    private static final boolean DEBUG = false;

    static final MethodHandles.Lookup TRUSTED_LOOKUP = LookupHelper.getTrustedLookup();
    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String NO_ARGS_RETURN_VOID_DESCRIPTOR = Type.getMethodDescriptor(Type.VOID_TYPE);
    private static final int CLASSFILE_VERSION = 52;
    // Reference to the same 'counter' field in java.lang.invoke.InnerClassLambdaMetafactory
    private static final AtomicInteger counter;
    private static final Unsafe UNSAFE;

    static {
        try {
            Class<?> innerFactoryClass = Class.forName("java.lang.invoke.InnerClassLambdaMetafactory");
            MethodHandle counterGetter = TRUSTED_LOOKUP.findStaticGetter(innerFactoryClass, "counter", AtomicInteger.class);
            counter = (AtomicInteger)counterGetter.invokeExact();

            // Get Unsafe instance
            // Getting fields from innerFactoryClass as it's in the java.* package rather than from Unsafe.class which
            // is in the sun.* package
            Field[] declaredFields = innerFactoryClass.getDeclaredFields();
            Unsafe temp = null;
            for (Field declaredField : declaredFields) {
                if (declaredField.getType() == Unsafe.class) {
                    temp = (Unsafe)TRUSTED_LOOKUP.unreflectGetter(declaredField).invokeExact();
                    break;
                }
            }
            if (temp != null) {
                UNSAFE = temp;
            }
            else {
                throw new RuntimeException("Couldn't get Unsafe instance");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private final ClassWriter classWriter;
    private final Class<?> targetClass;
    private final String lambdaClassName;
    private final Class<?> functionalInterfaceClass;
    private final MethodType interfaceMethodType;
    private final FieldOpType opType;
    private final String interfaceMethodName;
    private final MethodHandle implMethod;
    private final MethodType handleMethodType;

    private FieldLambdaMetafactory(MethodHandles.Lookup caller,
            String interfaceMethodName,
            MethodType invokedType,
            MethodType interfaceMethodType,
            MethodHandle implMethod,
            MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
        this.implMethod = implMethod;
        this.opType = FieldOpType.getOpType(this.implMethod);

        // Sanity checks
        if (this.opType == null) {
            throw new LambdaBuilder.LambdaBuildException("Invalid methodhandle, must be a direct method handle of GET/PUT STATIC/FIELD type");
        }
        this.handleMethodType = handleMethodType;
        this.interfaceMethodType = interfaceMethodType;
        Class<?>[] handleParameters = this.handleMethodType.parameterArray();
        Class<?>[] ifaceParameters = this.interfaceMethodType.parameterArray();
        int numMethodParameters = this.opType.getNumMethodParameters();
        if (numMethodParameters != handleParameters.length) {
            throw new LambdaBuilder.LambdaBuildException("MethodHandle has " + handleParameters.length + " parameters, expected " + numMethodParameters + ". Pretty sure this should be impossible.");
        }
        if (handleParameters.length != ifaceParameters.length) {
            throw new LambdaBuilder.LambdaBuildException("Interface method has " + ifaceParameters.length + " parameters, expected " + handleParameters.length);
        }

        this.targetClass = caller.lookupClass();
        this.lambdaClassName = this.targetClass.getName().replace('.', '/') + "$$Lambda$" + counter.getAndIncrement();

        this.classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        this.functionalInterfaceClass = invokedType.returnType();

        this.interfaceMethodName = interfaceMethodName;
    }

    public static CallSite metaFactory(@Nonnull MethodHandles.Lookup caller,
            String interfaceMethodName,
            @Nonnull MethodType invokedType,
            MethodType interfaceMethodType,
            @Nonnull MethodHandle implMethod,
            MethodType instantiatedMethodType) throws LambdaBuilder.LambdaBuildException {
        return new FieldLambdaMetafactory(
                caller,
                interfaceMethodName,
                invokedType,
                interfaceMethodType,
                implMethod,
                instantiatedMethodType).buildCallSite();
    }

    private CallSite buildCallSite() throws LambdaBuilder.LambdaBuildException {
        // Create class
        this.classWriter.visit(CLASSFILE_VERSION, Opcodes.ACC_SUPER + Opcodes.ACC_FINAL + Opcodes.ACC_SYNTHETIC,
                this.lambdaClassName, null,
                OBJECT_INTERNAL_NAME, new String[]{Type.getInternalName(this.functionalInterfaceClass)});

        //TODO: Add support for instance bound MethodHandles and generating InstanceBinder classes, will take a single ? extends Object argument and store it to a single field
        // Create 0 args constructor
        MethodVisitor constructorVisitor = this.classWriter.visitMethod(
                Opcodes.ACC_PRIVATE, CONSTRUCTOR_NAME, MethodType.methodType(void.class).toMethodDescriptorString(), null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_INTERNAL_NAME, CONSTRUCTOR_NAME, NO_ARGS_RETURN_VOID_DESCRIPTOR, false);
        constructorVisitor.visitInsn(Opcodes.RETURN);
        // Maxs computer by ClassWriter.COMPUTE_MAXS, these arguments are ignored
        constructorVisitor.visitMaxs(-1, -1);
        constructorVisitor.visitEnd();

        this.opType.generateMethod(this.classWriter, this.interfaceMethodName, this.interfaceMethodType, this.implMethod, this.handleMethodType);

        this.classWriter.visitEnd();

        final byte[] classBytes = this.classWriter.toByteArray();

        if (DEBUG) {
            printClassToFMLLogger(classBytes);
        }

        // Loads the class with the same classloader and privileges as this.targetClass
        // So the class can access private fields/methods/constructors and use INVOKESPECIAL instructions with method
        Class<?> innerClass = UNSAFE.defineAnonymousClass(this.targetClass, classBytes, null);

        Constructor<?>[] constructors = innerClass.getDeclaredConstructors();
        if (constructors.length == 1) {
            constructors[0].setAccessible(true);
        }
        else {
            throw new LambdaBuilder.LambdaBuildException("Expected 1 cosntructor for generated class, got " + constructors.length);
        }

        try {
            Object inst = constructors[0].newInstance();
            return new ConstantCallSite(MethodHandles.constant(this.functionalInterfaceClass, inst));
        } catch (ReflectiveOperationException e) {
            throw new LambdaBuilder.LambdaBuildException(e);
        }
    }

    private static void printClassToFMLLogger(byte[] bytes) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printClassToStream(bytes, byteArrayOutputStream);
        MystLib.logger.info("Loading generated inner class lambda:\n%s", byteArrayOutputStream);
    }

    /**
     * Internal method to print a class (from bytes), to an OutputStream.
     *
     * @param bytes        bytes that make up a class
     * @param outputStream stream to output the class's bytecode to
     */
    private static void printClassToStream(byte[] bytes, OutputStream outputStream) {
        ClassNode classNode2 = new ClassNode();
        ClassReader classReader2 = new ClassReader(bytes);
        PrintWriter writer = new PrintWriter(outputStream);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classNode2, writer);
        classReader2.accept(traceClassVisitor, 0);
        writer.flush();
    }

    private enum FieldOpType {
        GETSTATIC(MethodHandleInfo.REF_getStatic, 0) {
            @Override
            void visitInterfaceMethod(MethodVisitor mv, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
                Class<?> ifaceReturnType = interfaceClassMethodType.returnType();
                MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(fieldMethodHandle);
                Field field = methodHandleInfo.reflectAs(Field.class, TRUSTED_LOOKUP);
                Class<?> fieldDeclaringClass = field.getDeclaringClass();
                Class<?> fieldType = field.getType();
                if (!ifaceReturnType.isAssignableFrom(fieldType)) {
                    throw new LambdaBuilder.LambdaBuildException("Cannot return a " + fieldType + " as a " + ifaceReturnType);
                }
                String name = field.getName();
                mv.visitFieldInsn(Opcodes.GETSTATIC, Type.getInternalName(fieldDeclaringClass), name, Type.getDescriptor(fieldType));
                //TODO: No need for a CHECKCAST?
                mv.visitInsn(getReturnOpcode(ifaceReturnType));
            }
        },
        PUTSTATIC(MethodHandleInfo.REF_putStatic, 1) {
            @Override
            void visitInterfaceMethod(MethodVisitor mv, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
                if (interfaceClassMethodType.returnType() != void.class) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method return type must be 'void'");
                }
                MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(fieldMethodHandle);
                Field field = methodHandleInfo.reflectAs(Field.class, TRUSTED_LOOKUP);
                Class<?> fieldDeclaringClass = field.getDeclaringClass();
                Class<?> fieldType = field.getType();
                String name = field.getName();

                Class<?> ifaceArgClass = interfaceClassMethodType.parameterArray()[0];
                if (!ifaceArgClass.isAssignableFrom(fieldType)) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method parameter " + ifaceArgClass + " cannot represent a " + fieldType);
                }

                // Load the method's single parameter (at index 1 as 0 is occupied by 'this')
                mv.visitVarInsn(getLoadOpcode(ifaceArgClass), 1);
                if (fieldType != ifaceArgClass) {
                    // Can't be a primitive
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fieldType));
                }
                mv.visitFieldInsn(Opcodes.PUTSTATIC, Type.getInternalName(fieldDeclaringClass), name, Type.getDescriptor(fieldType));
                mv.visitInsn(Opcodes.RETURN);
            }
        },
        GETFIELD(MethodHandleInfo.REF_getField, 1) {
            @Override
            void visitInterfaceMethod(MethodVisitor mv, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
                Class<?> ifaceReturnType = interfaceClassMethodType.returnType();
                MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(fieldMethodHandle);
                Field field = methodHandleInfo.reflectAs(Field.class, TRUSTED_LOOKUP);
                Class<?> fieldDeclaringClass = field.getDeclaringClass();
                Class<?> fieldType = field.getType();
                String name = field.getName();
                Class<?> ifaceArgClass = interfaceClassMethodType.parameterArray()[0];

                if (!ifaceArgClass.isAssignableFrom(fieldDeclaringClass)) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method parameter " + ifaceArgClass + " cannot represent a " + fieldDeclaringClass);
                }
                if (!ifaceReturnType.isAssignableFrom(fieldType)) {
                    throw new LambdaBuilder.LambdaBuildException("Cannot return a " + fieldType + " as a " + ifaceReturnType);
                }
                // Load the method's single parameter (at index 1 as 0 is occupied by 'this')
                mv.visitVarInsn(getLoadOpcode(ifaceArgClass), 1);
                if (fieldDeclaringClass != ifaceArgClass) {
                    // Can't be a primitive
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fieldDeclaringClass));
                }
                mv.visitFieldInsn(Opcodes.GETFIELD, Type.getInternalName(fieldDeclaringClass), name, Type.getDescriptor(fieldType));
                //TODO: No need for a CHECKCAST?
                mv.visitInsn(getReturnOpcode(ifaceReturnType));
            }
        },
        PUTFIELD(MethodHandleInfo.REF_putField, 2) {
            @Override
            void visitInterfaceMethod(MethodVisitor mv, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
                if (interfaceClassMethodType.returnType() != void.class) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method return type must be 'void'");
                }
                MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(fieldMethodHandle);
                Field field = methodHandleInfo.reflectAs(Field.class, TRUSTED_LOOKUP);
                Class<?> fieldDeclaringClass = field.getDeclaringClass();
                Class<?> fieldType = field.getType();
                String name = field.getName();
                Class<?> ifaceInstanceArgClass = interfaceClassMethodType.parameterArray()[0];
                Class<?> ifaceValueToPutClass = interfaceClassMethodType.parameterArray()[1];

                if (!ifaceInstanceArgClass.isAssignableFrom(fieldDeclaringClass)) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method parameter " + ifaceInstanceArgClass + " cannot represent a " + fieldDeclaringClass);
                }
                if (!ifaceValueToPutClass.isAssignableFrom(fieldType)) {
                    throw new LambdaBuilder.LambdaBuildException("Interface method parameter " + ifaceValueToPutClass + " cannot represent a " + fieldType);
                }

                // Load the instance
                mv.visitVarInsn(getLoadOpcode(ifaceInstanceArgClass), 1);
                if (fieldDeclaringClass != ifaceInstanceArgClass) {
                    // Can't be a primitive
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fieldDeclaringClass));
                }
                // First argument must always be a <? extends Object>, so +1 to previous index for next parameter
                mv.visitVarInsn(getLoadOpcode(ifaceValueToPutClass), 2);
                if (fieldType != ifaceValueToPutClass) {
                    // Can't be a primitive
                    mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(fieldType));
                }
                mv.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(fieldDeclaringClass), name, Type.getDescriptor(fieldType));
                mv.visitInsn(Opcodes.RETURN);
            }
        };

        private static final TIntObjectHashMap<FieldOpType> LOOKUP_FROM_INT = new TIntObjectHashMap<>();

        static {
            for (FieldOpType fieldOpType : FieldOpType.values()) {
                LOOKUP_FROM_INT.put(fieldOpType.referenceKind, fieldOpType);
            }
        }

        private final int referenceKind;
        private final int numMethodParameters;

        FieldOpType(int referenceKind, int numMethodParameters) {
            this.referenceKind = referenceKind;
            this.numMethodParameters = numMethodParameters;
        }

        public static FieldOpType getOpType(MethodHandle methodHandle) {
            try {
                MethodHandleInfo methodHandleInfo = TRUSTED_LOOKUP.revealDirect(methodHandle);
                int referenceKind = methodHandleInfo.getReferenceKind();
                return LOOKUP_FROM_INT.get(referenceKind);
            } catch (Exception e) {
                //It's probably not a direct handle
                return null;
            }
        }

        static int getLoadOpcode(Class<?> c) {
            if (c == Void.TYPE) {
                throw new InternalError("Unexpected void type of load opcode");
            }
            return Opcodes.ILOAD + getOpcodeOffset(c);
        }

        private static int getOpcodeOffset(Class<?> c) {
            if (c.isPrimitive()) {
                if (c == Long.TYPE) {
                    return 1;
                }
                else if (c == Float.TYPE) {
                    return 2;
                }
                else if (c == Double.TYPE) {
                    return 3;
                }
                return 0;
            }
            else {
                return 4;
            }
        }

        static int getReturnOpcode(Class<?> c) {
            if (c == Void.TYPE) {
                return Opcodes.RETURN;
            }
            return Opcodes.IRETURN + getOpcodeOffset(c);
        }
//            mv.visitFieldInsn(Opcodes.GETSTATIC, "uk/co/mysterymayhem/gravitymod/DebugHelperListener", "privateStringField", "Ljava/lang/String;");
//            mv.visitInsn(Opcodes.ARETURN);

        private static int getParameterSize(Class<?> c) {
            if (c == Void.TYPE) {
                return 0;
            }
            else if (c == Long.TYPE || c == Double.TYPE) {
                return 2;
            }
            return 1;
        }

        public void generateMethod(ClassWriter classWriter, String interfaceClassMethodName, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException {
            if (this.numMethodParameters != interfaceClassMethodType.parameterCount() || this.numMethodParameters != handleMethodType.parameterCount()) {
                throw new LambdaBuilder.LambdaBuildException("Wrong number of parameters when trying to generate method");
            }
            MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, interfaceClassMethodName, interfaceClassMethodType.toMethodDescriptorString(), null, null);
            // Hides the method from stacktraces....I think (same behaviour as normal metafactory lambdas)
            mv.visitAnnotation("Ljava/lang/invoke/LambdaForm$Hidden;", true);
            mv.visitCode();
            this.visitInterfaceMethod(mv, interfaceClassMethodType, fieldMethodHandle, handleMethodType);
            // ClassWriter will calculate maxes
            mv.visitMaxs(-1, -1);
            mv.visitEnd();
        }

        abstract void visitInterfaceMethod(MethodVisitor mv, MethodType interfaceClassMethodType, MethodHandle fieldMethodHandle, MethodType handleMethodType) throws LambdaBuilder.LambdaBuildException;

        public int getNumMethodParameters() {
            return this.numMethodParameters;
        }

        public int getReferenceKind() {
            return this.referenceKind;
        }
    }
}
