package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.asm.EntityPlayerWithGravity;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.capabilities.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.util.Vec3dHelper;

/**
 * Used to roll the player's camera according to their current gravity
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class PlayerCameraListener {

    private static final double rotationSpeed = 2;
    private static final double rotationLength = GravityDirectionCapability.DEFAULT_TIMEOUT/rotationSpeed;
    private static final double rotationEnd = GravityDirectionCapability.DEFAULT_TIMEOUT - rotationLength;

    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {
        event.getRenderPartialTicks();

        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)renderViewEntity;
            IGravityDirectionCapability capability = GravityDirectionCapability.getGravityCapability(player);
            if (capability == null) {
                return;
            }
            float transitionRollAmount = 0;
            int timeoutTicks = capability.getTimeoutTicks();
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(player);
            double effectiveTimeoutTicks = timeoutTicks - (1 * event.getRenderPartialTicks());
//            if (timeoutTicks != 0 && (effectiveTimeoutTicks) > rotationEnd) {
//                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
//                double denominator = rotationLength;
//
//                double multiplier = 1 - numerator/denominator;
//                switch (capability.getPrevDirection()) {
//                    case UP:
//                        if (gravityDirection == EnumGravityDirection.DOWN) {
////                            multiplier = (timeoutTicks - 1 * event.getRenderPartialTicks())/(double)GravityDirectionCapability.DEFAULT_TIMEOUT;
////                            multiplier = 3;
//                            transitionRollAmount = (float)(-180 * multiplier);
//                        }
//                        break;
//                    case DOWN:
//                        if (gravityDirection == EnumGravityDirection.UP) {
//                            transitionRollAmount = (float)(180 * multiplier);
//                        }
//                        break;
//                    case SOUTH:
//                        break;
//                    case WEST:
//                        break;
//                    case NORTH:
//                        break;
////                    case EAST:
//                    default:
//                        break;
//                }
//            }

            double interpolatedPitch = (player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
            double interpolatedYaw = (player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());
//            float interpolatedYawHead = (float)(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.getRenderPartialTicks());

            interpolatedPitch %= 360;
            interpolatedYaw %= 360;

            double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(interpolatedYaw, interpolatedPitch, player);
            double relativeInterpolatedPitch = relativeYawAndPitch[Hooks.PITCH];
            double relativeInterpolatedYaw = relativeYawAndPitch[Hooks.YAW];

            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
                double denominator = rotationLength;
//
                double multiplier =  numerator/denominator;


                double f = Math.cos(-interpolatedYaw * (Math.PI / 180d) - Math.PI);
                double f1 = Math.sin(-interpolatedYaw * (Math.PI / 180d) - Math.PI);
                double f2 = -Math.cos(-interpolatedPitch * (Math.PI/180d));
                double f3 = Math.sin(-interpolatedPitch * (Math.PI/180d));

                Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);

                Vec3d adjustedVec = capability.getPrevDirection().getInverseAdjustMentFromDOWNDirection().adjustLookVec(lookVecWithDoubleAccuracy);
                double prevRelativeYaw = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
                double prevRelativePitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));

                relativeInterpolatedPitch = (prevRelativePitch + (relativeInterpolatedPitch - prevRelativePitch) * multiplier);
                relativeInterpolatedYaw = (prevRelativeYaw + (relativeInterpolatedYaw - prevRelativeYaw) * multiplier);
            }

//            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
//                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
//                double denominator = rotationLength;
////
//                double multiplier = 1- numerator/denominator;
//
//                // Current direction interpolated absolute look vec
//                Vec3d currentVec = Vec3dHelper.getPreciseVectorForRotation(interpolatedPitch, interpolatedYaw);
//
//                // Prev direction interpolated relative look vec
//                Vec3d adjustedVec = capability.getPrevDirection().getInverseAdjustMentFromDOWNDirection().adjustLookVec(currentVec);
//
//                // Current direction interpolated relative look vec
//                Vec3d relativeCurrentVec = gravityDirection.getInverseAdjustMentFromDOWNDirection().adjustLookVec(currentVec);
//
////                FMLLog.info("\nCurrent: " + relativeCurrentVec + "\nPrevious: " + adjustedVec + "\nAbsolute: " + currentVec);
//
//
//                relativeCurrentVec = new Vec3d(relativeCurrentVec.xCoord, 0, 0).normalize();
//                adjustedVec = new Vec3d(adjustedVec.xCoord, 0, 0).normalize();
//                FMLLog.info("" + relativeCurrentVec.lengthVector() + ", " + adjustedVec.lengthVector());
//                FMLLog.info("" + relativeCurrentVec.dotProduct(adjustedVec));// * 180/Math.PI);
//                FMLLog.info("" + relativeCurrentVec.dotProduct(adjustedVec) * 180/Math.PI);
//                FMLLog.info("" + Math.acos(MathHelper.clamp_double(relativeCurrentVec.dotProduct(adjustedVec), -1.0, 1.0)) * 180/Math.PI);
//
//                transitionRollAmount = -(float)(multiplier * Math.acos(MathHelper.clamp_double(relativeCurrentVec.dotProduct(adjustedVec), -1.0, 1.0)) * 180/Math.PI);
//
////                double prevRelativeYaw = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
////                double prevRelativePitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
////
////                relativeInterpolatedPitch = (prevRelativePitch + (relativeInterpolatedPitch - prevRelativePitch) * multiplier);
////                relativeInterpolatedYaw = (prevRelativeYaw + (relativeInterpolatedYaw - prevRelativeYaw) * multiplier);
////
////                double cosOfAngle = Vec3dHelper.getPreciseVectorForRotation(relativeInterpolatedPitch, relativeInterpolatedYaw).dotProduct(adjustedVec);
////                double angleBetweenVecs = Math.acos(cosOfAngle);
////                transitionRollAmount = (float)angleBetweenVecs;
//            }

            relativeInterpolatedPitch %= 360;
            relativeInterpolatedYaw %= 360;

            //TODO: Is it the relativeInterpolated values that need to also be interpolated between the previous and currect direction?

            // Read these in reverse order
//            GlStateManager.rotate((float)interpolatedPitch, 1, 0, 0);
//            GlStateManager.rotate((float)interpolatedYaw, 0, 1, 0);
            // 5: Rotate by the relative player's pitch, this, combined with the camera transformation set the correct camera pitch
            GlStateManager.rotate((float)relativeInterpolatedPitch, 1, 0, 0);
            // 4: Rotate by the relative player's yaw, this, combined with the camera transformation sets the correct camera yaw
            GlStateManager.rotate((float)relativeInterpolatedYaw, 0, 1, 0);

            // 3: Now that our look direction is effectively 0 yaw and 0 pitch, perform the rotation specific for this gravity
            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
                double denominator = rotationLength;
//
                double multiplier = numerator/denominator;

                Vec3i prevVars = capability.getPrevDirection().getCameraTransformVars();
                Vec3i currentVars = gravityDirection.getCameraTransformVars();
//                float xRot = (float)(currentVars.getX() + (prevVars.getX() - currentVars.getX()) * multiplier);
//                float yRot = (float)(currentVars.getY() + (prevVars.getY() - currentVars.getY()) * multiplier);
//                float zRot = (float)(currentVars.getZ() + (prevVars.getZ() - currentVars.getZ()) * multiplier);

                float xRot = (float)(prevVars.getX() + (currentVars.getX() - prevVars.getX()) * multiplier);
                float yRot = (float)(prevVars.getY() + (currentVars.getY() - prevVars.getY()) * multiplier);
                float zRot = (float)(prevVars.getZ() + (currentVars.getZ() - prevVars.getZ()) * multiplier);

                if (xRot != 0) {
                    GlStateManager.rotate(xRot, 1, 0, 0);
                }
                if (yRot != 0) {
                    GlStateManager.rotate(yRot, 0, 1, 0);
                }
                if (zRot != 0) {
                    GlStateManager.rotate(zRot, 0, 0, 1);
                }
            }
            else {
                gravityDirection.runCameraTransformation();
            }
//            gravityDirection.runCameraTransformation();
//            GlStateManager.rotate((float)-relativeInterpolatedYaw, 0, 1, 0);
//            GlStateManager.rotate((float)-relativeInterpolatedPitch, 1, 0, 0);
            // 2: Undo the absolute yaw rotation of the player
            GlStateManager.rotate((float)-interpolatedYaw, 0, 1, 0);
            // 1: Undo the absolute pitch rotation of the player
            GlStateManager.rotate((float)-interpolatedPitch, 1, 0, 0);
//            event.setRoll(event.getRoll() + 90);
            event.setRoll(event.getRoll() + transitionRollAmount);
        }
//        player.chasingPosX;
//        player.isSpectator();

    }
}
