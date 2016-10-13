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
import static uk.co.mysterymayhem.gravitymod.asm.ObfuscationHelper.HooksMethodInstruction;
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
        classNameToMethodMap.put("net.minecraft.client.entity.AbstractClientPlayer", Transformer::patchEntityPlayerSubClass);
        classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayerMP", Transformer::patchEntityPlayerSubClass);

        classNameToMethodMap.put("net.minecraft.client.audio.SoundManager", Transformer::patchSoundManager);
        classNameToMethodMap.put("net.minecraft.client.entity.EntityPlayerSP", Transformer::patchEntityPlayerSP);
        classNameToMethodMap.put("net.minecraft.client.network.NetHandlerPlayClient", Transformer::patchNetHandlerPlayClient);
        classNameToMethodMap.put("net.minecraft.client.renderer.EntityRenderer", Transformer::patchEntityRenderer);
        classNameToMethodMap.put("net.minecraft.client.renderer.entity.RenderLivingBase", Transformer::patchRenderLivingBase);
        classNameToMethodMap.put("net.minecraft.entity.Entity", Transformer::patchEntity);
        classNameToMethodMap.put("net.minecraft.entity.EntityLivingBase", Transformer::patchEntityLivingBase);
        classNameToMethodMap.put("net.minecraft.network.NetHandlerPlayServer", Transformer::patchNetHandlerPlayServer);
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
        log("Patched  %s", object);
    }

    private static void warn(String string, Object... objects) {
        FMLLog.warning("[UpAndDown] " + string, objects);
    }

    /**
     * Default 'crash Minecraft' method.
     * @param string Reason why we are forcing Minecraft to crash
     */
    private static void die(String string) throws RuntimeException {
        throw new RuntimeException("[UpAndDown] " + string);
    }

    /**
     * Option to used to compound multiple tests together
     * @param shouldContinue
     * @param dieMessage
     * @return
     */
    private static void dieIfFalse(boolean shouldContinue, String dieMessage) {
        if (!shouldContinue) {
            die(dieMessage);
        }
    }

    private static void dieIfFalse(boolean shouldContinue, ClassNode classNode) {
        dieIfFalse(shouldContinue, "Failed to find the methods to patch in " + classNode.name + ". The Minecraft version you are using likely does not match what the mod requires.");
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
        final MethodName BlockPos$getY_name = new MethodName("getY", "func_177956_o");
        final MethodName BlockPos$up_INT_ARG_name = new MethodName("up", "func_177981_b");
        final MethodName BlockPos$up_NO_ARGS_name = new MethodName("up", "func_177984_a");

        // (DDD) arguments
        final MethodName BlockPos$PooledMutableBlockPos$retain_name = new MethodName("retain", "func_185345_c");
        // (DDD) arguments
        final MethodName BlockPos$PooledMutableBlockPos$setPos_name = new MethodName("setPos", "func_189532_c");

        final MethodName Entity$getEntityBoundingBox_name = new MethodName("getEntityBoundingBox", "func_174813_aQ");
        final MethodName Entity$getEyeHeight_name = new MethodName("getEyeHeight", "func_70047_e");
        final MethodName Entity$getForward_name = new MethodName("getForward", "func_189651_aD");
        final MethodName Entity$getLookVec_name = new MethodName("getLookVec", "func_70040_Z");
        final MethodName Entity$isOffsetPositionInLiquid_name = new MethodName("isOffsetPositionInLiquid", "func_70038_c");
        final MethodName Entity$moveEntity_name = new MethodName("moveEntity", "func_70091_d");
        final MethodName Entity$moveRelative_name = new MethodName("moveRelative", "func_70060_a");
        final MethodName Entity$onUpdate_name = new MethodName("onUpdate", "func_70071_h_");

        final MethodName EntityLivingBase$jump_name = new MethodName("jump", "func_70664_aZ");
        final MethodName EntityLivingBase$moveEntityWithHeading_name = new MethodName("moveEntityWithHeading", "func_70612_e");
        final MethodName EntityLivingBase$onLivingUpdate_name = new MethodName("onLivingUpdate", "func_70636_d");
        final MethodName EntityLivingBase$updateDistance_name = new MethodName("updateDistance", "func_110146_f");

        final MethodName EntityPlayer$getFoodStats_name = new MethodName("getFoodStats", "func_71024_bL");

        final MethodName EntityPlayerMP$handleFalling_name = new MethodName("handleFalling", "func_71122_b");

        // Method added by forge, so no obf name
        final MethodName EntityPlayerSP$isHeadspaceFree_name = new MethodName("isHeadspaceFree");
        final MethodName EntityPlayerSP$onUpdateWalkingPlayer_name = new MethodName("onUpdateWalkingPlayer", "func_175161_p");
        final MethodName EntityPlayerSP$updateAutoJump_name = new MethodName("updateAutoJump", "func_189810_i");

        final MethodName EntityRenderer$getMouseOver_name = new MethodName("getMouseOver", "func_78473_a");

        final MethodName INetHandlerPlayClient$handlePlayerPosLook_name = new MethodName("handlePlayerPosLook", "func_184330_a");

        final MethodName INetHandlerPlayServer$processPlayer_name = new MethodName("processPlayer", "func_147347_a");
        final MethodName INetHandlerPlayServer$processPlayerDigging_name = new MethodName("processPlayerDigging", "func_147345_a");

        final MethodName RenderLivingBase$doRender_name = new MethodName("doRender", "func_76986_a");

        final MethodName SoundManager$setListener_name = new MethodName("setListener", "func_148615_a");

        final MethodName Vec3d$addVector_name = new MethodName("addVector", "func_72441_c");
        final MethodName Vec3d$scale_name = new MethodName("scale", "func_186678_a");

        final MethodName World$getEntitiesInAABBexcluding_name = new MethodName("getEntitiesInAABBexcluding", "func_175674_a");

        /*
            PaulsCode method names
         */
        final MethodName SoundSystem$setListenerOrientation_name = new MethodName("setListenerOrientation");

        /*
            Mojang field names
         */
        final FieldName AxisAlignedBB$maxY_name = new FieldName("maxY", "field_72337_e");
        final FieldName AxisAlignedBB$minY_name = new FieldName("minY", "field_72338_b");
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
        final FieldName NetHandlerPlayServer$lastGoodX_name = new FieldName("lastGoodX", "field_184352_o");
        final FieldName NetHandlerPlayServer$lastGoodY_name = new FieldName("lastGoodY", "field_184353_p");
        final FieldName NetHandlerPlayServer$lastGoodZ_name = new FieldName("lastGoodZ", "field_184354_q");
        final FieldName NetHandlerPlayServer$playerEntity_name = new FieldName("playerEntity", "field_147369_b");

        /*
            Shared reference class names
         */
        final ObjectClassName AbstractClientPlayer = new ObjectClassName("net/minecraft/client/entity/AbstractClientPlayer");
        final ObjectClassName AxisAlignedBB = new ObjectClassName("net/minecraft/util/math/AxisAlignedBB");
        final ObjectClassName Block = new ObjectClassName("net/minecraft/block/Block");
        final ObjectClassName BlockPos = new ObjectClassName("net/minecraft/util/math/BlockPos");
        final ObjectClassName BlockPos$PooledMutableBlockPos = new ObjectClassName("net/minecraft/util/math/BlockPos$PooledMutableBlockPos");
        final ObjectClassName Blocks = new ObjectClassName("net/minecraft/init/Blocks");
        final ObjectClassName Entity = new ObjectClassName("net/minecraft/entity/Entity");
        final ObjectClassName EntityLivingBase = new ObjectClassName("net/minecraft/entity/EntityLivingBase");
        final ObjectClassName EntityPlayer = new ObjectClassName("net/minecraft/entity/player/EntityPlayer");
        final ObjectClassName EntityPlayerMP = new ObjectClassName("net/minecraft/entity/player/EntityPlayerMP");
        final ObjectClassName EntityPlayerSP = new ObjectClassName("net/minecraft/client/entity/EntityPlayerSP");
        final ObjectClassName EntityPlayerWithGravity = new ObjectClassName("uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity");
        final ObjectClassName FoodStats = new ObjectClassName("net/minecraft/util/FoodStats");
        final ObjectClassName GravityAxisAlignedBB = new ObjectClassName("uk/co/mysterymayhem/gravitymod/util/GravityAxisAlignedBB");
        final ObjectClassName List = new ObjectClassName("java/util/List");
        final ObjectClassName NetHandlerPlayServer = new ObjectClassName("net/minecraft/network/NetHandlerPlayServer");
        final ObjectClassName Predicate = new ObjectClassName("com/google/common/base/Predicate");
        final ObjectClassName SoundSystem = new ObjectClassName("paulscode/sound/SoundSystem");
        final ObjectClassName Vec3d = new ObjectClassName("net/minecraft/util/math/Vec3d");
        final ObjectClassName WorldClient = new ObjectClassName("net/minecraft/client/multiplayer/WorldClient");

        /*
            Mojang field instructions
         */
        final FieldInstruction AxisAlignedBB$maxY_GET = new FieldInstruction(GETFIELD, AxisAlignedBB, AxisAlignedBB$maxY_name, DOUBLE);
        final FieldInstruction AxisAlignedBB$minY_GET = new FieldInstruction(GETFIELD, AxisAlignedBB, AxisAlignedBB$minY_name, DOUBLE);
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
        final FieldInstruction EntityPlayer$posY_GET = new FieldInstruction(GETFIELD, EntityPlayer, Entity$posY_name, DOUBLE);
        final FieldInstruction EntityPlayerMP$posY_GET = new FieldInstruction(GETFIELD, EntityPlayerMP, Entity$posY_name, DOUBLE);
        final FieldInstruction EntityPlayerSP$posX_GET = new FieldInstruction(GETFIELD, EntityPlayerSP, Entity$posX_name, DOUBLE);
        final FieldInstruction EntityPlayerSP$posY_GET = new FieldInstruction(GETFIELD, EntityPlayerSP, Entity$posY_name, DOUBLE);
        final FieldInstruction EntityPlayerSP$posZ_GET = new FieldInstruction(GETFIELD, EntityPlayerSP, Entity$posZ_name, DOUBLE);
        final FieldInstruction EntityPlayerSP$rotationYaw_GET = new FieldInstruction(GETFIELD, EntityPlayerSP, Entity$rotationYaw_name, FLOAT);
        final FieldInstruction NetHandlerPlayServer$lastGoodX_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodX_name, DOUBLE);
        final FieldInstruction NetHandlerPlayServer$lastGoodY_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodY_name, DOUBLE);
        final FieldInstruction NetHandlerPlayServer$lastGoodZ_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodZ_name, DOUBLE);
        final FieldInstruction NetHandlerPlayServer$playerEntity_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$playerEntity_name, EntityPlayerMP);

        /*
            Mojang method instructions
         */
        // While there could be a constructor for MethodInstruction that doesn't require a MethodDesc, it is useful to
        // visually split apart the arguments so it's easier to tell what each argument is
        final MethodInstruction AxisAlignedBB$INIT = new MethodInstruction(INVOKESPECIAL, AxisAlignedBB, INIT, new MethodDesc(VOID, Vec3d, Vec3d));
        final MethodInstruction AxisAlignedBB$calculateXOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateXOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction AxisAlignedBB$calculateYOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateYOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction AxisAlignedBB$calculateZOffset = new MethodInstruction(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateZOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
        final MethodInstruction BlockPos$down_NO_ARGS = new MethodInstruction(INVOKEVIRTUAL, BlockPos, BlockPos$down_NO_ARGS_name, new MethodDesc(BlockPos));
        final MethodInstruction BlockPos$getY = new MethodInstruction(INVOKEVIRTUAL, BlockPos, BlockPos$getY_name, new MethodDesc(INT));
        final MethodInstruction BlockPos$INIT = new MethodInstruction(INVOKESPECIAL, BlockPos, INIT, new MethodDesc(VOID, INT, INT, INT));
        final MethodInstruction BlockPos$up_INT_ARG = new MethodInstruction(INVOKEVIRTUAL, BlockPos, BlockPos$up_INT_ARG_name, new MethodDesc(BlockPos, INT));
        final MethodInstruction BlockPos$up_NO_ARGS = new MethodInstruction(INVOKEVIRTUAL, BlockPos, BlockPos$up_NO_ARGS_name, new MethodDesc(BlockPos));
        final MethodInstruction BlockPos$PooledMutableBlockPos$setPos = new MethodInstruction(
                INVOKEVIRTUAL, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$setPos_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction BlockPos$PooledMutableBlockPos$retain = new MethodInstruction(
                INVOKESTATIC, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$retain_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction Entity$getEntityBoundingBox = new MethodInstruction(INVOKEVIRTUAL, Entity, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
        final MethodInstruction EntityLivingBase$getLookVec = new MethodInstruction(INVOKEVIRTUAL, EntityLivingBase, Entity$getLookVec_name, new MethodDesc(Vec3d));
        final MethodInstruction EntityLivingBase$isOffsetPositionInLiquid = new MethodInstruction(
                INVOKEVIRTUAL, EntityLivingBase, Entity$isOffsetPositionInLiquid_name, new MethodDesc(BOOLEAN, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction EntityPlayerMP$getEyeHeight = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerMP, Entity$getEyeHeight_name, new MethodDesc(FLOAT));
        final MethodInstruction EntityPlayerMP$handleFalling = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerMP, EntityPlayerMP$handleFalling_name, new MethodDesc(VOID, DOUBLE, BOOLEAN));
        final MethodInstruction EntityPlayerMP$moveEntity = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerMP, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction EntityPlayerSP$getEntityBoundingBox = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerSP, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
        final MethodInstruction EntityPlayerSP$getForward = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerSP, Entity$getForward_name, new MethodDesc(Vec3d));
        final MethodInstruction EntityPlayer$getFoodStats = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerSP, EntityPlayer$getFoodStats_name, new MethodDesc(FoodStats));
        final MethodInstruction Vec3d$addVector = new MethodInstruction(INVOKEVIRTUAL, Vec3d, Vec3d$addVector_name, new MethodDesc(Vec3d, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction Vec3d$scale = new MethodInstruction(INVOKEVIRTUAL, Vec3d, Vec3d$scale_name, new MethodDesc(Vec3d, DOUBLE));
        final MethodInstruction WorldClient$getEntitiesInAABBexcluding = new MethodInstruction(
                INVOKEVIRTUAL, WorldClient, World$getEntitiesInAABBexcluding_name, new MethodDesc(List, Entity, AxisAlignedBB, Predicate));

        /*
            Up And Down And All Around asm hook method instructions
         */
        final HooksMethodInstruction Hooks$addAdjustedVector = new HooksMethodInstruction("addAdjustedVector", new MethodDesc(Vec3d, Vec3d, DOUBLE, DOUBLE, DOUBLE, Entity));
        final HooksMethodInstruction Hooks$adjustVec = new HooksMethodInstruction("adjustVec", new MethodDesc(Vec3d, Vec3d, Entity));
        final HooksMethodInstruction Hooks$adjustXYZ = new HooksMethodInstruction("adjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
        final HooksMethodInstruction Hooks$constructNewGAABBFrom2Vec3d = new HooksMethodInstruction("constructNewGAABBFrom2Vec3d", new MethodDesc(AxisAlignedBB, Vec3d, Vec3d, Entity));
        final HooksMethodInstruction Hooks$getBlockPosAtTopOfPlayer = new HooksMethodInstruction("getBlockPosAtTopOfPlayer", new MethodDesc(BlockPos, Entity));
        final HooksMethodInstruction Hooks$getBlockPostBelowEntity = new HooksMethodInstruction("getBlockPosBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, Entity));
        final HooksMethodInstruction Hooks$getBottomOfEntity = new HooksMethodInstruction("getBottomOfEntity", new MethodDesc(Vec3d, Entity));
        final HooksMethodInstruction Hooks$getImmutableBlockPosBelowEntity = new HooksMethodInstruction("getImmutableBlockPosBelowEntity", new MethodDesc(BlockPos, Entity));
        final HooksMethodInstruction Hooks$getOriginRelativePosX = new HooksMethodInstruction("getOriginRelativePosX", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getOriginRelativePosY = new HooksMethodInstruction("getOriginRelativePosY", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getOriginRelativePosZ = new HooksMethodInstruction("getOriginRelativePosZ", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getPrevRelativeYawHead = new HooksMethodInstruction("getPrevRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
        final HooksMethodInstruction Hooks$getRelativeBottomOfBB = new HooksMethodInstruction("getRelativeBottomOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
        final HooksMethodInstruction Hooks$getRelativeDownBlockPos = new HooksMethodInstruction("getRelativeDownBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
        final HooksMethodInstruction Hooks$getRelativeLookVec = new HooksMethodInstruction("getRelativeLookVec", new MethodDesc(Vec3d, Entity));
        final HooksMethodInstruction Hooks$getRelativePitch = new HooksMethodInstruction("getRelativePitch", new MethodDesc(FLOAT, Entity));
        final HooksMethodInstruction Hooks$getRelativePrevPitch = new HooksMethodInstruction("getRelativePrevPitch", new MethodDesc(FLOAT, Entity));
        final HooksMethodInstruction Hooks$getRelativePrevPosX = new HooksMethodInstruction("getRelativePrevPosX", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getRelativePrevPosZ = new HooksMethodInstruction("getRelativePrevPosZ", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getRelativePrevYaw = new HooksMethodInstruction("getRelativePrevYaw", new MethodDesc(FLOAT, Entity));
        final HooksMethodInstruction Hooks$getRelativePosX = new HooksMethodInstruction("getRelativePosX", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getRelativePosY = new HooksMethodInstruction("getRelativePosY", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getRelativePosZ = new HooksMethodInstruction("getRelativePosZ", new MethodDesc(DOUBLE, Entity));
        final HooksMethodInstruction Hooks$getRelativeTopOfBB = new HooksMethodInstruction("getRelativeTopOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
        final HooksMethodInstruction Hooks$getRelativeUpblockPos_INT_ARG = new HooksMethodInstruction("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, INT, Entity));
        final HooksMethodInstruction Hooks$getRelativeUpblockPos_NO_ARGS = new HooksMethodInstruction("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
        final HooksMethodInstruction Hooks$getRelativeYaw = new HooksMethodInstruction("getRelativeYaw", new MethodDesc(FLOAT, Entity));
        final HooksMethodInstruction Hooks$getRelativeYawHead = new HooksMethodInstruction("getRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
        final HooksMethodInstruction Hooks$getRelativeYOfBlockPos = new HooksMethodInstruction("getRelativeYOfBlockPos", new MethodDesc(INT, BlockPos, Entity));
        final HooksMethodInstruction Hooks$getVanillaEntityBoundingBox = new HooksMethodInstruction("getVanillaEntityBoundingBox", new MethodDesc(AxisAlignedBB, Entity));
        final HooksMethodInstruction Hooks$inverseAdjustXYZ = new HooksMethodInstruction("inverseAdjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
        final HooksMethodInstruction Hooks$isHeadspaceFree = new HooksMethodInstruction("isHeadspaceFree", new MethodDesc(BOOLEAN, EntityPlayerSP, BlockPos, INT));
        final HooksMethodInstruction Hooks$makePositionAbsolute = new HooksMethodInstruction("makePositionAbsolute", new MethodDesc(VOID, EntityLivingBase));
        final HooksMethodInstruction Hooks$makePositionRelative = new HooksMethodInstruction("makePositionRelative", new MethodDesc(VOID, EntityLivingBase));
        final HooksMethodInstruction Hooks$moveEntityAbsolute = new HooksMethodInstruction("moveEntityAbsolute", new MethodDesc(VOID, EntityPlayer, DOUBLE, DOUBLE, DOUBLE));
        final HooksMethodInstruction Hooks$netHandlerPlayServerHandleFallingYChange =
                new HooksMethodInstruction("netHandlerPlayServerHandleFallingYChange", new MethodDesc(DOUBLE, EntityPlayerMP, DOUBLE, DOUBLE, DOUBLE));
        final HooksMethodInstruction Hooks$pushEntityPlayerSPOutOfBlocks = new HooksMethodInstruction("pushEntityPlayerSPOutOfBlocks", new MethodDesc(VOID, EntityPlayerWithGravity, AxisAlignedBB));
        final HooksMethodInstruction Hooks$reverseXOffset = new HooksMethodInstruction("reverseXOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final HooksMethodInstruction Hooks$reverseYOffset = new HooksMethodInstruction("reverseYOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final HooksMethodInstruction Hooks$reverseZOffset = new HooksMethodInstruction("reverseZOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
        final HooksMethodInstruction Hooks$setListenerOrientationHook = new HooksMethodInstruction("setListenerOrientationHook", new MethodDesc(VOID, SoundSystem, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, EntityPlayer));
        final HooksMethodInstruction Hooks$setPooledMutableBlockPosToBelowEntity =
                new HooksMethodInstruction("setPooledMutableBlockPosToBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos, Entity));

        /*
            Debug only (currently only used in patchEntityPlayerSP when DEBUG_AUTO_JUMP is true)
         */
        final MethodInstruction AbstractClientPlayer$moveEntity_SPECIAL = new MethodInstruction(Opcodes.INVOKESPECIAL, AbstractClientPlayer, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
        final MethodInstruction AbstractClientPlayer$updateAutoJump_SPECIAL = new MethodInstruction(Opcodes.INVOKESPECIAL, AbstractClientPlayer, EntityPlayerSP$updateAutoJump_name, new MethodDesc(VOID, FLOAT, FLOAT));
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
            log("Patching class %s", className);
            byte[] toReturn = function.apply(bytes);
            log("Patched class  %s", className);
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
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.RenderLivingBase$doRender_name.is(methodNode)) {
                logPatchStarting(HELPER.RenderLivingBase$doRender_name);
                patchMethodUsingAbsoluteRotations(methodNode, ALL_GET_ROTATION_VARS);
                logPatchComplete(HELPER.RenderLivingBase$doRender_name);
                methodPatches++;
                break;
            }
        }

        dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityRenderer(byte[] bytes) {
        int methodPatches = 0;
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
                                methodPatches++;
                                break outerfor;
                            }
                        }
                        die("Failed to find " + HELPER.WorldClient$getEntitiesInAABBexcluding + " in " + classNode.name + "::" + HELPER.EntityRenderer$getMouseOver_name);
                    }
                }
            }
        }

        dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntity(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.Entity$moveEntity_name.is(methodNode)) {
                logPatchStarting(HELPER.Entity$moveEntity_name);
                int numReplaced = 0;
                boolean newBlockPosFound = false;
                boolean blockPos$downFound = false;
                boolean Blocks$LADDERFound = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (HELPER.AxisAlignedBB$calculateXOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseXOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (HELPER.AxisAlignedBB$calculateYOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseYOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (HELPER.AxisAlignedBB$calculateZOffset.is(methodInsnNode)) {
                            HELPER.Hooks$reverseZOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (!newBlockPosFound && HELPER.BlockPos$INIT.is(methodInsnNode)) {
                            // Starts with a simple 1:1 replacement
                            HELPER.Hooks$getImmutableBlockPosBelowEntity.replace(methodInsnNode);
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
                            HELPER.Hooks$getRelativeDownBlockPos.replace(methodInsnNode);
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

                dieIfFalse(numReplaced > 0, "Could not find any AxisAlignedBB:calculate[XYZ]Offset methods");
                dieIfFalse(newBlockPosFound, "Failed to find new instance creation of BlockPos");
                dieIfFalse(blockPos$downFound, "Failed to find usage of BlockPos::down");
                dieIfFalse(Blocks$LADDERFound, "Failed to find usage of Blocks.LADDER");
                logPatchComplete(HELPER.Entity$moveEntity_name);
                methodPatches++;
            }
            else if (HELPER.Entity$moveRelative_name.is(methodNode)) {
                logPatchStarting(HELPER.Entity$moveRelative_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(HELPER.Entity$moveRelative_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityLivingBase(byte[] bytes) {
        int methodPatches = 0;
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
                            HELPER.Hooks$getRelativeLookVec.replace(methodInsnNode);
                            replaced_getLookVec = true;
                        }
                        else if (replaced_rotationPitch
                                && !BlockPos$PooledMutableBlockPos$retainFound
                                && HELPER.BlockPos$PooledMutableBlockPos$retain.is(methodInsnNode)){
                            HELPER.Hooks$getBlockPostBelowEntity.replace(methodInsnNode);

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
                            HELPER.Hooks$setPooledMutableBlockPosToBelowEntity.replace(methodInsnNode);

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

                dieIfFalse(replaced_getLookVec, "Failed to replace getLookVec");
                dieIfFalse(replaced_rotationPitch, "Failed to replace rotationPitch");
                dieIfFalse(BlockPos$PooledMutableBlockPos$retainFound, "Failed to find BlockPos$PooledMutableBlockPos::retain");
                dieIfFalse(BlockPos$PooledMutableBlockPos$setPosFound, "Failed to find BlockPos$PooledMutableBlockPos::setPos");
                dieIfFalse(numEntityLivingBase$isOffsetPositionInLiquidFound == 2, "Found " + numEntityLivingBase$isOffsetPositionInLiquidFound + " EntityLivingBase::isOffsetPositionInLiquidFound, expected 2");
                dieIfFalse(GETFIELD_limbSwingAmount_found, "Failed to find GETFIELD limbSwingAmount");
                dieIfFalse(PUTFIELD_limbSwing_found, "Failed to find PUTFIELD limbSwing");

                logPatchComplete(HELPER.EntityLivingBase$moveEntityWithHeading_name);
                methodPatches++;
            }
            else if (HELPER.EntityLivingBase$updateDistance_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$updateDistance_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(HELPER.EntityLivingBase$updateDistance_name);
                methodPatches++;
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
                dieIfFalse(patchComplete, "Failed to patch " + HELPER.Entity$onUpdate_name);
                logPatchComplete(HELPER.Entity$onUpdate_name);
                methodPatches++;
            }
            else if (HELPER.EntityLivingBase$jump_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$jump_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(HELPER.EntityLivingBase$jump_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 4, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchNetHandlerPlayClient(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);


        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.INetHandlerPlayClient$handlePlayerPosLook_name.is(methodNode)) {
                logPatchStarting(HELPER.INetHandlerPlayClient$handlePlayerPosLook_name);
                boolean foundAtLeastOneMinY = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (HELPER.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                            HELPER.EntityPlayer$posY_GET.replace(iterator);
                            iterator.previous(); // the instruction we just replaced the old one with
                            iterator.previous(); // getEntityBoundingBox()
                            iterator.remove();
                            foundAtLeastOneMinY = true;
                        }
                    }
                }
                dieIfFalse(foundAtLeastOneMinY, "Could not find \"playerEntity.geEntityBoundingBox().minY\" in " + HELPER.INetHandlerPlayClient$handlePlayerPosLook_name);
                logPatchComplete(HELPER.INetHandlerPlayClient$handlePlayerPosLook_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchNetHandlerPlayServer(byte[] bytes) {
        int methodPatches = 0;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.INetHandlerPlayServer$processPlayer_name.is(methodNode)) {
                logPatchStarting(HELPER.INetHandlerPlayServer$processPlayer_name);

                boolean foundMoveEntity = false;
                boolean foundHandleFalling = false;

                boolean foundDifferenceLocalVars = false;
                int xDiffLocalVar = -1;
                int yDiffLocalVar = -1;
                int zDiffLocalVar = -1;
                int yDiffLoadCount = 0;
                boolean patchedYDiffLocalUsedToCalculate_floating = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (!foundMoveEntity && HELPER.EntityPlayerMP$moveEntity.is(methodInsnNode)) {
                            HELPER.Hooks$moveEntityAbsolute.replace(methodInsnNode);
                            foundMoveEntity = true;
                        }
                        else if (!foundHandleFalling && HELPER.EntityPlayerMP$handleFalling.is(methodInsnNode)) {
                            iterator.previous(); // INVOKEVIRTUAL handleFalling again
                            iterator.previous(); // INVOKEVIRTUAL isOnGround
                            iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                            next = iterator.previous(); // Should be DSUB
                            if (next.getOpcode() == Opcodes.DSUB) {
                                iterator.remove();
                                VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                                iterator.add(dload7);
                                HELPER.Hooks$netHandlerPlayServerHandleFallingYChange.addTo(iterator);
                                iterator.previous(); // Our newly added INVOKESTATIC
                                iterator.previous(); // Our newly added DLOAD 7
                                iterator.previous(); // DLOAD 9
                                next = iterator.previous(); // GETFIELD posY
                                if (next instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                    if (HELPER.EntityPlayerMP$posY_GET.is(fieldInsnNode))
                                    {
                                        VarInsnNode dload3 = new VarInsnNode(Opcodes.DLOAD, 3);
                                        iterator.remove();
                                        iterator.add(dload3);
                                        foundHandleFalling = true;
                                    }
                                    else {
                                        die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                    }
                                }
                                else {
                                    die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"GETFIELD posY\"");
                                }
                            }
                            else {
                                die("Unexpected instruction in NetHandletPlayServer::processPlayer, expecting \"DSUB\"");
                            }
                        }
                    }
                    else if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (!foundDifferenceLocalVars && HELPER.NetHandlerPlayServer$lastGoodX_GET.is(fieldInsnNode)) {
                            foundDifferenceLocalVars = true;
                            for (int foundCount = 0; foundCount < 3; ) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.DSTORE) {
                                    VarInsnNode varInsnNode = (VarInsnNode)next;
                                    switch (foundCount) {
                                        case 0:
                                            xDiffLocalVar = varInsnNode.var;
                                            break;
                                        case 1:
                                            yDiffLocalVar = varInsnNode.var;
                                            break;
                                        case 2:
                                            zDiffLocalVar = varInsnNode.var;
                                            break;
                                    }
                                    foundCount++;
                                }
                            }
                        }
                    }
                    else if (next instanceof VarInsnNode) {
                        VarInsnNode varInsnNode = (VarInsnNode)next;
                        if (foundDifferenceLocalVars
                                && !patchedYDiffLocalUsedToCalculate_floating
                                && varInsnNode.var == yDiffLocalVar
                                && varInsnNode.getOpcode() == Opcodes.DLOAD) {
                            yDiffLoadCount++;
                            if (yDiffLoadCount == 3) {
                                //Insert instructions to do Hooks.adjustXYZ
                                iterator.previous();//DLOAD yDiff
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                HELPER.NetHandlerPlayServer$playerEntity_GET.addTo(iterator);
                                iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar));
                                iterator.next();//DLOAD yDiff again
                                iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar));
                                HELPER.Hooks$adjustXYZ.addTo(iterator);
                                iterator.add(new InsnNode(Opcodes.ICONST_1));
                                iterator.add(new InsnNode(Opcodes.DALOAD));
                                //next instruction stores to local variable
                                patchedYDiffLocalUsedToCalculate_floating = true;
                            }

                        }
                    }
                }
                dieIfFalse(foundMoveEntity, "Could not find \"playerEntity.moveEntity(...)\"");
                dieIfFalse(foundHandleFalling, "Could not find \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\"");

                logPatchComplete(HELPER.INetHandlerPlayServer$processPlayer_name);
                methodPatches++;
            }
            else if (HELPER.INetHandlerPlayServer$processPlayerDigging_name.is(methodNode)) {
                logPatchStarting(HELPER.INetHandlerPlayServer$processPlayerDigging_name);

                boolean patched1point5ToGetEyeHeight = false;

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
                                HELPER.NetHandlerPlayServer$playerEntity_GET.addTo(iterator); //this.playerEntity
                                HELPER.EntityPlayerMP$getEyeHeight.addTo(iterator); //this.playerEntity.getEyeHeight()
                                iterator.add(new InsnNode(Opcodes.F2D)); //(double)this.playerEntity.getEyeHeight()
                                patched1point5ToGetEyeHeight = true;
                                break;
                            }
                        }
                    }
                }
                dieIfFalse(patched1point5ToGetEyeHeight, "Could not find \"1.5D\"");

                logPatchComplete(HELPER.INetHandlerPlayServer$processPlayerDigging_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static final boolean DEBUG_AUTO_JUMP = false;

    //TODO: Instead of changing the field, call a method in Hooks that takes the aabb as an argument and returns GravityAxisAlignedBB.getOrigin().getY() or AxisAlignedBB.minY
    private static byte[] patchEntityPlayerSP(byte[] bytes) {
        int methodPatches = 0;
        int expectedMethodPatches = 5;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

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
            if (HELPER.EntityPlayerSP$onUpdateWalkingPlayer_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityPlayerSP$onUpdateWalkingPlayer_name);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) next;
                        if (HELPER.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
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
                                        HELPER.EntityPlayerSP$posY_GET.replace(fieldInsnNode);
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
                dieIfFalse(numReplacements != 0, "Failed to find any instances of \"axisalignedbb.minY\"");
                logPatchComplete(HELPER.EntityPlayerSP$onUpdateWalkingPlayer_name);
                methodPatches++;
            }
            else if (HELPER.EntityLivingBase$onLivingUpdate_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityLivingBase$onLivingUpdate_name);

                boolean error = true;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (HELPER.EntityPlayerSP$getEntityBoundingBox.is(next)) {
                        next = iterator.next();
                        if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ASTORE) {
                            int axisalignedbb_var = ((VarInsnNode)next).var;

                            VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                            TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, HELPER.GravityAxisAlignedBB.toString());
                            JumpInsnNode ifeqJumpInsnNode = new JumpInsnNode(Opcodes.IFEQ, null);
                            VarInsnNode aLoad0 = new VarInsnNode(Opcodes.ALOAD, 0);
//                            VarInsnNode aLoad6_2 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                            JumpInsnNode gotoJumpInsnNode = new JumpInsnNode(Opcodes.GOTO, null);

                            // Load the localvariable which holds the value previously returned by this.getEntityBoundingBox()
                            iterator.add(aLoad6);
                            // Add 'if (axisalignedbb instanceof GravityAxisAlignedBB)'
                            iterator.add(instanceofGravityAxisAlignedBB);
                            // Add the conditional jump instruction
                            iterator.add(ifeqJumpInsnNode);
                            // Load 'this'
                            iterator.add(aLoad0);
                            // Load the localvariable which holds the value previously...
                            iterator.add(aLoad6.clone(null));
                            // Add our static hooks call
                            HELPER.Hooks$pushEntityPlayerSPOutOfBlocks.addTo(iterator);
                            // Add the unconditional jump
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
                                            if (HELPER.EntityPlayer$getFoodStats.is(methodInsnNode)) {
                                                for (; iterator.hasPrevious(); ) {
                                                    AbstractInsnNode previous = iterator.previous();
                                                    if (previous instanceof LabelNode) {
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
                                    dieIfFalse(labelForGotoJumpInsnNode != null, "Couldn't find correct label for jump instruction");
                                    gotoJumpInsnNode.label = labelForGotoJumpInsnNode;
                                    error = false;
                                    break;
                                }
                            }
                            break;
                        }
                        else {
                            die("Unexpected instruction after EntityPlayerSP::getEntityBoundingBox");
                        }
                    }
                }
                dieIfFalse(!error, "Patch failed");
                methodPatches++;
                logPatchComplete(HELPER.EntityLivingBase$onLivingUpdate_name);
            }
            else if (HELPER.EntityPlayerSP$isHeadspaceFree_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityPlayerSP$isHeadspaceFree_name);
                InsnList instructions = methodNode.instructions;
                AbstractInsnNode label = instructions.get(0);
                AbstractInsnNode lineNumber = instructions.get(1);
                AbstractInsnNode labelEnd = instructions.getLast();
                AbstractInsnNode returnNode = labelEnd.getPrevious();

                //TODO: Rewrite this so that it doesn't straight up delete all the existing instructions
                instructions.clear();
                instructions.add(label);
                instructions.add(lineNumber);
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                //TODO: Introduce a way to grab the localvariable indices
                instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
                HELPER.Hooks$isHeadspaceFree.addTo(instructions);
                instructions.add(new InsnNode(Opcodes.IRETURN));
                instructions.add(labelEnd);
                methodPatches++;
                logPatchComplete(HELPER.EntityPlayerSP$isHeadspaceFree_name);
            }
            else if (HELPER.EntityPlayerSP$updateAutoJump_name.is(methodNode)) {
                logPatchStarting(HELPER.EntityPlayerSP$updateAutoJump_name);
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

                // Used during development
                if (DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.FLOAD, 2));
                            HELPER.AbstractClientPlayer$updateAutoJump_SPECIAL.addTo(iterator);
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
                                    && HELPER.EntityPlayerSP$getEntityBoundingBox.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.next();
                                iterator.remove(); // GETFIELD net/minecraft/util/math/AxisAlignedBB.minY : D
                                HELPER.Hooks$getOriginRelativePosY.addTo(iterator);
                                iterator.next(); // DLOAD ? (likely 7)
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Have to pass 'this' to adjustVec as well
                                HELPER.Hooks$adjustVec.addTo(iterator);
                                patch4Complete = true;
                            }
                            //newPatch #5
                            else if (patch4Point5Complete
                                    && !patch5Complete
                                    && HELPER.Vec3d$scale.is(methodInsnNode)) {
                                next = iterator.next();
                                if (next instanceof VarInsnNode) {
                                    // Should also be ASTORE
                                    vec3d12_var = ((VarInsnNode)next).var;
                                    patch5Complete = true;
                                }
                                else {
                                    die("Expected a VarInsnNode after first usage of Vec3d::scale");
                                }
                            }
                            else if (patch5Complete
                                    && !patch5Point5Complete
                                    && HELPER.EntityPlayerSP$getForward.is(methodInsnNode)) {
                                HELPER.Hooks$getRelativeLookVec.replace(methodInsnNode);
                                patch5Point5Complete = true;
                            }
                            //PATCH #1
                            else if (HELPER.Vec3d$addVector.is(methodInsnNode)){
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$addAdjustedVector.addTo(iterator);
                                addVectorReplacements++;
                            }
                            //newPATCH #8
                            else if (patch7Complete
                                    && !patch8Complete
                                    && HELPER.AxisAlignedBB$INIT.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$constructNewGAABBFrom2Vec3d.addTo(iterator);
                                patch8Complete = true;
                            }
                            //PATCH #3
                            else if (HELPER.BlockPos$up_NO_ARGS.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$getRelativeUpblockPos_NO_ARGS.addTo(iterator);
                                blockPosUPCount++;
                            }
                            else if (HELPER.BlockPos$up_INT_ARG.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$getRelativeUpblockPos_INT_ARG.addTo(iterator);
                                blockPosUPCountI++;
                            }
                            //PATCH #4
                            else if (!foundFirstBlockPosGetY
                                    && HELPER.BlockPos$getY.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$getRelativeYOfBlockPos.addTo(iterator);
                                foundFirstBlockPosGetY = true;
                            }

                        }
                        else if (next instanceof TypeInsnNode
                                && next.getOpcode() == Opcodes.NEW) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                            //newPATCH #1
                            if (!patch1Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(HELPER.Vec3d.toString())) {
                                iterator.remove();
                                while(!patch1Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        HELPER.Hooks$getBottomOfEntity.addTo(iterator);
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
                                    && typeInsnNode.desc.equals(HELPER.AxisAlignedBB.toString())) {
                                iterator.remove();
                                next = iterator.next();
                                if (next instanceof InsnNode && next.getOpcode() == Opcodes.DUP) {
                                    iterator.remove();
                                    patch7Complete = true;
                                }
                                else {
                                    //what
                                    die("DUP instruction not found after expected NEW AxisAlignedBB");
                                }
                            }
                            //newPATCH #6
                            else if(patch5Point5Complete
                                    && !patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(HELPER.BlockPos.toString())) {
                                iterator.remove();
                                while(!patch6Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        HELPER.Hooks$getBlockPosAtTopOfPlayer.addTo(iterator);

                                        //newPATCH #6.5
                                        iterator.next(); //ASTORE ? (probably 17)
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, vec3d12_var));
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        HELPER.Hooks$adjustVec.addTo(iterator);
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
                                    && typeInsnNode.desc.equals(HELPER.Vec3d.toString())) {
                                iterator.next(); // DUP
                                iterator.next(); // DCONST_0
                                next = iterator.next();
                                if (next.getOpcode() == Opcodes.DCONST_1) {
                                    iterator.remove();
                                    iterator.add(new InsnNode(Opcodes.DCONST_0));
                                }
                                else {
                                    die("Expecting DCONST_0 followed by DCONST_1, but instead got " + next);
                                }
                                iterator.next(); // DCONST_0
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V

                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new InsnNode(Opcodes.DCONST_1));
                                iterator.add(new InsnNode(Opcodes.DCONST_0));
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$addAdjustedVector.addTo(iterator);

                                patch9Complete = true;
                            }

                        }
                        else if (next instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                            //newPATCH #2
                            if (patch1Complete
                                    && !patch2Complete
                                    && HELPER.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                HELPER.Hooks$getOriginRelativePosX.replace(iterator);
                                patch2Complete = true;
                            }
                            //newPATCH #3
                            else if(patch2Complete
                                    && !patch3Complete
                                    && HELPER.EntityPlayerSP$posZ_GET.is(fieldInsnNode)) {
                                HELPER.Hooks$getOriginRelativePosZ.replace(iterator);
                                patch3Complete = true;
                            }
                            else if(patch4Complete
                                    && !patch4Point5Complete
                                    && HELPER.EntityPlayerSP$rotationYaw_GET.is(fieldInsnNode)) {
                                HELPER.Hooks$getRelativeYaw.replace(iterator);
                                patch4Point5ReplacementCount++;
                                if (patch4Point5ReplacementCount >= 2) {
                                    patch4Point5Complete = true;
                                }
                            }
                            //newPATCH #10
                            else if (patch9Complete
                                    && axisAlignedBBmaxYCount < 2
                                    && HELPER.AxisAlignedBB$maxY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$getRelativeTopOfBB.addTo(iterator);
                                axisAlignedBBmaxYCount++;
                            }
                            //newPATCH #11
                            else if (axisAlignedBBmaxYCount == 2
                                    && axisAlignedBBminYCount < 2
                                    && HELPER.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                HELPER.Hooks$getRelativeBottomOfBB.addTo(iterator);
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
                    die("Unknown error occurred while patching EntityPlayerSP");
                }
                logPatchComplete(HELPER.EntityPlayerSP$updateAutoJump_name);
                methodPatches++;
            }
            else if (HELPER.Entity$moveEntity_name.is(methodNode)) {
                logPatchStarting(HELPER.Entity$moveEntity_name);
                if (DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 3));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 5));
                            HELPER.AbstractClientPlayer$moveEntity_SPECIAL.addTo(iterator);
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
                            HELPER.EntityPlayerSP$posY_GET.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            HELPER.EntityPlayerSP$posZ_GET.addTo(iterator);
                            HELPER.Hooks$inverseAdjustXYZ.addTo(iterator);
                            iterator.add(new InsnNode(Opcodes.DUP));
                            iterator.add(new InsnNode(Opcodes.ICONST_0));
                            iterator.add(new InsnNode(Opcodes.DALOAD));
                            //next is DSTORE 7
                            next = iterator.next();
                            if (next instanceof VarInsnNode) {
                                prevRelativeXPos_var = ((VarInsnNode)next).var;
                            }
                            else {
                                die("Was expecting DSTORE _ after GETFIELD posX, instead got " + next);
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
                                        die("Was expecting DSTORE _ after GETFIELD posZ, instead got " + next);
                                    }

                                    while(true) {
                                        next = iterator.next();
                                        if(next instanceof FieldInsnNode) {
                                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                            if (HELPER.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                                iterator.previous();
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.next(); // same GETFIELD instruction
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                HELPER.EntityPlayerSP$posY_GET.addTo(iterator);
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                HELPER.EntityPlayerSP$posZ_GET.addTo(iterator);
                                                HELPER.Hooks$inverseAdjustXYZ.addTo(iterator);
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
                logPatchComplete(HELPER.Entity$moveEntity_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == expectedMethodPatches, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);

        return classWriter.toByteArray();

    }

    private static byte[] patchSoundManager(byte[] bytes) {
        int methodPatches = 0;
        int expectedMethodPatches = 1;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (HELPER.SoundManager$setListener_name.is(methodNode)) {
                logPatchStarting(HELPER.SoundManager$setListener_name);
                boolean patched_setListenerOrientationInstruction = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof  MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) next;
                        if (HELPER.SoundSystem$setListenerOrientation_name.is(methodInsnNode.name)) {
                            HELPER.Hooks$setListenerOrientationHook.replace(methodInsnNode);
                            iterator.previous();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load EntityPlayer method argument
                            patched_setListenerOrientationInstruction = true;
                        }
                    }
                }
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW + GET_PREVROTATIONYAW + GET_ROTATIONPITCH + GET_PREVROTATIONPITCH);
                dieIfFalse(patched_setListenerOrientationInstruction, "Failed to patch setListenerOrientation instruction");
                logPatchComplete(HELPER.SoundManager$setListener_name);
                methodPatches++;
            }
        }
        dieIfFalse(methodPatches == expectedMethodPatches, "Could not find " + HELPER.SoundManager$setListener_name + " method");
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
