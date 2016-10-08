package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.TraceClassVisitor;

import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.MethodInstruction;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.ObjectClassName;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.MethodName;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.FieldName;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.MethodDesc;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.FieldInstruction;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.INIT;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.VOID;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.DOUBLE;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.INT;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.BOOLEAN;
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.FLOAT;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Created by Mysteryem on 2016-08-16.
 */
public class Transformer implements IClassTransformer {

    // Maps classes to their patch methods
    private static final HashMap<String, Function<byte[], byte[]>> classNameToMethodMap = new HashMap<>();
    private static final String classToReplace = "net/minecraft/entity/player/EntityPlayer";
    private static final String classReplacement = "uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity";

    static {
        classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayerMP", Transformer::patchEntityPlayerSubClass);
        classNameToMethodMap.put("net.minecraft.client.entity.AbstractClientPlayer", Transformer::patchEntityPlayerSubClass);
        classNameToMethodMap.put("net.minecraft.client.entity.EntityPlayerSP", Transformer::patchEntityPlayerSP);
        classNameToMethodMap.put("net.minecraft.network.NetHandlerPlayServer", Transformer::patchNetHandlerPlayServer);
        classNameToMethodMap.put("net.minecraft.entity.Entity", Transformer::patchEntity);
        classNameToMethodMap.put("net.minecraft.entity.EntityLivingBase", Transformer::patchEntityLivingBase);
        classNameToMethodMap.put("net.minecraft.client.renderer.EntityRenderer", Transformer::patchEntityRenderer);
        classNameToMethodMap.put("net.minecraft.client.audio.SoundManager", Transformer::patchSoundManager);
        classNameToMethodMap.put("net.minecraft.client.renderer.entity.RenderLivingBase", Transformer::patchRenderLivingBase);
        classNameToMethodMap.put("net.minecraft.client.network.NetHandlerPlayClient", Transformer::patchNetHandlerPlayClient);
    }

    /**
     * Default logging method.
     * @param string Formattable string a la String::format
     * @param objects Objects to be passed for formatting in log message
     */
    private static void log(String string, Object... objects) {
        FMLLog.info("[UpAndDown] " + string, objects);
    }

    private static void logPatchStarting(Object object) {
        log("Patching %s", object);
    }

    private static void logPatchComplete(Object object) {
        log("Patched %s", object);
    }

    private static void warn(String string, Object... objects) {
        FMLLog.warning("[UpAndDown] " + string, objects);
    }

    /**
     * Default 'crash Minecraft' method.
     * @param string Reason why we are forcing Minecraft to crash
     */
    private static void die(String string) {
        throw new RuntimeException("[UpAndDown] " + string);
    }

    /**
     * Option to used to compound multiple tests together
     * @param shouldContinue
     * @param dieMessage
     * @return
     */
    private static boolean dieOrTrue(boolean shouldContinue, String dieMessage) {
        if (shouldContinue) {
            return true;
        }
        else {
            die(dieMessage);
            //not that this return matters
            return false;
        }
    }


    /**
     * Used to ensure that patches are applied successfully.
     * If not, Minecraft will be made to crash.
     * @param shouldLog If we should log a success, or force a crash
     * @param dieMessage Reason why we are forcing Minecraft to crash
     * @param formattableLogMsg Formattable string a la String::format
     * @param objectsForFormatter Objects to be passed for formatting in log message
     */
    private static void logOrDie(boolean shouldLog, String dieMessage, String formattableLogMsg, Object... objectsForFormatter) {
        if (shouldLog) {
            log(formattableLogMsg, objectsForFormatter);
        }
        else {
            die(dieMessage);
        }
    }

    /**
     * Debug helper method to print a class, from bytes, to "standard" output.
     * Useful to compare a class's bytecode, before and after patching if the bytecode is not what we expect, or if the
     * patched bytecode is invalid.
     * @param bytes The bytes that make up the class
     */
    private static void printClassToStdOut(byte[] bytes) {
        printClassToStream(bytes, System.out);
    }

    /**
     * Internal method to print a class (from bytes), to an OutputStream.
     * @param bytes
     * @param outputStream
     */
    private static void printClassToStream(byte[] bytes, OutputStream outputStream) {
        ClassNode classNode2 = new ClassNode();
        ClassReader classReader2 = new ClassReader(bytes);
        PrintWriter writer = new PrintWriter(outputStream);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(classNode2, writer);
        classReader2.accept(traceClassVisitor, 0);
    }

    /**
     * See the comment on the DeobfHelper class
     */
    private static DeobfHelper HELPER = null;

    /**
     * The Transformer class is instantiated before we know if we are in a dev environment or not, this means that I
     * would not be able to make all the static or instance fields for the various methods and fields final and it would
     * mean having the declarations and assignments in different places.
     *
     * Instead, I have opted to introduce a class which will be created once we know if we're in a dev environment.
     * This class can then have all of its fields be final and can have the declaration and assignment done at the same
     * time.
     *
     * Fields have package-local access so as to prevent compiler from creating an accessor method for every field
     */
    private static class DeobfHelper {
        /*
            Mojang method names
         */
        final MethodName AxisAlignedBB$calculateXOffset_name = new MethodName("calculateXOffset", "func_72316_a");
        final MethodName AxisAlignedBB$calculateYOffset_name = new MethodName("calculateYOffset", "func_72323_b");
        final MethodName AxisAlignedBB$calculateZOffset_name = new MethodName("calculateZOffset", "func_72322_c");

        // () arguments
        final MethodName BlockPos$down_NO_ARGS_name = new MethodName("down", "func_177977_b");

        // (DDD) arguments
        final MethodName BlockPos$PooledMutableBlockPos$retain_name = new MethodName("retain", "func_185345_c");
        // (DDD) arguments
        final MethodName BlockPos$PooledMutableBlockPos$setPos_name = new MethodName("setPos", "func_189532_c");

        final MethodName Entity$getEntityBoundingBox_name = new MethodName("getEntityBoundingBox", "func_174813_aQ");
        final MethodName Entity$getLookVec_name = new MethodName("getLookVec", "func_70040_Z");
        final MethodName Entity$isOffsetPositionInLiquid_name = new MethodName("isOffsetPositionInLiquid", "func_70038_c");
        final MethodName Entity$moveEntity_name = new MethodName("moveEntity", "func_70091_d");
        final MethodName Entity$moveRelative_name = new MethodName("moveRelative", "func_70060_a");
        final MethodName Entity$onUpdate_name = new MethodName("onUpdate", "func_70071_h_");

        final MethodName EntityLivingBase$jump_name = new MethodName("jump", "func_70664_aZ");
        final MethodName EntityLivingBase$moveEntityWithHeading_name = new MethodName("moveEntityWithHeading", "func_70612_e");
        final MethodName EntityLivingBase$updateDistance_name = new MethodName("updateDistance", "func_110146_f");

        final MethodName EntityRenderer$getMouseOver_name = new MethodName("getMouseOver", "func_78473_a");

        final MethodName RenderLivingBase$doRender_name = new MethodName("doRender", "func_76986_a");

        final MethodName World$getEntitiesInAABBexcluding_name = new MethodName("getEntitiesInAABBexcluding", "func_175674_a");

        /*
            Mojang field names
         */
        final FieldName Blocks$LADDER_name = new FieldName("LADDER", "field_150468_ap");
        final FieldName Entity$posX_name = new FieldName("posX", "field_70165_t");
        final FieldName Entity$posY_name = new FieldName("posY", "field_70163_u");
        final FieldName Entity$posZ_name = new FieldName("posZ", "field_70161_v");
        final FieldName Entity$prevPosX_name = new FieldName("prevPosX", "field_70169_q");
        final FieldName Entity$prevPosZ_name = new FieldName("prevPosZ", "field_70166_s");
        final FieldName Entity$prevRotationPitch_name = new FieldName("prevRotationPitch", "field_70127_C");
        final FieldName Entity$prevRotationYaw_name = new FieldName("prevRotationYaw", "field_70126_B");
        final FieldName Entity$rotationPitch_name = new FieldName("rotationPitch", "field_70125_A");
        final FieldName Entity$rotationYaw_name = new FieldName("rotationYaw", "field_70177_z");
        final FieldName EntityLivingBase$limbSwing_name = new FieldName("limbSwing", "field_184619_aG");
        final FieldName EntityLivingBase$limbSwingAmount_name = new FieldName("limbSwingAmount", "field_70721_aZ");
        final FieldName EntityLivingBase$prevRotationYawHead_name = new FieldName("prevRotationYawHead", "field_70758_at");
        final FieldName EntityLivingBase$rotationYawHead_name = new FieldName("rotationYawHead", "field_70759_as");

        /*
            Shared reference class names
         */
        final ObjectClassName AxisAlignedBB = new ObjectClassName("net/minecraft/util/math/AxisAlignedBB");
        final ObjectClassName Block = new ObjectClassName("net/minecraft/block/Block");
        final ObjectClassName BlockPos = new ObjectClassName("net/minecraft/util/math/BlockPos");
        final ObjectClassName BlockPos$PooledMutableBlockPos = new ObjectClassName("net/minecraft/util/math/BlockPos$PooledMutableBlockPos");
        final ObjectClassName Blocks = new ObjectClassName("net/minecraft/init/Blocks");
        final ObjectClassName Entity = new ObjectClassName("net/minecraft/entity/Entity");
        final ObjectClassName EntityLivingBase = new ObjectClassName("net/minecraft/entity/EntityLivingBase");
        final ObjectClassName Hooks = new ObjectClassName("uk/co/mysterymayhem/gravitymod/asm/Hooks");
        final ObjectClassName List = new ObjectClassName("java/util/List");
        final ObjectClassName Predicate = new ObjectClassName("com/google/common/base/Predicate");
        final ObjectClassName Vec3d = new ObjectClassName("net/minecraft/util/math/Vec3d");
        final ObjectClassName WorldClient = new ObjectClassName("net/minecraft/client/multiplayer/WorldClient");

        /*
            Mojang field instructions
         */
        final FieldInstruction Blocks$LADDER_GET = new FieldInstruction(GETSTATIC, Blocks, Blocks$LADDER_name, Block);
        final FieldInstruction Entity$posX_GET = new FieldInstruction(GETFIELD, Entity, Entity$posX_name, DOUBLE);
        final FieldInstruction EntityLivingBase$limbSwing_PUT = new FieldInstruction(PUTFIELD, EntityLivingBase, EntityLivingBase$limbSwing_name, FLOAT);
        final FieldInstruction EntityLivingBase$limbSwingAmount_GET = new FieldInstruction(GETFIELD, EntityLivingBase, EntityLivingBase$limbSwingAmount_name, FLOAT);
        final FieldInstruction EntityLivingBase$posX_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$posX_name, DOUBLE);
        final FieldInstruction EntityLivingBase$posY_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$posY_name, DOUBLE);
        final FieldInstruction EntityLivingBase$posZ_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$posZ_name, DOUBLE);
        final FieldInstruction EntityLivingBase$prevPosX_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$prevPosX_name, DOUBLE);
        final FieldInstruction EntityLivingBase$prevPosZ_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$prevPosZ_name, DOUBLE);
        final FieldInstruction EntityLivingBase$rotationPitch_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$rotationPitch_name, FLOAT);
        final FieldInstruction EntityLivingBase$rotationYaw_GET = new FieldInstruction(GETFIELD, EntityLivingBase, Entity$rotationYaw_name, FLOAT);

        /*
            Mojang method instructions
         */
        // While there could be a constructor for MethodInstructions that doesn't require a MethodDesc, it is useful to
        // visually split apart the arguments so it's easier to tell what each argument is
        final MethodInstruction AxisAlignedBB$calculateXOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateXOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction AxisAlignedBB$calculateYOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateYOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction AxisAlignedBB$calculateZOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateZOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction BlockPos$down_NO_ARGS = new MethodInstruction(INVOKEVIRTUAL, BlockPos, BlockPos$down_NO_ARGS_name, new MethodDesc(BlockPos));
        final MethodInstruction BlockPos$INIT = new MethodInstruction(INVOKESPECIAL, BlockPos, INIT, new MethodDesc(VOID, INT, INT, INT));
        final MethodInstruction BlockPos$PooledMutableBlockPos$setPos = new MethodInstruction(
                INVOKEVIRTUAL, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$setPos_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction BlockPos$PooledMutableBlockPos$retain = new MethodInstruction(
                INVOKESTATIC, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$retain_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction Entity$getEntityBoundingBox = new MethodInstruction(INVOKEVIRTUAL, Entity, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
        final MethodInstruction EntityLivingBase$getLookVec = new MethodInstruction(INVOKEVIRTUAL, EntityLivingBase, Entity$getLookVec_name, new MethodDesc(Vec3d));
        final MethodInstruction EntityLivingBase$isOffsetPositionInLiquid = new MethodInstruction(
                INVOKEVIRTUAL, EntityLivingBase, Entity$isOffsetPositionInLiquid_name, new MethodDesc(BOOLEAN, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction WorldClient$getEntitiesInAABBexcluding = new MethodInstruction(
                INVOKEVIRTUAL, WorldClient, World$getEntitiesInAABBexcluding_name, new MethodDesc(List, Entity, AxisAlignedBB, Predicate));

        /*
            Up And Down And All Around asm hook method instructions
         */
        final MethodInstruction Hooks$getBlockPostBelowEntity = new MethodInstruction(INVOKESTATIC, Hooks, "getBlockPosBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, Entity));
        final MethodInstruction Hooks$getImmutableBlockPosBelowEntity = new MethodInstruction(INVOKESTATIC, Hooks, "getImmutableBlockPosBelowEntity", new MethodDesc(BlockPos, Entity));
        final MethodInstruction Hooks$getPrevRelativeYawHead = new MethodInstruction(INVOKESTATIC, Hooks, "getPrevRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
        final MethodInstruction Hooks$getRelativeDownBlockPos = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativeDownBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
        final MethodInstruction Hooks$getRelativeLookVec = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativeLookVec", new MethodDesc(Vec3d, Entity));
        final MethodInstruction Hooks$getRelativePitch = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePitch", new MethodDesc(FLOAT, Entity));
        final MethodInstruction Hooks$getRelativePrevPitch = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePrevPitch", new MethodDesc(FLOAT, Entity));
        final MethodInstruction Hooks$getRelativePrevPosX = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePrevPosX", new MethodDesc(DOUBLE, Entity));
        final MethodInstruction Hooks$getRelativePrevPosZ = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePrevPosZ", new MethodDesc(DOUBLE, Entity));
        final MethodInstruction Hooks$getRelativePrevYaw = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePrevYaw", new MethodDesc(FLOAT, Entity));
        final MethodInstruction Hooks$getRelativePosX = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePosX", new MethodDesc(DOUBLE, Entity));
        final MethodInstruction Hooks$getRelativePosY = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePosY", new MethodDesc(DOUBLE, Entity));
        final MethodInstruction Hooks$getRelativePosZ = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativePosZ", new MethodDesc(DOUBLE, Entity));
        final MethodInstruction Hooks$getRelativeYaw = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativeYaw", new MethodDesc(FLOAT, Entity));
        final MethodInstruction Hooks$getRelativeYawHead = new MethodInstruction(INVOKESTATIC, Hooks, "getRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
        final MethodInstruction Hooks$getVanillaEntityBoundingBox = new MethodInstruction(INVOKESTATIC, Hooks, "getVanillaEntityBoundingBox", new MethodDesc(AxisAlignedBB, Entity));
        final MethodInstruction Hooks$inverseAdjustXYZ = new MethodInstruction(INVOKESTATIC, Hooks, "inverseAdjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction Hooks$makePositionAbsolute = new MethodInstruction(INVOKESTATIC, Hooks, "makePositionAbsolute", new MethodDesc(VOID, EntityLivingBase));
        final MethodInstruction Hooks$makePositionRelative = new MethodInstruction(INVOKESTATIC, Hooks, "makePositionRelative", new MethodDesc(VOID, EntityLivingBase));
        final MethodInstruction Hooks$reverseXOffset = new MethodInstruction(INVOKESTATIC, Hooks, "reverseXOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final MethodInstruction Hooks$reverseYOffset = new MethodInstruction(INVOKESTATIC, Hooks, "reverseYOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final MethodInstruction Hooks$reverseZOffset = new MethodInstruction(INVOKESTATIC, Hooks, "reverseZOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final MethodInstruction Hooks$setPooledMutableBlockPosToBelowEntity = new MethodInstruction(
                INVOKESTATIC, Hooks, "setPooledMutableBlockPosToBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos, Entity));
    }


    /**
     * Called when we know if we're in a dev environment or not
     */
    static void setup() {
        // initialise all the DeobfAware stuff here
        Transformer.HELPER = new DeobfHelper();
    }

    /**
     * Core transformer method. Recieves classes by name and their bytes and processes them if necessary.
     * @param className
     * @param transformedClassName
     * @param bytes
     * @return
     */
    @Override
    public byte[] transform(String className, String transformedClassName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        Function<byte[], byte[]> function = classNameToMethodMap.get(transformedClassName);

        if (function == null) {
            return bytes;
        } else {
            log("Patching %s", className);
            byte[] toReturn = function.apply(bytes);
            log("Finished patching %s", className);
            return toReturn;
        }
    }

    /**
     * Inserts UpAndDown's EntityPlayerWithGravity class into EntityPlayerMP's and AbstractClientPlayer's hierarchy.
     * This is done to significantly lower the amount of ASM required and make it easier to code and debug the mod.
     * @param bytes
     * @return
     */
    private static byte[] patchEntityPlayerSubClass(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        classNode.superName = classReplacement;

        for (MethodNode methodNode : classNode.methods) {
            for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                AbstractInsnNode next = iterator.next();
                switch (next.getType()) {
                    case (AbstractInsnNode.TYPE_INSN):
                        TypeInsnNode typeInsnNode = (TypeInsnNode) next;
                        if (typeInsnNode.desc.equals(classToReplace)) {
                            typeInsnNode.desc = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.FIELD_INSN):
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) next;
                        if (fieldInsnNode.owner.equals(classToReplace)) {
                            fieldInsnNode.owner = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.METHOD_INSN):
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (methodInsnNode.owner.equals(classToReplace)) {
                            methodInsnNode.owner = classReplacement;
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        log("Injected super class into " + classNode.name);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchRenderLivingBase(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.RenderLivingBase$doRender_name.is(methodNode)) {
                logPatchStarting(HELPER.RenderLivingBase$doRender_name);
                patchMethodUsingAbsoluteRotations(methodNode, ALL_GET_ROTATION_VARS);
                logPatchComplete(HELPER.RenderLivingBase$doRender_name);
                break;
            }
        }

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityRenderer(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        outerfor:
        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.EntityRenderer$getMouseOver_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityRenderer$getMouseOver_name);
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (HELPER.WorldClient$getEntitiesInAABBexcluding.is(next)) {
                        iterator.previous();
                        while(iterator.hasPrevious()) {
                            next = iterator.previous();
                            if (HELPER.Entity$getEntityBoundingBox.is(next)) {
                                HELPER.Hooks$getVanillaEntityBoundingBox.replace(iterator);
                                logPatchComplete(HELPER.EntityRenderer$getMouseOver_name);
                                break outerfor;
                            }
                        }
                        die("Failed to find " + HELPER.WorldClient$getEntitiesInAABBexcluding + " in " + classNode.name + "::" + HELPER.EntityRenderer$getMouseOver_name);
                    }
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntity(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);



        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.Entity$moveEntity_name.is(methodNode)) {
                int numReplaced = 0;
                boolean newBlockPosFound = false;
                boolean blockPos$downFound = false;
                boolean Blocks$LADDERFound = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (HELPER.AxisAlignedBB$calculateXOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseXOffset.replace(iterator);
                            numReplaced++;
                        }
                        else if (HELPER.AxisAlignedBB$calculateYOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseYOffset.replace(iterator);
                            numReplaced++;
                        }
                        else if (HELPER.AxisAlignedBB$calculateZOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseZOffset.replace(iterator);
                            numReplaced++;
                        }
                        else if (!newBlockPosFound && HELPER.BlockPos$INIT.is(methodInsnNode)) {
                            // Starts with a simple 1:1 replacement
                            HELPER.Hooks$getImmutableBlockPosBelowEntity.replace(iterator);
                            iterator.previous(); // Our INNVOKESTATIC instruction

                            // Repeatedly remove the previous instruction until it's GETFIELD Entity.posX
                            // Remove that too
                            // If we were to call iterator.previous() again, we would get the ALOAD 0 that would normally
                            // be consumed by the GETFIELD instruction we just removed
                            // And it just so happens that our INVOKESTATIC instruction needs to have ALOAD 0 before it
                            // So we cut out a bunch of no longer used instructions and inserted our own instruction
                            // (hopefully it's fine to have local variables that are never used in bytecode)
                            for (;iterator.hasPrevious() && !newBlockPosFound;) {
                                AbstractInsnNode previous = iterator.previous();
                                if (HELPER.Entity$posX_GET.is(previous)) {
                                    newBlockPosFound = true;
                                }
                                //TODO: Do the labels and line numbers need to be left in? Just the labels?
//                                if (previous instanceof LineNumberNode) {
//                                    continue;
//                                }
                                if (previous instanceof LabelNode) {
                                    continue;
                                }
                                iterator.remove();
                            }

                            if(!newBlockPosFound) {
                                die("Failed to find " + HELPER.Entity$posX_GET + " prior to " + HELPER.BlockPos$INIT);
                            }
                        }
                        else if (newBlockPosFound && !blockPos$downFound && HELPER.BlockPos$down_NO_ARGS.is(methodInsnNode)) {
                            HELPER.Hooks$getRelativeDownBlockPos.replace(iterator);
                            iterator.previous();
                            VarInsnNode aload0 = new VarInsnNode(Opcodes.ALOAD, 0);
                            iterator.add(aload0);
                            blockPos$downFound = true;
                        }
                    }
                    else if (next instanceof FieldInsnNode
                            && newBlockPosFound && blockPos$downFound
                            /*&& !Blocks$LADDERFound*/) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        //TODO: Work out a way to find the correct localvariable indices instead of hardcoding them
                        if (!Blocks$LADDERFound && HELPER.Blocks$LADDER_GET.is(fieldInsnNode)){
                            iterator.previous(); // returns the same GETSTATIC instruction
                            iterator.previous(); // should be ALOAD 29

                            int countToUndo = 0;
                            final int posZVar; //34/77
                            while(true) {
                                countToUndo++;
                                AbstractInsnNode previous = iterator.previous();
                                if (previous.getOpcode() == Opcodes.DSTORE) {
                                    posZVar = ((VarInsnNode)previous).var;
                                    break;
                                }
                            }
                            for(; countToUndo > 0; countToUndo--) {
                                iterator.next();
                            }
                            final int posYVar = posZVar - 2; //32/75
                            final int posXVar = posYVar - 2; //30/73

                            // Start adding instructions just before the ALOAD 29 instruction
                            /*
                              Instruction // Top of stack
                                ALOAD 0   // this
                                DLOAD 30  // this,d12
                                DLOAD 32  // this,d12,d13
                                DLOAD 34  // this,d12,d13,d14
                                INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                                          // doubles
                                DUP       // doubles,doubles
                                DUP       // doubles,doubles,doubles
                                ICONST_0  // doubles,doubles,doubles,0
                                DALOAD    // doubles,doubles,doubles[0]
                                DSTORE 30 // doubles,doubles
                                ICONST_1  // doubles,doubles,1
                                DALOAD    // doubles,doubles[1]
                                DSTORE 32 // doubles
                                ICONST_2  // doubles,2
                                DALOAD    // doubles[2]
                                DSTORE 34 // [empty]
                            */
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posXVar));//30
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posYVar));//32
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, posZVar));//34
                            HELPER.Hooks$inverseAdjustXYZ.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posXVar));//30
                            iterator.add(new InsnNode(Opcodes.ICONST_1));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posYVar));//32
                            iterator.add(new InsnNode(Opcodes.ICONST_2));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            iterator.add(new VarInsnNode(Opcodes.DSTORE, posZVar));//34
                            Blocks$LADDERFound = true;
                        }
                    }
                }
                logOrDie(numReplaced > 0, "Could not find any AxisAlignedBB:calculate[XYZ]Offset methods in " + classNode.name + "::" + HELPER.Entity$moveEntity_name,
                        "Replaced " + numReplaced + " usages of AxisAlignedBB:calculate[XYZ]Offset with Hooks::reverse[XYZ]Offset in %s::%s", classNode.name, HELPER.Entity$moveEntity_name);
                logOrDie(newBlockPosFound, "Failed to find new instance creation of BlockPos in " + classNode.name + "::" + HELPER.Entity$moveEntity_name,
                        "Found new instance of BlockPos in %s::%s", classNode.name, HELPER.Entity$moveEntity_name);
                logOrDie(blockPos$downFound, "Failed to find usage of BlockPos::down in " + classNode.name + "::" + HELPER.Entity$moveEntity_name,
                        "Found usage of BlockPos::down in %s::%s", classNode.name, HELPER.Entity$moveEntity_name);
                logOrDie(Blocks$LADDERFound, "Failed to find usage of Blocks.LADDER in " + classNode.name + "::" + HELPER.Entity$moveEntity_name,
                        "Found usage of Blocks.LADDER in ");
            }
            else if (HELPER.Entity$moveRelative_name.is(methodNode)) {
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
            }
        }





        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityLivingBase(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);


        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.EntityLivingBase$moveEntityWithHeading_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$moveEntityWithHeading_name);

                boolean replaced_getLookVec = false;

                boolean replaced_rotationPitch = false;
                boolean BlockPos$PooledMutableBlockPos$retainFound = false;
                boolean BlockPos$PooledMutableBlockPos$setPosFound = false;
                int numEntityLivingBase$isOffsetPositionInLiquidFound = 0;
                MethodInsnNode previousisOffsetPositionInLiquidMethodInsnNode = null;
                boolean GETFIELD_limbSwingAmount_found = false;
                boolean PUTFIELD_limbSwing_found = false;


                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (!replaced_getLookVec && HELPER.EntityLivingBase$getLookVec.is(methodInsnNode)) {
                            HELPER.Hooks$getRelativeLookVec.replace(iterator);
                            replaced_getLookVec = true;
                        }
                        else if (replaced_rotationPitch
                                && !BlockPos$PooledMutableBlockPos$retainFound
                                && HELPER.BlockPos$PooledMutableBlockPos$retain.is(methodInsnNode)){
                            HELPER.Hooks$getBlockPostBelowEntity.replace(iterator);

                            iterator.previous(); //Our replacment INVOKESTATIC

                            // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                            int aload0foundCount = 0;
                            while(iterator.hasPrevious() && aload0foundCount < 3) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
                                    if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                            && varInsnNode.var == 0) {
                                        aload0foundCount++;
                                    }
                                }
                                if (aload0foundCount < 3) {
                                    iterator.remove();
                                }
                            }
                            if (aload0foundCount == 3) {
                                BlockPos$PooledMutableBlockPos$retainFound = true;
                            }
                            else {
                                die("Failed to find 3 uses of ALOAD 0 before inserted \"" + HELPER.Hooks$getBlockPostBelowEntity + "\" in " + HELPER.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        else if (BlockPos$PooledMutableBlockPos$retainFound
                                && !BlockPos$PooledMutableBlockPos$setPosFound
                                && HELPER.BlockPos$PooledMutableBlockPos$setPos.is(methodInsnNode)) {
                            HELPER.Hooks$setPooledMutableBlockPosToBelowEntity.replace(iterator);

                            iterator.previous(); //Our replacement INVOKESTATIC

                            // Interestingly, we have to find the same number of ALOAD 0 instructions as the previous
                            // Deletes everything before the INVOKESTATIC until we've reached our third ALOAD
                            int aload0foundCount = 0;
                            while (iterator.hasPrevious() && aload0foundCount < 3) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
                                    if (varInsnNode.getOpcode() == Opcodes.ALOAD
                                            && varInsnNode.var == 0) {
                                        aload0foundCount++;
                                    }
                                }
                                if (aload0foundCount < 3) {
                                    iterator.remove();
                                }
                            }
                            if (aload0foundCount == 3) {
                                BlockPos$PooledMutableBlockPos$setPosFound = true;
                            }
                            else {
                                die("Failed to find 3 uses of ALOAD 0 before inserted \"" + HELPER.Hooks$setPooledMutableBlockPosToBelowEntity + "\" in " + HELPER.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        else if (BlockPos$PooledMutableBlockPos$setPosFound
                                && numEntityLivingBase$isOffsetPositionInLiquidFound < 2
                                && HELPER.EntityLivingBase$isOffsetPositionInLiquid.is(methodInsnNode)){
                            if (methodInsnNode == previousisOffsetPositionInLiquidMethodInsnNode) {
                                continue;
                            }
                            previousisOffsetPositionInLiquidMethodInsnNode = methodInsnNode;
                            int numReplacementsMade = 0;
                            while (iterator.hasPrevious() && numReplacementsMade < 2) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                                    if (HELPER.EntityLivingBase$posY_GET.is(fieldInsnNode)) {
                                        // Replace the GETFIELD with an INVOKESTATIC of the Hook
                                        HELPER.Hooks$getRelativePosY.replace(iterator);
                                        numReplacementsMade++;
                                    }
                                }
                            }
                            if (numReplacementsMade == 2) {
                                numEntityLivingBase$isOffsetPositionInLiquidFound++;
                            }
                        }
                    }
                    else if(next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (replaced_getLookVec
                                && !replaced_rotationPitch
                                && HELPER.EntityLivingBase$rotationPitch_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativePitch.replace(iterator);
                            replaced_rotationPitch = true;
                        }
                        else if (numEntityLivingBase$isOffsetPositionInLiquidFound == 2
                                && !GETFIELD_limbSwingAmount_found
                                && HELPER.EntityLivingBase$limbSwingAmount_GET.is(fieldInsnNode)) {
                            iterator.previous(); // The GETFIELD instruction
                            iterator.previous(); // ALOAD 0
                            HELPER.Hooks$makePositionRelative.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            GETFIELD_limbSwingAmount_found = true;
                        }
                        else if (GETFIELD_limbSwingAmount_found
                                && !PUTFIELD_limbSwing_found
                                && HELPER.EntityLivingBase$limbSwing_PUT.is(fieldInsnNode)) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            HELPER.Hooks$makePositionAbsolute.addTo(iterator);
                            PUTFIELD_limbSwing_found = true;
                        }
                    }
                }

                boolean patchComplete = dieOrTrue(replaced_getLookVec, "Failed to replace getLookVec")
                        && dieOrTrue(replaced_rotationPitch, "Failed to replace rotationPitch")
                        && dieOrTrue(BlockPos$PooledMutableBlockPos$retainFound, "Failed to find BlockPos$PooledMutableBlockPos::retain")
                        && dieOrTrue(BlockPos$PooledMutableBlockPos$setPosFound, "Failed to find BlockPos$PooledMutableBlockPos::setPos")
                        && dieOrTrue(numEntityLivingBase$isOffsetPositionInLiquidFound == 2, "Found " + numEntityLivingBase$isOffsetPositionInLiquidFound + " EntityLivingBase::isOffsetPositionInLiquidFound, expected 2")
                        && dieOrTrue(GETFIELD_limbSwingAmount_found, "Failed to find GETFIELD limbSwingAmount")
                        && dieOrTrue(PUTFIELD_limbSwing_found, "Failed to find PUTFIELD limbSwing");

                //patchComplete will always be true
                logOrDie(patchComplete, "(UNUSED)Failed to patch " + HELPER.EntityLivingBase$moveEntityWithHeading_name, "Patched %s", HELPER.EntityLivingBase$moveEntityWithHeading_name);
            }
            else if (HELPER.EntityLivingBase$updateDistance_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$updateDistance_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(HELPER.EntityLivingBase$updateDistance_name);
            }
            else if (HELPER.Entity$onUpdate_name.is(methodNode)) {
                logPatchStarting(HELPER.Entity$onUpdate_name);
                boolean patchComplete = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (HELPER.EntityLivingBase$posX_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativePosX.replace(iterator);
                        }
                        else if (HELPER.EntityLivingBase$prevPosX_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativePrevPosX.replace(iterator);
                        }
                        else if (HELPER.EntityLivingBase$posZ_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativePosZ.replace(iterator);
                        }
                        else if (HELPER.EntityLivingBase$prevPosZ_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativePrevPosZ.replace(iterator);
                        }
                        else if (HELPER.EntityLivingBase$rotationYaw_GET.is(fieldInsnNode)) {
                            HELPER.Hooks$getRelativeYaw.replace(iterator);
                            patchComplete = true;
                            break;
                        }
                    }
                }
                logOrDie(patchComplete, "Failed to patch " + HELPER.Entity$onUpdate_name, "Patched %s", HELPER.Entity$onUpdate_name);
            }
            else if (HELPER.EntityLivingBase$jump_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$jump_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(HELPER.EntityLivingBase$jump_name);
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchNetHandlerPlayClient(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean foundAtLeastOneMinY = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("handlePlayerPosLook")) {
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode && next.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (fieldInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                && fieldInsnNode.name.equals("minY")
                                && fieldInsnNode.desc.equals("D")) {
                            fieldInsnNode.owner = "net/minecraft/entity/player/EntityPlayer";
                            fieldInsnNode.name = "posY";
                            iterator.previous(); // the instruction we just found
                            iterator.previous(); // getEntityBoundingBox()
                            iterator.remove();
                            foundAtLeastOneMinY = true;
                        }
                    }
                }
            }
        }

        if (foundAtLeastOneMinY) {
            log("Replaced \"playerEntity.geEntityBoundingBox().minY\" with \"playerEntity.posY\" in " + classNode.name + "::processPlayer");
        } else {
            throw new RuntimeException("Could not find \"playerEntity.geEntityBoundingBox().minY\" in " + classNode.name);
        }


        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchNetHandlerPlayServer(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean foundMoveEntity = false;
        boolean foundHandleFalling = false;
        boolean patched1point5ToGetEyeHeight = false;

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("processPlayer")) {
                log("Modifying NetHandlerPlayServer::processPlayer");
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (!foundMoveEntity
                                && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                && methodInsnNode.name.equals("moveEntity")
                                && methodInsnNode.desc.equals("(DDD)V"))
                        {
                            log("Found \"playerEntity.moveEntity(...)\"");
                            methodInsnNode.setOpcode(INVOKESTATIC);
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "moveEntityAbsolute";
                            methodInsnNode.desc = "(Lnet/minecraft/entity/player/EntityPlayer;DDD)V";
                            log("Replaced with \"Hooks.moveEntityAbsolute(...)\"");
                            foundMoveEntity = true;
                        }
                        else if (!foundHandleFalling
                                && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                && methodInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                && methodInsnNode.name.equals("handleFalling")
                                && methodInsnNode.desc.equals("(DZ)V"))
                        {
                            log("Found \"playerEntity.handleFalling(...)\"");
                            iterator.previous(); // INVOKEVIRTUAL handleFalling again
                            iterator.previous(); // INVOKEVIRTUAL isOnGround
                            iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                            next = iterator.previous(); // Should be DSUB
                            if (next.getOpcode() == Opcodes.DSUB) {
                                iterator.remove();
                                VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                                iterator.add(dload7);
                                MethodInsnNode invokeStaticHook = new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "netHandlerPlayServerHandleFallingYChange",
                                        "(Lnet/minecraft/entity/player/EntityPlayerMP;DDD)D",
                                        false);
                                iterator.add(invokeStaticHook);
                                iterator.previous(); // Our newly added INVOKESTATIC
                                iterator.previous(); // Our newly added DLOAD 7
                                iterator.previous(); // DLOAD 9
                                next = iterator.previous(); // GETFIELD posY
                                if (next instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                    if (fieldInsnNode.owner.equals("net/minecraft/entity/player/EntityPlayerMP")
                                            && fieldInsnNode.name.equals("posY")
                                            && fieldInsnNode.desc.equals("D"))
                                    {
                                        VarInsnNode dload3 = new VarInsnNode(Opcodes.DLOAD, 3);
                                        iterator.remove();
                                        iterator.add(dload3);
                                        foundHandleFalling = true;
                                    }
                                    else {
                                        throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                    }
                                }
                                else {
                                    throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                }
                            }
                            else {
                                throw new RuntimeException("[UpAndDownAndAllAround] Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"DSUB\"");
                            }
                        }
                    }
                }
            }
            else if (methodNode.name.equals("processPlayerDigging")) {

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode)next;
                        Object object = ldcInsnNode.cst;
                        if (object instanceof Double) {
                            Double d = (Double)object;
                            if (d == 1.5D) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this.
                                iterator.add(new FieldInsnNode(GETFIELD, //this.playerEntity
                                        "net/minecraft/network/NetHandlerPlayServer",
                                        "playerEntity",
                                        "Lnet/minecraft/entity/player/EntityPlayerMP;"));
                                iterator.add(new MethodInsnNode(INVOKEVIRTUAL, //this.playerEntity.getEyeHeight()
                                        "net/minecraft/entity/player/EntityPlayerMP",
                                        "getEyeHeight",
                                        "()F",
                                        false));
                                iterator.add(new InsnNode(Opcodes.F2D)); //(double)this.playerEntity.getEyeHeight()
                                patched1point5ToGetEyeHeight = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (foundMoveEntity) {
            log("Replaced \"playerEntity.moveEntity(...)\" with \"Hooks.moveEntityAbsolute(...)\" in " + classNode.name + "::processPlayer");
        } else {
            throw new RuntimeException("Could not find \"playerEntity.moveEntity(...)\" in " + classNode.name);
        }
        if (foundHandleFalling) {
            log("Replaced \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\" with \"this.playerEntity.handleFalling(Hooks.netHandlerPlayServerHandleFallingYChange(playerEntity, d0, d3, d2), packetIn.isOnGround());\" in " + classNode.name + "::processPlayer");
        } else {
            throw new RuntimeException("Could not find \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\" in " + classNode.name +"::processPlayer");
        }
        if (patched1point5ToGetEyeHeight) {
            log("Replaced 1.5D with this.playerEntity.getEyeHeight()");
        } else {
            throw new RuntimeException("Could not find \"1.5D\" in " + classNode.name + "::processPlayerDigging");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static final boolean DEBUG_AUTO_JUMP = false;

    //TODO: Instead of changing the field, call a method in Hooks that takes the aabb as an argument and returns GravityAxisAlignedBB.getOrigin().getY() or AxisAlignedBB.minY
    private static byte[] patchEntityPlayerSP(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        boolean isHeadSpaceFreeModified = false, onUpdateWalkingPlayerModified = false, onLivingUpdateModified = false;

        int numReplacements = 0;

//        IDeobfAware onUpdateWalkingPlayer = new DeobfAwareString("onUpdateWalkingPlayer", "func_175161_p");
//        IDeobfAwareClass AxisAlignedBB = new ObjectClassName("net/minecraft/util/math/AxisAlignedBB");
//        IDeobfAware minY = new DeobfAwareString("minY", "field_72338_b");
//        FieldInstruction minYFieldInsn = new FieldInstruction(GETFIELD, AxisAlignedBB, minY, DOUBLE);
//        IDeobfAwareClass EntityPlayerSP = new ObjectClassName("net/minecraft/client/entity/EntityPlayerSP");
//        IDeobfAware posY = new DeobfAwareString("posY", "field_70163_u");
//        FieldInstruction posYFieldInsn = new FieldInstruction(GETFIELD, EntityPlayerSP, posY, DOUBLE);

//        IDeobfAware onLivingUpdate = new DeobfAwareString("onLivingUpdate", "func_70636_d");

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("onUpdateWalkingPlayer")) {
                log("Modifying EntityPlayerSP::onUpdateWalkingPlayer");

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) next;
                        if (fieldInsnNode.getOpcode() == GETFIELD) {
                            if (fieldInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                    && fieldInsnNode.name.equals("minY")
                                    && fieldInsnNode.desc.equals("D")) {
                                if (iterator.hasPrevious()) {
                                    iterator.previous();
                                    AbstractInsnNode previous = iterator.previous();
                                    if (previous instanceof VarInsnNode) {
                                        VarInsnNode varInsnNode = (VarInsnNode) previous;
                                        if (varInsnNode.var == 3) {
                                                /*
                                                Replace
                                                axisalignedbb.minY
                                                with
                                                this.posY
                                                in
                                                EntityPlayerSP::onUpdateWalkingPlayer
                                                 */
                                            varInsnNode.var = 0;
                                            fieldInsnNode.owner = "net/minecraft/client/entity/EntityPlayerSP";
                                            fieldInsnNode.name = "posY";
                                            numReplacements++;

                                            // We went back two with .previous(), so go forwards again the same amount
                                            iterator.next();
                                            iterator.next();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (numReplacements != 0) {
                    log("Replaced \"axisalignedbb.minY\" with \"this.posY\" " + numReplacements + " time" + (numReplacements == 1 ? "" : "s") + " in " + classNode.name);
                    onUpdateWalkingPlayerModified = true;
                } else {
                    throw new RuntimeException("[UpAndDownAndAllAround] Failed to find any instances of \"axisalignedbb.minY\" in " + classNode.name);
                }
            }
            else if (methodNode.name.equals("onLivingUpdate")) {
                log("Modifying EntityPlayerSP::onLivingUpdate");

                boolean error = true;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof VarInsnNode) {
                        VarInsnNode varInsnNode = (VarInsnNode) next;
                        if (varInsnNode.var == 6 && varInsnNode.getOpcode() == Opcodes.ASTORE) {
                            log("Found \"ASTORE 6\"");

                            VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, 6);
                            TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, "uk/co/mysterymayhem/gravitymod/util/GravityAxisAlignedBB");
                            JumpInsnNode ifeqJumpInsnNode = new JumpInsnNode(Opcodes.IFEQ, null);
                            VarInsnNode aLoad0 = new VarInsnNode(Opcodes.ALOAD, 0);
                            VarInsnNode aLoad6_2 = new VarInsnNode(Opcodes.ALOAD, 6);
                            MethodInsnNode callHook = new MethodInsnNode(INVOKESTATIC,
                                    "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "pushEntityPlayerSPOutOfBlocks",
                                    "(Luk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity;Lnet/minecraft/util/math/AxisAlignedBB;)V", false);
                            JumpInsnNode gotoJumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);

                            iterator.add(aLoad6);
                            iterator.add(instanceofGravityAxisAlignedBB);
                            iterator.add(ifeqJumpInsnNode);
                            iterator.add(aLoad0);
                            iterator.add(aLoad6_2);
                            iterator.add(callHook);
                            iterator.add(gotoJumpInsnNode);

                            next = iterator.next();

                            if (next instanceof LabelNode) {
                                ifeqJumpInsnNode.label = (LabelNode) next;

                                next = iterator.next();

                                if (next instanceof LineNumberNode) {
                                    // ClassWriter is going to compute the frames, the commented out code is untested
//                                    FrameNode frameAppendNode = new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/util/math/AxisAlignedBB"}, 0, new Object[0]);
//                                    iterator.add(frameAppendNode);

                                    LabelNode labelForGotoJumpInsnNode = null;
                                    for (; iterator.hasNext(); ) {
                                        next = iterator.next();
                                        if (next instanceof MethodInsnNode) {
                                            MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                                            if (methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                                    && methodInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                                    && methodInsnNode.name.equals("getFoodStats")
                                                    && methodInsnNode.desc.equals("()Lnet/minecraft/util/FoodStats;")) {
                                                log("Found \"this.getFoodStats\"");
                                                for (; iterator.hasPrevious(); ) {
                                                    AbstractInsnNode previous = iterator.previous();
                                                    if (previous instanceof LabelNode) {
                                                        log("Found previous Label");
                                                        labelForGotoJumpInsnNode = (LabelNode) previous;
                                                        // ClassWriter is going to compute the frames, the commented out code is untested
//                                                        for(;iterator.hasNext();) {
//                                                            next = iterator.next();
//                                                            if (next instanceof VarInsnNode) {
//                                                                varInsnNode = (VarInsnNode)next;
//                                                                if (varInsnNode.getOpcode() == Opcodes.ALOAD && varInsnNode.var == 0) {
//                                                                    FrameNode frameSameNode = new FrameNode(Opcodes.F_SAME, 0, new Object[0], 0, new Object[0]);
//                                                                    break;
//                                                                }
//                                                            }
//                                                        }
                                                        break;
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if (labelForGotoJumpInsnNode != null) {
                                        gotoJumpInsnNode.label = labelForGotoJumpInsnNode;
                                        error = false;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
                if (error) {
                    throw new RuntimeException("[UpAndDownAndAllAround] Hook insertion into EntityPlayerSP::onLivingUpdate failed");
                } else {
                    log("Inserted an if statement and call to \"Hooks.pushEntityPlayerSPOutOfBlocks\" into \"EntityPlayerSP::onLivingUpdate\"");
                    onLivingUpdateModified = true;
                }
            }
            else if (methodNode.name.equals("isHeadspaceFree")) {
                log("Modifying EntityPlayerSP::isHeadspaceFree");
                InsnList instructions = methodNode.instructions;
                AbstractInsnNode label = instructions.get(0);
                AbstractInsnNode lineNumber = instructions.get(1);
                AbstractInsnNode labelEnd = instructions.getLast();
                AbstractInsnNode returnNode = labelEnd.getPrevious();

                instructions.clear();
                instructions.add(label);
                instructions.add(lineNumber);
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
                instructions.add(new MethodInsnNode(INVOKESTATIC,
                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                        "isHeadspaceFree",
                        "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/util/math/BlockPos;I)Z",
                        false));
                instructions.add(new InsnNode(Opcodes.IRETURN));
                instructions.add(labelEnd);
                log("Replaced EntityPlayerSP::isHeadspaceFree with call to Hooks::isHeadspaceFree");
                isHeadSpaceFreeModified = true;
            }
            else if (methodNode.name.equals("func_189810_i")) {
                int addVectorReplacements = 0;
                int blockPosUPCount = 0;
                int blockPosUPCountI = 0;
                boolean foundFirstBlockPosGetY = false;
                //newPATCH #1
                boolean patch1Complete = false;
                //newPATCH #2
                boolean patch2Complete = false;
                //newPATCH #3
                boolean patch3Complete = false;
                //newPATCH #4
                boolean patch4Complete = false;
                //newPATCH #4.5
                boolean patch4Point5Complete = false;
                int patch4Point5ReplacementCount = 0;
                //newPATCH #5 and #6.5
                boolean patch5Complete = false;
                int vec3d12_var = -1;
                boolean patch5Point5Complete = false;
                boolean patch6Complete = false;
                boolean patch7Complete = false;
                boolean patch8Complete = false;
                boolean patch9Complete = false;
                int axisAlignedBBmaxYCount = 0;
                int axisAlignedBBminYCount = 0;

                if (DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 2));
                            iterator.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/entity/AbstractClientPlayer", "func_189810_i", "(FF)V", false));
                            iterator.add(new InsnNode(Opcodes.RETURN));
                        }
                    }
                }
                else {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof MethodInsnNode) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                            //newPATCH #4
                            if (patch3Complete
                                    && !patch4Complete
                                    && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                    && methodInsnNode.name.equals("getEntityBoundingBox")
                                    && methodInsnNode.desc.equals("()Lnet/minecraft/util/math/AxisAlignedBB;")) {
                                iterator.remove();
                                iterator.next();
                                iterator.remove(); // GETFIELD net/minecraft/util/math/AxisAlignedBB.minY : D
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getOriginRelativePosY",
                                        "(Lnet/minecraft/entity/Entity;)D",
                                        false));
                                iterator.next(); // DLOAD ? (likely 7)
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Have to pass this to adjustVec as well
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "adjustVec",
                                        "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false));
                                patch4Complete = true;
                            }
                            //newPatch #5
                            else if (patch4Point5Complete
                                    && !patch5Complete
                                    && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/Vec3d")
                                    && methodInsnNode.name.equals("scale")
                                    && methodInsnNode.desc.equals("(D)Lnet/minecraft/util/math/Vec3d;")) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode) {
                                    // Should also be ASTORE
                                    vec3d12_var = ((VarInsnNode)next).var;
                                    patch5Complete = true;
                                }
                                else {
                                    throw new RuntimeException("Expected a VarInsnNode after first usage of Vec3d::scale");
                                }
                            }
                            else if (patch5Complete
                                    && !patch5Point5Complete
                                    && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                    && methodInsnNode.name.equals("func_189651_aD")
                                    && methodInsnNode.desc.equals("()Lnet/minecraft/util/math/Vec3d;")) {
                                methodInsnNode.setOpcode(INVOKESTATIC);
                                methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                                methodInsnNode.name = "getRelativeLookVec";
                                methodInsnNode.desc = "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;";
                                patch5Point5Complete = true;
                            }
                            //PATCH #1
                            else if (methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/Vec3d")
                                    && methodInsnNode.name.equals("addVector")
                                    && methodInsnNode.desc.equals("(DDD)Lnet/minecraft/util/math/Vec3d;")){
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "addAdjustedVector",
                                        "(Lnet/minecraft/util/math/Vec3d;DDDLnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false));
                                addVectorReplacements++;
                            }
                            //newPATCH #8
                            else if (patch7Complete
                                    && !patch8Complete
                                    && methodInsnNode.getOpcode() == INVOKESPECIAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                    && methodInsnNode.name.equals("<init>")
                                    && methodInsnNode.desc.equals("(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V")) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "constructNewGAABBFrom2Vec3d",
                                        "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/AxisAlignedBB;",
                                        false));
                                patch8Complete = true;
                            }
                            //PATCH #3
                            else if (methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos")
                                    && methodInsnNode.name.equals("up")) {
                                String desc = null;
                                if (methodInsnNode.desc.equals("()Lnet/minecraft/util/math/BlockPos;")) {
                                    desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;";
                                    blockPosUPCount++;
                                }
                                else if (methodInsnNode.desc.equals("(I)Lnet/minecraft/util/math/BlockPos;")) {
                                    desc = "(Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;";
                                    blockPosUPCountI++;
                                }

                                //Just in case there's ever some other 'up' method
                                if (desc != null) {
                                    iterator.remove();
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                    iterator.add(new MethodInsnNode(INVOKESTATIC,
                                            "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                            "getRelativeUpBlockPos",
                                            desc,
                                            false));
                                }
                            }
                            //PATCH #4
                            else if (!foundFirstBlockPosGetY
                                    && methodInsnNode.getOpcode() == INVOKEVIRTUAL
                                    && methodInsnNode.owner.equals("net/minecraft/util/math/BlockPos")
                                    && methodInsnNode.name.equals("getY")
                                    && methodInsnNode.desc.equals("()I")) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getRelativeYOfBlockPos",
                                        "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)I",
                                        false));
                                foundFirstBlockPosGetY = true;
                            }

                        }
                        else if (next instanceof TypeInsnNode
                                && next.getOpcode() == Opcodes.NEW) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                            //newPATCH #1
                            if (!patch1Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals("net/minecraft/util/math/Vec3d")) {
                                iterator.remove();
                                while(!patch1Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        iterator.add(new MethodInsnNode(INVOKESTATIC,
                                                "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                                "getBottomOfEntity",
                                                "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                                false));
                                        patch1Complete = true;
                                    }
                                    else {
                                        iterator.remove();
                                    }
                                }
                            }
                            //newPATCH #7
                            else if (!patch7Complete
                                    && patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals("net/minecraft/util/math/AxisAlignedBB")) {
                                iterator.remove();
                                next = iterator.next();
                                if (next instanceof InsnNode && next.getOpcode() == Opcodes.DUP) {
                                    iterator.remove();
                                    patch7Complete = true;
                                }
                                else {
                                    //what
                                    throw new RuntimeException("[UpAndDownAndAllAround] DUP instruction not found after expected NEW AxisAlignedBB");
                                }
                            }
                            //newPATCH #6
                            else if(patch5Point5Complete
                                    && !patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals("net/minecraft/util/math/BlockPos")) {
                                iterator.remove();
                                while(!patch6Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        iterator.add(new MethodInsnNode(INVOKESTATIC,
                                                "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                                "getBlockPosAtTopOfPlayer",
                                                "(Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/BlockPos;",
                                                false));

                                        //newPATCH #6.5
                                        iterator.next(); //ASTORE ? (probably 17)
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, vec3d12_var));
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        iterator.add(new MethodInsnNode(INVOKESTATIC,
                                                "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                                "adjustVec",
                                                "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                                false));
                                        iterator.add(new VarInsnNode(Opcodes.ASTORE, vec3d12_var));

                                        patch6Complete = true;
                                    }
                                    else {
                                        iterator.remove();
                                    }
                                }
                            }
                            //newPATCH #9
                            else if(patch8Complete
                                    && !patch9Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals("net/minecraft/util/math/Vec3d")) {
                                iterator.next(); // DUP
                                iterator.next(); // DCONST_0
                                next = iterator.next();
                                if (next.getOpcode() == Opcodes.DCONST_1) {
                                    iterator.remove();
                                    iterator.add(new InsnNode(Opcodes.DCONST_0));
                                }
                                else {
                                    throw new RuntimeException("Expecting DCONST_0 followed by DCONST_1, but instead got " + next);
                                }
                                iterator.next(); // DCONST_0
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V

                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new InsnNode(Opcodes.DCONST_1));
                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "addAdjustedVector",
                                        "(Lnet/minecraft/util/math/Vec3d;DDDLnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/Vec3d;",
                                        false));

                                patch9Complete = true;
                            }

                        }
                        else if (next instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                            //newPATCH #2
                            if (patch1Complete
                                    && !patch2Complete
                                    && fieldInsnNode.getOpcode() == GETFIELD
                                    && fieldInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                    && fieldInsnNode.name.equals("posX")
                                    && fieldInsnNode.desc.equals("D")) {
                                iterator.remove();
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getOriginRelativePosX",
                                        "(Lnet/minecraft/entity/Entity;)D",
                                        false));
                                patch2Complete = true;
                            }
                            //newPATCH #3
                            else if(patch2Complete
                                    && !patch3Complete
                                    && fieldInsnNode.getOpcode() == GETFIELD
                                    && fieldInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                    && fieldInsnNode.name.equals("posZ")
                                    && fieldInsnNode.desc.equals("D")) {
                                iterator.remove();
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getOriginRelativePosZ",
                                        "(Lnet/minecraft/entity/Entity;)D",
                                        false));
                                patch3Complete = true;
                            }
                            else if(patch4Complete
                                    && !patch4Point5Complete
                                    && fieldInsnNode.getOpcode() == GETFIELD
                                    && fieldInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                    && fieldInsnNode.name.equals("rotationYaw")
                                    && fieldInsnNode.desc.equals("F")) {
                                iterator.remove();
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getRelativeYaw",
                                        "(Lnet/minecraft/entity/Entity;)F",
                                        false));
                                patch4Point5ReplacementCount++;
                                if (patch4Point5ReplacementCount >= 2) {
                                    patch4Point5Complete = true;
                                }
                            }
                            //newPATCH #10
                            else if (patch9Complete
                                    && axisAlignedBBmaxYCount < 2
                                    && fieldInsnNode.getOpcode() == GETFIELD
                                    && fieldInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                    && fieldInsnNode.name.equals("maxY")
                                    && fieldInsnNode.desc.equals("D")) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getRelativeTopOfBB",
                                        "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)D",
                                        false));
                                axisAlignedBBmaxYCount++;
                            }
                            //newPATCH #11
                            else if (axisAlignedBBmaxYCount == 2
                                    && axisAlignedBBminYCount < 2
                                    && fieldInsnNode.getOpcode() == GETFIELD
                                    && fieldInsnNode.owner.equals("net/minecraft/util/math/AxisAlignedBB")
                                    && fieldInsnNode.name.equals("minY")
                                    && fieldInsnNode.desc.equals("D")) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                iterator.add(new MethodInsnNode(INVOKESTATIC,
                                        "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                        "getRelativeBottomOfBB",
                                        "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)D",
                                        false));
                                axisAlignedBBminYCount++;
                            }
                        }
                    }
//                    addVectorReplacements
//                    axisAlignedBBmaxYCount
//                    axisAlignedBBminYCount
//                    blockPosUPCount
//                    blockPosUPCountI
                    log("addVector: " + addVectorReplacements
                            + ", maxY: " + axisAlignedBBmaxYCount
                            + ", minY: " + axisAlignedBBminYCount
                            + ", up(): " + blockPosUPCount
                            + ", up(int): " + blockPosUPCountI);
                }

                if (!patch9Complete && !DEBUG_AUTO_JUMP) {
                    throw new RuntimeException("[UpAndDown] Error occured while patching EntityPlayerSP");
                }
            }
            else if (methodNode.name.equals("moveEntity")) {
                if (DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 3));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 5));
                            iterator.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/client/entity/AbstractClientPlayer",
                                    "moveEntity", "(DDD)V", false));
                            iterator.add(new InsnNode(Opcodes.RETURN));
                        }
                    }
                }
                else {
                    int prevRelativeXPos_var = -1;
                    int prevRelativeZPos_var = -1;

                    outerfor:
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ALOAD) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.next(); // GETFIELD net/minecraft/client/entity/EntityPlayerSP.posX : D
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "posY", "D"));
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "posZ", "D"));
                            iterator.add(new MethodInsnNode(INVOKESTATIC, "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                    "inverseAdjustXYZ", "(Lnet/minecraft/entity/Entity;DDD)[D", false));
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            //next is DSTORE 7
                            next = iterator.next();
                            if (next instanceof VarInsnNode) {
                                prevRelativeXPos_var = ((VarInsnNode)next).var;
                            }
                            else {
                                throw new RuntimeException("Was expecting DSTORE _ after GETFIELD posX, instead got " + next);
                            }

                            while(true) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ALOAD) {
                                    //ALOAD 0
                                    iterator.remove();
                                    iterator.next(); //GETFIELD net/minecraft/client/entity/EntityPlayerSP.posZ : D
                                    iterator.remove();
                                    iterator.add(new InsnNode(Opcodes.ICONST_2));
                                    iterator.add(new InsnNode(Opcodes.DALOAD));
                                    //next is DSTORE 9
                                    next = iterator.next();
                                    if (next instanceof VarInsnNode) {
                                        prevRelativeZPos_var = ((VarInsnNode)next).var;
                                    }
                                    else {
                                        throw new RuntimeException("Was expecting DSTORE _ after GETFIELD posZ, instead got " + next);
                                    }

                                    while(true) {
                                        next = iterator.next();
                                        if(next instanceof FieldInsnNode) {
                                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                            if (fieldInsnNode.getOpcode() == GETFIELD
                                                    && fieldInsnNode.owner.equals("net/minecraft/client/entity/EntityPlayerSP")
                                                    && fieldInsnNode.name.equals("posX")
                                                    && fieldInsnNode.desc.equals("D")) {
                                                iterator.previous();
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.next(); // same GETFIELD instruction
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "posY", "D"));
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/entity/EntityPlayerSP", "posZ", "D"));
                                                iterator.add(new MethodInsnNode(INVOKESTATIC, "uk/co/mysterymayhem/gravitymod/asm/Hooks",
                                                        "inverseAdjustXYZ", "(Lnet/minecraft/entity/Entity;DDD)[D", false));
                                                iterator.add(new InsnNode(Opcodes.DUP));
                                                iterator.add(new InsnNode(Opcodes.ICONST_2));
                                                iterator.add(new InsnNode(Opcodes.DALOAD));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.DSUB));
                                                iterator.add(new VarInsnNode(Opcodes.DSTORE, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.ICONST_0));
                                                iterator.add(new InsnNode(Opcodes.DALOAD));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeXPos_var));
                                                iterator.add(new InsnNode(Opcodes.DSUB));
                                                iterator.add(new InsnNode(Opcodes.D2F));
                                                iterator.add(new VarInsnNode(Opcodes.DLOAD, prevRelativeZPos_var));
                                                iterator.add(new InsnNode(Opcodes.D2F));

                                                while(true) {
                                                    next = iterator.next();
                                                    if (next.getOpcode() == INVOKEVIRTUAL) {
                                                        //INVOKEVIRTUAL net/minecraft/client/entity/EntityPlayerSP.func_189810_i (FF)V // Auto-jump method
                                                        break outerfor;
                                                    }
                                                    else {
                                                        iterator.remove();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    /*
                    // access flags 0x1
                      public moveEntity(DDD)V
                       L0
                        LINENUMBER 602 L0
                        ALOAD 0
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posX : D
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posY : D
                        ALOAD 0
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posZ : D
                        INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                        DUP
                        ICONST_0
                        DALOAD
                        DSTORE 7
                       L1
                        LINENUMBER 604 L2
                        ICONST_2
                        DALOAD
                        DSTORE 9
                       L2
                        LINENUMBER 605 L3
                        ALOAD 0
                        DLOAD 1
                        DLOAD 3
                        DLOAD 5
                        INVOKESPECIAL net/minecraft/entity/player/EntityPlayer.moveEntity (DDD)V
                       L3
                        LINENUMBER 606 L4
                        ALOAD 0
                          this
                        ALOAD 0
                          this, this
                        ALOAD 0
                          this, this, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posX : D
                          this, this, posX
                        ALOAD 0
                          this, this, posX, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posY : D
                          this, this, posX, posY
                        ALOAD 0
                          this, this, posX, posY, this
                        GETFIELD uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.posZ : D
                          this, this, posX, posY, posZ
                        INVOKESTATIC uk/co/mysterymayhem/gravitymod/asm/Hooks.inverseAdjustXYZ (Lnet/minecraft/entity/Entity;DDD)[D
                          this, doubles
                        DUP
                          this, doubles, doubles
                        ICONST_2
                          this, doubles, doubles, 2
                        DALOAD
                          this, doubles, doubles[2](newRelZ)
                        DLOAD 9
                          this, doubles, doubles[2](newRelZ), d1(oldRelZ)
                        DSUB
                          this, doubles, doubles[2](newRelZ)-d1(oldRelZ)
                        DSTORE 9
                          this, doubles
                        ICONST_0
                          this, doubles, 0
                        DALOAD
                          this, doubles[0](newRelX)
                        DLOAD 7
                          this, doubles[0](newRelX), d0(oldRelX)
                        DSUB
                          this, doubles[0](newRelX)-d0(oldRelX)
                        D2F
                          this, (float)doubles[0](newRelX)-d0(oldRelX)
                        DLOAD 9
                          this, (float)doubles[0](newRelX)-d0(oldRelX), doubles[2](newRelZ)-d1(oldRelZ)
                        D2F
                          this, (float)doubles[0](newRelX)-d0(oldRelX), (float)doubles[2](newRelZ)-d1(oldRelZ)
                        INVOKEVIRTUAL uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity.func_189810_i (FF)V
                          <empty>
                       L4
                        LINENUMBER 608 L6
                        RETURN
                       L5
                     */
                }
            }
        }

        if (isHeadSpaceFreeModified && onLivingUpdateModified && onUpdateWalkingPlayerModified) {
//            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(classWriter);

            return classWriter.toByteArray();
        }
        else {
            throw new RuntimeException("[UpAndDownAndAllAround] Modification of EntityPlayerSP failed, UpAndDownAndAllAround is probably being used with the wrong Minecraft version.");
        }

    }

    private static byte[] patchSoundManager(byte[] bytes) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("setListener")) {
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof  MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (methodInsnNode.name.equals("setListenerOrientation")) {
                            methodInsnNode.owner = "uk/co/mysterymayhem/gravitymod/asm/Hooks";
                            methodInsnNode.name = "setListenerOrientationHook";
                            methodInsnNode.desc = "(Lpaulscode/sound/SoundSystem;FFFFFFLnet/minecraft/entity/player/EntityPlayer;)V";
                            methodInsnNode.setOpcode(INVOKESTATIC);
                            iterator.previous();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load EntityPlayer method argument
                        }
                    }
                }
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW + GET_PREVROTATIONYAW + GET_ROTATIONPITCH + GET_PREVROTATIONPITCH);
            }
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    //Set up bit mask
    private static final int GET_ROTATIONYAW = 0b1;
    private static final int GET_ROTATIONPITCH = 0b10;
    private static final int GET_PREVROTATIONYAW = 0b100;
    private static final int GET_PREVROTATIONPITCH = 0b1000;
    private static final int GET_ROTATIONYAWHEAD = 0b10000;
    private static final int GET_PREVROTATIONYAWHEAD = 0b100000;
    private static final int ALL_GET_ROTATION_VARS = GET_ROTATIONYAW | GET_ROTATIONPITCH | GET_PREVROTATIONYAW
            | GET_PREVROTATIONPITCH | GET_ROTATIONYAWHEAD | GET_PREVROTATIONYAWHEAD;

    private static void patchMethodUsingAbsoluteRotations(MethodNode methodNode, int fieldBits) {
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
                if (HELPER.Entity$rotationYaw_name.is(node.name)) {
                    HELPER.Hooks$getRelativeYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (HELPER.Entity$rotationPitch_name.is(node.name)) {
                    HELPER.Hooks$getRelativePitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYaw) {
            biPredicates[index] = (iterator, node) -> {
                if (HELPER.Entity$prevRotationYaw_name.is(node.name)) {
                    HELPER.Hooks$getRelativePrevYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (HELPER.Entity$prevRotationPitch_name.is(node.name)) {
                    HELPER.Hooks$getRelativePrevPitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (HELPER.EntityLivingBase$rotationYawHead_name.is(node.name)) {
                    HELPER.Hooks$getRelativeYawHead.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (HELPER.EntityLivingBase$prevRotationYawHead_name.is(node.name)) {
                    HELPER.Hooks$getPrevRelativeYawHead.replace(iterator);
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
}
