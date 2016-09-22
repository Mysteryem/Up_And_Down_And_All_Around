package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;

/**
 * Created by Mysteryem on 2016-08-16.
 */
public class Hooks {

    //TODO: Where is this used? Is it used in any ASM-ed code?
    public static void moveEntityAbsolute(EntityPlayer player, double x, double y, double z) {
        double[] doubles = API.getGravityDirection(player).getInverseAdjustMentFromDOWNDirection().adjustXYZValues(x, y, z);
        player.moveEntity(doubles[0], doubles[1], doubles[2]);
    }

    //TODO: Insert Hook
    /**
     * ASM Hook used in EntityLivingBase::moveEntityWithHeading
     * This hook is useful when Mojang code stores an entity's Y position, runs code that may change it, gets the Y
     * position again and then does something with the difference between the starting and resultant values.
     * @param entity
     * @return
     */
    public static double getRelativePosY(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getDirection().adjustXYZValues(entity.posX, entity.posY, entity.posZ)[1];
        }
        else {
            return entity.posY;
        }
    }

    /**
     * ASM Hook used in EntityLivingBase::moveEntityWithHeading
     * @param entity
     * @return
     */
    public static BlockPos.PooledMutableBlockPos getBlockPosBelowEntity(Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            Vec3d origin = ((GravityAxisAlignedBB) entityBoundingBox).offset(0, -1, 0).getOrigin();
            return BlockPos.PooledMutableBlockPos.retain(origin.xCoord, origin.yCoord, origin.zCoord);
        }
        else {
            return BlockPos.PooledMutableBlockPos.retain(entity.posX, entity.getEntityBoundingBox().minY - 1.0D, entity.posZ);
        }
//        return BlockPos.PooledMutableBlockPos.retain(entity.posX, entity.getEntityBoundingBox().minY - 1.0D, entity.posZ);
    }

    /**
     * ASM Hook used in EntityLivingBase::moveEntityWithHeading
     * @param mutableBlockPos
     * @param entity
     * @return
     */
    public static BlockPos.PooledMutableBlockPos setPooledMutableBlockPosToBelowEntity(BlockPos.PooledMutableBlockPos mutableBlockPos, Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            Vec3d origin = ((GravityAxisAlignedBB) entityBoundingBox).offset(0, -1, 0).getOrigin();
            return mutableBlockPos.setPos(origin.xCoord, origin.yCoord, origin.zCoord);
        }
        else {
            return mutableBlockPos.setPos(entity.posX, entityBoundingBox.minY - 1.0D, entity.posZ);
        }
//        return mutableBlockPos.setPos(entity.posX, entity.getEntityBoundingBox().minY - 1.0D, entity.posZ);
    }

    //TODO: Find the method the hook is used in
    /**
     * ASM Hook used in EntityPlayerSP::?
     * @param playerWithGravity
     * @param bb
     */
    public static void pushEntityPlayerSPOutOfBlocks(EntityPlayerWithGravity playerWithGravity, AxisAlignedBB bb) {
        // Called from ASM-ed code where we do an INSTANCEOF check beforehand, so this cast is fine
        GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
        Vec3d origin = gBB.offset(-playerWithGravity.width * 0.35, 0.5, playerWithGravity.width * 0.35).getOrigin();
        playerWithGravity.pushOutOfBlocksDelegate(origin.xCoord, origin.yCoord, origin.zCoord);
        origin = gBB.offset(-playerWithGravity.width * 0.35, 0.5, -playerWithGravity.width * 0.35).getOrigin();
        playerWithGravity.pushOutOfBlocksDelegate(origin.xCoord, origin.yCoord, origin.zCoord);
        origin = gBB.offset(playerWithGravity.width * 0.35, 0.5, -playerWithGravity.width * 0.35).getOrigin();
        playerWithGravity.pushOutOfBlocksDelegate(origin.xCoord, origin.yCoord, origin.zCoord);
        origin = gBB.offset(playerWithGravity.width * 0.35, 0.5, playerWithGravity.width * 0.35).getOrigin();
        playerWithGravity.pushOutOfBlocksDelegate(origin.xCoord, origin.yCoord, origin.zCoord);
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * Some Mojang code will call calculateXOffset on an AxisAlignedBB with a GravityAxisAlignedBB argument. In some cases
     * we want the 'X' offset to be the relative 'X' direction of the GravityAxisAlignedBB argument. We can alter the code
     * so that calculateXOffset is called on the GravityAxisAlignedBB with the AxisAlignedBB as the argument instead, such
     * that the returned value is the relative 'X' offset instead. If the GravityAxisAlignedBB's direction is DOWN, the
     * value returned will be the same as if the original Mojang method was called
     * @param instance The AxisAlignedBB that is usually the instance
     * @param other The AxisAlignedBB that is usually one of the arguments for calculateYOffset, this will pretty much
     *              always be a GravityAxisAlignedBB
     * @param offset If offset is closer to zero than the X offset between the bounding boxes, then it is returned instead
     * @return The relative X offset between the two bounding boxes, or offset if it's closer to zero.
     */
    public static double reverseXOffset(AxisAlignedBB instance, AxisAlignedBB other, double offset) {
        //Normal behaviour in Mojang code:
//        return instance.calculateXOffset(other, offset);
        //Reverse behaviour: (returns the same result if both aaBB objects are members of the same class)
        return -other.calculateXOffset(instance, -offset);
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * Some Mojang code will call calculateYOffset on an AxisAlignedBB with a GravityAxisAlignedBB argument. In some cases
     * we want the 'Y' offset to be the relative 'Y' direction of the GravityAxisAlignedBB argument. We can alter the code
     * so that calculateYOffset is called on the GravityAxisAlignedBB with the AxisAlignedBB as the argument instead, such
     * that the returned value is the relative 'Y' offset instead. If the GravityAxisAlignedBB's direction is DOWN, the
     * value returned will be the same as if the original Mojang method was called
     * @param instance The AxisAlignedBB that is usually the instance
     * @param other The AxisAlignedBB that is usually one of the arguments for calculateYOffset, this will pretty much
     *              always be a GravityAxisAlignedBB
     * @param offset If offset is closer to zero than the Y offset between the bounding boxes, then it is returned instead
     * @return The relative Y offset between the two bounding boxes, or offset if it's closer to zero.
     */
    public static double reverseYOffset(AxisAlignedBB instance, AxisAlignedBB other, double offset) {
        //Normal behaviour in Mojang code:
//        return instance.calculateYOffset(other, offset);
        //Reverse behaviour: (returns the same result if both aaBB objects are members of the same class)
        return -other.calculateYOffset(instance, -offset);
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * Some Mojang code will call calculateXOffset on an AxisAlignedBB with a GravityAxisAlignedBB argument. In some cases
     * we want the 'Z' offset to be the relative 'Z' direction of the GravityAxisAlignedBB argument. We can alter the code
     * so that calculateXOffset is called on the GravityAxisAlignedBB with the AxisAlignedBB as the argument instead, such
     * that the returned value is the relative 'Z' offset instead. If the GravityAxisAlignedBB's direction is DOWN, the
     * value returned will be the same as if the original Mojang method was called
     * @param instance The AxisAlignedBB that is usually the instance
     * @param other The AxisAlignedBB that is usually one of the arguments for calculateYOffset, this will pretty much
     *              always be a GravityAxisAlignedBB
     * @param offset If offset is closer to zero than the Z offset between the bounding boxes, then it is returned instead
     * @return The relative Z offset between the two bounding boxes, or offset if it's closer to zero.
     */
    public static double reverseZOffset(AxisAlignedBB instance, AxisAlignedBB other, double offset) {
        //Normal behaviour in Mojang code:
//        return instance.calculateZOffset(other, offset);
        //Reverse behaviour: (returns the same result if both aaBB objects are members of the same class)
        return -other.calculateZOffset(instance, -offset);
    }

    @SideOnly(Side.CLIENT)
    public static boolean isHeadspaceFree(EntityPlayerSP playerSP, BlockPos pos, int height) {
        EnumGravityDirection gravityDirection = API.getGravityDirection(playerSP);
        for (int y = 0; y < height; y++)
        {
            double[] d = gravityDirection.adjustXYZValues(0, y, 0);
            if (!isOpenBlockSpace(playerSP, pos.add(d[0], d[1], d[2]))) return false;
        }
        return true;
    }

    //TODO: Access Transformer EntityPlayerSP::isOpenBlockSpace to public
    @SideOnly(Side.CLIENT)
    private static boolean isOpenBlockSpace(EntityPlayerSP playerSP, BlockPos pos) {
        return !playerSP.worldObj.getBlockState(pos).isNormalCube();
    }

    public static void makeMotionAbsolute(Entity entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity)entity).makeMotionAbsolute();
        }
    }

    public static void makeMotionRelative(Entity entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity)entity).makeMotionRelative();
        }
    }

    public static void setRelativeMotionX(Entity entity, double value) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)entityBoundingBox;
            EnumGravityDirection direction = gBB.getDirection();

            double[] doubles = direction.getInverseAdjustMentFromDOWNDirection().adjustXYZValues(entity.motionX, entity.motionY, entity.motionZ);

            //Relative motionX
            doubles[0] = value;
            doubles = direction.adjustXYZValues(doubles[0], doubles[1], doubles[2]);
            entity.motionX = doubles[0];
            entity.motionY = doubles[1];
            entity.motionZ = doubles[2];
        }
        else {
            entity.motionX = value;
        }
    }

    public static void setRelativeMotionY(Entity entity, double value) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)entityBoundingBox;
            EnumGravityDirection direction = gBB.getDirection();

            double[] doubles = direction.getInverseAdjustMentFromDOWNDirection().adjustXYZValues(entity.motionX, entity.motionY, entity.motionZ);

            //Relative motionY
            doubles[1] = value;
            doubles = direction.adjustXYZValues(doubles[0], doubles[1], doubles[2]);
            entity.motionX = doubles[0];
            entity.motionY = doubles[1];
            entity.motionZ = doubles[2];
        }
        else {
            entity.motionY = value;
        }
    }

    public static void setRelativeMotionZ(Entity entity, double value) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)entityBoundingBox;
            EnumGravityDirection direction = gBB.getDirection();

            double[] doubles = direction.getInverseAdjustMentFromDOWNDirection().adjustXYZValues(entity.motionX, entity.motionY, entity.motionZ);

            //Relative motionZ
            doubles[2] = value;
            doubles = direction.adjustXYZValues(doubles[0], doubles[1], doubles[2]);
            entity.motionX = doubles[0];
            entity.motionY = doubles[1];
            entity.motionZ = doubles[2];
        }
        else {
            entity.motionZ = value;
        }
    }

    public static double[] adjustXYZ(Entity entity, double x, double y, double z) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) entityBoundingBox).getDirection().adjustXYZValues(x, y, z);
        }
        else {
            return new double[]{x,y,z};
        }
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * @param entity
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[] inverseAdjustXYZ(Entity entity, double x, double y, double z) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) entityBoundingBox).getDirection().getInverseAdjustMentFromDOWNDirection().adjustXYZValues(x, y, z);
        }
        else {
            return new double[]{x,y,z};
        }
    }

    //TODO: Add to transformer
    /**
     * ASM Hook used in EntityLivingBase::moveEntityWithHeading
     * @param entity
     */
    public static void makePositionAbsolute(EntityLivingBase entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity)entity).makePositionAbsolute();
        }
    }

    //TODO: Add to transformer
    /**
     * ASM Hook used in EntityLivingBase::moveEntityWithHeading
     * @param entity
     */
    public static void makePositionRelative(EntityLivingBase entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity)entity).makePositionRelative();
        }
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * @param entity
     * @return
     */
    public static BlockPos getImmutableBlockPosBelowEntity(Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)entityBoundingBox;
            return new BlockPos(gBB.offset(0, -0.20000000298023224D, 0).getOrigin());
        }
        else {
            int x = MathHelper.floor_double(entity.posX);
            int y = MathHelper.floor_double(entity.posY - 0.20000000298023224D);
            int z = MathHelper.floor_double(entity.posZ);
            return new BlockPos(x, y, z);
        }
    }

    /**
     * ASM Hook used in Entity::moveEntity
     * @param entity
     * @param other
     * @return
     */
    public static BlockPos getRelativeDownBlockPos(BlockPos other, Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            switch (((GravityAxisAlignedBB)entityBoundingBox).getDirection()) {
                case UP:
                    return other.up();
                case DOWN:
                    return other.down();
                case SOUTH:
                    return other.south();
                case WEST:
                    return other.west();
                case NORTH:
                    return other.north();
//                case EAST:
                default:
                    return other.east();
            }
        }
        else {
            return other.down();
        }
    }

    //TODO: ASM: Insert into EntityLivingBase::updateFallState
    public static void spawnLandingParticles(EntityLivingBase entity, int i, IBlockState blockState) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            Vec3d origin = ((GravityAxisAlignedBB) entityBoundingBox).getOrigin();
            ((WorldServer) entity.worldObj).spawnParticle(EnumParticleTypes.BLOCK_DUST, origin.xCoord, origin.yCoord, origin.zCoord, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(blockState));
        }
        else {
            ((WorldServer) entity.worldObj).spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ, i, 0.0D, 0.0D, 0.0D, 0.15000000596046448D, Block.getStateId(blockState));
        }
    }

    /**
     * ASM Hook used in NetHandlerPlayServer::processPlayer
     * as a replacement for "this.playerEntity.handleFalling(this.playerEntity.posY - d3, packetIn.isOnGround());"
     * @param player
     * @param oldXPos
     * @param oldYPos
     * @param oldZPos
     * @return
     */
    public static double netHandlerPlayServerHandleFallingYChange(EntityPlayerMP player, double oldXPos, double oldYPos, double oldZPos) {
        AxisAlignedBB entityBoundingBox = player.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            EnumGravityDirection direction = ((GravityAxisAlignedBB) entityBoundingBox).getDirection().getInverseAdjustMentFromDOWNDirection();
            double[] relativePlayerPos = direction.adjustXYZValues(player.posX, player.posY, player.posZ);
            double[] relativeOldPos = direction.adjustXYZValues(oldXPos, oldYPos, oldZPos);
            return relativePlayerPos[1] - relativeOldPos[1];
        }
        else {
            return player.posY - oldYPos;
        }
    }


    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    // UNUSED HOOKS
    //
    //
    //
    //
    //
    //
    //
    //
    //
    static AxisAlignedBB getGravityAdjustedHitbox(EntityPlayer player, float width, float height) {
        return GravityDirectionCapability.getGravityDirection(player).getGravityAdjustedAABB(player, width, height);
    }

    static AxisAlignedBB getGravityAdjustedHitbox(EntityPlayer player) {
        return GravityDirectionCapability.getGravityDirection(player).getGravityAdjustedAABB(player, player.width, player.height);
    }

    static Vec3d adjustLook(EntityPlayer player, Vec3d normal) {
        return API.getGravityDirection(player).adjustLookVec(normal);
    }

    static AxisAlignedBB replaceWithGravityAware(EntityPlayer player, AxisAlignedBB old) {
        if (old instanceof GravityAxisAlignedBB) {
            return old;
        }
        else {
            return GravityDirectionCapability.newGravityAxisAligned(player, old);
        }
    }

    static Vec3d getPositionEyes(EntityPlayer player, float partialTicks) {
        EnumGravityDirection direction = API.getGravityDirection(player);
        double[] doubles = direction.adjustXYZValues(0, (double) API.getStandardEyeHeight(player), 0);
        if (partialTicks == 1.0F)
        {
            return new Vec3d(player.posX + doubles[0], player.posY + doubles[1], player.posZ + doubles[2]);
        }
        else
        {
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks + doubles[0];
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + doubles[1];
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks + doubles[2];
            return new Vec3d(d0, d1, d2);
        }
    }

    static boolean resetPositionToBB(EntityPlayer player) {
        AxisAlignedBB axisalignedbb = player.getEntityBoundingBox();
        if (axisalignedbb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB g_AxisAlignedBB = (GravityAxisAlignedBB)axisalignedbb;
//            float eyeHeight = player.getEyeHeight();
//            double[] doubles = g_AxisAlignedBB.getDirection().adjustXYZValues(0, (double) eyeHeight, 0);
            Vec3d vec3d = g_AxisAlignedBB.getOrigin();//.addVector(doubles[0], doubles[1], doubles[2]);
            EnumGravityDirection direction = g_AxisAlignedBB.getDirection();
            switch (direction) {
                case UP:
                case DOWN:
                    break;
                case SOUTH:
                case WEST:
                case NORTH:
                case EAST:
                    float eyeHeight = API.getStandardEyeHeight(player);
                    double[] doubles = direction.adjustXYZValues(0, (double)eyeHeight, 0);
                    vec3d = vec3d.addVector(doubles[0], doubles[1], doubles[2]);
                    break;
            }
            player.posX = vec3d.xCoord;
            player.posY = vec3d.yCoord;
            player.posZ = vec3d.zCoord;
            return true;
        }
        FMLLog.warning("Player BB is not GravityAxisAlignedBB");
        return false;
//        switch (API.getGravityDirection(player)) {
//            case DOWN:
//                player.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
//                player.posY = axisalignedbb.minY;
//                player.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
//                break;
//            case UP:
//                player.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
//                player.posY = axisalignedbb.maxY;
//                player.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
//                break;
//            case NORTH:
//                player.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
//                player.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
//                player.posZ = axisalignedbb.minZ;
//                break;
//            case EAST:
//                player.posX = axisalignedbb.maxX;
//                player.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
//                player.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
//                break;
//            case SOUTH:
//                player.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
//                player.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
//                player.posZ = axisalignedbb.maxZ;
//                break;
//            case WEST:
//                player.posX = axisalignedbb.minX;
//                player.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
//                player.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
//                break;
//
//        }
    }

//    static void updateSizePseudoOverride(EntityPlayer player) {
//        float f;
//        float f1;
//
//        if (player.isElytraFlying())
//        {
//            f = 0.6F;
//            f1 = 0.6F;
//        }
//        else if (player.isPlayerSleeping())
//        {
//            f = 0.2F;
//            f1 = 0.2F;
//        }
//        else if (player.isSneaking())
//        {
//            f = 0.6F;
//            f1 = 1.65F;
//        }
//        else
//        {
//            f = 0.6F;
//            f1 = 1.8F;
//        }
//
//        if (f != player.width || f1 != player.height)
//        {
//            AxisAlignedBB axisalignedbb = Hooks.getGravityAdjustedHitbox(player, f, f1);
//
//
//            if (!player.worldObj.collidesWithAnyBlock(axisalignedbb))
//            {
//                EntityPlayer_setSize_Handle.invoke(player, f, f1);
//                player.setSize(f, f1);
//            }
//        }
//        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPlayerPostTick(player);
//    }
}
