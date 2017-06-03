package uk.co.mysterymayhem.gravitymod.asm;

import org.objectweb.asm.Opcodes;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.MethodDesc;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.FieldInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.HookInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.instructions.MethodInsn;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.FieldName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.MethodName;
import uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.names.ObjectName;

import static org.objectweb.asm.Opcodes.*;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.*;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.DOUBLE;
import static uk.co.mysterymayhem.gravitymod.asm.util.obfuscation.ObfuscationHelper.FLOAT;

/**
 * Created by Mysteryem on 2017-02-01.
 */
public class Ref {
    /*
    Mojang method names
    */
    public static final MethodName AxisAlignedBB$calculateXOffset_name = new MethodName("calculateXOffset", "func_72316_a");
    public static final MethodName AxisAlignedBB$calculateYOffset_name = new MethodName("calculateYOffset", "func_72323_b");
    public static final MethodName AxisAlignedBB$calculateZOffset_name = new MethodName("calculateZOffset", "func_72322_c");
    // () arguments
    public static final MethodName BlockPos$down_NO_ARGS_name = new MethodName("down", "func_177977_b");
    public static final MethodName BlockPos$getY_name = new MethodName("getY", "func_177956_o");
    public static final MethodName BlockPos$up_INT_ARG_name = new MethodName("up", "func_177981_b");
    public static final MethodName BlockPos$up_NO_ARGS_name = new MethodName("up", "func_177984_a");
    // (DDD) arguments
    public static final MethodName BlockPos$PooledMutableBlockPos$retain_name = new MethodName("retain", "func_185345_c");
    // (DDD) arguments
    public static final MethodName BlockPos$PooledMutableBlockPos$setPos_name = new MethodName("setPos", "func_189532_c");
    public static final MethodName Entity$getEntityBoundingBox_name = new MethodName("getEntityBoundingBox", "func_174813_aQ");
    public static final MethodName Entity$getEyeHeight_name = new MethodName("getEyeHeight", "func_70047_e");
    public static final MethodName Entity$getForward_name = new MethodName("getForward", "func_189651_aD");
    public static final MethodName Entity$getLookVec_name = new MethodName("getLookVec", "func_70040_Z");
    public static final MethodName Entity$isOffsetPositionInLiquid_name = new MethodName("isOffsetPositionInLiquid", "func_70038_c");
    public static final MethodName Entity$moveEntity_name = new MethodName("moveEntity", "func_70091_d");
    public static final MethodName Entity$moveRelative_name = new MethodName("moveRelative", "func_70060_a");
    public static final MethodName Entity$onUpdate_name = new MethodName("onUpdate", "func_70071_h_");
    public static final MethodName Entity$pushOutOfBlocks = new MethodName("pushOutOfBlocks", "func_145771_j");
    public static final MethodName EntityItem$combineItems_name = new MethodName("combineItems", "func_70289_a");
    public static final MethodName EntityLivingBase$jump_name = new MethodName("jump", "func_70664_aZ");
    public static final MethodName EntityLivingBase$moveEntityWithHeading_name = new MethodName("moveEntityWithHeading", "func_70612_e");
    public static final MethodName EntityLivingBase$onLivingUpdate_name = new MethodName("onLivingUpdate", "func_70636_d");
    public static final MethodName EntityLivingBase$updateDistance_name = new MethodName("updateDistance", "func_110146_f");
    public static final MethodName EntityPlayer$getFoodStats_name = new MethodName("getFoodStats", "func_71024_bL");
    public static final MethodName EntityPlayerMP$handleFalling_name = new MethodName("handleFalling", "func_71122_b");
    // Method added by forge, so no obf name
    public static final MethodName EntityPlayerSP$isHeadspaceFree_name = new MethodName("isHeadspaceFree");
    public static final MethodName EntityPlayerSP$onUpdateWalkingPlayer_name = new MethodName("onUpdateWalkingPlayer", "func_175161_p");
    public static final MethodName EntityPlayerSP$updateAutoJump_name = new MethodName("updateAutoJump", "func_189810_i");
    public static final MethodName EntityRenderer$drawNameplate_name = new MethodName("drawNameplate", "func_189692_a");
    public static final MethodName EntityRenderer$getMouseOver_name = new MethodName("getMouseOver", "func_78473_a");
    public static final MethodName GlStateManager$disableLighting_name = new MethodName("disableLighting", "func_179140_f");
    public static final MethodName INetHandlerPlayClient$handlePlayerPosLook_name = new MethodName("handlePlayerPosLook", "func_184330_a");
    public static final MethodName INetHandlerPlayServer$processPlayer_name = new MethodName("processPlayer", "func_147347_a");
    public static final MethodName INetHandlerPlayServer$processPlayerDigging_name = new MethodName("processPlayerDigging", "func_147345_a");
    public static final MethodName Item$onItemUse_name = new MethodName("onItemUse", "func_180614_a");
    public static final MethodName Item$onPlayerStoppedUsing_name = new MethodName("onPlayerStoppedUsing", "func_77615_a");
    public static final MethodName Item$onRightClick_name = new MethodName("onItemRightClick", "func_77659_a");
    public static final MethodName ItemStack$onItemUse_name = new MethodName("onItemUse", "func_179546_a");
    public static final MethodName ItemStack$onPlayerStoppedUsing_name = new MethodName("onPlayerStoppedUsing", "func_77974_b");
    public static final MethodName ItemStack$useItemRightClick_name = new MethodName("useItemRightClick", "func_77957_a");
    public static final MethodName RenderLivingBase$doRender_name = new MethodName("doRender", "func_76986_a");
    public static final MethodName SoundManager$setListener_name = new MethodName("setListener", "func_148615_a");
    public static final MethodName Vec3d$addVector_name = new MethodName("addVector", "func_72441_c");
    public static final MethodName Vec3d$scale_name = new MethodName("scale", "func_186678_a");
    public static final MethodName World$getEntitiesInAABBexcluding_name = new MethodName("getEntitiesInAABBexcluding", "func_175674_a");
    /*
        PaulsCode method names
     */
    public static final MethodName SoundSystem$setListenerOrientation_name = new MethodName("setListenerOrientation");

    /*
        Mojang field names
     */
    public static final FieldName AxisAlignedBB$maxY_name = new FieldName("maxY", "field_72337_e");
    public static final FieldName AxisAlignedBB$minY_name = new FieldName("minY", "field_72338_b");
    public static final FieldName Blocks$LADDER_name = new FieldName("LADDER", "field_150468_ap");
    public static final FieldName Entity$posX_name = new FieldName("posX", "field_70165_t");
    public static final FieldName Entity$posY_name = new FieldName("posY", "field_70163_u");
    public static final FieldName Entity$posZ_name = new FieldName("posZ", "field_70161_v");
    public static final FieldName Entity$prevPosX_name = new FieldName("prevPosX", "field_70169_q");
    public static final FieldName Entity$prevPosY_name = new FieldName("prevPosY", "field_70167_r");
    public static final FieldName Entity$prevPosZ_name = new FieldName("prevPosZ", "field_70166_s");
    public static final FieldName Entity$prevRotationPitch_name = new FieldName("prevRotationPitch", "field_70127_C");
    public static final FieldName Entity$prevRotationYaw_name = new FieldName("prevRotationYaw", "field_70126_B");
    public static final FieldName Entity$rotationPitch_name = new FieldName("rotationPitch", "field_70125_A");
    public static final FieldName Entity$rotationYaw_name = new FieldName("rotationYaw", "field_70177_z");
    public static final FieldName EntityLivingBase$limbSwing_name = new FieldName("limbSwing", "field_184619_aG");
    public static final FieldName EntityLivingBase$limbSwingAmount_name = new FieldName("limbSwingAmount", "field_70721_aZ");
    public static final FieldName EntityLivingBase$prevRotationYawHead_name = new FieldName("prevRotationYawHead", "field_70758_at");
    public static final FieldName EntityLivingBase$rotationYawHead_name = new FieldName("rotationYawHead", "field_70759_as");
    public static final FieldName NetHandlerPlayServer$lastGoodX_name = new FieldName("lastGoodX", "field_184352_o");
    //    public static final FieldName NetHandlerPlayServer$lastGoodY_name = new FieldName("lastGoodY", "field_184353_p");
//    public static final FieldName NetHandlerPlayServer$lastGoodZ_name = new FieldName("lastGoodZ", "field_184354_q");
    public static final FieldName NetHandlerPlayServer$playerEntity_name = new FieldName("playerEntity", "field_147369_b");

    /*
        Shared reference class names
     */
    public static final ObjectName ActionResult = new ObjectName("net/minecraft/util/ActionResult");
    public static final ObjectName AbstractClientPlayer = new ObjectName("net/minecraft/client/entity/AbstractClientPlayer");
    public static final ObjectName AxisAlignedBB = new ObjectName("net/minecraft/util/math/AxisAlignedBB");
    public static final ObjectName Block = new ObjectName("net/minecraft/block/Block");
    public static final ObjectName BlockPos = new ObjectName("net/minecraft/util/math/BlockPos");
    public static final ObjectName BlockPos$PooledMutableBlockPos = new ObjectName("net/minecraft/util/math/BlockPos$PooledMutableBlockPos");
    public static final ObjectName Blocks = new ObjectName("net/minecraft/init/Blocks");
    public static final ObjectName CPacketPlayer = new ObjectName("net/minecraft/network/play/client/CPacketPlayer");
    public static final ObjectName Entity = new ObjectName("net/minecraft/entity/Entity");
    public static final ObjectName EntityLivingBase = new ObjectName("net/minecraft/entity/EntityLivingBase");
    public static final ObjectName EntityPlayer = new ObjectName("net/minecraft/entity/player/EntityPlayer");
    public static final ObjectName EntityPlayerMP = new ObjectName("net/minecraft/entity/player/EntityPlayerMP");
    public static final ObjectName EntityPlayerSP = new ObjectName("net/minecraft/client/entity/EntityPlayerSP");
    public static final ObjectName EntityOtherPlayerMP = new ObjectName("net/minecraft/client/entity/EntityOtherPlayerMP");
    public static final ObjectName EnumActionResult = new ObjectName("net/minecraft/util/EnumActionResult");
    public static final ObjectName EnumFacing = new ObjectName("net/minecraft/util/EnumFacing");
    public static final ObjectName EnumHand = new ObjectName("net/minecraft/util/EnumHand");
    public static final ObjectName FoodStats = new ObjectName("net/minecraft/util/FoodStats");
    public static final ObjectName Item = new ObjectName("net/minecraft/item/Item");
    public static final ObjectName ItemStack = new ObjectName("net/minecraft/item/ItemStack");
    public static final ObjectName NetHandlerPlayServer = new ObjectName("net/minecraft/network/NetHandlerPlayServer");
    public static final ObjectName Vec3d = new ObjectName("net/minecraft/util/math/Vec3d");
    public static final ObjectName World = new ObjectName("net/minecraft/world/World");
    public static final ObjectName WorldClient = new ObjectName("net/minecraft/client/multiplayer/WorldClient");
    // Non-minecraft classes
//    public static final ObjectClassName EntityFloatingItem = new ObjectClassName("uk/co/mysterymayhem/gravitymod/common/entities/EntityFloatingItem");
    public static final ObjectName EntityPlayerWithGravity = new ObjectName("uk/co/mysterymayhem/gravitymod/asm/EntityPlayerWithGravity");
    public static final ObjectName GravityAxisAlignedBB = new ObjectName("uk/co/mysterymayhem/gravitymod/common/util/boundingboxes/GravityAxisAlignedBB");
    public static final ObjectName ItemStackAndBoolean = new ObjectName("uk/co/mysterymayhem/gravitymod/asm/util/ItemStackAndBoolean");
    public static final ObjectName List = new ObjectName("java/util/List");
    public static final ObjectName Predicate = new ObjectName("com/google/common/base/Predicate");
    public static final ObjectName SoundSystem = new ObjectName("paulscode/sound/SoundSystem");

    /*
        Mojang field instructions
     */
    public static final FieldInsn AxisAlignedBB$maxY_GET = new FieldInsn(GETFIELD, AxisAlignedBB, AxisAlignedBB$maxY_name, DOUBLE);
    public static final FieldInsn AxisAlignedBB$minY_GET = new FieldInsn(GETFIELD, AxisAlignedBB, AxisAlignedBB$minY_name, DOUBLE);
    public static final FieldInsn Blocks$LADDER_GET = new FieldInsn(GETSTATIC, Blocks, Blocks$LADDER_name, Block);
    public static final FieldInsn Entity$posX_GET = new FieldInsn(GETFIELD, Entity, Entity$posX_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$limbSwing_PUT = new FieldInsn(PUTFIELD, EntityLivingBase, EntityLivingBase$limbSwing_name, FLOAT);
    public static final FieldInsn EntityLivingBase$limbSwingAmount_GET = new FieldInsn(GETFIELD, EntityLivingBase, EntityLivingBase$limbSwingAmount_name, FLOAT);
    public static final FieldInsn EntityLivingBase$posX_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posX_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$posY_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posY_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$posZ_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$posZ_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$prevPosX_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$prevPosX_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$prevPosZ_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$prevPosZ_name, DOUBLE);
    public static final FieldInsn EntityLivingBase$rotationPitch_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$rotationPitch_name, FLOAT);
    public static final FieldInsn EntityLivingBase$rotationYaw_GET = new FieldInsn(GETFIELD, EntityLivingBase, Entity$rotationYaw_name, FLOAT);
    public static final FieldInsn EntityOtherPlayerMP$posX_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$posX_name, DOUBLE);
    public static final FieldInsn EntityOtherPlayerMP$posY_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$posY_name, DOUBLE);
    public static final FieldInsn EntityOtherPlayerMP$posZ_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$posZ_name, DOUBLE);
    public static final FieldInsn EntityOtherPlayerMP$prevPosX_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$prevPosX_name, DOUBLE);
    public static final FieldInsn EntityOtherPlayerMP$prevPosY_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$prevPosY_name, DOUBLE);
    public static final FieldInsn EntityOtherPlayerMP$prevPosZ_GET = new FieldInsn(GETFIELD, EntityOtherPlayerMP, Entity$prevPosZ_name, DOUBLE);
    public static final FieldInsn EntityPlayer$posY_GET = new FieldInsn(GETFIELD, EntityPlayer, Entity$posY_name, DOUBLE);
    public static final FieldInsn EntityPlayerMP$posX_GET = new FieldInsn(GETFIELD, EntityPlayerMP, Entity$posX_name, DOUBLE);
    public static final FieldInsn EntityPlayerMP$posY_GET = new FieldInsn(GETFIELD, EntityPlayerMP, Entity$posY_name, DOUBLE);
    public static final FieldInsn EntityPlayerMP$posZ_GET = new FieldInsn(GETFIELD, EntityPlayerMP, Entity$posZ_name, DOUBLE);
    public static final FieldInsn EntityPlayerSP$posX_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posX_name, DOUBLE);
    public static final FieldInsn EntityPlayerSP$posY_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posY_name, DOUBLE);
    public static final FieldInsn EntityPlayerSP$posZ_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$posZ_name, DOUBLE);
    public static final FieldInsn EntityPlayerSP$rotationYaw_GET = new FieldInsn(GETFIELD, EntityPlayerSP, Entity$rotationYaw_name, FLOAT);
    public static final FieldInsn NetHandlerPlayServer$lastGoodX_GET = new FieldInsn(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodX_name, DOUBLE);
    //    public static final FieldInstruction NetHandlerPlayServer$lastGoodY_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodY_name, DOUBLE);
//    public static final FieldInstruction NetHandlerPlayServer$lastGoodZ_GET = new FieldInstruction(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$lastGoodZ_name, DOUBLE);
    public static final FieldInsn NetHandlerPlayServer$playerEntity_GET = new FieldInsn(GETFIELD, NetHandlerPlayServer, NetHandlerPlayServer$playerEntity_name, EntityPlayerMP);

    /*
        Mojang method instructions
     */
    // While there could be a constructor for MethodInstruction that doesn't require a MethodDesc, it is useful to
    // visually split apart the arguments so it's easier to tell what each argument is
    public static final MethodInsn AxisAlignedBB$INIT = new MethodInsn(INVOKESPECIAL, AxisAlignedBB, INIT, new MethodDesc(VOID, Vec3d, Vec3d));
    public static final MethodInsn AxisAlignedBB$calculateXOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateXOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    public static final MethodInsn AxisAlignedBB$calculateYOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateYOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    public static final MethodInsn AxisAlignedBB$calculateZOffset = new MethodInsn(INVOKEVIRTUAL, AxisAlignedBB, AxisAlignedBB$calculateZOffset_name, new MethodDesc(DOUBLE, AxisAlignedBB, DOUBLE));
    public static final MethodInsn BlockPos$down_NO_ARGS = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$down_NO_ARGS_name, new MethodDesc(BlockPos));
    public static final MethodInsn BlockPos$getY = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$getY_name, new MethodDesc(INT));
    public static final MethodInsn BlockPos$INIT = new MethodInsn(INVOKESPECIAL, BlockPos, INIT, new MethodDesc(VOID, INT, INT, INT));
    public static final MethodInsn BlockPos$up_INT_ARG = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$up_INT_ARG_name, new MethodDesc(BlockPos, INT));
    public static final MethodInsn BlockPos$up_NO_ARGS = new MethodInsn(INVOKEVIRTUAL, BlockPos, BlockPos$up_NO_ARGS_name, new MethodDesc(BlockPos));
    public static final MethodInsn BlockPos$PooledMutableBlockPos$setPos = new MethodInsn(
            INVOKEVIRTUAL, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$setPos_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn BlockPos$PooledMutableBlockPos$retain = new MethodInsn(
            INVOKESTATIC, BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos$retain_name, new MethodDesc(BlockPos$PooledMutableBlockPos, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn Entity$getEntityBoundingBox = new MethodInsn(INVOKEVIRTUAL, Entity, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
    public static final MethodInsn EntityLivingBase$getLookVec = new MethodInsn(INVOKEVIRTUAL, EntityLivingBase, Entity$getLookVec_name, new MethodDesc(Vec3d));
    public static final MethodInsn EntityLivingBase$isOffsetPositionInLiquid = new MethodInsn(
            INVOKEVIRTUAL, EntityLivingBase, Entity$isOffsetPositionInLiquid_name, new MethodDesc(BOOLEAN, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn EntityPlayerMP$getEyeHeight = new MethodInsn(INVOKEVIRTUAL, EntityPlayerMP, Entity$getEyeHeight_name, new MethodDesc(FLOAT));
    public static final MethodInsn EntityPlayerMP$handleFalling = new MethodInsn(INVOKEVIRTUAL, EntityPlayerMP, EntityPlayerMP$handleFalling_name, new MethodDesc(VOID, DOUBLE, BOOLEAN));
    //    public static final MethodInstruction EntityPlayerMP$moveEntity = new MethodInstruction(INVOKEVIRTUAL, EntityPlayerMP, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn EntityPlayerSP$getEntityBoundingBox = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, Entity$getEntityBoundingBox_name, new MethodDesc(AxisAlignedBB));
    public static final MethodInsn EntityPlayerSP$getForward = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, Entity$getForward_name, new MethodDesc(Vec3d));
    public static final MethodInsn EntityPlayer$getFoodStats = new MethodInsn(INVOKEVIRTUAL, EntityPlayerSP, EntityPlayer$getFoodStats_name, new MethodDesc(FoodStats));
    public static final MethodInsn Item$onItemRightClick = new MethodInsn(INVOKEVIRTUAL, Item, Item$onRightClick_name, new MethodDesc(ActionResult, ItemStack, World, EntityPlayer, EnumHand));
    public static final MethodInsn Item$onItemUse = new MethodInsn(
            INVOKEVIRTUAL, Item, Item$onItemUse_name, new MethodDesc(EnumActionResult, ItemStack, EntityPlayer, World, BlockPos, EnumHand, EnumFacing, FLOAT, FLOAT, FLOAT));
    public static final MethodInsn Item$onPlayerStoppedUsing = new MethodInsn(INVOKEVIRTUAL, Item, Item$onPlayerStoppedUsing_name, new MethodDesc(VOID, ItemStack, World, EntityLivingBase, INT));
    public static final MethodInsn Vec3d$addVector = new MethodInsn(INVOKEVIRTUAL, Vec3d, Vec3d$addVector_name, new MethodDesc(Vec3d, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn Vec3d$scale = new MethodInsn(INVOKEVIRTUAL, Vec3d, Vec3d$scale_name, new MethodDesc(Vec3d, DOUBLE));
    public static final MethodName World$getCollisionBoxes_name = new MethodName("getCollisionBoxes", "func_184144_a");
    public static final MethodInsn WorldClient$getEntitiesInAABBexcluding = new MethodInsn(
            INVOKEVIRTUAL, WorldClient, World$getEntitiesInAABBexcluding_name, new MethodDesc(List, Entity, AxisAlignedBB, Predicate));

    /*
        Up And Down And All Around asm hook method instructions
     */
    public static final HookInsn Hooks$addAdjustedVector = new HookInsn("addAdjustedVector", new MethodDesc(Vec3d, Vec3d, DOUBLE, DOUBLE, DOUBLE, Entity));
    public static final HookInsn Hooks$adjustVec = new HookInsn("adjustVec", new MethodDesc(Vec3d, Vec3d, Entity));
    //    public static final HooksMethodInstruction Hooks$adjustXYZ = new HooksMethodInstruction("adjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$constructNewGAABBFrom2Vec3d = new HookInsn("constructNewGAABBFrom2Vec3d", new MethodDesc(AxisAlignedBB, Vec3d, Vec3d, Entity));
    public static final HookInsn Hooks$getBlockPosAtTopOfPlayer = new HookInsn("getBlockPosAtTopOfPlayer", new MethodDesc(BlockPos, Entity));
    public static final HookInsn Hooks$getBlockPostBelowEntity = new HookInsn("getBlockPosBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, Entity));
    public static final HookInsn Hooks$getBottomOfEntity = new HookInsn("getBottomOfEntity", new MethodDesc(Vec3d, Entity));
    public static final HookInsn Hooks$getImmutableBlockPosBelowEntity = new HookInsn("getImmutableBlockPosBelowEntity", new MethodDesc(BlockPos, Entity));
    public static final HookInsn Hooks$getOriginRelativePosX = new HookInsn("getOriginRelativePosX", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getOriginRelativePosY = new HookInsn("getOriginRelativePosY", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getOriginRelativePosZ = new HookInsn("getOriginRelativePosZ", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getPrevRelativeYawHead = new HookInsn("getPrevRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
    public static final HookInsn Hooks$getRelativeBottomOfBB = new HookInsn("getRelativeBottomOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
    public static final HookInsn Hooks$getRelativeDownBlockPos = new HookInsn("getRelativeDownBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
    public static final HookInsn Hooks$getRelativeLookVec = new HookInsn("getRelativeLookVec", new MethodDesc(Vec3d, Entity));
    public static final HookInsn Hooks$getRelativePitch = new HookInsn("getRelativePitch", new MethodDesc(FLOAT, Entity));
    public static final HookInsn Hooks$getRelativePrevPitch = new HookInsn("getRelativePrevPitch", new MethodDesc(FLOAT, Entity));
    public static final HookInsn Hooks$getRelativePrevPosX = new HookInsn("getRelativePrevPosX", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getRelativePrevPosZ = new HookInsn("getRelativePrevPosZ", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getRelativePrevYaw = new HookInsn("getRelativePrevYaw", new MethodDesc(FLOAT, Entity));
    public static final HookInsn Hooks$getRelativePosX = new HookInsn("getRelativePosX", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getRelativePosY = new HookInsn("getRelativePosY", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getRelativePosZ = new HookInsn("getRelativePosZ", new MethodDesc(DOUBLE, Entity));
    public static final HookInsn Hooks$getRelativeTopOfBB = new HookInsn("getRelativeTopOfBB", new MethodDesc(DOUBLE, AxisAlignedBB, Entity));
    public static final HookInsn Hooks$getRelativeUpblockPos_INT_ARG = new HookInsn("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, INT, Entity));
    public static final HookInsn Hooks$getRelativeUpblockPos_NO_ARGS = new HookInsn("getRelativeUpBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
    public static final HookInsn Hooks$getRelativeYaw = new HookInsn("getRelativeYaw", new MethodDesc(FLOAT, Entity));
    public static final HookInsn Hooks$getRelativeYawHead = new HookInsn("getRelativeYawHead", new MethodDesc(FLOAT, EntityLivingBase));
    public static final HookInsn Hooks$getRelativeYOfBlockPos = new HookInsn("getRelativeYOfBlockPos", new MethodDesc(INT, BlockPos, Entity));
    public static final HookInsn Hooks$getVanillaEntityBoundingBox = new HookInsn("getVanillaEntityBoundingBox", new MethodDesc(AxisAlignedBB, Entity));
    public static final HookInsn Hooks$inverseAdjustXYZ = new HookInsn("inverseAdjustXYZ", new MethodDesc(DOUBLE.asArray(), Entity, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$isHeadspaceFree = new HookInsn("isHeadspaceFree", new MethodDesc(BOOLEAN, EntityPlayerSP, BlockPos, INT));
    public static final HookInsn Hooks$makePositionAbsolute = new HookInsn("makePositionAbsolute", new MethodDesc(VOID, EntityLivingBase));
    public static final HookInsn Hooks$makePositionRelative = new HookInsn("makePositionRelative", new MethodDesc(VOID, EntityLivingBase));
    //    public static final HooksMethodInstruction Hooks$moveEntityAbsolute = new HooksMethodInstruction("moveEntityAbsolute", new MethodDesc(VOID, EntityPlayer, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$makeRelativeBlockPos = new HookInsn("makeRelativeBlockPos", new MethodDesc(BlockPos, BlockPos, Entity));
    public static final HookInsn Hooks$netHandlerPlayServerGetPacketZ = new HookInsn("netHandlerPlayServerGetPacketZ", new MethodDesc(DOUBLE, NetHandlerPlayServer, CPacketPlayer));
    public static final HookInsn Hooks$netHandlerPlayServerGetRelativeY = new HookInsn("netHandlerPlayServerGetRelativeY", new MethodDesc(DOUBLE, NetHandlerPlayServer, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$netHandlerPlayServerHandleFallingYChange =
            new HookInsn("netHandlerPlayServerHandleFallingYChange", new MethodDesc(DOUBLE, EntityPlayerMP, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$netHandlerPlayServerSetRelativeYToZero =
            new HookInsn("netHandlerPlayServerSetRelativeYToZero", new MethodDesc(DOUBLE.asArray(), NetHandlerPlayServer, DOUBLE, DOUBLE, DOUBLE));
    public static final HookInsn Hooks$normaliseAABB = new HookInsn("normaliseAABB", new MethodDesc(AxisAlignedBB, AxisAlignedBB));
    public static final HookInsn Hooks$onItemRightClickPost = new HookInsn("onItemRightClickPost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityPlayer));
    public static final HookInsn Hooks$onItemRightClickPre = new HookInsn("onItemRightClickPre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityPlayer));
    public static final HookInsn Hooks$onItemUsePost = new HookInsn("onItemUsePost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityPlayer));
    public static final HookInsn Hooks$onItemUsePre = new HookInsn("onItemUsePre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityPlayer));
    public static final HookInsn Hooks$onPlayerStoppedUsingPost = new HookInsn("onPlayerStoppedUsingPost", new MethodDesc(VOID, ItemStackAndBoolean, ItemStack, EntityLivingBase));
    public static final HookInsn Hooks$onPlayerStoppedUsingPre = new HookInsn("onPlayerStoppedUsingPre", new MethodDesc(ItemStackAndBoolean, ItemStack, EntityLivingBase));
    public static final HookInsn Hooks$pushEntityPlayerSPOutOfBlocks = new HookInsn("pushEntityPlayerSPOutOfBlocks", new MethodDesc(VOID, EntityPlayerWithGravity, AxisAlignedBB));
    public static final HookInsn Hooks$reverseXOffset = new HookInsn("reverseXOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    public static final HookInsn Hooks$reverseYOffset = new HookInsn("reverseYOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    public static final HookInsn Hooks$reverseZOffset = new HookInsn("reverseZOffset", new MethodDesc(DOUBLE, AxisAlignedBB, AxisAlignedBB, DOUBLE));
    public static final HookInsn Hooks$runNameplateCorrection = new HookInsn("runNameplateCorrection", new MethodDesc(VOID, BOOLEAN));
    public static final HookInsn Hooks$setListenerOrientationHook = new HookInsn("setListenerOrientationHook", new MethodDesc(VOID, SoundSystem, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, FLOAT, EntityPlayer));
    public static final HookInsn Hooks$setPooledMutableBlockPosToBelowEntity =
            new HookInsn("setPooledMutableBlockPosToBelowEntity", new MethodDesc(BlockPos$PooledMutableBlockPos, BlockPos$PooledMutableBlockPos, Entity));

    /*
        Debug only (currently only used in patchEntityPlayerSP when DEBUG_AUTO_JUMP is true)
     */
    public static final MethodInsn AbstractClientPlayer$moveEntity_SPECIAL = new MethodInsn(Opcodes.INVOKESPECIAL, AbstractClientPlayer, Entity$moveEntity_name, new MethodDesc(VOID, DOUBLE, DOUBLE, DOUBLE));
    public static final MethodInsn AbstractClientPlayer$updateAutoJump_SPECIAL = new MethodInsn(Opcodes.INVOKESPECIAL, AbstractClientPlayer, EntityPlayerSP$updateAutoJump_name, new MethodDesc(VOID, FLOAT, FLOAT));
}
