package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLLog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.*;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.FieldInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.HookInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.MethodInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.DeobfAwareString;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.FieldName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.MethodName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ObjectName;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static org.objectweb.asm.Opcodes.*;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.*;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.DOUBLE;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.FLOAT;

/**
 * Transformer class that uses ASM to patch vanilla Minecraft classes
 * Created by Mysteryem on 2016-08-16.
 */
public class Transformer implements IClassTransformer {

    // Maps classes to their patch methods
    private static final HashMap<String, Function<byte[], byte[]>> classNameToMethodMap = new HashMap<>();

    // Superclass replacement details:
    // Effectively patches:
    //  Cleanly calls super method:
    //      moveEntity, jump, handleWaterMovement, setEntityBoundingBox, updateFallState, pushOutOfBlocks (NYI), knockback, setAngles
    //  Should always call super:
    //      getEntityBoundingBox (in the case it doesn't call super, the mod probably isn't going to work anyway)
    //  Sometimes calls super:
    //      getEyeHeight (super call is used elsewhere to simulate the effects of calling super)
    //  Only calls super for downwards gravity:
    //      resetPositionToBB, (method is basically completely replaced outside of downwards gravity, ASM probably not worth it)
    //      playStepSound (done in order to bypass some vanilla code)
    //  Shouldn't ever call super:
    //      isEntityInsideOpaqueBlock, (replacing with ASM looks like a good idea)
    //      createRunningParticles, (replacing with ASM looks like a good idea)
    //      isOnLadder (method pretty much entirely replaced)
    //  Never calls super!:
    //      updateSize, (replacing with ASM looks like a good idea)
    //      setSize, (probably can be replaced with ASM?)
    //      setPosition (method pretty much entirely replaced)
    private static final String classToReplace = "net/minecraft/entity/player/EntityPlayer";
    private static final String classReplacement = "uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity";
    /*
        Mojang method names
    */
    private static final MethodName AxisAlignedBB$calculateXOffset_name = new MethodName("calculateXOffset", "func_72316_a");
    private static final MethodName AxisAlignedBB$calculateYOffset_name = new MethodName("calculateYOffset", "func_72323_b");
    private static final MethodName AxisAlignedBB$calculateZOffset_name = new MethodName("calculateZOffset", "func_72322_c");
    // () arguments
    private static final MethodName BlockPos$down_NO_ARGS_name = new MethodName("down", "func_177977_b");
    private static final MethodName BlockPos$getY_name = new MethodName("getY", "func_177956_o");
    private static final MethodName BlockPos$up_INT_ARG_name = new MethodName("up", "func_177981_b");
    private static final MethodName BlockPos$up_NO_ARGS_name = new MethodName("up", "func_177984_a");
    // (DDD) arguments
    private static final MethodName BlockPos$PooledMutableBlockPos$retain_name = new MethodName("retain", "func_185345_c");
    // (DDD) arguments
    private static final MethodName BlockPos$PooledMutableBlockPos$setPos_name = new MethodName("setPos", "func_189532_c");
    private static final MethodName Entity$getEntityBoundingBox_name = new MethodName("getEntityBoundingBox", "func_174813_aQ");
    private static final MethodName Entity$getEyeHeight_name = new MethodName("getEyeHeight", "func_70047_e");
    private static final MethodName Entity$getForward_name = new MethodName("getForward", "func_189651_aD");
    private static final MethodName Entity$getLookVec_name = new MethodName("getLookVec", "func_70040_Z");
    private static final MethodName Entity$isOffsetPositionInLiquid_name = new MethodName("isOffsetPositionInLiquid", "func_70038_c");
    private static final MethodName Entity$moveEntity_name = new MethodName("moveEntity", "func_70091_d");
    private static final MethodName Entity$moveRelative_name = new MethodName("moveRelative", "func_70060_a");
    private static final MethodName Entity$onUpdate_name = new MethodName("onUpdate", "func_70071_h_");
    private static final MethodName EntityItem$combineItems_name = new MethodName("combineItems", "func_70289_a");
    private static final MethodName EntityLivingBase$jump_name = new MethodName("jump", "func_70664_aZ");
    private static final MethodName EntityLivingBase$moveEntityWithHeading_name = new MethodName("moveEntityWithHeading", "func_70612_e");
    private static final MethodName EntityLivingBase$onLivingUpdate_name = new MethodName("onLivingUpdate", "func_70636_d");
    private static final MethodName EntityLivingBase$updateDistance_name = new MethodName("updateDistance", "func_110146_f");
    private static final MethodName EntityPlayer$getFoodStats_name = new MethodName("getFoodStats", "func_71024_bL");
    private static final MethodName EntityPlayerMP$handleFalling_name = new MethodName("handleFalling", "func_71122_b");
    // Method added by forge, so no obf name
    private static final MethodName EntityPlayerSP$isHeadspaceFree_name = new MethodName("isHeadspaceFree");
    private static final MethodName EntityPlayerSP$onUpdateWalkingPlayer_name = new MethodName("onUpdateWalkingPlayer", "func_175161_p");
    private static final MethodName EntityPlayerSP$updateAutoJump_name = new MethodName("updateAutoJump", "func_189810_i");
    private static final MethodName EntityRenderer$drawNameplate_name = new MethodName("drawNameplate", "func_189692_a");
    private static final MethodName EntityRenderer$getMouseOver_name = new MethodName("getMouseOver", "func_78473_a");
    private static final MethodName GlStateManager$disableLighting_name = new MethodName("disableLighting", "func_179140_f");
    private static final MethodName INetHandlerPlayClient$handlePlayerPosLook_name = new MethodName("handlePlayerPosLook", "func_184330_a");
    private static final MethodName INetHandlerPlayServer$processPlayer_name = new MethodName("processPlayer", "func_147347_a");
    private static final MethodName INetHandlerPlayServer$processPlayerDigging_name = new MethodName("processPlayerDigging", "func_147345_a");
    private static final MethodName Item$onRightClick_name = new MethodName("onItemRightClick", "func_77659_a");
    private static final MethodName Item$onItemUse_name = new MethodName("onItemUse", "func_180614_a");
    private static final MethodName Item$onPlayerStoppedUsing_name = new MethodName("onPlayerStoppedUsing", "func_77615_a");
    private static final MethodName ItemStack$onItemUse_name = new MethodName("onItemUse", "func_179546_a");
    private static final MethodName ItemStack$onPlayerStoppedUsing_name = new MethodName("onPlayerStoppedUsing", "func_77974_b");
    private static final MethodName ItemStack$useItemRightClick_name = new MethodName("useItemRightClick", "func_77957_a");
    private static final MethodName RenderLivingBase$doRender_name = new MethodName("doRender", "func_76986_a");
    private static final MethodName SoundManager$setListener_name = new MethodName("setListener", "func_148615_a");
    private static final MethodName Vec3d$addVector_name = new MethodName("addVector", "func_72441_c");
    private static final MethodName Vec3d$scale_name = new MethodName("scale", "func_186678_a");
    private static final MethodName World$getEntitiesInAABBexcluding_name = new MethodName("getEntitiesInAABBexcluding", "func_175674_a");
    /*
        PaulsCode method names
     */
    private static final MethodName SoundSystem$setListenerOrientation_name = new MethodName("setListenerOrientation");
    /*
        Mojang field names
     */
    private static final FieldName AxisAlignedBB$maxY_name = new FieldName("maxY", "field_72337_e");
    private static final FieldName AxisAlignedBB$minY_name = new FieldName("minY", "field_72338_b");
    private static final FieldName Blocks$LADDER_name = new FieldName("LADDER", "field_150468_ap");
    private static final FieldName Entity$posX_name = new FieldName("posX", "field_70165_t");
    private static final FieldName Entity$posY_name = new FieldName("posY", "field_70163_u");
    private static final FieldName Entity$posZ_name = new FieldName("posZ", "field_70161_v");
    private static final FieldName Entity$prevPosX_name = new FieldName("prevPosX", "field_70169_q");
    private static final FieldName Entity$prevPosZ_name = new FieldName("prevPosZ", "field_70166_s");
    private static final FieldName Entity$prevRotationPitch_name = new FieldName("prevRotationPitch", "field_70127_C");
    private static final FieldName Entity$prevRotationYaw_name = new FieldName("prevRotationYaw", "field_70126_B");
    private static final FieldName Entity$rotationPitch_name = new FieldName("rotationPitch", "field_70125_A");
    private static final FieldName Entity$rotationYaw_name = new FieldName("rotationYaw", "field_70177_z");
    private static final FieldName EntityLivingBase$limbSwing_name = new FieldName("limbSwing", "field_184619_aG");
    private static final FieldName EntityLivingBase$limbSwingAmount_name = new FieldName("limbSwingAmount", "field_70721_aZ");
    private static final FieldName EntityLivingBase$prevRotationYawHead_name = new FieldName("prevRotationYawHead", "field_70758_at");
    private static final FieldName EntityLivingBase$rotationYawHead_name = new FieldName("rotationYawHead", "field_70759_as");
    private static final FieldName NetHandlerPlayServer$lastGoodX_name = new FieldName("lastGoodX", "field_184352_o");
    //    private static final FieldName NetHandlerPlayServer$lastGoodY_name = new FieldName("lastGoodY", "field_184353_p");
//    private static final FieldName NetHandlerPlayServer$lastGoodZ_name = new FieldName("lastGoodZ", "field_184354_q");
    private static final FieldName NetHandlerPlayServer$playerEntity_name = new FieldName("playerEntity", "field_147369_b");
    /*
        Shared reference class names
     */
    private static final ObjectName ActionResult = new ObjectName("net/minecraft/util/ActionResult");
    private static final ObjectName AbstractClientPlayer = new ObjectName("net/minecraft/client/entity/AbstractClientPlayer");
    private static final ObjectName AxisAlignedBB = new ObjectName("net/minecraft/util/math/AxisAlignedBB");
    private static final ObjectName Block = new ObjectName("net/minecraft/block/Block");
    private static final ObjectName BlockPos = new ObjectName("net/minecraft/util/math/BlockPos");
    private static final ObjectName BlockPos$PooledMutableBlockPos = new ObjectName("net/minecraft/util/math/BlockPos$PooledMutableBlockPos");
    private static final ObjectName Blocks = new ObjectName("net/minecraft/init/Blocks");
    private static final ObjectName CPacketPlayer = new ObjectName("net/minecraft/network/play/client/CPacketPlayer");
    private static final ObjectName Entity = new ObjectName("net/minecraft/entity/Entity");
    private static final ObjectName EntityLivingBase = new ObjectName("net/minecraft/entity/EntityLivingBase");
    private static final ObjectName EntityPlayer = new ObjectName("net/minecraft/entity/player/EntityPlayer");
    private static final ObjectName EntityPlayerMP = new ObjectName("net/minecraft/entity/player/EntityPlayerMP");
    private static final ObjectName EntityPlayerSP = new ObjectName("net/minecraft/client/entity/EntityPlayerSP");
    private static final ObjectName EnumActionResult = new ObjectName("net/minecraft/util/EnumActionResult");
    private static final ObjectName EnumFacing = new ObjectName("net/minecraft/util/EnumFacing");
    private static final ObjectName EnumHand = new ObjectName("net/minecraft/util/EnumHand");
    private static final ObjectName FoodStats = new ObjectName("net/minecraft/util/FoodStats");
    private static final ObjectName Item = new ObjectName("net/minecraft/item/Item");
    private static final ObjectName ItemStack = new ObjectName("net/minecraft/item/ItemStack");
    private static final ObjectName NetHandlerPlayServer = new ObjectName("net/minecraft/network/NetHandlerPlayServer");
    private static final ObjectName Vec3d = new ObjectName("net/minecraft/util/math/Vec3d");
    private static final ObjectName World = new ObjectName("net/minecraft/world/World");
    private static final ObjectName WorldClient = new ObjectName("net/minecraft/client/multiplayer/WorldClient");
    // Non-minecraft classes
//    private static final ObjectClassName EntityFloatingItem = new ObjectClassName("uk/co/mysterymayhem/gravitymod/common/entities/EntityFloatingItem");
    private static final ObjectName EntityPlayerWithGravity = new ObjectName("uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity");
    private static final ObjectName GravityAxisAlignedBB = new ObjectName("uk/co/mysterymayhem/gravitymod/common/util/boundingboxes/GravityAxisAlignedBB");
    private static final ObjectName ItemStackAndBoolean = new ObjectName("uk/co/mysterymayhem/gravitymod/asm/util/ItemStackAndBoolean");
    private static final ObjectName List = new ObjectName("java/util/List");
    private static final ObjectName Predicate = new ObjectName("com/google/common/base/Predicate");
    private static final ObjectName SoundSystem = new ObjectName("paulscode/sound/SoundSystem");
    /*
        Mojang field instructions
     */
    private static final FieldInsn AxisAlignedBB$maxY_GET = new FieldInsn(GETFIELD, AxisAlignedBB, AxisAlignedBB$maxY_name, DOUBLE);
    private static final FieldInsn AxisAlignedBB$minY_GET = new FieldInsn(GETFIELD, AxisAlignedBB, AxisAlignedBB$minY_name, DOUBLE);
    private static final FieldInsn Blocks$LADDER_GET = new FieldInsn(GETSTATIC, Blocks, Blocks$LADDER_name, Block);
    private static final FieldInsn Entity$posX_GET = new FieldInsn(GETFIELD, Entity, Entity$posX_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$limbSwing_PUT = new FieldInsn(PUTFIELD, EntityLivingBase, EntityLivingBase$limbSwing_name, FLOAT);
    private static final FieldInsn EntityLivingBase$limbSwingAmount_GET = new FieldInsn(GETFIELD, EntityLivingBase, EntityLivingBase$limbSwingAmount_name, FLOAT);
    private static final FieldInsn EntityLivingBase$posX_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posX_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$posY_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posY_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$posZ_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posZ_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$prevPosX_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$prevPosX_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$prevPosZ_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$prevPosZ_name, DOUBLE);
    private static final FieldInsn EntityLivingBase$rotationPitch_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$rotationPitch_name, FLOAT);
    private static final FieldInsn EntityLivingBase$rotationYaw_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$rotationYaw_name, FLOAT);
    private static final FieldInsn EntityPlayer$posY_GET = new FieldInsn(GETFIELD, EntityPlayer, Entity$posY_name, DOUBLE);
    private static final FieldInsn EntityPlayerMP$posY_GET = new FieldInsn(GETFIELD, EntityPlayerMP, Entity$posY_name, DOUBLE);
    private static final FieldInsn EntityPlayerMP$posZ_GET = new FieldInsn(GETFIELD, EntityPlayerMP, Entity$posZ_name, DOUBLE);
    private static final FieldInsn EntityPlayerSP$posX_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posX_name, DOUBLE);
    private static final FieldInsn EntityPlayerSP$posY_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posY_name, DOUBLE);
    private static final FieldInsn EntityPlayerSP$posZ_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posZ_name, DOUBLE);
    private static final FieldInsn EntityPlayerSP$rotationYaw_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$rotationYaw_name, FLOAT);
    private static final FieldInsn NetHandlerPlayServer$lastGoodX_GET = new FieldInsn(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodX_name, DOUBLE);
    //    private static final FieldInstruction NetHandlerPlayServer$lastGoodY_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodY_name, DOUBLE);
//    private static final FieldInstruction NetHandlerPlayServer$lastGoodZ_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodZ_name, DOUBLE);
    private static final FieldInsn NetHandlerPlayServer$playerEntity_GET = new FieldInsn(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$playerEntity_name, EntityPlayerMP);
    /*
        Mojang method instructions
     */
    // While there could be a constructor for MethodInstruction that doesn't require a MethodDesc, it is useful to
    // visually split apart the arguments so it's easier to tell what each argument is
    private static final MethodInsn AxisAlignedBB$INIT = new MethodInsn(INVOKESPECIAL, AxisAlignedBB, INIT, new MethodDesc(VOID, Vec3d, Vec3d));
    private static final MethodInsn AxisAlignedBB$calculateXOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateXOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    private static final MethodInsn AxisAlignedBB$calculateYOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateYOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    private static final MethodInsn AxisAlignedBB$calculateZOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateZOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    private static final MethodInsn BlockPos$down_NO_ARGS = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$down_NO_ARGS_name, new MethodDesc(BlockPos));
    private static final MethodInsn BlockPos$getY = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$getY_name, new MethodDesc(INT));
    private static final MethodInsn BlockPos$INIT = new MethodInsn(INVOKESPECIAL, BlockPos, INIT, new MethodDesc(VOID, INT, INT, INT));
    private static final MethodInsn BlockPos$up_INT_ARG = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$up_INT_ARG_name, new MethodDesc(BlockPos, INT));
    private static final MethodInsn BlockPos$up_NO_ARGS = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$up_NO_ARGS_name, new MethodDesc(BlockPos));
    private static final MethodInsn BlockPos$PooledMutableBlockPos$setPos = new MethodInsn(
            INVOKEVIRTUAL, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$setPos_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn BlockPos$PooledMutableBlockPos$retain = new MethodInsn(
            INVOKESTATIC, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$retain_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn Entity$getEntityBoundingBox = new MethodInsn(INVOKEVIRTUAL, Entity, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
    private static final MethodInsn EntityLivingBase$getLookVec = new MethodInsn(INVOKEVIRTUAL, EntityLivingBase, Entity$getLookVec_name, new MethodDesc(Vec3d));
    private static final MethodInsn EntityLivingBase$isOffsetPositionInLiquid = new MethodInsn(
            INVOKEVIRTUAL, EntityLivingBase, Entity$isOffsetPositionInLiquid_name, new MethodDesc(BOOLEAN, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn EntityPlayerMP$getEyeHeight = new MethodInsn(INVOKEVIRTUAL, EntityPlayerMP, Entity$getEyeHeight_name, new MethodDesc(FLOAT));
    private static final MethodInsn EntityPlayerMP$handleFalling = new MethodInsn(INVOKEVIRTUAL, EntityPlayerMP, EntityPlayerMP$handleFalling_name, new MethodDesc(VOID, DOUBLE, BOOLEAN));
    //    private static final MethodInstruction EntityPlayerMP$moveEntity = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerMP, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn EntityPlayerSP$getEntityBoundingBox = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
    private static final MethodInsn EntityPlayerSP$getForward = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, Entity$getForward_name, new MethodDesc(Vec3d));
    private static final MethodInsn EntityPlayer$getFoodStats = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, EntityPlayer$getFoodStats_name, new MethodDesc(FoodStats));
    private static final MethodInsn Item$onItemRightClick = new MethodInsn(INVOKEVIRTUAL, Item, Item$onRightClick_name, new MethodDesc(ActionResult, ItemStack, World, EntityPlayer, EnumHand));
    private static final MethodInsn Item$onItemUse = new MethodInsn(
            INVOKEVIRTUAL, Item, Item$onItemUse_name, new MethodDesc(EnumActionResult, ItemStack, EntityPlayer, World, BlockPos, EnumHand, EnumFacing, FLOAT, FLOAT, FLOAT));
    private static final MethodInsn Item$onPlayerStoppedUsing = new MethodInsn(INVOKEVIRTUAL, Item, Item$onPlayerStoppedUsing_name, new MethodDesc(VOID, ItemStack, World, EntityLivingBase, INT));
    private static final MethodInsn Vec3d$addVector = new MethodInsn(INVOKEVIRTUAL, Vec3d, Vec3d$addVector_name, new MethodDesc(Vec3d, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn Vec3d$scale = new MethodInsn(INVOKEVIRTUAL, Vec3d, Vec3d$scale_name, new MethodDesc(Vec3d, DOUBLE));
    private static final MethodInsn WorldClient$getEntitiesInAABBexcluding = new MethodInsn(
            INVOKEVIRTUAL, WorldClient, World$getEntitiesInAABBexcluding_name, new MethodDesc(List, Entity, AxisAlignedBB, Predicate));
    /*
        Up And Down And All Around asm hook method instructions
     */
    private static final HookInsn Hooks$addAdjustedVector = new HookInsn("addAdjustedVector", new MethodDesc(Vec3d, Vec3d, DOUBLE, DOUBLE, DOUBLE, Entity));
    private static final HookInsn Hooks$adjustVec = new HookInsn("adjustVec", new MethodDesc(Vec3d, Vec3d, Entity));
    //    private static final HooksMethodInstruction Hooks$adjustXYZ = new HooksMethodInstruction("adjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$constructNewGAABBFrom2Vec3d = new HookInsn("constructNewGAABBFrom2Vec3d", new MethodDesc(AxisAlignedBB, Vec3d, Vec3d, Entity));
    private static final HookInsn Hooks$getBlockPosAtTopOfPlayer = new HookInsn("getBlockPosAtTopOfPlayer", new MethodDesc(BlockPos, Entity));
    private static final HookInsn Hooks$getBlockPostBelowEntity = new HookInsn("getBlockPosBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, Entity));
    private static final HookInsn Hooks$getBottomOfEntity = new HookInsn("getBottomOfEntity", new MethodDesc(Vec3d, Entity));
    private static final HookInsn Hooks$getImmutableBlockPosBelowEntity = new HookInsn("getImmutableBlockPosBelowEntity", new MethodDesc(BlockPos, Entity));
    private static final HookInsn Hooks$getOriginRelativePosX = new HookInsn("getOriginRelativePosX", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getOriginRelativePosY = new HookInsn("getOriginRelativePosY", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getOriginRelativePosZ = new HookInsn("getOriginRelativePosZ", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getPrevRelativeYawHead = new HookInsn("getPrevRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
    private static final HookInsn Hooks$getRelativeBottomOfBB = new HookInsn("getRelativeBottomOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
    private static final HookInsn Hooks$getRelativeDownBlockPos = new HookInsn("getRelativeDownBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
    private static final HookInsn Hooks$getRelativeLookVec = new HookInsn("getRelativeLookVec", new MethodDesc(Vec3d, Entity));
    private static final HookInsn Hooks$getRelativePitch = new HookInsn("getRelativePitch", new MethodDesc(FLOAT, Entity));
    private static final HookInsn Hooks$getRelativePrevPitch = new HookInsn("getRelativePrevPitch", new MethodDesc(FLOAT, Entity));
    private static final HookInsn Hooks$getRelativePrevPosX = new HookInsn("getRelativePrevPosX", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getRelativePrevPosZ = new HookInsn("getRelativePrevPosZ", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getRelativePrevYaw = new HookInsn("getRelativePrevYaw", new MethodDesc(FLOAT, Entity));
    private static final HookInsn Hooks$getRelativePosX = new HookInsn("getRelativePosX", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getRelativePosY = new HookInsn("getRelativePosY", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getRelativePosZ = new HookInsn("getRelativePosZ", new MethodDesc(DOUBLE, Entity));
    private static final HookInsn Hooks$getRelativeTopOfBB = new HookInsn("getRelativeTopOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
    private static final HookInsn Hooks$getRelativeUpblockPos_INT_ARG = new HookInsn("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, INT, Entity));
    private static final HookInsn Hooks$getRelativeUpblockPos_NO_ARGS = new HookInsn("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
    private static final HookInsn Hooks$getRelativeYaw = new HookInsn("getRelativeYaw", new MethodDesc(FLOAT, Entity));
    private static final HookInsn Hooks$getRelativeYawHead = new HookInsn("getRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
    private static final HookInsn Hooks$getRelativeYOfBlockPos = new HookInsn("getRelativeYOfBlockPos", new MethodDesc(INT, BlockPos, Entity));
    private static final HookInsn Hooks$getVanillaEntityBoundingBox = new HookInsn("getVanillaEntityBoundingBox", new MethodDesc(AxisAlignedBB, Entity));
    private static final HookInsn Hooks$inverseAdjustXYZ = new HookInsn("inverseAdjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$isHeadspaceFree = new HookInsn("isHeadspaceFree", new MethodDesc(BOOLEAN, EntityPlayerSP, BlockPos, INT));
    private static final HookInsn Hooks$makePositionAbsolute = new HookInsn("makePositionAbsolute", new MethodDesc(VOID, EntityLivingBase));
    private static final HookInsn Hooks$makePositionRelative = new HookInsn("makePositionRelative", new MethodDesc(VOID, EntityLivingBase));
    //    private static final HooksMethodInstruction Hooks$moveEntityAbsolute = new HooksMethodInstruction("moveEntityAbsolute", new MethodDesc(VOID, EntityPlayer, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$netHandlerPlayServerGetPacketZ = new HookInsn("netHandlerPlayServerGetPacketZ", new MethodDesc(DOUBLE, NetHandlerPlayServer, CPacketPlayer));
    private static final HookInsn Hooks$netHandlerPlayServerGetRelativeY = new HookInsn("netHandlerPlayServerGetRelativeY", new MethodDesc(DOUBLE, NetHandlerPlayServer, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$netHandlerPlayServerHandleFallingYChange =
            new HookInsn("netHandlerPlayServerHandleFallingYChange", new MethodDesc(DOUBLE, EntityPlayerMP, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$netHandlerPlayServerSetRelativeYToZero =
            new HookInsn("netHandlerPlayServerSetRelativeYToZero", new MethodDesc(DOUBLE.asArray(), NetHandlerPlayServer, DOUBLE, DOUBLE, DOUBLE));
    private static final HookInsn Hooks$onItemRightClickPost = new HookInsn("onItemRightClickPost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityPlayer));
    private static final HookInsn Hooks$onItemRightClickPre = new HookInsn("onItemRightClickPre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityPlayer));
    private static final HookInsn Hooks$onItemUsePost = new HookInsn("onItemUsePost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityPlayer));
    private static final HookInsn Hooks$onItemUsePre = new HookInsn("onItemUsePre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityPlayer));
    private static final HookInsn Hooks$onPlayerStoppedUsingPost = new HookInsn("onPlayerStoppedUsingPost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityLivingBase));
    private static final HookInsn Hooks$onPlayerStoppedUsingPre = new HookInsn("onPlayerStoppedUsingPre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityLivingBase));
    private static final HookInsn Hooks$pushEntityPlayerSPOutOfBlocks = new HookInsn("pushEntityPlayerSPOutOfBlocks", new MethodDesc(VOID, EntityPlayerWithGravity, AxisAlignedBB));
    private static final HookInsn Hooks$reverseXOffset = new HookInsn("reverseXOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    private static final HookInsn Hooks$reverseYOffset = new HookInsn("reverseYOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    private static final HookInsn Hooks$reverseZOffset = new HookInsn("reverseZOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    private static final HookInsn Hooks$runNameplateCorrection = new HookInsn("runNameplateCorrection", new MethodDesc(VOID, BOOLEAN));
    private static final HookInsn Hooks$setListenerOrientationHook = new HookInsn("setListenerOrientationHook", new MethodDesc(VOID, SoundSystem, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, EntityPlayer));
    private static final HookInsn Hooks$setPooledMutableBlockPosToBelowEntity =
            new HookInsn("setPooledMutableBlockPosToBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos, Entity));
    /*
        Debug only (currently only used in patchEntityPlayerSP when DEBUG_AUTO_JUMP is true)
     */
    private static final MethodInsn AbstractClientPlayer$moveEntity_SPECIAL = new MethodInsn(Opcodes.INVOKESPECIAL, AbstractClientPlayer, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
    private static final MethodInsn AbstractClientPlayer$updateAutoJump_SPECIAL = new MethodInsn(Opcodes.INVOKESPECIAL, AbstractClientPlayer, EntityPlayerSP$updateAutoJump_name, new MethodDesc(VOID, FLOAT, FLOAT));
    private static final boolean DEBUG_AUTO_JUMP = false;
    //Set up bit mask
    private static final int GET_ROTATIONYAW = 0b1;
    private static final int GET_ROTATIONPITCH = 0b10;
    private static final int GET_PREVROTATIONYAW = 0b100;
    private static final int GET_PREVROTATIONPITCH = 0b1000;
    private static final int GET_ROTATIONYAWHEAD = 0b10000;
    private static final int GET_PREVROTATIONYAWHEAD = 0b100000;
    private static final int ALL_GET_ROTATION_VARS = GET_ROTATIONYAW | GET_ROTATIONPITCH | GET_PREVROTATIONYAW
            | GET_PREVROTATIONPITCH | GET_ROTATIONYAWHEAD | GET_PREVROTATIONYAWHEAD;

    static {
        // Superclass replaced with EntityPlayerWithGravity
        classNameToMethodMap.put("net.minecraft.client.entity.AbstractClientPlayer", Transformer::patchEntityPlayerSubClass);
        // Superclass replaced with EntityPlayerWithGravity
        classNameToMethodMap.put("net.minecraft.entity.player.EntityPlayerMP", Transformer::patchEntityPlayerSubClass);
        // Patches setListener
        classNameToMethodMap.put("net.minecraft.client.audio.SoundManager", Transformer::patchSoundManager);
        // Patches onUpdateWalkingPlayer, onLivingUpdate, isHeadspaceFree, updateAutoJump, moveEntity
        classNameToMethodMap.put("net.minecraft.client.entity.EntityPlayerSP", Transformer::patchEntityPlayerSP);
        // Patches handlePlayerPosLook
        classNameToMethodMap.put("net.minecraft.client.network.NetHandlerPlayClient", Transformer::patchNetHandlerPlayClient);
        // Patches getMouseOver, drawNameplate
        classNameToMethodMap.put("net.minecraft.client.renderer.EntityRenderer", Transformer::patchEntityRenderer);
        // Patches doRender
        classNameToMethodMap.put("net.minecraft.client.renderer.entity.RenderLivingBase", Transformer::patchRenderLivingBase);
        // Patches moveEntity, moveRelative
        classNameToMethodMap.put("net.minecraft.entity.Entity", Transformer::patchEntity);
        // Patches moveEntityWithHeading, updateDistance, onUpdate, jump
        classNameToMethodMap.put("net.minecraft.entity.EntityLivingBase", Transformer::patchEntityLivingBase);
        // Patches onItemUse, useItemRightClick, onPlayerStoppedUsing
        //      (all three are used to add compatibility with other mods, they are not otherwise used by this mod)
        classNameToMethodMap.put("net.minecraft.item.ItemStack", Transformer::patchItemStack);
        // Patches processPlayer, processPlayerDigging
        classNameToMethodMap.put("net.minecraft.network.NetHandlerPlayServer", Transformer::patchNetHandlerPlayServer);
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
    private static void logOrDie(boolean shouldLog, String dieMessage, String formattableLogMsg, Object... objectsForFormatter) {
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
    private static void log(String string, Object... objects) {
        FMLLog.info("[UpAndDown] " + string, objects);
    }

    /**
     * Default 'crash Minecraft' method.
     *
     * @param string Reason why we are forcing Minecraft to crash
     */
    private static void die(String string) throws RuntimeException {
        throw new RuntimeException("[UpAndDown] " + string);
    }

    /**
     * Debug helper method to print a class, from bytes, to "standard" output.
     * Useful to compare a class's bytecode, before and after patching if the bytecode is not what we expect, or if the
     * patched bytecode is invalid.
     *
     * @param bytes The bytes that make up the class
     */
    private static void printClassToStdOut(byte[] bytes) {
        printClassToStream(bytes, System.out);
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

    private static void printClassToFMLLogger(byte[] bytes) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printClassToStream(bytes, byteArrayOutputStream);
        FMLLog.info("\n%s", byteArrayOutputStream);
    }

    private static void printMethodToStdOut(MethodNode methodNode) {
        printMethodToStream(methodNode, System.out);
    }

    private static void printMethodToStream(MethodNode methodNode, OutputStream outputStream) {
        PrintWriter writer = new PrintWriter(outputStream);
        Textifier textifier = new Textifier();
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(textifier);
        methodNode.accept(traceMethodVisitor);
        textifier.print(writer);
        writer.flush();
    }

    private static void printMethodToFMLLogger(MethodNode methodNode) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        printMethodToStream(methodNode, byteArrayOutputStream);
        FMLLog.info("\n%s", byteArrayOutputStream);
    }

    /**
     * Inserts UpAndDown's EntityPlayerWithGravity class into EntityPlayerMP's and AbstractClientPlayer's hierarchy.
     * This is done to significantly lower the amount of ASM required and make it easier to code and debug the mod.
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
                        TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                        if (typeInsnNode.desc.equals(classToReplace)) {
                            typeInsnNode.desc = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.FIELD_INSN):
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (fieldInsnNode.owner.equals(classToReplace)) {
                            fieldInsnNode.owner = classReplacement;
                        }
                        break;
                    case (AbstractInsnNode.METHOD_INSN):
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
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

    // Upon using items, we generally need to ensure the player has absolute motion fields, while there are events that
    // are fired before items are used, there aren't any events fired immediately after the item is used. I need a way
    // to put the player's rotation fields back to normal after the item has finished processing, so I've added what is
    // effectively some events around the item use methods in the ItemStack class.
    private static byte[] patchItemStack(byte[] bytes) {
        int methodPatches = 0;
        final int expectedMethodPatches = 3;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            if (Transformer.ItemStack$onItemUse_name.is(methodNode)) {
                logPatchStarting(Transformer.ItemStack$onItemUse_name);

                int insertedLocalIndex = addLocalVar(methodNode, ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Item$onItemUse.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                        Hooks$onItemUsePre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onItemUse
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // this, EntityPlayer
                        Hooks$onItemUsePost.addTo(iterator);
                        methodPatches++;
                        logPatchComplete(Transformer.ItemStack$onItemUse_name);
                        break;
                    }
                }
            }
            else if (Transformer.ItemStack$useItemRightClick_name.is(methodNode)) {
                logPatchStarting(Transformer.ItemStack$useItemRightClick_name);

                int insertedLocalIndex = addLocalVar(methodNode, ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Item$onItemRightClick.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                        Hooks$onItemRightClickPre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onItemRightClick
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityPlayer
                        Hooks$onItemRightClickPost.addTo(iterator);
                        methodPatches++;
                        logPatchComplete(Transformer.ItemStack$useItemRightClick_name);
                        break;
                    }
                }
            }
            else if (Transformer.ItemStack$onPlayerStoppedUsing_name.is(methodNode)) {
                logPatchStarting(Transformer.ItemStack$onPlayerStoppedUsing_name);

                int insertedLocalIndex = addLocalVar(methodNode, ItemStackAndBoolean);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Item$onPlayerStoppedUsing.is(next)) {
                        iterator.previous();
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                        Hooks$onPlayerStoppedUsingPre.addTo(iterator);
                        iterator.add(new VarInsnNode(Opcodes.ASTORE, insertedLocalIndex));
                        iterator.next(); // Item$onPlayerStoppedUsing
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, insertedLocalIndex)); // origItemStackCopy
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 2)); // this, EntityLivingBase
                        Hooks$onPlayerStoppedUsingPost.addTo(iterator);
                        methodPatches++;
                        logPatchComplete(Transformer.ItemStack$onPlayerStoppedUsing_name);
                        break;
                    }
                }
            }
            if (methodPatches == 3) {
                break;
            }
        }

        dieIfFalse(methodPatches == expectedMethodPatches, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static void logPatchStarting(DeobfAwareString deobfAwareString) {
        logPatchStarting(deobfAwareString.getDeobf());
    }

    /**
     * Use to add a local variable to a method.
     *
     * @param methodNode      Method to add the localvariable to
     * @param objectClassName The variable's class
     * @return The index of the new local variable
     */
    private static int addLocalVar(MethodNode methodNode, ObjectName objectClassName) {
        LocalVariablesSorter localVariablesSorter = new LocalVariablesSorter(methodNode.access, methodNode.desc, methodNode);
        return localVariablesSorter.newLocal(Type.getObjectType(objectClassName.toString()));
    }

    private static void logPatchComplete(DeobfAwareString deobfAwareString) {
        logPatchComplete(deobfAwareString.getDeobf());
    }

    private static void dieIfFalse(boolean shouldContinue, ClassNode classNode) {
        dieIfFalse(shouldContinue, "Failed to find the methods to patch in " + classNode.name +
                ". The Minecraft version you are using likely does not match what the mod requires.");
    }

    private static void logPatchStarting(Object object) {
        log("Patching %s", object);
    }

    private static void logPatchComplete(Object object) {
        log("Patched  %s", object);
    }

    /**
     * Option to used to compound multiple tests together
     *
     * @param shouldContinue boolean to check, if false, Minecraft is forced to crash with the passed dieMessage
     * @param dieMessage     The message to use as the cause when making Minecraft crash
     * @return
     */
    private static void dieIfFalse(boolean shouldContinue, String dieMessage) {
        if (!shouldContinue) {
            die(dieMessage);
        }
    }

    private static byte[] patchRenderLivingBase(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // If a player looks 'up' from their perspective, we want their head to look 'up'.
            // If a player has non standard gravity, then their pitch (and yaw) won't match their own perspective
            //      (player rotation is always relative to normal minecraft gravity so that bows and similar work properly)
            // This replaces all accesses to the player's rotation fields with static hook calls that calculate and
            //      return the field in question, as seen from the player's perspective.
            if (Transformer.RenderLivingBase$doRender_name.is(methodNode)) {
                logPatchStarting(Transformer.RenderLivingBase$doRender_name);
                patchMethodUsingAbsoluteRotations(methodNode, ALL_GET_ROTATION_VARS);
                logPatchComplete(Transformer.RenderLivingBase$doRender_name);
                methodPatches++;
                break;
            }
        }

        dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

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
                if (Transformer.Entity$rotationYaw_name.is(node.name)) {
                    Transformer.Hooks$getRelativeYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (Transformer.Entity$rotationPitch_name.is(node.name)) {
                    Transformer.Hooks$getRelativePitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYaw) {
            biPredicates[index] = (iterator, node) -> {
                if (Transformer.Entity$prevRotationYaw_name.is(node.name)) {
                    Transformer.Hooks$getRelativePrevYaw.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationPitch) {
            biPredicates[index] = (iterator, node) -> {
                if (Transformer.Entity$prevRotationPitch_name.is(node.name)) {
                    Transformer.Hooks$getRelativePrevPitch.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changeRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (Transformer.EntityLivingBase$rotationYawHead_name.is(node.name)) {
                    Transformer.Hooks$getRelativeYawHead.replace(iterator);
                    return true;
                }
                return false;
            };
            index++;
        }
        if (changePrevRotationYawHead) {
            biPredicates[index] = (iterator, node) -> {
                if (Transformer.EntityLivingBase$prevRotationYawHead_name.is(node.name)) {
                    Transformer.Hooks$getPrevRelativeYawHead.replace(iterator);
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
        FMLLog.warning("[UpAndDown] " + string, objects);
    }

    private static byte[] patchEntityRenderer(byte[] bytes) {
        int methodPatches = 0;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // Minecraft checks if the player is looking at an entity by expanding their bounding box and then effectively
            // raytracing those found entities (I think it just does some angle and distance calculations).
            //
            // The issue is that the player's bounding box is a GravityAxisAlignedBB.
            //
            // The vanilla method gets the player's look vector and then adds it's x, y and z coordinates (multiplied by a scalar)
            // to the player's bounding box. The player's look is absolute, but the player's bounding box moves in a relative fashion.
            //
            // Either we have to get the relative look of the player or we have to get a bounding box that doesn't move in a relative fashion.
            //
            // I have chosen to insert a hook that calls the instance method GravityAxisAlignedBB::toVanilla if the bounding box in question
            // is a GravityAxisAlignedBB. The hook returns a vanilla AxisAlignedBB with the same shape/position as the existing GravityAxisAlignedBB.
            // This vanilla AxisAlignedBB will then move in an absolute fashion
            if (Transformer.EntityRenderer$getMouseOver_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityRenderer$getMouseOver_name);
                outerfor:
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    // This is a pretty unique method call, so we'll use it as the basis for finding the bytecode to patch
                    if (Transformer.WorldClient$getEntitiesInAABBexcluding.is(next)) {
                        // Not strictly necessary, but the first previous() call will return the getEntitiesInAABBexcluding method we just found
                        iterator.previous();
                        while (iterator.hasPrevious()) {
                            next = iterator.previous();
                            if (Transformer.Entity$getEntityBoundingBox.is(next)) {
                                Transformer.Hooks$getVanillaEntityBoundingBox.replace(iterator);
                                logPatchComplete(Transformer.EntityRenderer$getMouseOver_name);
                                methodPatches++;
                                break outerfor;
                            }
                        }
                        die("Failed to find " + Transformer.WorldClient$getEntitiesInAABBexcluding + " in " + classNode.name + "::" + Transformer.EntityRenderer$getMouseOver_name);
                    }
                }
            }
            else if (Transformer.EntityRenderer$drawNameplate_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityRenderer$drawNameplate_name);
                boolean patchComplete = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.GlStateManager$disableLighting_name.is(methodInsnNode.name)) {
                            iterator.previous();
                            // boolean isThirdPersonFrontal argument, '8' should be consistent unless the method's description changes
                            iterator.add(new VarInsnNode(Opcodes.ILOAD, 8));
                            Transformer.Hooks$runNameplateCorrection.addTo(iterator);
                            logPatchComplete(Transformer.EntityRenderer$drawNameplate_name);
                            patchComplete = true;
                            methodPatches++;
                            break;
                        }
                    }
                }
                dieIfFalse(patchComplete, "Failed to find " + Transformer.GlStateManager$disableLighting_name + " in " + classNode.name + "::" + Transformer.EntityRenderer$drawNameplate_name);
            }
        }

        dieIfFalse(methodPatches == 2, classNode);

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
            // moveEntity moves the player by the arguments supplied to it or until the player collides with a block
            // in the world.
            // It also:
            // performs the player's automatic step-up (half slabs etc.),
            // keeps the player from falling off of blocks while sneaking,
            // determines if the player has collided with blocks and/or is on the ground,
            // updates the fall state (increment damage on landing if still in the air, or cause fall damage if hitting the ground),
            // increments the player's walked distance and plays step sounds if the distance is great enough,
            // and decrements the number of ticks the player should be on fire for
            //
            // We make almost everything work with non-vanilla gravity by allowing the player's motion fields to be relative
            // and then having the player's bounding box move in a relative manner through use of the GravityAxisAlignedBB class.
            //
            // There are a few cases where instead of calling an instance method on the player's bounding box (which will
            // be a GravityAxisAlignedBB), it calls the instance method on a bounding box found in the world. These are the
            // calculateX/Y/ZOffset methods, we replace their calls with static hooks that effectively run the same code, but
            // on the player's bounding box instead of those found in the world.
            if (Transformer.Entity$moveEntity_name.is(methodNode)) {
                logPatchStarting(Transformer.Entity$moveEntity_name);
                int numReplaced = 0;
                boolean newBlockPosFound = false;
                boolean blockPos$downFound = false;
                boolean Blocks$LADDERFound = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.AxisAlignedBB$calculateXOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseXOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (Transformer.AxisAlignedBB$calculateYOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseYOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        else if (Transformer.AxisAlignedBB$calculateZOffset.is(methodInsnNode)) {
                            Transformer.Hooks$reverseZOffset.replace(methodInsnNode);
                            numReplaced++;
                        }
                        // To work out what block the player has landed/walked on, the vanilla method creates a BlockPos
                        // about 0.2 of a block below the player's position. We need to adjust this code so that the new
                        // BlockPos is _relatively_ about 0.2 below the player's position.
                        //
                        // In bytecode, new instances are created by first running a NEW instruction, then loading the
                        // arguments for the constructor onto the stack and then calling the constructor. We need to remove
                        // the vanilla bytecode from the NEW instruction to the INVOKESPECIAL instruction (the constructor call).
                        //
                        // I've also chosen to remove the instructions that prepare the local variables that are passed as
                        // arguments to the constructor in question, this isn't necessarry and can be removed if needed.
                        else if (!newBlockPosFound && Transformer.BlockPos$INIT.is(methodInsnNode)) {
                            // Starts with a simple 1:1 replacement
                            Transformer.Hooks$getImmutableBlockPosBelowEntity.replace(methodInsnNode);
                            iterator.previous(); // Our INNVOKESTATIC instruction

                            // Repeatedly remove the previous instruction until it's GETFIELD Entity.posX
                            // Remove that too
                            // If we were to call iterator.previous() again, we would get the ALOAD 0 that would normally
                            // be consumed by the GETFIELD instruction we just removed
                            // And it just so happens that our INVOKESTATIC instruction needs to have ALOAD 0 before it
                            // So we cut out a bunch of no longer used instructions and inserted our own instruction
                            // (hopefully it's fine to have local variables that are never used in bytecode)
                            for (; iterator.hasPrevious() && !newBlockPosFound; ) {
                                AbstractInsnNode previous = iterator.previous();
                                if (Transformer.Entity$posX_GET.is(previous)) {
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

                            if (!newBlockPosFound) {
                                die("Failed to find " + Transformer.Entity$posX_GET + " prior to " + Transformer.BlockPos$INIT);
                            }
                        }
                        // TODO: Change/remove this
                        // If the block ~0.2 below (relative) the player's feet is air, then vanilla checks if the block
                        // below that is a fence/wall/fence gate
                        else if (newBlockPosFound && !blockPos$downFound && Transformer.BlockPos$down_NO_ARGS.is(methodInsnNode)) {
                            Transformer.Hooks$getRelativeDownBlockPos.replace(methodInsnNode);
                            iterator.previous();
                            VarInsnNode aload0 = new VarInsnNode(Opcodes.ALOAD, 0);
                            iterator.add(aload0);
                            blockPos$downFound = true;
                        }
                    }
                    // When the player moves, minecraft tracks how far they've moved and uses it to work out the distance
                    // they've walked, which is used to play step sounds and water 'sloshing' sounds and it might be used
                    // to do walking animations too.
                    //
                    // We fix this by loading the local variables that store the change in X/Y/Z, adjusting them and then
                    // storing the adjusted values back into the local variables
                    else if (next instanceof FieldInsnNode
                            && newBlockPosFound && blockPos$downFound
                            /*&& !Blocks$LADDERFound*/) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (!Blocks$LADDERFound && Transformer.Blocks$LADDER_GET.is(fieldInsnNode)) {
                            iterator.previous(); // returns the same GETSTATIC instruction
                            iterator.previous(); // should be ALOAD 29

                            int countToUndo = 0;
                            final int posZVar; //34/77
                            while (true) {
                                countToUndo++;
                                AbstractInsnNode previous = iterator.previous();
                                if (previous.getOpcode() == Opcodes.DSTORE) {
                                    posZVar = ((VarInsnNode)previous).var;
                                    break;
                                }
                            }
                            for (; countToUndo > 0; countToUndo--) {
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
                            Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
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
                logPatchComplete(Transformer.Entity$moveEntity_name);
                methodPatches++;
            }
            // Movement of the player is based off of their yaw and their forwards and strafing input
            // We need to get the relative yaw when calculating the resultant movement due to strafe/forwards input
            else if (Transformer.Entity$moveRelative_name.is(methodNode)) {
                logPatchStarting(Transformer.Entity$moveRelative_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(Transformer.Entity$moveRelative_name);
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
            //FIXME: Elytra flying doesn't work! Why not?!
            if (Transformer.EntityLivingBase$moveEntityWithHeading_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityLivingBase$moveEntityWithHeading_name);

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
                        // Replaces getLookVec in the Elytra movement with a relative version
                        if (!replaced_getLookVec && Transformer.EntityLivingBase$getLookVec.is(methodInsnNode)) {
                            Transformer.Hooks$getRelativeLookVec.replace(methodInsnNode);
                            replaced_getLookVec = true;
                        }
                        // In normal movement, a PooledMutableBlockPos is create 1.0 below the player's position. We
                        // need to change it to be relative below.
                        //
                        // The original code that is called is removed
                        else if (replaced_rotationPitch
                                && !BlockPos$PooledMutableBlockPos$retainFound
                                && Transformer.BlockPos$PooledMutableBlockPos$retain.is(methodInsnNode)) {
                            Transformer.Hooks$getBlockPostBelowEntity.replace(methodInsnNode);

                            iterator.previous(); //Our replacment INVOKESTATIC

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
                                BlockPos$PooledMutableBlockPos$retainFound = true;
                            }
                            else {
                                die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Transformer.Hooks$getBlockPostBelowEntity + "\" in " + Transformer.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        // In normal movement, if the player is on the ground, the mutable BlockPos gets set back to 1.0
                        // below the player, again, we set it to be relatively below the player
                        else if (BlockPos$PooledMutableBlockPos$retainFound
                                && !BlockPos$PooledMutableBlockPos$setPosFound
                                && Transformer.BlockPos$PooledMutableBlockPos$setPos.is(methodInsnNode)) {
                            Transformer.Hooks$setPooledMutableBlockPosToBelowEntity.replace(methodInsnNode);

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
                                die("Failed to find 3 uses of ALOAD 0 before inserted \"" + Transformer.Hooks$setPooledMutableBlockPosToBelowEntity + "\" in " + Transformer.EntityLivingBase$moveEntityWithHeading_name);
                            }
                        }
                        // For these liquid calculations, the passed arguments are (motionX, motionY + change in posY, motionZ)
                        // The motion fields are all relative, but the change in posY is absolute, so we need to get the change
                        // in whatever direction positive Y is from the player's perspective. Both the initial posY and the
                        // later posY are modified
                        else if (BlockPos$PooledMutableBlockPos$setPosFound
                                && numEntityLivingBase$isOffsetPositionInLiquidFound < 2
                                && Transformer.EntityLivingBase$isOffsetPositionInLiquid.is(methodInsnNode)) {
                            if (methodInsnNode == previousisOffsetPositionInLiquidMethodInsnNode) {
                                continue;
                            }
                            previousisOffsetPositionInLiquidMethodInsnNode = methodInsnNode;
                            int numReplacementsMade = 0;
                            while (iterator.hasPrevious() && numReplacementsMade < 2) {
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)previous;
                                    if (Transformer.EntityLivingBase$posY_GET.is(fieldInsnNode)) {
                                        // Replace the GETFIELD with an INVOKESTATIC of the Hook
                                        Transformer.Hooks$getRelativePosY.replace(iterator);
                                        numReplacementsMade++;
                                    }
                                }
                            }
                            if (numReplacementsMade == 2) {
                                numEntityLivingBase$isOffsetPositionInLiquidFound++;
                            }
                        }
                    }
                    else if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        // Replaces access of the player's pitch with a get-relative-pitch hook, in the Elytra movement
                        if (replaced_getLookVec
                                && !replaced_rotationPitch
                                && Transformer.EntityLivingBase$rotationPitch_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePitch.replace(iterator);
                            replaced_rotationPitch = true;
                        }
                        // Limb swing (arms+legs swinging as you walk around) is based on change in X and Z position
                        // instead of messing with the existing code, we can insert a hook that changes the player's
                        // position fields to act as if they're relative, so that when the vanilla code calculates
                        // the change in X and Z position, it's instead the relative X and Z movement the player made
                        else if (numEntityLivingBase$isOffsetPositionInLiquidFound == 2
                                && !GETFIELD_limbSwingAmount_found
                                && Transformer.EntityLivingBase$limbSwingAmount_GET.is(fieldInsnNode)) {
                            iterator.previous(); // The GETFIELD instruction
                            iterator.previous(); // ALOAD 0
                            Transformer.Hooks$makePositionRelative.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            GETFIELD_limbSwingAmount_found = true;
                        }
                        // Once the limb swing has been set properly, we need to put the player's position fields back to
                        // normal
                        else if (GETFIELD_limbSwingAmount_found
                                && !PUTFIELD_limbSwing_found
                                && Transformer.EntityLivingBase$limbSwing_PUT.is(fieldInsnNode)) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Transformer.Hooks$makePositionAbsolute.addTo(iterator);
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

                logPatchComplete(Transformer.EntityLivingBase$moveEntityWithHeading_name);
                methodPatches++;
            }
            // This method is pretty badly named in my opinion, it seems to set up the turn of the player's head or body
            // (I'm not sure), however it's rendering based and the rotation needs to be made relative. Only the
            // rotationYaw field is accessed, so that is all that is changed
            else if (Transformer.EntityLivingBase$updateDistance_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityLivingBase$updateDistance_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(Transformer.EntityLivingBase$updateDistance_name);
                methodPatches++;
            }
            // For some of the vanilla code that also seems to be to do with head turning, the vanilla code uses the
            // change in X and Z pos from last tick to the current tick, as well as the player's rotationYaw.
            //
            // We replace these field accesses with hooks that get the relative equivalents to them
            else if (Transformer.Entity$onUpdate_name.is(methodNode)) {
                logPatchStarting(Transformer.Entity$onUpdate_name);
                boolean patchComplete = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.EntityLivingBase$posX_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePosX.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$prevPosX_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePrevPosX.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$posZ_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePosZ.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$prevPosZ_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativePrevPosZ.replace(iterator);
                        }
                        else if (Transformer.EntityLivingBase$rotationYaw_GET.is(fieldInsnNode)) {
                            Transformer.Hooks$getRelativeYaw.replace(iterator);
                            patchComplete = true;
                            break;
                        }
                    }
                }
                dieIfFalse(patchComplete, "Failed to patch " + Transformer.Entity$onUpdate_name);
                logPatchComplete(Transformer.Entity$onUpdate_name);
                methodPatches++;
            }
            // When sprinting, the player's rotationYaw is used to determine the boost in forwards movement to add
            // This needs to be made relative to match the direction the player is actually sprinting in
            else if (Transformer.EntityLivingBase$jump_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityLivingBase$jump_name);
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW);
                logPatchComplete(Transformer.EntityLivingBase$jump_name);
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
            // Mojang seems to assume that the bottom of a player's bounding box is equivalent to a player's posY a lot
            // of the time, which is pretty wrong in my opinion (and breaks things when players have non-standard bounding
            // boxes). So I replaced the access of minY of the player's bounding box with
            // their Y position
            if (Transformer.INetHandlerPlayClient$handlePlayerPosLook_name.is(methodNode)) {
                logPatchStarting(Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                boolean foundAtLeastOneMinY = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                            Transformer.EntityPlayer$posY_GET.replace(iterator);
                            iterator.previous(); // the instruction we just replaced the old one with
                            iterator.previous(); // getEntityBoundingBox()
                            iterator.remove();
                            foundAtLeastOneMinY = true;
                        }
                    }
                }
                dieIfFalse(foundAtLeastOneMinY, "Could not find \"playerEntity.geEntityBoundingBox().minY\" in " + Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                logPatchComplete(Transformer.INetHandlerPlayClient$handlePlayerPosLook_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 1, classNode);

        ClassWriter classWriter = new ClassWriter(0);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    //TODO: processPlayer patch is still using hardcoded localvariable indices
    private static byte[] patchNetHandlerPlayServer(byte[] bytes) {
        int methodPatches = 0;

        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        for (MethodNode methodNode : classNode.methods) {
            // When syncing player position between the client and server, the server moves the player by the difference
            // between their server-side position and position received in a packet sent by the player. But player movement
            // is done in a relative manner, so the arguments supplied to moveEntity must be adjusted for the player's
            // current gravity.
            //
            // In a similar manner, the player has handleFalling called on them, but the vanilla code passes the change
            // in Y position as one of the arguments, this has to be changed to a change in the relative upwards
            // direction of the player.
            if (Transformer.INetHandlerPlayServer$processPlayer_name.is(methodNode)) {
                logPatchStarting(Transformer.INetHandlerPlayServer$processPlayer_name);

                boolean foundHandleFalling = false;

                boolean foundDifferenceLocalVars = false;
                int xDiffLocalVar = -1;
                int yDiffLocalVar = -1;
                int zDiffLocalVar = -1;
                int yDiffLoadCount = 0;
                boolean patchedXYZDiffLocalVars = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        // processPlayer calls handleFalling(...) with the player's change in Y position
                        // We insert a hook that calculates the player's relative Y change and deletes the instructions
                        // that calculate the change in Y position (this.playerEntity.posY - d3)
                        if (!foundHandleFalling && Transformer.EntityPlayerMP$handleFalling.is(methodInsnNode)) {
                            iterator.previous(); // INVOKEVIRTUAL handleFalling again
                            iterator.previous(); // INVOKEVIRTUAL isOnGround
                            iterator.previous(); // ALOAD 1 (CPacketPlayer method argument)
                            next = iterator.previous(); // Should be DSUB
                            if (next.getOpcode() == Opcodes.DSUB) {
                                iterator.remove();
                                VarInsnNode dload7 = new VarInsnNode(Opcodes.DLOAD, 7);
                                iterator.add(dload7);
                                Transformer.Hooks$netHandlerPlayServerHandleFallingYChange.addTo(iterator);
                                iterator.previous(); // Our newly added INVOKESTATIC
                                iterator.previous(); // Our newly added DLOAD 7
                                iterator.previous(); // DLOAD 9
                                next = iterator.previous(); // GETFIELD posY
                                if (next instanceof FieldInsnNode) {
                                    FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                    if (Transformer.EntityPlayerMP$posY_GET.is(fieldInsnNode)) {
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
                        // We use the lastGoodX field access as a basis for finding the local variable indices we need
                        if (!foundDifferenceLocalVars && Transformer.NetHandlerPlayServer$lastGoodX_GET.is(fieldInsnNode)) {
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
                        // Some parts of processPlayer need the change in X/Y/Z local variables to be absolute, while
                        // some need them relative, so we change the instructions where they get loaded as necessary.
                        if (foundDifferenceLocalVars
                                && !patchedXYZDiffLocalVars
                                && varInsnNode.var == yDiffLocalVar
                                && varInsnNode.getOpcode() == Opcodes.DLOAD) {
                            yDiffLoadCount++;
                            switch (yDiffLoadCount) {
                                case 4://d8 > -0.5D

                                    // run "d9 = d6 - this.playerEntity.posZ;" early so that d9 is given the correct value
                                    // for when it is passed to Hooks$netHandlerPlayServerGetRelativeY which will be
                                    // called instead of loading [yDiffLocalVar]/[d8]/[21]
                                    iterator.previous(); // the DLOAD [yDiffLocalVar]/[21] instruction

                                    // Get z from the packet, (instead of tracking the localvariable's index)
                                    // In which case, you would add this:
                                    //iterator.add(new VarInsnNode(Opcodes.DLOAD, [zPosFromPacket]));
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); //this, packetIn
                                    Hooks$netHandlerPlayServerGetPacketZ.addTo(iterator); //packetIn.getZ(this.playerEntity.posZ), same as local variable d6
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));//'d6', this
                                    NetHandlerPlayServer$playerEntity_GET.addTo(iterator);//'d6', this.playerEntity
                                    EntityPlayerMP$posZ_GET.addTo(iterator);//'d6', this.playerEntity.posZ
                                    iterator.add(new InsnNode(Opcodes.DSUB));//'d6' - this.playerEntity.posZ
                                    iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar));//[empty]
                                    iterator.next(); // back to the DLOAD [yDiffLocalVar]/[21] instruction

                                case 1://if (this.playerEntity.onGround && !packetIn.isOnGround() && d8 > 0.0D)
                                case 3://double d12 = d8;
                                case 5://d8 < 0.5D
                                    //get relative y
                                    iterator.remove();
                                    iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
                                    iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
                                    Hooks$netHandlerPlayServerGetRelativeY.addTo(iterator); //relativeY
                                    break;
                                //case 2://this.playerEntity.moveEntity(d7, d8, d9);
                                // no change, as we're going to call moveEntityAbsolute hook, or the method will take
                                // care of the absolute arguments on its own when using the MotionStack (NYI)
                                // Either way, we'll delegate this to the default case
                                default:
                                    break;
                            }

                            // After patching the 5th load, (assuming we jump inside the if statement), vanilla code does d8 = 0.0D;
                            // We need to set the local variable which coincides with the player's relative y axis to
                            // zero, instead of always the absolute y axis
                            if (yDiffLoadCount == 5) {
                                while (true) {
                                    if (iterator.next().getOpcode() == Opcodes.DCONST_0) {
                                        iterator.remove(); // remove the first instruction
                                        iterator.next(); // DSTORE [yDiffLocalVar]
                                        iterator.remove(); // remove the DSTORE instruction

                                        // Load this and the localvariables
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, xDiffLocalVar)); //this, xDiff
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, yDiffLocalVar)); //this, xDiff, yDiff
                                        iterator.add(new VarInsnNode(Opcodes.DLOAD, zDiffLocalVar)); //this, xDiff, yDiff, zDiff
                                        // Call the hook that returns an array of size == 3
                                        Hooks$netHandlerPlayServerSetRelativeYToZero.addTo(iterator); //double[]

                                        // So we don't need to create any extra local variables, we duplicate the array reference
                                        // on the top of the stack
                                        iterator.add(new InsnNode(Opcodes.DUP)); //double[], double[]
                                        iterator.add(new InsnNode(Opcodes.DUP)); //double[], double[], double[]

                                        // double[0] -> xDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_0)); //double[], double[], double[], 0
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[], double[], double[0]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, xDiffLocalVar)); //double[], double[]

                                        // double[1] -> yDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_1)); //double[], double[], 1
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[], double[1]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, yDiffLocalVar)); //double[]

                                        // double[2] -> zDiffLocalVar
                                        iterator.add(new InsnNode(Opcodes.ICONST_2)); //double[], 2
                                        iterator.add(new InsnNode(Opcodes.DALOAD)); //double[2]
                                        iterator.add(new VarInsnNode(Opcodes.DSTORE, zDiffLocalVar)); //[empty]

                                        patchedXYZDiffLocalVars = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                dieIfFalse(patchedXYZDiffLocalVars, "Could not find 5 DLOADs of local variable " + yDiffLocalVar);
                dieIfFalse(foundHandleFalling, "Could not find \"this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());\"");

                logPatchComplete(Transformer.INetHandlerPlayServer$processPlayer_name);
                methodPatches++;
            }
            // When the server checks if a player can actually reach a block they're trying to break, instead of getting
            // the player's eye height and adding that to their position, for some reason, Mojang have it hardcoded to
            // xPos, yPos + 1.5, zPos
            //
            // This patch removes the "+ 1.5" and replaces it with "this.playerEntity.getEyeHeight()".
            // The patch is very much needed with this mod, as upwards gravity gives the player a negative eye height
            // for example
            //
            // This behaviour should actually be buggy in vanilla, as sneaking changes the player's eye height slightly
            else if (Transformer.INetHandlerPlayServer$processPlayerDigging_name.is(methodNode)) {
                logPatchStarting(Transformer.INetHandlerPlayServer$processPlayerDigging_name);

                boolean patched1point5ToGetEyeHeight = false;

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof LdcInsnNode) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode)next;
                        Object object = ldcInsnNode.cst;
                        if (object instanceof Double) {
                            Double d = (Double)object;
                            if (d == 1.5D) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); //this.
                                Transformer.NetHandlerPlayServer$playerEntity_GET.addTo(iterator); //this.playerEntity
                                Transformer.EntityPlayerMP$getEyeHeight.addTo(iterator); //this.playerEntity.getEyeHeight()
                                iterator.add(new InsnNode(Opcodes.F2D)); //(double)this.playerEntity.getEyeHeight()
                                patched1point5ToGetEyeHeight = true;
                                break;
                            }
                        }
                    }
                }
                dieIfFalse(patched1point5ToGetEyeHeight, "Could not find \"1.5D\"");

                logPatchComplete(Transformer.INetHandlerPlayServer$processPlayerDigging_name);
                methodPatches++;
            }
        }

        dieIfFalse(methodPatches == 2, classNode);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static byte[] patchEntityPlayerSP(byte[] bytes) {
        int methodPatches = 0;
        int expectedMethodPatches = 5;
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        int numReplacements = 0;

        for (MethodNode methodNode : classNode.methods) {
            // onUpdateWalkingPlayer makes the assumption that the minY of the player's bounding box is the same as their y position
            //
            // We remove the usage of axisalignedbb.minY and replace it with this.posY
            if (Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name);

                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                        if (Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                            if (iterator.hasPrevious()) {
                                iterator.previous();
                                AbstractInsnNode previous = iterator.previous();
                                if (previous instanceof VarInsnNode) {
                                    VarInsnNode varInsnNode = (VarInsnNode)previous;
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
                                        Transformer.EntityPlayerSP$posY_GET.replace(fieldInsnNode);
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
                logPatchComplete(Transformer.EntityPlayerSP$onUpdateWalkingPlayer_name);
                methodPatches++;
            }
            // onLivingUpdate calls EntityPlayerSP::pushOutOfBlocks a bunch of times with arguments that look like they won't respect
            //      non-standard gravity, the inserted Hook currently just performs the vanilla behaviour
            else if (Transformer.EntityLivingBase$onLivingUpdate_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityLivingBase$onLivingUpdate_name);

                boolean error = true;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (Transformer.EntityPlayerSP$getEntityBoundingBox.is(next)) {
                        next = iterator.next();
                        if (next instanceof VarInsnNode && next.getOpcode() == Opcodes.ASTORE) {
                            int axisalignedbb_var = ((VarInsnNode)next).var;

                            VarInsnNode aLoad6 = new VarInsnNode(Opcodes.ALOAD, axisalignedbb_var);
                            TypeInsnNode instanceofGravityAxisAlignedBB = new TypeInsnNode(Opcodes.INSTANCEOF, Transformer.GravityAxisAlignedBB.toString());
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
                            Transformer.Hooks$pushEntityPlayerSPOutOfBlocks.addTo(iterator);
                            // Add the unconditional jump
                            iterator.add(gotoJumpInsnNode);

                            next = iterator.next();

                            if (next instanceof LabelNode) {
                                ifeqJumpInsnNode.label = (LabelNode)next;

                                next = iterator.next();

                                if (next instanceof LineNumberNode) {
                                    // ClassWriter is going to compute the frames, the commented out code is untested
//                                    FrameNode frameAppendNode = new FrameNode(Opcodes.F_APPEND, 1, new Object[]{"net/minecraft/util/math/AxisAlignedBB"}, 0, new Object[0]);
//                                    iterator.add(frameAppendNode);

                                    LabelNode labelForGotoJumpInsnNode = null;

                                    for (; iterator.hasNext(); ) {
                                        next = iterator.next();
                                        if (next instanceof MethodInsnNode) {
                                            MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                                            if (Transformer.EntityPlayer$getFoodStats.is(methodInsnNode)) {
                                                for (; iterator.hasPrevious(); ) {
                                                    AbstractInsnNode previous = iterator.previous();
                                                    if (previous instanceof LabelNode) {
                                                        labelForGotoJumpInsnNode = (LabelNode)previous;
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
                logPatchComplete(Transformer.EntityLivingBase$onLivingUpdate_name);
            }
            // isHeadSpaceFree checks upwards, it's changed to check upwards relative to the player (the method is small,
            //      I deleted the original entirely for the time being.
            else if (Transformer.EntityPlayerSP$isHeadspaceFree_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityPlayerSP$isHeadspaceFree_name);
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
                Transformer.Hooks$isHeadspaceFree.addTo(instructions);
                instructions.add(new InsnNode(Opcodes.IRETURN));
                instructions.add(labelEnd);
                methodPatches++;
                logPatchComplete(Transformer.EntityPlayerSP$isHeadspaceFree_name);
            }
            // updateAutoJump assumes the player has normal gravity in a lot of places, a few patches may not be neccessary now
            //      that player pitch and yaw is absolute, but it will likely still be the largest patch by far // TODO: 2016-10-19 Update for absolute pitch/yaw.
            //
            //      DEBUG_AUTO_JUMP instead makes the method call super.updateAutoJump(...), which doesn't exist normally, but
            //      EntityPlayerWithGravity (the class we insert into the hierarchy) can be given its own updateAutoJump method.
            //      This can copy the vanilla code with changes that take into account the gravity direction. This method is then
            //      used to create the patches for the vanilla method
            else if (Transformer.EntityPlayerSP$updateAutoJump_name.is(methodNode)) {
                logPatchStarting(Transformer.EntityPlayerSP$updateAutoJump_name);
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
                            Transformer.AbstractClientPlayer$updateAutoJump_SPECIAL.addTo(iterator);
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
                                    && Transformer.EntityPlayerSP$getEntityBoundingBox.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.next();
                                iterator.remove(); // GETFIELD net/minecraft/util/math/AxisAlignedBB.minY : D
                                Transformer.Hooks$getOriginRelativePosY.addTo(iterator);
                                iterator.next(); // DLOAD ? (likely 7)
                                iterator.next(); // INVOKESPECIAL net/minecraft/util/math/Vec3d.<init> (DDD)V
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Have to pass 'this' to adjustVec as well
                                Transformer.Hooks$adjustVec.addTo(iterator);
                                patch4Complete = true;
                            }
                            //newPatch #5
                            else if (patch4Point5Complete
                                    && !patch5Complete
                                    && Transformer.Vec3d$scale.is(methodInsnNode)) {
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
                                    && Transformer.EntityPlayerSP$getForward.is(methodInsnNode)) {
                                Transformer.Hooks$getRelativeLookVec.replace(methodInsnNode);
                                patch5Point5Complete = true;
                            }
                            //PATCH #1
                            else if (Transformer.Vec3d$addVector.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$addAdjustedVector.addTo(iterator);
                                addVectorReplacements++;
                            }
                            //newPATCH #8
                            else if (patch7Complete
                                    && !patch8Complete
                                    && Transformer.AxisAlignedBB$INIT.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$constructNewGAABBFrom2Vec3d.addTo(iterator);
                                patch8Complete = true;
                            }
                            //PATCH #3
                            else if (Transformer.BlockPos$up_NO_ARGS.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeUpblockPos_NO_ARGS.addTo(iterator);
                                blockPosUPCount++;
                            }
                            else if (Transformer.BlockPos$up_INT_ARG.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeUpblockPos_INT_ARG.addTo(iterator);
                                blockPosUPCountI++;
                            }
                            //PATCH #4
                            else if (!foundFirstBlockPosGetY
                                    && Transformer.BlockPos$getY.is(methodInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeYOfBlockPos.addTo(iterator);
                                foundFirstBlockPosGetY = true;
                            }

                        }
                        else if (next instanceof TypeInsnNode
                                && next.getOpcode() == Opcodes.NEW) {
                            TypeInsnNode typeInsnNode = (TypeInsnNode)next;
                            //newPATCH #1
                            if (!patch1Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.Vec3d.toString())) {
                                iterator.remove();
                                while (!patch1Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$getBottomOfEntity.addTo(iterator);
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
                                    && typeInsnNode.desc.equals(Transformer.AxisAlignedBB.toString())) {
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
                            else if (patch5Point5Complete
                                    && !patch6Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.BlockPos.toString())) {
                                iterator.remove();
                                while (!patch6Complete) {
                                    next = iterator.next();
                                    if (next instanceof MethodInsnNode && next.getOpcode() == INVOKESPECIAL) {
                                        iterator.remove();
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$getBlockPosAtTopOfPlayer.addTo(iterator);

                                        //newPATCH #6.5
                                        iterator.next(); //ASTORE ? (probably 17)
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, vec3d12_var));
                                        iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                        Transformer.Hooks$adjustVec.addTo(iterator);
                                        iterator.add(new VarInsnNode(Opcodes.ASTORE, vec3d12_var));

                                        patch6Complete = true;
                                    }
                                    else {
                                        iterator.remove();
                                    }
                                }
                            }
                            //newPATCH #9
                            else if (patch8Complete
                                    && !patch9Complete
//                                    && typeInsnNode.getOpcode() == Opcodes.NEW
                                    && typeInsnNode.desc.equals(Transformer.Vec3d.toString())) {
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
                                Transformer.Hooks$addAdjustedVector.addTo(iterator);

                                patch9Complete = true;
                            }

                        }
                        else if (next instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                            //newPATCH #2
                            if (patch1Complete
                                    && !patch2Complete
                                    && Transformer.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getOriginRelativePosX.replace(iterator);
                                patch2Complete = true;
                            }
                            //newPATCH #3
                            else if (patch2Complete
                                    && !patch3Complete
                                    && Transformer.EntityPlayerSP$posZ_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getOriginRelativePosZ.replace(iterator);
                                patch3Complete = true;
                            }
                            else if (patch4Complete
                                    && !patch4Point5Complete
                                    && Transformer.EntityPlayerSP$rotationYaw_GET.is(fieldInsnNode)) {
                                Transformer.Hooks$getRelativeYaw.replace(iterator);
                                patch4Point5ReplacementCount++;
                                if (patch4Point5ReplacementCount >= 2) {
                                    patch4Point5Complete = true;
                                }
                            }
                            //newPATCH #10
                            else if (patch9Complete
                                    && axisAlignedBBmaxYCount < 2
                                    && Transformer.AxisAlignedBB$maxY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeTopOfBB.addTo(iterator);
                                axisAlignedBBmaxYCount++;
                            }
                            //newPATCH #11
                            else if (axisAlignedBBmaxYCount == 2
                                    && axisAlignedBBminYCount < 2
                                    && Transformer.AxisAlignedBB$minY_GET.is(fieldInsnNode)) {
                                iterator.remove();
                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                Transformer.Hooks$getRelativeBottomOfBB.addTo(iterator);
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
                logPatchComplete(Transformer.EntityPlayerSP$updateAutoJump_name);
                methodPatches++;
            }
            // moveEntity passes the change in X and Z movement to the updateAutoJump method, we need to pass the change in
            //      X and Z relative to the player's view instead
            //
            //      DEBUG_AUTO_JUMP instead calls super to help with debugging EntityPlayerWithGravity::updateAutoJump
            else if (Transformer.Entity$moveEntity_name.is(methodNode)) {
                logPatchStarting(Transformer.Entity$moveEntity_name);
                if (DEBUG_AUTO_JUMP) {
                    for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                        AbstractInsnNode next = iterator.next();
                        if (next instanceof LineNumberNode) {
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 1));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 3));
                            iterator.add(new VarInsnNode(Opcodes.DLOAD, 5));
                            Transformer.AbstractClientPlayer$moveEntity_SPECIAL.addTo(iterator);
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
                            Transformer.EntityPlayerSP$posY_GET.addTo(iterator);
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                            Transformer.EntityPlayerSP$posZ_GET.addTo(iterator);
                            Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
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

                            while (true) {
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

                                    while (true) {
                                        next = iterator.next();
                                        if (next instanceof FieldInsnNode) {
                                            FieldInsnNode fieldInsnNode = (FieldInsnNode)next;
                                            if (Transformer.EntityPlayerSP$posX_GET.is(fieldInsnNode)) {
                                                iterator.previous();
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                iterator.next(); // same GETFIELD instruction
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                Transformer.EntityPlayerSP$posY_GET.addTo(iterator);
                                                iterator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                                Transformer.EntityPlayerSP$posZ_GET.addTo(iterator);
                                                Transformer.Hooks$inverseAdjustXYZ.addTo(iterator);
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

                                                while (true) {
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
                logPatchComplete(Transformer.Entity$moveEntity_name);
                methodPatches++;
            }
            // TODO: Patch pushOutOfBlocks
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
            // When the camera rotates due to a change in gravity, the player's ears would also have to be rotated
            //      setListener calls SoundSystem::setListenerOrientation as if the player has normal gravity. A hook is inserted
            //      that replaces this call and calls SoundSystem::setListenerOrientation with arguments that take into account
            //      the rotation of the player's camera
            if (Transformer.SoundManager$setListener_name.is(methodNode)) {
                logPatchStarting(Transformer.SoundManager$setListener_name);
                boolean patched_setListenerOrientationInstruction = false;
                for (ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator(); iterator.hasNext(); ) {
                    AbstractInsnNode next = iterator.next();
                    if (next instanceof MethodInsnNode) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)next;
                        if (Transformer.SoundSystem$setListenerOrientation_name.is(methodInsnNode.name)) {
                            Transformer.Hooks$setListenerOrientationHook.replace(methodInsnNode);
                            iterator.previous();
                            iterator.add(new VarInsnNode(Opcodes.ALOAD, 1)); // load EntityPlayer method argument
                            patched_setListenerOrientationInstruction = true;
                        }
                    }
                }
                patchMethodUsingAbsoluteRotations(methodNode, GET_ROTATIONYAW + GET_PREVROTATIONYAW + GET_ROTATIONPITCH + GET_PREVROTATIONPITCH);
                dieIfFalse(patched_setListenerOrientationInstruction, "Failed to patch setListenerOrientation instruction");
                logPatchComplete(Transformer.SoundManager$setListener_name);
                methodPatches++;
            }
        }
        dieIfFalse(methodPatches == expectedMethodPatches, "Could not find " + Transformer.SoundManager$setListener_name + " method");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
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
            log("Patching class %s", transformedClassName);
            byte[] toReturn = function.apply(bytes);
            log("Patched class  %s", transformedClassName);
            return toReturn;
        }
    }
}
