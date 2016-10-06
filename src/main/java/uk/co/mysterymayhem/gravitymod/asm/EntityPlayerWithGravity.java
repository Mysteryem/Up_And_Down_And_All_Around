package uk.co.mysterymayhem.gravitymod.asm;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.API;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.util.GravityAxisAlignedBB;
import uk.co.mysterymayhem.gravitymod.util.Vec3dHelper;
import uk.co.mysterymayhem.gravitymod.util.reflection.LookupThief;
import static uk.co.mysterymayhem.gravitymod.util.Vec3dHelper.PITCH;
import static uk.co.mysterymayhem.gravitymod.util.Vec3dHelper.YAW;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

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

        this.motionVarsAreRelative = true;
        this.positionVarsAreRelative = false;
        this.angleVarsAreRelative = false;


        // Starting known values are the same, gravity direction is assumed to be DEFAULT_GRAVITY (DOWN) while entity is constructing
        this.lastKnownRotationYaw = this.rotationYaw;
        this.lastKnownRotationPitch = this.rotationPitch;
        this.lastKnownRotationYawHead = this.rotationYawHead;
        this.lastKnownPrevRotationYaw = this.prevRotationYaw;
        this.lastKnownPrevRotationPitch = this.prevRotationPitch;
        this.lastKnownPrevRotationYawHead = this.prevRotationYawHead;

        this.lastKnownRelativeRotationYaw = this.rotationYaw;
        this.lastKnownRelativeRotationPitch = this.rotationPitch;
        this.lastKnownRelativeRotationYawHead = this.rotationYawHead;
        this.lastKnownRelativePrevRotationYaw = this.prevRotationYaw;
        this.lastKnownRelativePrevRotationPitch = this.prevRotationPitch;
        this.lastKnownRelativePrevRotationYawHead = this.prevRotationYawHead;


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

    /*
    v- Didn't work
     */

//    @Override
//    public void moveEntityWithHeading(float strafe, float forward) {
//        this.makeAngleVarsRelative();
//        super.moveEntityWithHeading(strafe, forward);
//        this.makeAngleVarsAbsolute();
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void setAngles(float yaw, float pitch) {
//        this.makeAngleVarsRelative();
//        double d = this.d_rotationPitch;
//        double d1 = this.d_rotationYaw;
//        this.d_rotationYaw = this.d_rotationYaw + yaw * 0.15D;
//        this.d_rotationPitch = this.d_rotationPitch - pitch * 0.15D;
//        this.d_rotationPitch = MathHelper.clamp_double(this.d_rotationPitch, -90.0F, 90.0F);
//        this.d_prevRotationPitch += this.d_rotationPitch - d;
//        this.d_prevRotationYaw += this.d_rotationYaw - d1;
//
//        float f = this.rotationPitch;
//        float f1 = this.rotationYaw;
//        float rotationYaw = (float)((double)this.rotationYaw + (double)yaw * 0.15D);
//        float rotationPitch = (float)((double)this.rotationPitch - (double)pitch * 0.15D);
//        rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90.0F, 90.0F);
//        float prevRotationPitch = this.prevRotationPitch + this.rotationPitch - f;
//        float prevRotationYaw = this.prevRotationYaw + this.rotationYaw - f1;
//
//        super.setAngles(yaw, pitch);
//
////        super.setAngles(yaw, pitch);
////        if (this.rotationYaw < -180) {
////            this.yawToReAdd = (this.rotationYaw + 180);
////        }
////        else if (this.rotationYaw > 180) {
////            this.yawToReAdd = (this.rotationYaw - 180);
////        }
////        if (this.prevRotationYaw < -180) {
////            this.prevYawToReAdd = (this.prevRotationYaw + 180);
////        } else if (this.prevRotationYaw > 180) {
////            this.prevYawToReAdd = (this.prevRotationYaw - 180);
////        }
//        this.makeAngleVarsAbsolute();
//
//        return;
////        //
////        this.makeAngleVarsRelative();
////        //
////        this.makeAngleVarsAbsolute();
//    }
//
//    private double yawToReAddInRelative = 0;
//    private double prevYawToReAddInRelative = 0;
//    private double yawToReAddInAbsolute = 0;
//    private double prevYawToReAddInAbsolute = 0;
//    private double headYawToReAddInRelative = 0;
//    private double prevHeadYawToReAddInRelative = 0;
//    private double headYawToReAddInAbsolute = 0;
//    private double prevHeadYawToReAddInAbsolute = 0;
//    private double d_rotationYaw;
//    private double d_rotationYawHead;
//    private double d_rotationPitch;
//    private double d_prevRotationYaw;
//    private double d_prevRotationYawHead;
//    private double d_prevRotationPitch;
//
//    private static final double PI_OVER_180 = Math.PI/180d;
//
//    public void makeAngleVarsRelative() {
//
//        if (!this.angleVarsAreRelative) {
//            this.angleVarsAreRelative = true;
//
//            double pitch = this.rotationPitch;
//
//            double yaw = (pitch == 90) || (pitch == -90) ? this.d_rotationYaw : this.rotationYaw;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.yawToReAddInAbsolute = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.yawToReAddInAbsolute = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.yawToReAddInAbsolute = 0;
//            }
//
//            double f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            double f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            double f2 = -Math.cos(-pitch * (PI_OVER_180));
//            double f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
////            Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
//            Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.rotationYaw = this.yawToReAddInRelative + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_rotationYaw = this.yawToReAddInRelative + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.rotationYaw = (float)this.d_rotationYaw;
//
////            this.rotationYaw = (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
////            this.rotationPitch = (float) -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.d_rotationPitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.rotationPitch = (float) this.d_rotationPitch;
//
//            pitch = this.prevRotationPitch;
//
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_prevRotationYaw : this.prevRotationYaw;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.prevYawToReAddInAbsolute = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.prevYawToReAddInAbsolute = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.prevYawToReAddInAbsolute = 0;
//            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
////            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
//            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.prevRotationYaw = this.prevYawToReAddInRelative + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_prevRotationYaw = this.prevYawToReAddInRelative + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.prevRotationYaw = (float)this.d_prevRotationYaw;
////            this.prevRotationYaw = (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
////            this.prevRotationPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.d_prevRotationPitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.prevRotationPitch = (float)d_prevRotationPitch;
//
//            //head
//            pitch = this.rotationPitch;
//
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_rotationYawHead : this.rotationYawHead;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.headYawToReAddInAbsolute = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.headYawToReAddInAbsolute = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.headYawToReAddInAbsolute = 0;
//            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
////            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
//            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.rotationYawHead = this.headYawToReAddInRelative + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_rotationYawHead = this.headYawToReAddInRelative + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.rotationYawHead = (float)d_rotationYawHead;
////            this.prevRotationYaw = (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
////            this.prevRotationPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//
//            //prevHead
//            pitch = this.prevRotationPitch;
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_prevRotationYawHead : this.prevRotationYawHead;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.prevHeadYawToReAddInAbsolute = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.prevHeadYawToReAddInAbsolute = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.prevHeadYawToReAddInAbsolute = 0;
//            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
////            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
//            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.prevRotationYawHead = this.prevHeadYawToReAddInRelative + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_prevRotationYawHead = this.prevHeadYawToReAddInRelative + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.prevRotationYawHead = (float)this.d_prevRotationYawHead;
//        }
//    }
//
//    public void makeAngleVarsAbsolute() {
//
//        if (this.angleVarsAreRelative) {
//            this.angleVarsAreRelative = false;
//            double pitch = this.rotationPitch;
//
//            double yaw = (pitch == 90) || (pitch == -90) ? this.d_rotationYaw : this.rotationYaw;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.yawToReAddInRelative = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.yawToReAddInRelative = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.yawToReAddInRelative = 0;
//            }
////            if (yaw <= -180 || yaw >= 180) {
////                this.yawToReAdd = ((int)yaw/180) * 180;
////            }
////            else if (yaw > 180) {
////                this.yawToReAdd = (float)(yaw - 180);
////            }
//
//
//
//            double f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            double f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            double f2 = -Math.cos(-pitch * (PI_OVER_180));
//            double f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
//            Vec3d adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
////        Vec3d adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.rotationYaw = this.yawToReAddInAbsolute + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_rotationYaw = this.yawToReAddInAbsolute + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.rotationYaw = (float)this.d_rotationYaw;
////            this.rotationPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.d_rotationPitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.rotationPitch = (float)this.d_rotationPitch;
//
//            pitch = this.prevRotationPitch;
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_prevRotationYaw : this.prevRotationYaw;
//
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.prevYawToReAddInRelative = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.prevYawToReAddInRelative = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.prevYawToReAddInRelative = 0;
//            }
////            if (yaw <= -180 || yaw >= 180) {
////                this.prevYawToReAdd = ((int)yaw/180) * 180;
////            }
////            if (yaw < -180) {
////                this.prevYawToReAdd = (float)(yaw + 180);
////            } else if (yaw > 180) {
////                this.prevYawToReAdd = (float)(yaw - 180);
////            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy = new Vec3d(f1 * f2, f3, f * f2);
//
//            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
////            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.prevRotationYaw = this.prevYawToReAddInAbsolute + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_prevRotationYaw = this.prevYawToReAddInAbsolute + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.prevRotationYaw = (float)this.d_prevRotationYaw;
////            this.prevRotationPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.d_prevRotationPitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//            this.prevRotationPitch = (float)this.d_prevRotationPitch;
//
//            //head
//            pitch = this.rotationPitch;
//
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_rotationYawHead : this.rotationYawHead;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.headYawToReAddInRelative = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.headYawToReAddInRelative = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.headYawToReAddInRelative = 0;
//            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
//            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
////            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.rotationYawHead = this.headYawToReAddInAbsolute + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_rotationYawHead = this.headYawToReAddInAbsolute + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.rotationYawHead = (float)this.d_rotationYawHead;
////            this.prevRotationYaw = (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
////            this.prevRotationPitch = (float)-(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//
//            //prevHead
//            pitch = this.prevRotationPitch;
//            yaw = (pitch == 90) || (pitch == -90) ? this.d_prevRotationYawHead : this.prevRotationYawHead;
//            if (yaw <= -180) {
////                this.yawToReAdd = -360;
//                this.prevHeadYawToReAddInRelative = 360 * (((int)(yaw-180))/360);
//            }
//            else if (yaw >= 180) {
////                this.yawToReAdd = -720;
//                this.prevHeadYawToReAddInRelative = 360 * (((int)(yaw+180))/360);
//            }
//            else {
//                this.prevHeadYawToReAddInRelative = 0;
//            }
//
//
//            f = Math.cos(-yaw * (PI_OVER_180) - Math.PI);
//            f1 = Math.sin(-yaw * (PI_OVER_180) - Math.PI);
//            f2 = -Math.cos(-pitch * (PI_OVER_180));
//            f3 = Math.sin(-pitch * (PI_OVER_180));
//
//            lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
//            adjustedVec = Hooks.adjustVec(lookVecWithDoubleAccuracy, this);
////            adjustedVec = Hooks.inverseAdjustVec(lookVecWithDoubleAccuracy, this);
////            this.prevRotationYawHead = this.prevHeadYawToReAddInAbsolute + (float)(Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.d_prevRotationYawHead = this.prevHeadYawToReAddInAbsolute + (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//            this.prevRotationYawHead = (float)this.d_prevRotationYawHead;
//        }
//    }
    /*
    ^ Didn't work
     */

//    // Updated whenever we need to use relative values
//    private float relativeRotationYaw;
//    private float relativeRotationYawHead;
//    private float relativeRotationPitch;
//    private float relativePrevRotationYaw;
//    private float relativePrevRotationYawHead;
//    private float relativePrevRotationPitch;

    // Last known value of the Mojang fields, compared with the current values to work out the changes that have been
    // made since then, these changes will be inverse-adjusted and applied to the relative rotations
    private float lastKnownRotationYaw;
    private float lastKnownRotationPitch;
    private float lastKnownRotationYawHead;
    private float lastKnownPrevRotationYaw;
    private float lastKnownPrevRotationPitch;
    private float lastKnownPrevRotationYawHead;

    private float lastKnownCameraYaw;
    private float lastKnownCameraPitch;
    private float lastKnownPrevCameraYaw;
    private float lastKnownPrevCameraPitch;

    // Last known value of the gravity-relative fields, compared with the current values to work out the changes that
    // have been made since then, these changes will be adjusted and applied to the relative Mojang fields
    private float lastKnownRelativeRotationYaw;
    private float lastKnownRelativeRotationPitch;
    private float lastKnownRelativeRotationYawHead;
    private float lastKnownRelativePrevRotationYaw;
    private float lastKnownRelativePrevRotationPitch;
    private float lastKnownRelativePrevRotationYawHead;

    private float lastKnownRelativeCameraYaw;
    private float lastKnownRelativeCameraPitch;
    private float lastKnownRelativePrevCameraYaw;
    private float lastKnownRelativePrevCameraPitch;

//    private void swapRotationVars() {
//        float holder = rotationYaw;
//        rotationYaw = relativeRotationYaw;
//        relativeRotationYaw = holder;
//
//        holder = rotationYawHead;
//        rotationYawHead = relativeRotationYawHead;
//        relativeRotationYawHead = holder;
//
//        holder = rotationPitch;
//        rotationPitch = relativeRotationPitch;
//        relativeRotationPitch = holder;
//
//        holder = prevRotationYaw;
//        prevRotationYaw = relativePrevRotationYaw;
//        relativePrevRotationYaw = holder;
//
//        holder = prevRotationYawHead;
//        prevRotationYawHead = relativePrevRotationYawHead;
//        relativePrevRotationYawHead = holder;
//
//        holder = prevRotationPitch;
//        prevRotationPitch = relativePrevRotationPitch;
//        relativePrevRotationPitch = holder;
//    }
//
//    void makeAngleVarsRelative() {
//        if (!angleVarsAreRelative) {
//            angleVarsAreRelative = true;
//            swapRotationVars();
//        }
//    }
//
//    void makeAngleVarsAbsolute() {
//        if (angleVarsAreRelative) {
//            angleVarsAreRelative = false;
//            swapRotationVars();
//        }
//    }

    void makeAngleVarsRelative() {
        if (!angleVarsAreRelative) {
            this.angleVarsAreRelative = true;

            float deltaYaw = this.rotationYaw - this.lastKnownRotationYaw;
            float deltaPitch = this.rotationPitch - this.lastKnownRotationPitch;
            float deltaYawHead = this.rotationYawHead - this.lastKnownRotationYawHead;

            float deltaPrevYaw = this.prevRotationYaw - this.lastKnownPrevRotationYaw;
            float deltaPrevPitch = this.prevRotationPitch - this.lastKnownPrevRotationPitch;
            float deltaPrevYawHead = this.prevRotationYawHead - this.lastKnownPrevRotationYawHead;

            Vec3d vDeltaPitchYaw = this.getVectorForRotation(deltaPitch, deltaYaw);
            Vec3d vDeltaPitchYawHead = this.getVectorForRotation(deltaPitch, deltaYawHead);

            Vec3d vDeltaPrevPitchYaw = this.getVectorForRotation(deltaPrevPitch, deltaPrevYaw);
            Vec3d vDeltaPrevPitchYawHead = this.getVectorForRotation(deltaPrevPitch, deltaPrevYawHead);

            // Absolute values are rotated based on the gravity direction such that code interacting with pitch/yaw works
            //
            EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();

            vDeltaPitchYaw = direction.adjustLookVec(vDeltaPitchYaw);
            vDeltaPitchYawHead = direction.adjustLookVec(vDeltaPitchYawHead);

            vDeltaPrevPitchYaw = direction.adjustLookVec(vDeltaPrevPitchYaw);
            vDeltaPrevPitchYawHead = direction.adjustLookVec(vDeltaPrevPitchYawHead);

            float relativeDeltaYaw = Vec3dHelper.getYaw(vDeltaPitchYaw);
            float relativeDeltaPitch = Vec3dHelper.getPitch(vDeltaPitchYaw);
            //TODO: What's the best way to handle yawHead? Ignore it completely? Take an average of yaw and yawHead when calculating pitch?
            float relativeDeltaYawHead = Vec3dHelper.getYaw(vDeltaPitchYawHead);

            float relativePrevDeltaYaw = Vec3dHelper.getYaw(vDeltaPrevPitchYaw);
            float relativePrevDeltaPitch = Vec3dHelper.getPitch(vDeltaPrevPitchYaw);
            float relativePrevDeltaYawHead = Vec3dHelper.getYaw(vDeltaPrevPitchYawHead);

            this.lastKnownRotationYaw = this.rotationYaw;
            this.lastKnownRotationPitch = this.rotationPitch;
            this.lastKnownRotationYawHead = this.rotationYawHead;

            this.lastKnownPrevRotationYaw = this.prevRotationYaw;
            this.lastKnownPrevRotationPitch = this.prevRotationPitch;
            this.lastKnownPrevRotationYawHead = this.prevRotationYawHead;

            this.rotationYaw = this.lastKnownRelativeRotationYaw += relativeDeltaYaw;
            this.rotationPitch = this.lastKnownRelativeRotationPitch += relativeDeltaPitch;
            this.rotationYawHead = this.lastKnownRelativeRotationYawHead += relativeDeltaYawHead;
//            this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90f, 90f);
//            this.lastKnownRelativeRotationPitch = MathHelper.clamp_float(this.lastKnownRelativeRotationPitch, -90f, 90f);

            this.prevRotationYaw = this.lastKnownRelativePrevRotationYaw += relativePrevDeltaYaw;
            this.prevRotationPitch = this.lastKnownRelativePrevRotationPitch += relativePrevDeltaPitch;
            this.prevRotationYawHead = this.lastKnownRelativePrevRotationYawHead += relativePrevDeltaYawHead;
//            this.prevRotationPitch = MathHelper.clamp_float(this.prevRotationPitch, -90f, 90f);
//            this.lastKnownRelativePrevRotationPitch = MathHelper.clamp_float(this.lastKnownRelativePrevRotationPitch, -90f, 90f);

        }
    }

    void makeAngleVarsAbsolute() {
        if (angleVarsAreRelative) {
            this.angleVarsAreRelative = false;

            float deltaYaw = this.rotationYaw - this.lastKnownRelativeRotationYaw;
            float deltaPitch = this.rotationPitch - this.lastKnownRelativeRotationPitch;
            float deltaYawHead = this.rotationYawHead - this.lastKnownRelativeRotationYawHead;

            float deltaPrevYaw = this.prevRotationYaw - this.lastKnownRelativePrevRotationYaw;
            float deltaPrevPitch = this.prevRotationPitch - this.lastKnownRelativePrevRotationPitch;
            float deltaPrevYawHead = this.prevRotationYawHead - this.lastKnownRelativePrevRotationYawHead;

            Vec3d vDeltaPitchYaw = this.getVectorForRotation(deltaPitch, deltaYaw);
            Vec3d vDeltaPitchYawHead = this.getVectorForRotation(deltaPitch, deltaYawHead);

            Vec3d vDeltaPrevPitchYaw = this.getVectorForRotation(deltaPrevPitch, deltaPrevYaw);
            Vec3d vDeltaPrevPitchYawHead = this.getVectorForRotation(deltaPrevPitch, deltaPrevYawHead);

            EnumGravityDirection direction = API.getGravityDirection(this);

            vDeltaPitchYaw = direction.adjustLookVec(vDeltaPitchYaw);
            vDeltaPitchYawHead = direction.adjustLookVec(vDeltaPitchYawHead);

            vDeltaPrevPitchYaw = direction.adjustLookVec(vDeltaPrevPitchYaw);
            vDeltaPrevPitchYawHead = direction.adjustLookVec(vDeltaPrevPitchYawHead);

            float absoluteDeltaYaw = Vec3dHelper.getYaw(vDeltaPitchYaw);
            float absoluteDeltaPitch = Vec3dHelper.getPitch(vDeltaPitchYaw);
            //TODO: What's the best way to handle yawHead? Ignore it completely? Take an average of yaw and yawHead when calculating pitch?
            float absoluteDeltaYawHead = Vec3dHelper.getYaw(vDeltaPitchYawHead);

            float absolutePrevDeltaYaw = Vec3dHelper.getYaw(vDeltaPrevPitchYaw);
            float absolutePrevDeltaPitch = Vec3dHelper.getPitch(vDeltaPrevPitchYaw);
            float absolutePrevDeltaYawHead = Vec3dHelper.getYaw(vDeltaPrevPitchYawHead);

            this.lastKnownRelativeRotationYaw = this.rotationYaw;
            this.lastKnownRelativeRotationPitch = this.rotationPitch;
            this.lastKnownRelativeRotationYawHead = this.rotationYawHead;

            this.lastKnownRelativePrevRotationYaw = this.prevRotationYaw;
            this.lastKnownRelativePrevRotationPitch = this.prevRotationPitch;
            this.lastKnownRelativePrevRotationYawHead = this.prevRotationYawHead;

            this.rotationYaw = this.lastKnownRotationYaw += absoluteDeltaYaw;
            this.rotationPitch = this.lastKnownRotationPitch += absoluteDeltaPitch;
            this.rotationYawHead = this.lastKnownRotationYawHead += absoluteDeltaYawHead;
//            this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90f, 90f);
//            this.lastKnownRotationPitch = MathHelper.clamp_float(this.lastKnownRotationPitch, -90f, 90f);
//            this.rotationPitch %= 90;
//            this.lastKnownRotationPitch %= 90;

            this.prevRotationYaw = this.lastKnownPrevRotationYaw += absolutePrevDeltaYaw;
            this.prevRotationPitch = this.lastKnownPrevRotationPitch += absolutePrevDeltaPitch;
            this.prevRotationYawHead = this.lastKnownPrevRotationYawHead += absolutePrevDeltaYawHead;
//            this.prevRotationPitch = MathHelper.clamp_float(this.prevRotationPitch, -90f, 90f);
//            this.lastKnownPrevRotationPitch = MathHelper.clamp_float(this.lastKnownPrevRotationPitch, -90f, 90f);
//            this.prevRotationPitch %= 90;
//            this.lastKnownPrevRotationPitch %= 90;
        }
    }

    // Something to do when changing gravity direction?
    void resetExtraAngleVars() {

    }

//    @Override
//    public void moveEntityWithHeading(float strafe, float forward) {
//        this.makeAngleVarsRelative();
//        super.moveEntityWithHeading(strafe, forward);
//        this.makeAngleVarsAbsolute();
//    }
//
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public void setAngles(float yaw, float pitch) {
//        this.makeAngleVarsRelative();
//        super.setAngles(yaw, pitch);
//        this.makeAngleVarsAbsolute();
//    }

    @Override
    public void setAngles(float yaw, float pitch) {
//        this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90.0F, 90.0F);
        final float absolutePitch = this.rotationPitch;
        final float absoluteYaw = this.rotationYaw;
        final float f2 = this.prevRotationPitch;
        final float f3 = this.prevRotationYaw;


        super.setAngles(yaw, pitch);

        //new

        final EnumGravityDirection direction = API.getGravityDirection(this);
        final EnumGravityDirection reverseDirection = direction.getInverseAdjustMentFromDOWNDirection();

        final double relativePitchChange = -pitch*0.15d;
        final double relativeYawChange = yaw*0.15d;

        final Vec3d normalLookVec = Vec3dHelper.getPreciseVectorForRotation(absolutePitch, absoluteYaw);

        final Vec3d relativeLookVec = reverseDirection.adjustLookVec(normalLookVec);
        final double[] relativePY = Vec3dHelper.getPrecisePitchAndYawFromVector(relativeLookVec);
        final double relativePitch = relativePY[PITCH];
        final double relativeYaw = relativePY[YAW];

        final double changedRelativeYaw = relativeYaw + relativeYawChange;
        final double changedRelativePitch = relativePitch + relativePitchChange;
        final double clampedRelativePitch;

        // Any closer to -90 or 90 produce tiny values that the trig functions will effectively treat as zero
        // this causes an inability to rotate the camera when looking straight up or down
        final double maxRelativeYaw = direction == EnumGravityDirection.UP || direction == EnumGravityDirection.DOWN ? 90d : 89.99d;
        final double minRelativeYaw = direction == EnumGravityDirection.UP || direction == EnumGravityDirection.DOWN ? -90d : -89.99d;

        if (changedRelativePitch > maxRelativeYaw) {
            clampedRelativePitch = maxRelativeYaw;
        }
        else if (changedRelativePitch < minRelativeYaw) {
            clampedRelativePitch = minRelativeYaw;
        }
        else {
            clampedRelativePitch = changedRelativePitch;
        }

        // Work out the change in absolute pitch and yaw and add that to the current pitch and yaw
//        final double clampedRelativePitchChange = clampedRelativePitch - relativePitch;
//
//        final Vec3d relativeChangeVec = Vec3dHelper.getPreciseVectorForRotation(clampedRelativePitchChange, relativeYawChange);
//
//        final Vec3d absoluteChangeVec = direction.adjustLookVec(relativeChangeVec);
//
//        final double[] absolutePYChange = Vec3dHelper.getPrecisePitchAndYawFromVector(absoluteChangeVec);
//        this.rotationPitch = (float)(absolutePitch + absolutePYChange[PITCH]);
//        this.rotationYaw = (float)(absoluteYaw + absolutePYChange[YAW]);

        // Directly set pitch and yaw
        final Vec3d relativeChangedLookVec = Vec3dHelper.getPreciseVectorForRotation(clampedRelativePitch, changedRelativeYaw);

        final Vec3d absoluteLookVec = direction.adjustLookVec(relativeChangedLookVec);
        final double[] absolutePY = Vec3dHelper.getPrecisePitchAndYawFromVector(absoluteLookVec);

        final double changedAbsolutePitch = absolutePY[PITCH];
        final double changedAbsoluteYaw = (absolutePY[YAW] % 360);

        // Yaw calculated through yaw change
        final double absoluteYawChange;
        final double absoluteYawToReAdd;
        final double effectiveStartingAbsoluteYaw = absoluteYaw % 360;
        final double positiveEffectiveStartingAbsoluteYaw;

        if (Math.abs(effectiveStartingAbsoluteYaw - changedAbsoluteYaw) > 180) {
            if (effectiveStartingAbsoluteYaw < changedAbsoluteYaw) {
                absoluteYawChange = changedAbsoluteYaw - (effectiveStartingAbsoluteYaw + 360);
            }
            else {
                absoluteYawChange = (changedAbsoluteYaw + 360) - effectiveStartingAbsoluteYaw;
            }
        }
        else {
            absoluteYawChange = changedAbsoluteYaw - effectiveStartingAbsoluteYaw;
        }
//
//
//        // make both starting and changed yaw in range [0, 359]
//        if (effectiveStartingAbsoluteYaw < 0) {
//            positiveEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw;// + 360;
//        } else {
//            positiveEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw;
//        }
//        final double positiveEffectiveChangedAbsoluteYaw;
//        //changedAbsoluteYaw will be in range [-180, 180] due to trig functions used, so don't need to %360 first
//        if (changedAbsoluteYaw < 0) {
//            positiveEffectiveChangedAbsoluteYaw = changedAbsoluteYaw;// + 360;
//        } else {
//            positiveEffectiveChangedAbsoluteYaw = changedAbsoluteYaw;
//        }
//
//        absoluteYawChange = positiveEffectiveChangedAbsoluteYaw - positiveEffectiveStartingAbsoluteYaw;

//        if (Math.abs(effectiveStartingAbsoluteYaw - changedAbsoluteYaw) > 180) {
//
//            // make both starting and changed yaw in range [0, 359]
//            if (effectiveStartingAbsoluteYaw < 0) {
//                positiveEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw + 360;
//            } else {
//                positiveEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw;
//            }
//            final double positiveEffectiveChangedAbsoluteYaw;
//            //changedAbsoluteYaw will be in range [-180, 180] due to trig functions used, so don't need to %360 first
//            if (changedAbsoluteYaw < 0) {
//                positiveEffectiveChangedAbsoluteYaw = changedAbsoluteYaw + 360;
//            } else {
//                positiveEffectiveChangedAbsoluteYaw = changedAbsoluteYaw;
//            }
//
//            // if difference between them is greater than 180 degrees
//            if (Math.abs(positiveEffectiveStartingAbsoluteYaw - positiveEffectiveChangedAbsoluteYaw) > 180) {
//                // add 180 degrees to both and refit them to range [0, 359]
//                double effectiveStartYaw180 = (positiveEffectiveStartingAbsoluteYaw + 180) % 360;
//                double changedAbsoluteYaw180 = (positiveEffectiveChangedAbsoluteYaw + 180) % 360;
//                absoluteYawChange = changedAbsoluteYaw180 - effectiveStartYaw180;
//            } else {
//                absoluteYawChange = positiveEffectiveChangedAbsoluteYaw - positiveEffectiveStartingAbsoluteYaw;
//            }
//        }
//        else {
//            absoluteYawChange = changedAbsoluteYaw - effectiveStartingAbsoluteYaw;
//        }
//
//        final boolean needsFixing = (Math.abs(positiveEffectiveChangedAbsoluteYaw - positiveEffectiveStartingAbsoluteYaw) > 180);
//        if (needsFixing) {
//            double effStartYawPlus180 = positiveEffectiveStartingAbsoluteYaw + 180;
//            double effChangYawPlus180 = positiveEffectiveChangedAbsoluteYaw + 180;
//        }
//
//        absoluteYawChange = positiveEffectiveChangedAbsoluteYaw - positiveEffectiveStartingAbsoluteYaw;

//        final double clampedEffectiveStartingAbsoluteYaw;
//
//        if (effectiveStartingAbsoluteYaw > 180) {
//            clampedEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw - 360;
//        }
//        else if (effectiveStartingAbsoluteYaw < -180) {
//            clampedEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw + 360;
//        }
//        else {
//            clampedEffectiveStartingAbsoluteYaw = effectiveStartingAbsoluteYaw;
//        }
//
//        absoluteYawChange = changedAbsoluteYaw - clampedEffectiveStartingAbsoluteYaw;

        this.rotationYaw = (float)(absoluteYaw + absoluteYawChange);

        this.rotationPitch = (float)changedAbsolutePitch;
//        this.rotationYaw = changedAbsoluteYaw;
        this.prevRotationPitch = f2;
        this.prevRotationPitch += this.rotationPitch - absolutePitch;
        this.prevRotationYaw = f3;
        this.prevRotationYaw += this.rotationYaw - absoluteYaw;


//        //old
//
//        float normalResultRotationYaw = this.rotationYaw;
//        float normalResultRotationPitch = this.rotationPitch;
//        float normalResultPrevRotationPitch = this.prevRotationPitch;
//        float normalResultPrevRotationYaw = this.prevRotationYaw;
//
//        // change is already relative!!!!!!
////        double[] yp = Hooks.getRelativeYawAndPitch(yaw, pitch, this);
////        double relativeYawChange = yp[Hooks.YAW];
////        double d_pitch = yp[Hooks.PITCH];
//
//        double[] baseYP = Hooks.getRelativeYawAndPitch(f1 + yaw * 0.15d, f - pitch * 0.15d, this);
//        double relativeYaw = baseYP[Hooks.YAW];
//        double relativePitch = baseYP[Hooks.PITCH];
////
////        double relativePitchAfterChange = relativePitch - pitch * 0.15D;
//
////        double clampAdjustment = 0;
////
////        if (relativePitchAfterChange > 90.0D) {
////            clampAdjustment = 90.0D - relativePitchAfterChange;
////        }
////        else if (relativePitchAfterChange < -90.0D) {
////            clampAdjustment = -90.0D - relativePitchAfterChange;
////        }
//
//        relativePitch = MathHelper.clamp_double(relativePitch, -90d, 90d);
//
//        double[] absoluteYP = Hooks.getAbsoluteYawAndPitch(relativeYaw, relativePitch, this);
//
//        this.rotationPitch = (float)absoluteYP[Hooks.PITCH];
//        this.rotationYaw = (float)absoluteYP[Hooks.YAW];
//        this.prevRotationPitch = f2;
//        this.prevRotationPitch += this.rotationPitch - f;
//        this.prevRotationYaw = f3;
//        this.prevRotationYaw += this.rotationYaw - f1;
//
//////        FMLLog.info(relativePitchAfterChange + ", " + (clampAdjustment + f - d_pitch * 0.15D));
////
//////        pitch+=clampAdjustment;
////
////        double[] absoluteAdjustment = Hooks.getAbsoluteYawAndPitch(yaw * 0.15d, pitch * 0.15d - clampAdjustment, this);
////
//////        double[] adjustment = Hooks.getAbsoluteYawAndPitch(0, clampAdjustment, this);
////        double yawAdjustment = absoluteAdjustment[Hooks.YAW];
////        double pitchAdjustment = absoluteAdjustment[Hooks.PITCH];
////
////        //Adjustment is working for normal, but not others, two of the gravity directions are still very broken
////
//////        d_pitch += (clampAdjustment/0.15D);
//////        yp = Hooks.getRelativeYawAndPitch(yaw, pitch + clampAdjustment, this);
//////        d_yaw = yp[Hooks.YAW];
//////        d_pitch = yp[Hooks.PITCH];
////
////        // Ignores any changes that may have been made to pitch/yaw by calling super
////        this.rotationYaw = (float)(f1 + yawAdjustment);
////        this.rotationPitch = (float)(f - pitchAdjustment);
////        // Necessary?
//////        this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90.0F, 90.0F);
//////        this.rotationPitch += clampAdjustment;
////        this.prevRotationPitch = f2;
////        this.prevRotationPitch += this.rotationPitch - f;
////        this.prevRotationYaw = f3;
////        this.prevRotationYaw += this.rotationYaw - f1;
//
////        boolean errored = false;
////
//////        if (this.rotationYaw != normalResultRotationYaw) {
//////            FMLLog.info("Yaw: " + this.rotationYaw + ", Expected: " + normalResultRotationYaw);
//////            errored = true;
//////        }
////        if (this.rotationPitch != normalResultRotationPitch) {
////            FMLLog.info("Pitch: " + this.rotationPitch + ", Expected: " + normalResultRotationPitch);
////            errored = true;
////        }
//////        if (this.prevRotationYaw != normalResultPrevRotationYaw) {
//////            FMLLog.info("PrevYaw: " + this.prevRotationYaw + ", Expected: " + normalResultPrevRotationYaw);
//////            errored = true;
//////        }
////        if (this.prevRotationPitch != normalResultPrevRotationPitch) {
////            FMLLog.info("PrevPitch: " + this.prevRotationPitch + ", Expected: " + normalResultPrevRotationPitch);
////            errored = true;
////        }
////
////        if (errored) {
////            FMLLog.info("InYaw: " + yaw + ", InPitch: " + pitch);
////        }
//
////        this.rotationPitch = MathHelper.clamp_float(this.rotationPitch, -90.0F, 90.0F);
    }

//    @Override
//    public void moveEntityWithHeading(float strafe, float forward) {
//        super.moveEntityWithHeading(strafe, forward);
//    }

    //    @Override
//    public Vec3d getLook(float partialTicks) {
//        Vec3d vectorForRotation;
//        if (partialTicks == 1.0F) {
//            vectorForRotation = this.getVectorForRotation(this.rotationPitch, this.rotationYawHead);
//    //            return this.getVectorForRotation(this.rotationPitch, this.rotationYaw);
//        } else {
//            float interpolatedRotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * partialTicks;
//            float interpolatedRotationYaw = this.prevRotationYawHead + (this.rotationYawHead - this.prevRotationYawHead) * partialTicks;
//            vectorForRotation = this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
//    //            return this.getVectorForRotation(interpolatedRotationPitch, interpolatedRotationYaw);
//
//        }
//        return API.getGravityDirection(this).adjustLookVec(vectorForRotation);
//    }

    public Vec3d getSuperLook(float partialTicks) {
        return super.getLook(partialTicks);
    }

//    @Override
//    public void moveEntityWithHeading(float strafe, float forward) {
//        super.moveEntityWithHeading(strafe, forward);
//    }

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
        if (this.noClip || this.sleeping) {
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
            return isOnLadder;
        }
        else {
            return super.isOnLadder();
        }
    }

    //TODO: Replace with AT on the EntityPlayerSP method?
    // Required so we can call this method from inside this package, this will end up calling EntityPlayerSP::func_189810_i instead
    @SideOnly(Side.CLIENT)
    protected void func_189810_i(float p_189810_1_, float p_189810_2_) {
        FMLLog.warning("Erroneously tried to call func_189810_i(auto-jump method) from " + this);
//        throw new RuntimeException("Unreachable code reached");
    }

    @Override
    public void knockBack(Entity attacker, float strength, double xRatio, double zRatio) {
//        FMLLog.info("x:%s, z:%s", xRatio, zRatio);
        if (attacker != null) {
            // Standard mob attacks, also arrows, not sure about others
            if (xRatio == attacker.posX - this.posX && zRatio == attacker.posZ - this.posZ) {
//                FMLLog.info("Distance based attack");
                EnumGravityDirection direction = API.getGravityDirection(this).getInverseAdjustMentFromDOWNDirection();
                double[] d_attacker = direction.adjustXYZValues(attacker.posX, attacker.posY, attacker.posZ);
                double[] d_player = direction.adjustXYZValues(this.posX, this.posY, this.posZ);
                xRatio = d_attacker[0] - d_player[0];
                zRatio = d_attacker[2] - d_player[2];
            }
            // Usually the knockback enchantment
            else if (xRatio == (double)MathHelper.sin(attacker.rotationYaw * 0.017453292F) && zRatio == (double)-MathHelper.cos(attacker.rotationYaw * 0.017453292F)) {
//                FMLLog.info("Rotation based attack");
                Vec3d lookVec = attacker.getLookVec();
                EnumGravityDirection direction = API.getGravityDirection(this);
                Vec3d adjustedLook = direction.adjustLookVec(lookVec);
                xRatio = -adjustedLook.xCoord;
                zRatio = -adjustedLook.zCoord;
            }
            else {
//                FMLLog.info("Unknown attack");
            }
        }
        super.knockBack(attacker, strength, xRatio, zRatio);
    }

}
