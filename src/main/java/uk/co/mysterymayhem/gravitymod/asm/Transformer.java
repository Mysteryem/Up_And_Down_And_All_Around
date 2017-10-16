package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;
import uk.co.mysterymayhem.gravitymod.asm.patches.*;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.IClassName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.ClassPatcher;
import uk.co.mysterymayhem.gravitymod.asm.util.patching.PatchFailedException;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.GETFIELD;

/**
 * Transformer class that uses ASM to patch vanilla Minecraft classes
 * Created by Mysteryem on 2016-08-16.
 */
public class Transformer implements IClassTransformer {

    // Maps classes to their patch methods
    private static final HashMap<String, ClassPatcher> classNameToMethodMap = new HashMap<>();
    public static final Logger logger = LogManager.getLogger("UpAndDown-Core", StringFormatterMessageFactory.INSTANCE);

    public static final boolean DEBUG_AUTO_JUMP = false;
    //Set up bit mask
    public static final int GET_ROTATIONYAW = 0b1;
    public static final int GET_ROTATIONPITCH = 0b10;
    public static final int GET_PREVROTATIONYAW = 0b100;
    public static final int GET_PREVROTATIONPITCH = 0b1000;
    public static final int GET_ROTATIONYAWHEAD = 0b10000;
    public static final int GET_PREVROTATIONYAWHEAD = 0b100000;
    public static final int ALL_GET_ROTATION_VARS = GET_ROTATIONYAW | GET_ROTATIONPITCH | GET_PREVROTATIONYAW
            | GET_PREVROTATIONPITCH | GET_ROTATIONYAWHEAD | GET_PREVROTATIONYAWHEAD;

    static {
        // Superclass replaced with EntityPlayerWithGravity
        addClassPatch(new PatchEntityPlayerSubClass(Ref.AbstractClientPlayer));
        // Superclass replaced with EntityPlayerWithGravity
        addClassPatch(new PatchEntityPlayerSubClass(Ref.EntityPlayerMP));
        // Patches setListener
        addClassPatch(new PatchSoundManager());
        // Patches onUpdateWalkingPlayer, onLivingUpdate, isHeadspaceFree, updateAutoJump, moveEntity
        addClassPatch(new PatchEntityPlayerSP());
        // Patches handlePlayerPosLook
        addClassPatch(new PatchNetHandlerPlayClient());
        // Patches getMouseOver, drawNameplate
        addClassPatch(new PatchEntityRenderer());
        // Patches doRender
        addClassPatch(new PatchRenderLivingBase());
        // Patches moveEntity, moveRelative
        addClassPatch(new PatchEntity());
        // Patches moveEntityWithHeading, updateDistance, onUpdate, jump
        addClassPatch(new PatchEntityLivingBase());
        // Patches onItemUse, useItemRightClick, onPlayerStoppedUsing
        //      (all three are used to add compatibility with other mods, they are not otherwise used by this mod)
        addClassPatch(new PatchItemStack());
        // Patches processPlayer, processPlayerDigging
        addClassPatch(new PatchNetHandlerPlayServer());
        // Patches onUpdate
        addClassPatch(new PatchEntityOtherPlayerMP());
        // Patches getCollisionBoxes
        addClassPatch(new PatchWorld());
    }

    private static void addClassPatch(ClassPatcher patcher) {
        ClassPatcher oldVal = classNameToMethodMap.put(patcher.getClassName(), patcher);
        if (oldVal != null) {
            die("Duplicate class patch for " + oldVal.getClassName());
        }
    }

    /**
     * Used to ensure that patches are applied successfully.
     * If not, Minecraft will be made to crash.
     *
     * @param shouldLog           If we should log a success, or force a crash
     * @param dieMessage          Reason why we are forcing Minecraft to crash
     * @param formattableLogMsg   Formattable string a la String::format
     * @param objectsForFormatter Objects to be passed for formatting in log message
     */
    public static void logOrDie(boolean shouldLog, String dieMessage, String formattableLogMsg, Object... objectsForFormatter) {
        if (shouldLog) {
            log(formattableLogMsg, objectsForFormatter);
        }
        else {
            die(dieMessage);
        }
    }

    /**
     * Default logging method.
     *
     * @param string  Formattable string a la String::format
     * @param objects Objects to be passed for formatting in log message
     */
    public static void log(String string, Object... objects) {
        logger.info(string, objects);
    }

    /**
     * Default 'crash Minecraft' method.
     *
     * @param string Reason why we are forcing Minecraft to crash
     */
    public static void die(String string) throws PatchFailedException {
        throw new PatchFailedException("[UpAndDown] " + string);
    }

    public static void die(String string, Throwable cause) throws PatchFailedException {
        throw new PatchFailedException("[UpAndDown] " + string, cause);
    }

    /**
     * Debug helper method to print a class, from bytes, to "standard" output.
     * Useful to compare a class's bytecode, before and after patching if the bytecode is not what we expect, or if the
     * patched bytecode is invalid.
     *
     * @param bytes The bytes that make up the class
     */
    public static void printClassToStdOut(byte[] bytes) {
        printClassToStream(bytes, System.out);
    }

    /**
     * Internal method to print a class (from bytes), to an OutputStream.
     *
     * @param bytes        bytes that make up a class
     * @param outputStream stream to output the class's bytecode to
     */
    public static void printClassToStream(byte[] bytes, OutputStream outputStream) {
        ClassNode classNode2 = new ClassNode();
        ClassReader classReader2 = new ClassReader(bytes);
        PrintWriter writer = new PrintWriter(outputStream);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classNode2, writer);
        classReader2.accept(traceClassVisitor, 0);
        writer.flush();
    }

    public static void printClassToFMLLogger(byte[] bytes) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printClassToStream(bytes, byteArrayOutputStream);
        logger.info("\n%s", byteArrayOutputStream);
    }

    public static void printMethodToStdOut(MethodNode methodNode) {
        printMethodToStream(methodNode, System.out);
    }

    public static void printMethodToStream(MethodNode methodNode, OutputStream outputStream) {
        PrintWriter writer = new PrintWriter(outputStream);
        Textifier textifier = new Textifier();
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(textifier);
        methodNode.accept(traceMethodVisitor);
        textifier.print(writer);
        writer.flush();
    }

    public static void printMethodToFMLLogger(MethodNode methodNode) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printMethodToStream(methodNode, byteArrayOutputStream);
        logger.info("\n%s", byteArrayOutputStream);
    }

    public static void logPatchStarting(DeobfAwareString deobfAwareString) {
        logPatchStarting(deobfAwareString.getDeobf());
    }

    public static void logPatchStarting(Object object) {
        log("Patching %s", object);
    }

    /**
     * Use to add a local variable to a method.
     *
     * @param methodNode Method to add the localvariable to
     * @param iClassName The variable's class
     * @return The index of the new local variable
     */
    public static int addLocalVar(MethodNode methodNode, IClassName iClassName) {
        LocalVariablesSorter localVariablesSorter = new LocalVariablesSorter(methodNode.access, methodNode.desc, methodNode);
        return localVariablesSorter.newLocal(iClassName.asType());
    }

    public static void logPatchComplete(DeobfAwareString deobfAwareString) {
        logPatchComplete(deobfAwareString.getDeobf());
    }

    public static void logPatchComplete(Object object) {
        log("Patched  %s", object);
    }

    public static void dieIfFalse(boolean shouldContinue, ClassNode classNode) {
        dieIfFalse(shouldContinue, "Failed to find the methods to patch in " + classNode.name +
                ". The Minecraft version you are using likely does not match what the mod requires.");
    }

    /**
     * Option to used to compound multiple tests together
     *
     * @param shouldContinue boolean to check, if false, Minecraft is forced to crash with the passed dieMessage
     * @param dieMessage     The message to use as the cause when making Minecraft crash
     * @return
     */
    public static void dieIfFalse(boolean shouldContinue, String dieMessage) {
        if (!shouldContinue) {
            die(dieMessage);
        }
    }

    public static void patchMethodUsingAbsoluteRotations(MethodNode methodNode, int fieldBits) {
        patchMethodUsingAbsoluteRotations(methodNode, fieldBits, 1, -1);
    }

    @SuppressWarnings("unchecked")
    private static void patchMethodUsingAbsoluteRotations(MethodNode methodNode, int fieldBits, int minNumberOfPatches, int expectedNumberOfPatches) {
        if (fieldBits == 0) {
            return;
        }

        int numPatchesCompleted = 0;

        boolean changeRotationYaw = (fieldBits & GET_ROTATIONYAW) == GET_ROTATIONYAW;
        boolean changeRotationPitch = (fieldBits & GET_ROTATIONPITCH) == GET_ROTATIONPITCH;
        boolean changePrevRotationYaw = (fieldBits & GET_PREVROTATIONYAW) == GET_PREVROTATIONYAW;
        boolean changePrevRotationPitch = (fieldBits & GET_PREVROTATIONPITCH) == GET_PREVROTATIONPITCH;

        // Field introduced in EntityLivingBase
        boolean changeRotationYawHead = (fieldBits & GET_ROTATIONYAWHEAD) == GET_ROTATIONYAWHEAD;
        boolean changePrevRotationYawHead = (fieldBits & GET_PREVROTATIONYAWHEAD) == GET_PREVROTATIONYAWHEAD;

        BiPredicate<ListIterator<AbstractInsnNode>, FieldInsnNode>[] biPredicates = new BiPredicate[Integer.bitCount(fieldBits)];
        int index = 0;
        if (changeRotationYaw) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.Entity$rotationYaw_name.is(node.name)) {
                    Ref.Hooks$getRelativeYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.Entity$rotationPitch_name.is(node.name)) {
                    Ref.Hooks$getRelativePitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYaw) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.Entity$prevRotationYaw_name.is(node.name)) {
                    Ref.Hooks$getRelativePrevYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.Entity$prevRotationPitch_name.is(node.name)) {
                    Ref.Hooks$getRelativePrevPitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.EntityLivingBase$rotationYawHead_name.is(node.name)) {
                    Ref.Hooks$getRelativeYawHead.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (Ref.EntityLivingBase$prevRotationYawHead_name.is(node.name)) {
                    Ref.Hooks$getPrevRelativeYawHead.replace(iterator);
                    return true;
                }
                return false;
            };
//            index++;
        }

        for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
            AbstractInsnNode next = iterator.next();
            if (next.getOpcode() == GETFIELD/* && next instanceof FieldInsnNode*/) {
                FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                for (BiPredicate<ListIterator<AbstractInsnNode>, FieldInsnNode> biPredicate : biPredicates) {
                    if (biPredicate.test(iterator, fieldInsnNode)) {
                        break;
                    }
                }
            }
        }

        if (minNumberOfPatches < numPatchesCompleted) {
            die("Patching rotation field access to getRelative hook methods failed in " + methodNode.name);
        }
        if (expectedNumberOfPatches > 0 && numPatchesCompleted > expectedNumberOfPatches) {
            warn("Expected to patch rotation field access " + expectedNumberOfPatches + " times in " + methodNode
                    + ", but ended up patching " + numPatchesCompleted + " times instead");
        }
    }

    private static void warn(String string, Object... objects) {
        logger.warn(string, objects);
    }

    /**
     * Core transformer method. Recieves classes by name and their bytes and processes them if necessary.
     *
     * @param className            class name prior to deobfuscation (I think)
     * @param transformedClassName runtime deobfuscated class name
     * @param bytes                the bytes that make up the class sent to the transformer
     * @return the bytes of the now processed class
     */
    @Override
    public byte[] transform(String className, String transformedClassName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Function<byte[], byte[]> function = classNameToMethodMap.get(transformedClassName);

        if (function == null) {
            return bytes;
        }
        else {
            // Remove from the map to remove reference to the patcher so garbage collector can hopefully pick it up
            classNameToMethodMap.remove(transformedClassName);
            log("Patching class %s", transformedClassName);
            byte[] toReturn = function.apply(bytes);
            log("Patched class  %s", transformedClassName);
            return toReturn;
        }
    }
}
