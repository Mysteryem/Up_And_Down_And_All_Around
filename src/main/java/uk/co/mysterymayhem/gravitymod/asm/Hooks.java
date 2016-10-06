package uk.co.mysterymayhem.gravitymod.asm;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.SoundManager;
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
import org.lwjgl.util.vector.Vector3f;
import paulscode.sound.SoundSystem;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;

/**
 * Created by Mysteryem on 2016-08-16.
 */
public class Hooks {

    public static void makeAnglesRelative(Entity entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity) entity).makeAngleVarsRelative();
        }
    }

    public static void makeAnglesAbsolute(Entity entity) {
        if (entity instanceof EntityPlayerWithGravity) {
            ((EntityPlayerWithGravity) entity).makeAngleVarsAbsolute();
        }
    }

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

    public static double getOriginPosX(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getOrigin().xCoord;
        }
        else {
            return entity.posX;
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param entity
     * @return
     */
    public static double getOriginRelativePosX(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            return gBB.getDirection().getInverseAdjustMentFromDOWNDirection().adjustLookVec(gBB.getOrigin()).xCoord;
        }
        else {
            return entity.posX;
        }
    }

    public static double getOriginPosY(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getOrigin().yCoord;
        }
        else {
            return entity.posY;
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param entity
     * @return
     */
    public static double getOriginRelativePosY(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            return gBB.getDirection().getInverseAdjustMentFromDOWNDirection().adjustLookVec(gBB.getOrigin()).yCoord;
        }
        else {
            return entity.posY;
        }
    }

    public static double getOriginPosZ(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getOrigin().zCoord;
        }
        else {
            return entity.posZ;
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param entity
     * @return
     */
    public static double getOriginRelativePosZ(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            return gBB.getDirection().getInverseAdjustMentFromDOWNDirection().adjustLookVec(gBB.getOrigin()).zCoord;
        }
        else {
            return entity.posZ;
        }
    }

    /**
     * Used in EntityPlayerSP::func_189810_i (auto-jump method)
     * @param entity
     * @return
     */
    public static double getRelativePosX(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getDirection().adjustXYZValues(entity.posX, entity.posY, entity.posZ)[0];
        }
        return entity.posX;
    }

    /**
     * Used in EntityPlayerSP::func_189810_i (auto-jump method)
     * @param entity
     * @return
     */
    public static double getRelativePosZ(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getDirection().adjustXYZValues(entity.posX, entity.posY, entity.posZ)[2];
        }
        return entity.posZ;
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


    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param normal
     * @param entity
     * @return
     */
    public static Vec3d adjustVec(Vec3d normal, Entity entity) {
        if (entity instanceof EntityPlayer) {
//            double x = entity.posX;
//            double y = entity.posY + entity.getEyeHeight();
//            double z = entity.posZ;

            Vec3d vec3d = API.getGravityDirection((EntityPlayer) entity).adjustLookVec(normal);

//            entity.worldObj.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, normal.xCoord, normal.yCoord, normal.zCoord, 0, 0, 0);
//            entity.worldObj.spawnParticle(EnumParticleTypes.END_ROD, vec3d.xCoord, vec3d.yCoord, vec3d.zCoord, 0, 0, 0);
            return vec3d;
        }
        else {
            return normal;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void runAutoJump(double oldRelativeX, double oldRelativeZ, EntityPlayerWithGravity player) {
        double[] doubles = Hooks.inverseAdjustXYZ(player, player.posX, player.posY, player.posZ);
        player.func_189810_i((float)(doubles[0] - oldRelativeX), (float)(doubles[2] - oldRelativeZ));
    }

    public static Vec3d inverseAdjustVec(Vec3d normal, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            EnumGravityDirection direction = ((GravityAxisAlignedBB) bb).getDirection().getInverseAdjustMentFromDOWNDirection();
            normal = direction.adjustLookVec(normal);
            return new Vec3d(normal.xCoord, normal.yCoord, normal.zCoord);
        }
        else {
            return normal;
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param entity
     * @return
     */
    public static BlockPos getBlockPosAtTopOfPlayer(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return new BlockPos(((GravityAxisAlignedBB)bb).offset(0, entity.height, 0).getOrigin());
        }
        else {
            return new BlockPos(entity.posX, bb.maxY, entity.posZ);
        }
    }

    //TODO: If we work out which two input x, y and z values are needed in lookVecWithDoubleAccuracy, we can skip calculating one of the doubles
    /**
     * ASM Hook used in
     * BlockChest::onBlockPlacedBy (Mojang. Ahem. Entity::getHorizontalFacing.),
     * BlockCocoa::onBlockPlacedBy
     * BlockFenceGate::onBlockActivated
     * SoundManager::setListener
     * ParticleManager::renderLitParticles
     * @param entity
     * @return
     */
    public static float getAdjustedYaw(Entity entity) {
//        return entity.rotationYaw;
        final double yaw = entity.rotationYaw;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
//        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static final int YAW = 0;
    public static final int PITCH = 1;

    public static double[] getRelativeYawAndPitch(double yawIn, double pitchIn, Entity entity) {

        double f = Math.cos(-yawIn * (Math.PI / 180d) - Math.PI);
        double f1 = Math.sin(-yawIn * (Math.PI / 180d) - Math.PI);
        double f2 = -Math.cos(-pitchIn * (Math.PI/180d));
        double f3 = Math.sin(-pitchIn * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        double yawOut = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
        double pitchOut = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
        return new double[]{yawOut, pitchOut};
    }

    public static double[] getAbsoluteYawAndPitch(double yawIn, double pitchIn, Entity entity) {

        double f = Math.cos(-yawIn * (Math.PI / 180d) - Math.PI);
        double f1 = Math.sin(-yawIn * (Math.PI / 180d) - Math.PI);
        double f2 = -Math.cos(-pitchIn * (Math.PI/180d));
        double f3 = Math.sin(-pitchIn * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        double yawOut = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
        double pitchOut = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
        return new double[]{yawOut, pitchOut};
    }

    public static Vec3d getRelativeLookVec(Entity entity) {
        Vec3d vec3d = Hooks.inverseAdjustVec(entity.getLookVec(), entity);

//        Vec3d lookpos = vec3d.addVector(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
//        entity.worldObj.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, lookpos.xCoord, lookpos.yCoord, lookpos.zCoord, 0, 0, 0);
        return vec3d;
    }

    /**
     *
     * @param entity
     * @return
     */
    public static float getRelativeYaw(Entity entity) {
//        return entity.rotationYaw;
        final double yaw = entity.rotationYaw;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static float getRelativeYawHead(EntityLivingBase entity) {
//        return entity.rotationYaw;
        final double yaw = entity.rotationYawHead;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static float getPrevRelativeYawHead(EntityLivingBase entity) {
//        return entity.rotationYaw;
        final double yaw = entity.prevRotationYawHead;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static float getAbsoluteYaw(Entity entity) {
//        return entity.rotationYaw;
        final double yaw = entity.rotationYaw;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
//        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static float getRelativePitch(Entity entity) {
//        return entity.rotationPitch;
        final double yaw = entity.rotationYaw;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
    }

    //TODO: If we work out which input x, y and z value is needed in lookVecWithDoubleAccuracy, we can skip calculating two of the doubles
    /**
     * AMS Hook used in
     * ParticleManager::renderLitParticles
     * @param entity
     * @return
     */
    public static float getAdjustedPitch(Entity entity) {
//        return entity.rotationPitch;
        final double yaw = entity.rotationYaw;
        final double pitch = entity.rotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
//        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
    }

    /**
     * ASM Hook use in
     * ParticleManager::renderLitParticles
     * @param entity
     * @return
     */
    public static float getAdjustedPrevYaw(Entity entity) {
//        return entity.prevRotationYaw;
        final double yaw = entity.prevRotationYaw;
        final double pitch = entity.prevRotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
//        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    public static float getRelativePrevYaw(Entity entity) {
//        return entity.prevRotationYaw;
        final double yaw = entity.prevRotationYaw;
        final double pitch = entity.prevRotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
    }

    //TODO: If we work out which input x, y and z value is needed in lookVecWithDoubleAccuracy, we can skip calculating two of the doubles
    /**
     * AMS Hook used in
     * ParticleManager::renderLitParticles
     * @param entity
     * @return
     */
    public static float getAdjustedPrevPitch(Entity entity) {
//        return entity.prevRotationPitch;
        final double yaw = entity.prevRotationYaw;
        final double pitch = entity.prevRotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
//        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
    }

    public static float getRelativePrevPitch(Entity entity) {
//        return entity.prevRotationPitch;
        final double yaw = entity.prevRotationYaw;
        final double pitch = entity.prevRotationPitch;

        final double f = Math.cos(-yaw * (Math.PI / 180d) - Math.PI);
        final double f1 = Math.sin(-yaw * (Math.PI / 180d) - Math.PI);
        final double f2 = -Math.cos(-pitch * (Math.PI/180d));
        final double f3 = Math.sin(-pitch * (Math.PI/180d));

        Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

//        Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, entity);
        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, entity);
        return (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
    }

    public static float getCosOfAngleBetweenVecsOnRelativeXYPlane(Entity entity, Vec3d normal1, Vec3d normal2) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            EnumGravityDirection direction = ((GravityAxisAlignedBB) bb).getDirection().getInverseAdjustMentFromDOWNDirection();
            normal1 = direction.adjustLookVec(normal1);
            normal2 = direction.adjustLookVec(normal2);
        }
        return (float)(normal1.xCoord * normal2.xCoord + normal1.zCoord * normal2.zCoord);
    }

    //TODO: Where was the other place this is used?
    /**
     * ASM Hook used in EntityRenderer::orientCamera and somewhere else
     * @param entity
     * @param x
     * @param y
     * @param z
     * @return
     */
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

    public static Vec3d getNonGAffectedLook(Entity entity, float partialticks) {
        return entity.getLook(partialticks);
//        if (entity instanceof EntityPlayerWithGravity) {
//            return ((EntityPlayerWithGravity) entity).getSuperLook(partialticks);
//        }
//        else {
//            return entity.getLook(partialticks);
//        }
    }

    /**
     * ASM Hook used in RenderGlobal::getViewVector, I think this might have something to do with terrain culling, not sure
     * @param normal
     * @param entity
     * @return
     */
    @SideOnly(Side.CLIENT)
    public static Vector3f adjustViewVector(Vector3f normal, Entity entity) {
        double[] doubles = Hooks.adjustXYZ(entity, normal.getX(), normal.getY(), normal.getZ());

        return new Vector3f((float)doubles[0], (float)doubles[1], (float)doubles[2]);
    }

    /**
     * ASM Hook, to possibly be used in Particle:renderParticle
     * @param vecs
     * @param entity
     * @return
     */
    @SideOnly(Side.CLIENT)
    public static Vec3d[] adjustVecs(Vec3d[] vecs, Entity entity) {
        if (entity instanceof EntityPlayer) {
            EnumGravityDirection direction = API.getGravityDirection((EntityPlayer) entity);
            if (direction != EnumGravityDirection.DOWN) {
                for (int i = 0; i < vecs.length; i++) {
                    vecs[i] = direction.adjustLookVec(vecs[i]);
                }
            }
        }
        return vecs;
    }

    /**
     * ASM Hook used in SoundManager::setListener
     * @param soundSystem
     * @param lookX
     * @param lookY
     * @param lookZ
     * @param upX
     * @param upY
     * @param upZ
     * @param player
     */
    @SideOnly(Side.CLIENT)
    public static void setListenerOrientationHook(SoundSystem soundSystem, float lookX, float lookY, float lookZ, float upX, float upY, float upZ, EntityPlayer player) {
        EnumGravityDirection gravityDirection = API.getGravityDirection(player);
        double[] d = gravityDirection.adjustXYZValues(lookX, lookY, lookZ);
        double[] d1 = gravityDirection.adjustXYZValues(upX, upY, upZ);

        soundSystem.setListenerOrientation((float)d[0], (float)d[1], (float)d[2], (float)d1[0], (float)d1[1], (float)d1[2]);
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

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i (auto-jump method)
     * @param other
     * @param entity
     * @return
     */
    public static BlockPos getRelativeUpBlockPos(BlockPos other, Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            switch (((GravityAxisAlignedBB)entityBoundingBox).getDirection()) {
                case UP:
                    return other.down();
                case DOWN:
                    return other.up();
                case SOUTH:
                    return other.north();
                case WEST:
                    return other.east();
                case NORTH:
                    return other.south();
//                case EAST:
                default:
                    return other.west();
            }
        }
        else {
            return other.up();
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param other
     * @param entity
     * @return
     */
    public static int getRelativeYOfBlockPos(BlockPos other, Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            switch (((GravityAxisAlignedBB)entityBoundingBox).getDirection()) {
                case UP:
                    return -other.getY();
                case DOWN:
                    return other.getY();
                case SOUTH:
                    return -other.getZ();
                case WEST:
                    return other.getX();
                case NORTH:
                    return other.getZ();
//                case EAST:
                default:
                    return -other.getX();
            }
        } else {
            return other.getY();
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param other
     * @param count
     * @param entity
     * @return
     */
    public static BlockPos getRelativeUpBlockPos(BlockPos other, int count, Entity entity) {
        AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
        if (entityBoundingBox instanceof GravityAxisAlignedBB) {
            switch (((GravityAxisAlignedBB)entityBoundingBox).getDirection()) {
                case UP:
                    return other.down(count);
                case DOWN:
                    return other.up(count);
                case SOUTH:
                    return other.north(count);
                case WEST:
                    return other.east(count);
                case NORTH:
                    return other.south(count);
//                case EAST:
                default:
                    return other.west(count);
            }
        }
        else {
            return other.up();
        }
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param first
     * @param second
     * @param entity
     * @return
     */
    public static AxisAlignedBB constructNewGAABBFrom2Vec3d(Vec3d first, Vec3d second, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gbb = (GravityAxisAlignedBB)bb;
            return new GravityAxisAlignedBB(gbb, first, second);
        }
        return new AxisAlignedBB(first, second);
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param vecIn
     * @param xIn
     * @param yIn
     * @param zIn
     * @param entity
     * @return
     */
    public static Vec3d addAdjustedVector(Vec3d vecIn, double xIn, double yIn, double zIn, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            double[] d = ((GravityAxisAlignedBB) bb).getDirection().adjustXYZValues(xIn, yIn, zIn);
            return vecIn.addVector(d[0], d[1], d[2]);
        }
        return vecIn.addVector(xIn, yIn, zIn);
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param other
     * @param entity
     * @return
     */
    public static double getRelativeTopOfBB(AxisAlignedBB other, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            switch(((GravityAxisAlignedBB) bb).getDirection()){
                case UP:
                    return -other.minY;
                case DOWN:
                    return other.maxY;
                case SOUTH:
                    return -other.minZ;
                case WEST:
                    return other.maxX;
                case NORTH:
                    return other.maxZ;
//                case EAST:
                default:
                    return -other.minX;
            }
        }
        return other.maxY;
    }

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param other
     * @param entity
     * @return
     */
    public static double getRelativeBottomOfBB(AxisAlignedBB other, Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            switch(((GravityAxisAlignedBB) bb).getDirection()){
                case UP:
                    return -other.maxY;
                case DOWN:
                    return other.minY;
                case SOUTH:
                    return -other.maxZ;
                case WEST:
                    return other.minX;
                case NORTH:
                    return other.minZ;
//                case EAST:
                default:
                    return -other.maxX;
            }
        }
        return other.minY;
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

    /**
     * ASM Hook used in EntityPlayerSP::func_189810_i
     * @param entity
     * @return
     */
    public static Vec3d getBottomOfEntity(Entity entity) {
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) bb).getOrigin();
        }
        else {
            return new Vec3d(entity.posX, entity.posY, entity.posZ);
        }
    }

    public static AxisAlignedBB getVanillaEntityBoundingBox(Entity entity) {
        AxisAlignedBB aabb = entity.getEntityBoundingBox();
        if (aabb instanceof GravityAxisAlignedBB) {
            return ((GravityAxisAlignedBB) aabb).toVanilla();
        }
        else {
            return aabb;
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
//        EnumGravityDirection direction = API.getGravityDirection(player);
//        double[] doubles = direction.adjustXYZValues(0, API.getStandardEyeHeight(player), 0);
        if (partialTicks == 1.0F)
        {
            return new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        }
        else
        {
            double d0 = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;// + doubles[0];
            double d1 = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks + player.getEyeHeight();// + doubles[1];
            double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;// + doubles[2];
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
