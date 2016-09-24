package uk.co.mysterymayhem.gravitymod.asm;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;
import uk.co.mysterymayhem.gravitymod.util.reflection.LookupThief;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Created by Mysteryem on 2016-09-04.
 */
public abstract class EntityPlayerWithGravity extends EntityPlayer {

    // TODO: Isn't all motion relative by default? That's part of the reasoning behind GravityAxisAlignedBB right?
    private boolean motionVarsAreRelative;

    // Relative position doesn't exist, but this allows adjustments made in other methods such as 'this.posY+=3',
    // to correspond to whatever we think 'UP' is
    private boolean positionVarsAreRelative;

    private boolean angleVarsAreRelative;

    public EntityPlayerWithGravity(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);

        this.motionVarsAreRelative = false;
        this.positionVarsAreRelative = false;
        this.angleVarsAreRelative = false;
    }

    void makeMotionRelative() {
//        if (!this.motionVarsAreRelative) {
//            double[] doubles = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection().adjustXYZValues(this.motionX, this.motionY, this.motionZ);
//            this.motionVarsAreRelative = true;
//            this.motionX = doubles[0];
//            this.motionY = doubles[1];
//            this.motionZ = doubles[2];
//        }
    }

    void makeMotionAbsolute() {
//        if (this.motionVarsAreRelative) {
//            double[] doubles = API.getGravityDirection(this).adjustXYZValues(this.motionX, this.motionY, this.motionZ);
//            this.motionVarsAreRelative = false;
//            this.motionX = doubles[0];
//            this.motionY = doubles[1];
//            this.motionZ = doubles[2];
//        }
    }

    void makePositionRelative() {
        if (!this.positionVarsAreRelative) {
            EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();

            double[] doubles = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
            this.posX = doubles[0];
            this.posY = doubles[1];
            this.posZ = doubles[2];

            doubles = direction.adjustXYZValues(this.prevPosX, this.prevPosY, this.prevPosZ);
            this.prevPosX = doubles[0];
            this.prevPosY = doubles[1];
            this.prevPosZ = doubles[2];

            this.positionVarsAreRelative = true;
        }
    }

    void makePositionAbsolute() {
        if (this.positionVarsAreRelative) {
            EnumGravityDirection direction = API.getGravityDirection(this);

            double[] doubles = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
            this.posX = doubles[0];
            this.posY = doubles[1];
            this.posZ = doubles[2];

            doubles = direction.adjustXYZValues(this.prevPosX, this.prevPosY, this.prevPosZ);
            this.prevPosX = doubles[0];
            this.prevPosY = doubles[1];
            this.prevPosZ = doubles[2];

            this.positionVarsAreRelative = false;
        }
    }

//
//    /**
//     *
//     * @param vec
//     * @return yaw, then pitch
//     */
//    float[] getRotationAndPitchFromVec(Vec3d vec) {
//        Vec3d eyePosition = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
//        Vec3d target = eyePosition.add(vec);
//
////        double d0 = this.posX - this.entity.posX;
////        double d0 = eyePosition.xCoord - target.xCoord;
//        double d0 = vec.xCoord;
////        double d1 = this.posY - (this.entity.posY + (double) this.entity.getEyeHeight());
////        double d1 = eyePosition.yCoord - target.yCoord;
//        double d1 = vec.yCoord;
////        double d2 = this.posZ - this.entity.posZ;
////        double d2 = eyePosition.zCoord - target.zCoord;
//        double d2 = vec.zCoord;
//        double d3 = (double) MathHelper.sqrt_double(d0 * d0 + d2 * d2);
//        float yaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
//        float pitch = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
//        return new float[]{yaw, pitch};
////        this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, f1, this.deltaLookPitch);
////        this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, f, this.deltaLookYaw);
//    }
//


//    @Override
//    public Vec3d getLook(float partialTicks) {
////        Vec3d vectorForRotation;
////        if (partialTicks == 1.0F) {
////            vectorForRotation = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
//////            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
////        } else {
////            float interpolatedRotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
////            float interpolatedRotationYaw = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
////            vectorForRotation = this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
//////            return this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
////
////        }
////        return this.getGravityDirection().adjustLookVec(vectorForRotation);
//        return Hooks.adjustLook(this, super.getLook(partialTicks));
//    }
//
    @Override
    public Vec3d getLook(float partialTicks) {
        Vec3d vectorForRotation;
        if (partialTicks == 1.0F) {
            vectorForRotation = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
    //            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
        } else {
            float interpolatedRotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
            float interpolatedRotationYaw = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
            vectorForRotation = this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
    //            return this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);

        }
        return API.getGravityDirection(this).adjustLookVec(vectorForRotation);
    }

    //    void makeAnglesRelative() {
//        if (!this.angleVarsAreRelative) {
//            EnumGravityDirection gravityDirection = API.getGravityDirection(this);
//            Vec3d headVec = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
//            Vec3d bodyVec = this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
//            Vec3d prevHeadVec = this.getVectorForRotation(this.prevRotationPitch, this.prevRotationYawHead);
//            Vec3d prevBodyVec = this.getVectorForRotation(this.prevRotationPitch, this.prevRotationYaw);
//            headVec = gravityDirection.adjustLookVec(headVec);
//            bodyVec = gravityDirection.adjustLookVec(bodyVec);
//            prevHeadVec = gravityDirection.adjustLookVec(prevHeadVec);
//            prevBodyVec = gravityDirection.adjustLookVec(prevBodyVec);
//            float[] headVars = getRotationAndPitchFromVec(headVec);
//            float[] bodyVars = getRotationAndPitchFromVec(bodyVec);
//            float[] prevHeadVars = getRotationAndPitchFromVec(prevHeadVec);
//            float[] prevBodyVars = getRotationAndPitchFromVec(prevBodyVec);
//            this.rotationPitch = bodyVars[1];
//            this.rotationYaw = bodyVars[0];
//            this.rotationYawHead = headVars[0];
//            this.prevRotationPitch = prevBodyVars[1];
//            this.prevRotationYaw = prevBodyVars[0];
//            this.prevRotationYawHead = prevHeadVars[0];
//            this.angleVarsAreRelative = true;
//        }
//    }
//
//    void makeAnglesAbsolute() {
//        if (this.angleVarsAreRelative) {
//            EnumGravityDirection gravityDirection = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();
//            Vec3d headVec = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
//            Vec3d bodyVec = this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
//            Vec3d prevHeadVec = this.getVectorForRotation(this.prevRotationPitch, this.prevRotationYawHead);
//            Vec3d prevBodyVec = this.getVectorForRotation(this.prevRotationPitch, this.prevRotationYaw);
//            headVec = gravityDirection.adjustLookVec(headVec);
//            bodyVec = gravityDirection.adjustLookVec(bodyVec);
//            prevHeadVec = gravityDirection.adjustLookVec(prevHeadVec);
//            prevBodyVec = gravityDirection.adjustLookVec(prevBodyVec);
//            float[] headVars = getRotationAndPitchFromVec(headVec);
//            float[] bodyVars = getRotationAndPitchFromVec(bodyVec);
//            float[] prevHeadVars = getRotationAndPitchFromVec(prevHeadVec);
//            float[] prevBodyVars = getRotationAndPitchFromVec(prevBodyVec);
//            this.rotationPitch = bodyVars[1];
//            this.rotationYaw = bodyVars[0];
//            this.rotationYawHead = headVars[0];
//            this.prevRotationPitch = prevBodyVars[1];
//            this.prevRotationYaw = prevBodyVars[0];
//            this.prevRotationYawHead = prevHeadVars[0];
//            this.angleVarsAreRelative = false;
//        }
//    }

//    @Override
//    public void moveEntityWithHeading(float strafe, float forward) {
////        this.makeRotationRelative();
//        super.moveEntityWithHeading(strafe, forward);
////        this.makeRotationAbsolute();
//    }
//
//    private int relCount = 0;
//    private int absCount = 0;
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void setAngles(float yaw, float pitch) {
//        this.makeRotationRelative();
//        super.setAngles(yaw, pitch);
//        this.makeRotationAbsolute();
//    }
//     void rotateAndBack() {
//         if (this.worldObj.isRemote) {
//             FMLLog.info("-----Starting-----");
//             FMLLog.info("Making relative");
//         }
//         this.makeRotationRelative();
//         if (this.worldObj.isRemote) {
//             FMLLog.info("Making absolute");
//         }
//         this.makeRotationAbsolute();
//         if (this.worldObj.isRemote) {
//             FMLLog.info("=======Done=======");
//         }
//     }
//
//    void makeRotationRelative() {
//        if (!this.angleVarsAreRelative) {
//            if (this.worldObj.isRemote) {
//                FMLLog.info("MakeREL, Absolute: (" + this.rotationPitch + ", " + this.prevRotationPitch + "), (" + this.rotationYaw + ", " + this.prevRotationYaw + "), (" + this.rotationYawHead + ", " + this.prevRotationYawHead + ")");
//            }
//            EnumGravityDirection gravityDirection = API.getGravityDirection(this);
////            if (gravityDirection == EnumGravityDirection.DOWN) {
////                this.motionVarsAreRelative = true;
////                return;
////            }
//            Vec3d headVec = this.getPreciseVectorForRotation(this.rotationPitch, this.rotationYawHead);
//            Vec3d bodyVec = this.getPreciseVectorForRotation(this.rotationPitch, this.rotationYaw);
//            Vec3d prevHeadVec = this.getPreciseVectorForRotation(this.prevRotationPitch, this.prevRotationYawHead);
//            Vec3d prevBodyVec = this.getPreciseVectorForRotation(this.prevRotationPitch, this.prevRotationYaw);
//            headVec = gravityDirection.adjustLookVec(headVec);
//            bodyVec = gravityDirection.adjustLookVec(bodyVec);
//            prevHeadVec = gravityDirection.adjustLookVec(prevHeadVec);
//            prevBodyVec = gravityDirection.adjustLookVec(prevBodyVec);
//            float[] headVars = getPitchAndYawFromVec(headVec);
//            float[] bodyVars = getPitchAndYawFromVec(bodyVec);
//            float[] prevHeadVars = getPitchAndYawFromVec(prevHeadVec);
//            float[] prevBodyVars = getPitchAndYawFromVec(prevBodyVec);
//            this.rotationPitch = bodyVars[0];
//            this.rotationYaw = bodyVars[1];
//            this.rotationYawHead = headVars[1];
//            this.prevRotationPitch = prevBodyVars[0];
//            this.prevRotationYaw = prevBodyVars[1];
//            this.prevRotationYawHead = prevHeadVars[1];
//            this.angleVarsAreRelative = true;
//            if (this.worldObj.isRemote) {
//                FMLLog.info("MakeREL, Relative: (" + this.rotationPitch + ", " + this.prevRotationPitch + "), (" + this.rotationYaw + ", " + this.prevRotationYaw + "), (" + this.rotationYawHead + ", " + this.prevRotationYawHead + ")");
//            }
//        }
//    }
//
//    void makeRotationAbsolute() {
//        if (this.angleVarsAreRelative) {
//            if (this.worldObj.isRemote) {
//                FMLLog.info("MakeABS, Relative: (" + this.rotationPitch + ", " + this.prevRotationPitch + "), (" + this.rotationYaw + ", " + this.prevRotationYaw + "), (" + this.rotationYawHead + ", " + this.prevRotationYawHead + ")");
//            }
//            EnumGravityDirection gravityDirection = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();
////            if (gravityDirection == EnumGravityDirection.DOWN) {
////                this.motionVarsAreRelative = true;
////                return;
////            }
//            Vec3d headVec = this.getPreciseVectorForRotation(this.rotationPitch, this.rotationYawHead);
//            Vec3d bodyVec = this.getPreciseVectorForRotation(this.rotationPitch, this.rotationYaw);
//            Vec3d prevHeadVec = this.getPreciseVectorForRotation(this.prevRotationPitch, this.prevRotationYawHead);
//            Vec3d prevBodyVec = this.getPreciseVectorForRotation(this.prevRotationPitch, this.prevRotationYaw);
//            headVec = gravityDirection.adjustLookVec(headVec);
//            bodyVec = gravityDirection.adjustLookVec(bodyVec);
//            prevHeadVec = gravityDirection.adjustLookVec(prevHeadVec);
//            prevBodyVec = gravityDirection.adjustLookVec(prevBodyVec);
//            float[] headVars = getPitchAndYawFromVec(headVec);
//            float[] bodyVars = getPitchAndYawFromVec(bodyVec);
//            float[] prevHeadVars = getPitchAndYawFromVec(prevHeadVec);
//            float[] prevBodyVars = getPitchAndYawFromVec(prevBodyVec);
//            this.rotationPitch = bodyVars[0];
//            this.rotationYaw = bodyVars[1];
//            this.rotationYawHead = headVars[1];
//            this.prevRotationPitch = prevBodyVars[0];
//            this.prevRotationYaw = prevBodyVars[1];
//            this.prevRotationYawHead = prevHeadVars[1];
//            this.angleVarsAreRelative = false;
//            if (this.worldObj.isRemote) {
//                FMLLog.info("MakeABS, Absolute: (" + this.rotationPitch + ", " + this.prevRotationPitch + "), (" + this.rotationYaw + ", " + this.prevRotationYaw + "), (" + this.rotationYawHead + ", " + this.prevRotationYawHead + ")");
//            }
//        }
//    }
//
//    private static final double ONE_HUNDRED_EIGHTY_OVER_PI = 180D/Math.PI;
//    private static final double FRAC_BIAS;
//    private static final double[] ASINE_TAB;
//
//    static {
//        MethodHandles.Lookup lookup = LookupThief.INSTANCE.lookup(MathHelper.class);
//        MethodHandle getFRAC_BIAS;
//        MethodHandle getASINE_TAB;
//        try {
//            try {
//                getFRAC_BIAS = lookup.findStaticGetter(MathHelper.class, "FRAC_BIAS", double.class);
//            } catch (NoSuchFieldException e) {
//                getFRAC_BIAS = lookup.findStaticGetter(MathHelper.class, "field_181163_d", double.class);
//            }
//            try {
//                getASINE_TAB = lookup.findStaticGetter(MathHelper.class, "ASINE_TAB", double[].class);
//            } catch (NoSuchFieldException e) {
//                getASINE_TAB = lookup.findStaticGetter(MathHelper.class, "field_181164_e", double[].class);
//            }
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            FRAC_BIAS = (double)getFRAC_BIAS.invokeExact();
//            ASINE_TAB = (double[])getASINE_TAB.invokeExact();
//        } catch (Throwable throwable) {
//            throw new RuntimeException(throwable);
//        }
//    }
//
//
//    private static final double PI_OVER_180 = Math.PI/180D;
//
//    Vec3d getPreciseVectorForRotation(float pitch, float yaw) {
//        double f = Math.cos(-yaw * PI_OVER_180 - Math.PI);
//        double f1 = Math.sin(-yaw * PI_OVER_180 - Math.PI);
//        double f2 = -Math.cos(-pitch * PI_OVER_180);
//        double f3 = Math.sin(-pitch * PI_OVER_180);
//        return new Vec3d((f1 * f2), f3, (f * f2));
//    }
//
//    private static double fastASin(double value) {
//        return Math.asin(value);
////        if (value < 0) {
////            return -ASINE_TAB[(int)Double.doubleToRawLongBits(-value + FRAC_BIAS)];
////        }
////        return ASINE_TAB[(int)Double.doubleToRawLongBits(value + FRAC_BIAS)];
//    }
//
//    private static float[] getPitchAndYawFromVec(Vec3d vec3d) {
//        double pitch = -(fastASin(vec3d.yCoord) * ONE_HUNDRED_EIGHTY_OVER_PI);
//        double yaw = Math.atan2(-vec3d.xCoord, vec3d.zCoord) * ONE_HUNDRED_EIGHTY_OVER_PI;
//        return new float[]{(float)pitch, (float)yaw};
//    }
//
//    private static float[] getPitchAndYawFromVecSafe(Vec3d vec3d) {
//        return getPitchAndYawFromVec(vec3d.normalize());
//    }

    //    /**
//     //     * Adds 15% to the entity's yaw and subtracts 15% from the pitch. Clamps pitch from -90 to 90. Both arguments in
//     //     * degrees.
//     //     */
////    @SideOnly(Side.CLIENT)
////    public void setAngles(float yaw, float pitch)
////    {
////        //make angles relative
////        this.isRelative();
////        super.setAngles(yaw, pitch);
////        this.makeAnglesAbsolute();
////        //make angles absolute
////    }

    @Override
    public void setEntityBoundingBox(AxisAlignedBB bb) {
        bb = Hooks.replaceWithGravityAware(this, bb);
        super.setEntityBoundingBox(bb);
    }

    //TODO: ASM the EntityPlayerMP class instead
    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        if (!this.worldObj.isRemote) {

            BlockPos blockpos;

            AxisAlignedBB offsetAABB = this.getEntityBoundingBox().offset(0, -0.20000000298023224D, 0);
            if (offsetAABB instanceof GravityAxisAlignedBB) {
                GravityAxisAlignedBB offsetAABB_g = (GravityAxisAlignedBB)offsetAABB;
                Vec3d origin = offsetAABB_g.getOrigin();
                blockpos = new BlockPos(origin);
            }
            else {
                // Fallback
                int i, j, k;
                i = MathHelper.floor_double(this.posX);
                j = MathHelper.floor_double(this.posY - 0.20000000298023224D);
                k = MathHelper.floor_double(this.posZ);
                blockpos = new BlockPos(i, j, k);
                FMLLog.warning("Player bounding box is not gravity aware. In [UpAndDownAndAllAround]:EntityPlayerWithGravity::updateFallState");
            }

            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            if (iblockstate.getBlock().isAir(iblockstate, this.worldObj, blockpos))
            {
                BlockPos blockpos1;
                switch(API.getGravityDirection(this)) {
                    case UP:
                        blockpos1 = blockpos.up();
                        break;
                    case DOWN:
                        blockpos1 = blockpos.down();
                        break;
                    case SOUTH:
                        blockpos1 = blockpos.south();
                        break;
                    case WEST:
                        blockpos1 = blockpos.west();
                        break;
                    case NORTH:
                        blockpos1 = blockpos.north();
                        break;
//                    case EAST:
                    default:
                        blockpos1 = blockpos.east();
                        break;
                }

                IBlockState iblockstate1 = this.worldObj.getBlockState(blockpos1);
                Block block = iblockstate1.getBlock();

                if (block instanceof BlockFence || block instanceof BlockWall || block instanceof BlockFenceGate)
                {
                    blockpos = blockpos1;
                    iblockstate = iblockstate1;
                }
            }

            super.updateFallState(y, onGroundIn, iblockstate, blockpos);
        }
        else {
            super.updateFallState(y, onGroundIn, state, pos);
        }
    }

    //TODO: ASM?
    @Override
    public boolean isEntityInsideOpaqueBlock()
    {
        if (this.noClip) {
            return false;
        }
        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            EnumGravityDirection direction = gBB.getDirection();
            Vec3d origin = gBB.getOrigin();

            BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

            for (int i = 0; i < 8; ++i)
            {
                double yMovement = (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)API.getStandardEyeHeight(this);
                double xMovement = (double)(((float)((i >> 1) % 2) - 0.5F) * this.width * 0.8F);
                double zMovement = (double)(((float)((i >> 2) % 2) - 0.5F) * this.width * 0.8F);
                double[] d = direction.adjustXYZValues(xMovement, yMovement, zMovement);

                int j = MathHelper.floor_double(origin.yCoord + d[1]);
                int k = MathHelper.floor_double(origin.xCoord + d[0]);
                int l = MathHelper.floor_double(origin.zCoord + d[2]);

                if (blockpos$pooledmutableblockpos.getX() != k || blockpos$pooledmutableblockpos.getY() != j || blockpos$pooledmutableblockpos.getZ() != l)
                {
                    blockpos$pooledmutableblockpos.setPos(k, j, l);

                    if (this.worldObj.getBlockState(blockpos$pooledmutableblockpos).getBlock().isVisuallyOpaque())
                    {
                        blockpos$pooledmutableblockpos.release();
                        return true;
                    }
                }
            }

            blockpos$pooledmutableblockpos.release();
            return false;

        }
        else {
            return super.isEntityInsideOpaqueBlock();
        }
//        this.makePositionRelative();
//        boolean entityInsideOpaqueBlock = super.isEntityInsideOpaqueBlock();
//        this.makePositionAbsolute();
//        return entityInsideOpaqueBlock;
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox()
    {
        AxisAlignedBB entityBoundingBox = super.getEntityBoundingBox();
        if (!(entityBoundingBox instanceof GravityAxisAlignedBB)) {
            entityBoundingBox = Hooks.replaceWithGravityAware(this, entityBoundingBox);
            this.setEntityBoundingBox(entityBoundingBox);
        }
        return entityBoundingBox;
    }
//
//
//
//    @Override
//    protected void updateSize()
//    {
//        float f;
//        float f1;
//
//        if (this.isElytraFlying())
//        {
//            f = 0.6F;
//            f1 = 0.6F;
//        }
//        else if (this.isPlayerSleeping())
//        {
//            f = 0.2F;
//            f1 = 0.2F;
//        }
//        else if (this.isSneaking())
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
//        if (f != this.width || f1 != this.height)
//        {
//            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
//            axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)f, axisalignedbb.minY + (double)f1, axisalignedbb.minZ + (double)f);
//
//            if (!this.worldObj.collidesWithAnyBlock(axisalignedbb))
//            {
//                this.setSize(f, f1);
//            }
//        }
//        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPlayerPostTick(this);
//    }
//
//    /**
//     * Sets the width and height of the entity.
//     */
//    @Override
//    protected void setSize(float width, float height) {
//        if (width != this.width || height != this.height)
//        {
//            float prevWidth = this.width;
//            float prevHeight = this.height;
//            this.width = width;
//            this.height = height;
//
//            // expand increases max by specified amount AND decreases min by specified amount, so need to half the change
//            double widthExpansion = (width - prevWidth)/2d;
//            double heightExpansion = (height - prevHeight)/2d;
//            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
//            axisalignedbb = axisalignedbb.expand(widthExpansion, heightExpansion, widthExpansion);
//
//            // The expansion will have decreased minY (or the equivalent field for a GravityAxisAllignedBB)
//            axisalignedbb = axisalignedbb.offset(0, heightExpansion, 0);
//
//            this.setEntityBoundingBox(axisalignedbb);
////            this.setEntityBoundingBox(new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)this.width, axisalignedbb.minY + (double)this.height, axisalignedbb.minZ + (double)this.width));
//
//            if (this.width > prevWidth && !this.firstUpdate && !this.worldObj.isRemote)
//            {
//                double[] doubles = API.getGravityDirection(this).adjustXYZValues(prevWidth - this.width, 0.0d, prevWidth - this.width);
//                this.moveEntity(doubles[0], doubles[1], doubles[2]);
//            }
//        }
//
//
//
////        if (width != this.width || height != this.height) {
////            float f = this.width;
////            float f1 = this.height;
////            this.width = width;
////            this.height = height;
////            AxisAlignedBB oldAABB = this.getEntityBoundingBox();
////            AxisAlignedBB newAABB = Hooks.getGravityAdjustedHitbox(this);
////
////            switch (API.getGravityDirection(this)) {
////                case UP:
////                    break;
////                case DOWN:
////                    break;
////                case NORTH:
////                    if (newAABB.maxZ > oldAABB.maxZ) {
////                        double distanceToMoveUp = newAABB.maxZ - oldAABB.maxZ;
////                        this.moveEntity(0, distanceToMoveUp, 0);
////                        newAABB = newAABB.offset(0, 0, -distanceToMoveUp);
////                    }
////                    break;
////                case EAST:
////                    if (newAABB.minX < oldAABB.minX) {
////                        double distanceToMoveUp = oldAABB.minX - newAABB.minX;
////                        this.moveEntity(0, distanceToMoveUp, 0);
////                        newAABB = newAABB.offset(distanceToMoveUp, 0, 0);
////                    }
////                    break;
////                case SOUTH:
////                    if (newAABB.minZ < oldAABB.minZ) {
////                        double distanceToMoveUp = oldAABB.minZ - newAABB.minZ;
////                        this.moveEntity(0, distanceToMoveUp, 0);
////                        newAABB = newAABB.offset(0, 0, distanceToMoveUp);
////                    }
////                    break;
////                case WEST:
////                    if (newAABB.maxX > oldAABB.maxX) {
////                        double distanceToMoveUp = newAABB.maxX - oldAABB.maxX;
////                        this.moveEntity(0, distanceToMoveUp, 0);
////                        newAABB = newAABB.offset(-distanceToMoveUp, 0, 0);
////                    }
////                    break;
////            }
////            this.setEntityBoundingBox(newAABB);
////
////            if (this.width > f && !this.firstUpdate && !this.worldObj.isRemote) {
////                this.moveEntity((double) (f - this.width), 0, (double) (f - this.width));
////            }
////        }
//    }
//
//    /**
//     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
//     */
//    //TODO: ASM
//    @Override
//    public void setPosition(double x, double y, double z) {
//        this.posX = x;
//        this.posY = y;
//        this.posZ = z;
//        float f = this.width / 2.0F;
//        float f1 = this.height;
//
//        //ASM new line
//        this.setEntityBoundingBox(Hooks.getGravityAdjustedHitbox(this));
//        //ASM old line
//        //this.setEntityBoundingBox(new AxisAlignedBB(x - (double) f, y, z - (double) f, x + (double) f, y + (double) f1, z + (double) f));
//        //ASM end
////        this.setSize(this.width, this.height);
//    }
//
//    @Override
//    public void onUpdate() {
//        this.worldObj.spawnParticle(EnumParticleTypes.END_ROD, this.posX, this.posY, this.posZ, 0, 0, 0);
//        this.worldObj.spawnParticle(EnumParticleTypes.FLAME, this.posX, this.posY + 1, this.posZ, 0, 0, 0);
//        this.worldObj.spawnParticle(EnumParticleTypes.CRIT_MAGIC, this.posX, this.posY + this.getEyeHeight(), this.posZ, 0, 0, 0);
//        double[] adjusted = API.getGravityDirection(this).adjustXYZValues(0, 1, 0);
//        this.worldObj.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, this.posX + adjusted[0], this.posY + adjusted[1], this.posZ + adjusted[2], 0, 0, 0);
//        super.onUpdate();
//    }
//
//    @Override
//    //TODO: ASM
//    public void resetPositionToBB() {
//        //ASM BEGIN
//        if (Hooks.resetPositionToBB(this)) {
//            return;
//        }
//        //ASM END
//        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
//        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
//        this.posY = axisalignedbb.minY;
//        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
////        // Oh no you don't I need my weird asymmetrical hitboxes
////        // TODO: Is this going to break spectator mode?
////        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
////        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
////        this.posY = axisalignedbb.minY;
////        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
////
////        switch (API.getGravityDirection(this)) {
////            case DOWN:
////                super.resetPositionToBB();
////                break;
////            case UP:
////                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
////                this.posY = axisalignedbb.maxY;
////                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
////                break;
////            case NORTH:
////                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
////                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
////                this.posZ = axisalignedbb.maxZ - API.getStandardEyeHeight(this);
////                break;
////            case EAST:
////                this.posX = axisalignedbb.minX + API.getStandardEyeHeight(this);
////                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
////                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
////                break;
////            case SOUTH:
////                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
////                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
////                this.posZ = axisalignedbb.minZ + API.getStandardEyeHeight(this);
////                break;
////            case WEST:
////                this.posX = axisalignedbb.maxX - API.getStandardEyeHeight(this);
////                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
////                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
////                break;
////
////        }
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public Vec3d getPositionEyes(float partialTicks)
//    {
//        return Hooks.getPositionEyes(this, partialTicks);
//    }
//
//    @Override
//    public float getEyeHeight() {
//        switch (API.getGravityDirection(this)) {
//            case UP:
//                return -API.getStandardEyeHeight(this);
//            case DOWN:
//                return API.getStandardEyeHeight(this);
////            case NORTH:
////            case EAST:
////            case SOUTH:
////            case WEST:
//            default:
//                return 0f;
//        }
//    }


    @Override
    public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
        super.setPositionAndRotation(x, y, z, yaw, pitch);
    }

    @Override
    public void addMovementStat(double p_71000_1_, double p_71000_3_, double p_71000_5_) {
        EnumGravityDirection direction = API.getGravityDirection(this);
        super.addMovementStat(p_71000_1_, p_71000_3_, p_71000_5_);
    }

    @Override
    public float getEyeHeight() {
        switch (API.getGravityDirection(this)) {
            case UP:
                return -API.getStandardEyeHeight(this);
            case DOWN:
                return API.getStandardEyeHeight(this);
//            case NORTH:
//            case EAST:
//            case SOUTH:
//            case WEST:
            default:
                return 0f;
        }
    }

    public float super_getEyeHeight() {
        return super.getEyeHeight();
    }

    @Override
    protected void createRunningParticles()
    {
        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            // Based on the super implementation
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            Vec3d belowBlock = gBB.addCoord(0, -0.20000000298023224D, 0).getOrigin();
            BlockPos blockpos = new BlockPos(belowBlock);
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE)
            {
                Vec3d particleSpawnPoint = gBB.addCoord(((double) this.rand.nextFloat() - 0.5D) * (double) this.width, 0.1D, ((double) this.rand.nextFloat() - 0.5D) * (double) this.width).getOrigin();
                double[] d = gBB.getDirection().adjustXYZValues(-this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D);
                this.worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                        particleSpawnPoint.xCoord,
                        particleSpawnPoint.yCoord,
                        particleSpawnPoint.zCoord,
                        d[0],
                        d[1],
                        d[2],
                        Block.getStateId(iblockstate));
            }
        }
        else {
            super.createRunningParticles();
        }
    }

    @Override
    protected void updateSize() {
        float f;
        float f1;

        if (this.isElytraFlying()) {
            f = 0.6F;
            f1 = 0.6F;
        } else if (this.isPlayerSleeping()) {
            f = 0.2F;
            f1 = 0.2F;
        } else if (this.isSneaking()) {
            f = 0.6F;
            f1 = 1.65F;
        } else {
            f = 0.6F;
            f1 = 1.8F;
        }

        if (f != this.width || f1 != this.height) {
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
            //axisalignedbb = new AxisAlignedBB(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.minX + (double)f, axisalignedbb.minY + (double)f1, axisalignedbb.minZ + (double)f);
            //TODO: ASM
            double heightExpansion = (f1 - this.height)/2d;
            double widthExpansion = (f - this.width)/2d;
            axisalignedbb = axisalignedbb.expand(widthExpansion, heightExpansion, widthExpansion).offset(0, heightExpansion, 0);
//            axisalignedbb = Hooks.getGravityAdjustedHitbox(this, f, f1);

            if (!this.worldObj.collidesWithAnyBlock(axisalignedbb)) {
                this.setSize(f, f1);
            }
        }
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPlayerPostTick(this);
    }

    /**
     * Sets the width and height of the entity.
     */
    protected void setSize(float width, float height) {
        if (width != this.width || height != this.height) {
            float oldWidth = this.width;
            float oldHeight = this.height;
            this.width = width;
            this.height = height;
            AxisAlignedBB oldAABB = this.getEntityBoundingBox();

            double heightExpansion = (this.height - oldHeight)/2d;
            double widthExpansion = (this.width - oldWidth)/2d;
            AxisAlignedBB newAABB = oldAABB.expand(widthExpansion, heightExpansion, widthExpansion).offset(0, heightExpansion, 0);

//            switch (API.getGravityDirection(this)) {
//                case UP:
//                    break;
//                case DOWN:
//                    break;
//                case NORTH:
//                    if (newAABB.maxZ > oldAABB.maxZ) {
//                        double distanceToMoveUp = newAABB.maxZ - oldAABB.maxZ;
//                        this.moveEntity(0, distanceToMoveUp, 0);
//                        newAABB = newAABB.offset(0, distanceToMoveUp, 0);
//                    }
//                    break;
//                case EAST:
//                    if (newAABB.minX < oldAABB.minX) {
//                        double distanceToMoveUp = oldAABB.minX - newAABB.minX;
//                        this.moveEntity(0, distanceToMoveUp, 0);
//                        newAABB = newAABB.offset(0, distanceToMoveUp, 0);
//                    }
//                    break;
//                case SOUTH:
//                    if (newAABB.minZ < oldAABB.minZ) {
//                        double distanceToMoveUp = oldAABB.minZ - newAABB.minZ;
//                        this.moveEntity(0, distanceToMoveUp, 0);
//                        newAABB = newAABB.offset(0, distanceToMoveUp, 0);
//                    }
//                    break;
//                case WEST:
//                    if (newAABB.maxX > oldAABB.maxX) {
//                        double distanceToMoveUp = newAABB.maxX - oldAABB.maxX;
//                        this.moveEntity(0, distanceToMoveUp, 0);
//                        newAABB = newAABB.offset(0, distanceToMoveUp, 0);
//                    }
//                    break;
//            }
            this.setEntityBoundingBox(newAABB);

            if (this.width > oldWidth && !this.firstUpdate && !this.worldObj.isRemote) {
                this.moveEntity((double) (oldWidth - this.width), 0, (double) (oldWidth - this.width));
            }
        }
    }

    //TODO: Move contents of switch into EnumGravityDirection
    @Override
    public void resetPositionToBB() {
        // Oh no you don't vanilla MC
        // I need my weird asymmetrical hitboxes so player.posY + player.getEyeHeight() still get's the player's eye position
        AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
        this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
        this.posY = axisalignedbb.minY;
        this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

        switch (API.getGravityDirection(this)) {
            case DOWN:
                super.resetPositionToBB();
                break;
            case UP:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = axisalignedbb.maxY;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;
            case SOUTH:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = axisalignedbb.maxZ - API.getStandardEyeHeight(this);
                break;
            case WEST:
                this.posX = axisalignedbb.minX + API.getStandardEyeHeight(this);
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;
            case NORTH:
                this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = axisalignedbb.minZ + API.getStandardEyeHeight(this);
                break;
            case EAST:
                this.posX = axisalignedbb.maxX - API.getStandardEyeHeight(this);
                this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
                this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
                break;

        }
    }

    //TODO: Could ASM or call super.setPosition() and then my own code?
    /**
     * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
//        float f = this.width / 2.0F;
//        float f1 = this.height;
        this.setEntityBoundingBox(Hooks.getGravityAdjustedHitbox(this));
        this.setSize(this.width, this.height);
    }

    //TODO: Fix this, so players are pushed out of blocks in differing direction based on their gravity
    @Override
    protected boolean pushOutOfBlocks(double x, double y, double z) {
        // pushOutOfBlocks directly modifies motion fields
        // Called for non-EntityPlayerSP (it overrides this method and does not call super)
//        this.makeMotionAbsolute();
        boolean result = super.pushOutOfBlocks(x, y, z);
//        this.makeMotionRelative();

        return result;
    }

    //TODO: Fix this, so players are pushed out of blocks in differing direction based on their gravity
    public boolean pushOutOfBlocksDelegate(double x, double y, double z) {
//        this.makeMotionAbsolute();
        boolean result = this.pushOutOfBlocks(x, y, z);
//        this.makeMotionRelative();
        return result;
    }


    //TODO: Work out what's wrong with my Access Transformer
    // MethodHandles used in isOnLadder() as my Access Transformer doesn't want to work...
    private static final MethodHandle nextStepDistance_Get;
    private static final MethodHandle nextStepDistance_Set;

    static {
        MethodHandles.Lookup lookup = LookupThief.INSTANCE.lookup(Entity.class);
        try {
            nextStepDistance_Get = lookup.findGetter(Entity.class, "nextStepDistance", int.class);
            nextStepDistance_Set = lookup.findSetter(Entity.class, "nextStepDistance", int.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setNextStepDistance(int value){
        try {
            nextStepDistance_Set.invokeExact((Entity)this, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    private int getNextStepDistance(){
        try {
            return (int)nextStepDistance_Get.invokeExact((Entity)this);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    @Override
    public boolean isOnLadder()
    {
        if (this.isSpectator()) {
            return false;
        }

        boolean isMonkeyBars = false;

        AxisAlignedBB bb = this.getEntityBoundingBox();
        if (bb instanceof GravityAxisAlignedBB) {
            GravityAxisAlignedBB gBB = (GravityAxisAlignedBB)bb;
            BlockPos blockpos = new BlockPos(gBB.offset(0, 0.001, 0).getOrigin());
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

            boolean isOnLadder = net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);

            if (iblockstate != null) {
                if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                    if (iblockstate.getPropertyNames().contains(BlockHorizontal.FACING)) {
                        EnumFacing facing = iblockstate.getValue(BlockHorizontal.FACING);
                        EnumGravityDirection direction = gBB.getDirection();
                        switch(direction) {
                            case EAST:
                            case WEST:
                                if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                                    isOnLadder = false;
                                }
                                break;
                            case NORTH:
                            case SOUTH:
                                if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                                    isOnLadder = false;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            if (!isOnLadder) {
                Vec3d topOfHead = gBB.offset(0, this.height, 0).getOrigin();
                blockpos = new BlockPos(topOfHead);
                iblockstate = this.worldObj.getBlockState(blockpos);
                if (iblockstate != null) {
                    if (iblockstate.getBlock().isLadder(iblockstate, worldObj, blockpos, this)) {
                        if (iblockstate.getPropertyNames().contains(BlockHorizontal.FACING)) {
                            EnumFacing facing = iblockstate.getValue(BlockHorizontal.FACING);
                            EnumGravityDirection direction = gBB.getDirection();
                            switch(direction) {
                                case EAST:
                                    isOnLadder = facing == EnumFacing.EAST && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case WEST:
                                    isOnLadder = facing == EnumFacing.WEST && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case NORTH:
                                    isOnLadder = facing == EnumFacing.NORTH && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                case SOUTH:
                                    isOnLadder = facing == EnumFacing.SOUTH && net.minecraftforge.common.ForgeHooks.isLivingOnLadder(iblockstate, worldObj, blockpos, this);
                                    break;
                                default:
                                    break;
                            }
                            isMonkeyBars = true;
                        }
                    }
                }
            }
            if (isOnLadder && isMonkeyBars) {
                if (this.distanceWalkedOnStepModified > (float)this.getNextStepDistance()) {
                    this.setNextStepDistance((int)this.distanceWalkedOnStepModified + 1);
                    this.playStepSound(blockpos, iblockstate.getBlock());
                }
            }
//            Block block = iblockstate.getBlock();
            return isOnLadder;
        }
        else {
            return super.isOnLadder();
        }
    }


    //    @Override
//    public Vec3d getLook(float partialTicks) {
//        Vec3d vectorForRotation;
//        if (partialTicks == 1.0F) {
//            vectorForRotation = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
////            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
//        } else {
//            float interpolatedRotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
//            float interpolatedRotationYaw = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
//            vectorForRotation = this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
////            return this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
//
//        }
//        return API.getGravityDirection().adjustLookVec(vectorForRotation);
//    }
}
