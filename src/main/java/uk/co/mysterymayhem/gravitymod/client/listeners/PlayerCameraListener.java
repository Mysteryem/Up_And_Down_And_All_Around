package uk.co.mysterymayhem.gravitymod.client.listeners;

import net.minecraft.client.Minecraft;
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
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper;

import static uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper.PITCH;

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

    //TODO: Add the movement of the player's eyes to the interpolation between old and new gravity direction
    //TODO: Work out the camera roll amount needed to interpolate between the old and new gravity directions instead of the slightly crazy current interpolation
    // It may be possible to work out the upwards vector of the player before and after, then, the angle between them
    // should give the camera roll.
    // Not going to clean up this class until the above is working
    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)renderViewEntity;
            IGravityDirectionCapability capability = GravityDirectionCapability.getGravityCapability(player);
            EnumGravityDirection gravityDirection = capability.getDirection();

            float transitionRollAmount = 0;
            int timeoutTicks = capability.getTimeoutTicks();
            double effectiveTimeoutTicks = timeoutTicks - (1 * event.getRenderPartialTicks());

            double interpolatedPitch = (player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
            double interpolatedYaw = (player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());

            interpolatedPitch %= 360;
            interpolatedYaw %= 360;

            Vec3d interpolatedLookVec = Vec3dHelper.getPreciseVectorForRotation(interpolatedPitch, interpolatedYaw);
            Vec3d relativeInterpolatedLookVec = gravityDirection.getInverseAdjustmentFromDOWNDirection().adjustLookVec(interpolatedLookVec);
            double[] precisePitchAndYawFromVector = Vec3dHelper.getPrecisePitchAndYawFromVector(relativeInterpolatedLookVec);

            double relativeInterpolatedPitch = precisePitchAndYawFromVector[Vec3dHelper.PITCH];
            double relativeInterpolatedYaw = precisePitchAndYawFromVector[Vec3dHelper.YAW];

            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {

                double rotationAngle;

                // We don't want to run all this code every render tick, so we store the angle to rotate by over the transition
                if (!capability.hasTransitionAngle()) {
                    double yaw = player.rotationYaw;
                    double pitch = player.rotationPitch;

                    // Get the absolute look vector
                    Vec3d absoluteLookVec = Vec3dHelper.getPreciseVectorForRotation(pitch, yaw);

                    // Get the relative look vector for the current gravity direction
                    Vec3d relativeCurrentLookVector = gravityDirection.getInverseAdjustmentFromDOWNDirection().adjustLookVec(absoluteLookVec);
                    // Get the pitch and yaw from the relative look vector
                    double[] pitchAndYawRelativeCurrentLook = Vec3dHelper.getPrecisePitchAndYawFromVector(relativeCurrentLookVector);
                    // Pitch - 90, -90 changes it from the forwards direction to the upwards direction
                    double relativeCurrentPitch = pitchAndYawRelativeCurrentLook[Vec3dHelper.PITCH] - 90;
                    // Yaw
                    double relativeCurrentYaw = pitchAndYawRelativeCurrentLook[Vec3dHelper.YAW];
                    // Get the relative upwards vector
                    Vec3d relativeCurrentUpVector = Vec3dHelper.getPreciseVectorForRotation(
                            relativeCurrentPitch, relativeCurrentYaw);
                    // Get the absolute vector for the relative upwards vector
                    Vec3d absoluteCurrentUpVector = gravityDirection.adjustLookVec(relativeCurrentUpVector);

                    // Get the relative look vector for the previous gravity direction
                    Vec3d relativePrevLookVector = capability.getPrevDirection().getInverseAdjustmentFromDOWNDirection().adjustLookVec(absoluteLookVec);
                    // Get the pitch and yaw from the relative look vector
                    double[] pitchAndYawRelativePrevLook = Vec3dHelper.getPrecisePitchAndYawFromVector(relativePrevLookVector);
                    // Pitch - 90, -90 changes it from the forwards direction to the upwards direction
                    double relativePrevPitch = pitchAndYawRelativePrevLook[Vec3dHelper.PITCH] - 90;
                    // Yaw
                    double relativePrevYaw = pitchAndYawRelativePrevLook[Vec3dHelper.YAW];
                    // Get the relative upwards vector
                    Vec3d relativePrevUpVector = Vec3dHelper.getPreciseVectorForRotation(
                            relativePrevPitch, relativePrevYaw);
                    // Get the absolute vector for the relative upwards vector
                    Vec3d absolutePrevUpVector = capability.getPrevDirection().adjustLookVec(relativePrevUpVector);

//                    FMLLog.info("\n" + absoluteCurrentUpVector + ", " + absolutePrevUpVector + "\n");
//                    FMLLog.info("Angle between up vectors: " + (180d/Math.PI)*Math.acos(absoluteCurrentUpVector.dotProduct(absolutePrevUpVector)));

//                    // Angle between the two up vectors (the result of the dotproduct is clamped between [-1,1] to prevent Math.acos returning NaN when the dot product
//                    // goes outside of [-1,1] due to precision erors
//                    double radAngleBetweenUpVecs = Math.acos(MathHelper.clamp_double(absoluteCurrentUpVector.dotProduct(absolutePrevUpVector), -1d, 1d));
//                    // Cross product for the vector between the two up vectors
//                    Vec3d crossProductOfAbsoluteUpVecs = absoluteCurrentUpVector.crossProduct(absolutePrevUpVector);
//
//                    //
//                    double signDecider = absoluteLookVec.dotProduct(crossProductOfAbsoluteUpVecs);
//                    if (signDecider < 0) {
//                        if (radAngleBetweenUpVecs > 0) {
//                            radAngleBetweenUpVecs = -radAngleBetweenUpVecs;
//                        }
//                    }
//                    rotationAngle = radAngleBetweenUpVecs * (180d/Math.PI);

                    //See http://stackoverflow.com/a/33920320 for the maths
                    rotationAngle = (180d/Math.PI) * Math.atan2(
                            absoluteCurrentUpVector.crossProduct(absolutePrevUpVector).dotProduct(absoluteLookVec),
                            absoluteCurrentUpVector.dotProduct(absolutePrevUpVector));

                    capability.setTransitionAngle(rotationAngle);
                }
                else {
                    rotationAngle = capability.getTransitionAngle();
                }
                // Get relative upwards vector
                // Adjust for previous direction
                // Adjust for current direction
                // Calculate angle between them

//                Vec3d fastUpwardsVector = Vec3dHelper.getFastUpwardsVector((float) interpolatedPitch, (float) interpolatedYaw);

                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
                double denominator = rotationLength;
//
                double multiplier = 1 - numerator/denominator;

                transitionRollAmount = (float)(rotationAngle * multiplier);
//                transitionRollAmount = (float)(-rotationAngle);

//                FMLLog.info("Angle: " + (float)(rotationAngle));
//                FMLLog.info("Current up Vec: " + currentDirectionUpVec);
//                FMLLog.info("Prev up Vec: " + prevDirectionUpVec + ", " + capability.getPrevDirection().name());
//                FMLLog.info("Current up Vec: " + currentDirectionUpVec + ", " + gravityDirection.name());
//                FMLLog.info("Angle: " + transitionRollAmount);
//                FMLLog.info("Multiplier: " + multiplier);
//                transitionRollAmount = 0;
            }

            relativeInterpolatedPitch %= 360;
            relativeInterpolatedYaw %= 360;

            // Read these in reverse order
//            GlStateManager.rotate((float)interpolatedPitch, 1, 0, 0);
//            GlStateManager.rotate((float)interpolatedYaw, 0, 1, 0);
            // 5: Rotate by the relative player's pitch, this, combined with the camera transformation set the correct camera pitch
            GlStateManager.rotate((float)relativeInterpolatedPitch, 1, 0, 0);
            // 4: Rotate by the relative player's yaw, this, combined with the camera transformation sets the correct camera yaw
            GlStateManager.rotate((float)relativeInterpolatedYaw, 0, 1, 0);

            // 3: Now that our look direction is effectively 0 yaw and 0 pitch, perform the rotation specific for this gravity
            gravityDirection.runCameraTransformation();
//            GlStateManager.rotate((float)-relativeInterpolatedYaw, 0, 1, 0);
//            GlStateManager.rotate((float)-relativeInterpolatedPitch, 1, 0, 0);
            // 2: Undo the absolute yaw rotation of the player
            GlStateManager.rotate((float)-interpolatedYaw, 0, 1, 0);
            // 1: Undo the absolute pitch rotation of the player
            GlStateManager.rotate((float)-interpolatedPitch, 1, 0, 0);
            event.setRoll(event.getRoll() + transitionRollAmount);
        }
//        player.chasingPosX;
//        player.isSpectator();

//        event.getRenderPartialTicks();
//
//        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
//        if (renderViewEntity instanceof EntityPlayer) {
//            EntityPlayer player = (EntityPlayer)renderViewEntity;
//            IGravityDirectionCapability capability = GravityDirectionCapability.getGravityCapability(player);
//            if (capability == null) {
//                return;
//            }
//            float transitionRollAmount = 0;
//            int timeoutTicks = capability.getTimeoutTicks();
//            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(player);
//            double effectiveTimeoutTicks = timeoutTicks - (1 * event.getRenderPartialTicks());
////            if (timeoutTicks != 0 && (effectiveTimeoutTicks) > rotationEnd) {
////                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
////                double denominator = rotationLength;
////
////                double multiplier = 1 - numerator/denominator;
////                switch (capability.getPrevDirection()) {
////                    case UP:
////                        if (gravityDirection == EnumGravityDirection.DOWN) {
//////                            multiplier = (timeoutTicks - 1 * event.getRenderPartialTicks())/(double)GravityDirectionCapability.DEFAULT_TIMEOUT;
//////                            multiplier = 3;
////                            transitionRollAmount = (float)(-180 * multiplier);
////                        }
////                        break;
////                    case DOWN:
////                        if (gravityDirection == EnumGravityDirection.UP) {
////                            transitionRollAmount = (float)(180 * multiplier);
////                        }
////                        break;
////                    case SOUTH:
////                        break;
////                    case WEST:
////                        break;
////                    case NORTH:
////                        break;
//////                    case EAST:
////                    default:
////                        break;
////                }
////            }
//
//            double interpolatedPitch = (player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
//            double interpolatedYaw = (player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());
////            float interpolatedYawHead = (float)(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.getRenderPartialTicks());
//
//            interpolatedPitch %= 360;
//            interpolatedYaw %= 360;
//
//            double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(interpolatedYaw, interpolatedPitch, player);
//            double relativeInterpolatedPitch = relativeYawAndPitch[Hooks.PITCH];
//            double relativeInterpolatedYaw = relativeYawAndPitch[Hooks.YAW];
//
//            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
//                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
//                double denominator = rotationLength;
////
//                double multiplier =  numerator/denominator;
//
//
//                double f = Math.cos(-interpolatedYaw * (Math.PI / 180d) - Math.PI);
//                double f1 = Math.sin(-interpolatedYaw * (Math.PI / 180d) - Math.PI);
//                double f2 = -Math.cos(-interpolatedPitch * (Math.PI/180d));
//                double f3 = Math.sin(-interpolatedPitch * (Math.PI/180d));
//
//                Vec3d lookVecWithDoubleAccuracy =  new Vec3d(f1 * f2, f3, f * f2);
//
//                Vec3d adjustedVec = capability.getPrevDirection().getInverseAdjustmentFromDOWNDirection().adjustLookVec(lookVecWithDoubleAccuracy);
//                double prevRelativeYaw = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//                double prevRelativePitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//
//                relativeInterpolatedPitch = (prevRelativePitch + (relativeInterpolatedPitch - prevRelativePitch) * multiplier);
//                relativeInterpolatedYaw = (prevRelativeYaw + (relativeInterpolatedYaw - prevRelativeYaw) * multiplier);
//            }
//
////            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
////                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
////                double denominator = rotationLength;
//////
////                double multiplier = 1- numerator/denominator;
////
////                // Current direction interpolated absolute look vec
////                Vec3d currentVec = Vec3dHelper.getPreciseVectorForRotation(interpolatedPitch, interpolatedYaw);
////
////                // Prev direction interpolated relative look vec
////                Vec3d adjustedVec = capability.getPrevDirection().getInverseAdjustmentFromDOWNDirection().adjustLookVec(currentVec);
////
////                // Current direction interpolated relative look vec
////                Vec3d relativeCurrentVec = gravityDirection.getInverseAdjustmentFromDOWNDirection().adjustLookVec(currentVec);
////
//////                FMLLog.info("\nCurrent: " + relativeCurrentVec + "\nPrevious: " + adjustedVec + "\nAbsolute: " + currentVec);
////
////
////                relativeCurrentVec = new Vec3d(relativeCurrentVec.xCoord, 0, 0).normalize();
////                adjustedVec = new Vec3d(adjustedVec.xCoord, 0, 0).normalize();
////                FMLLog.info("" + relativeCurrentVec.lengthVector() + ", " + adjustedVec.lengthVector());
////                FMLLog.info("" + relativeCurrentVec.dotProduct(adjustedVec));// * 180/Math.PI);
////                FMLLog.info("" + relativeCurrentVec.dotProduct(adjustedVec) * 180/Math.PI);
////                FMLLog.info("" + Math.acos(MathHelper.clamp_double(relativeCurrentVec.dotProduct(adjustedVec), -1.0, 1.0)) * 180/Math.PI);
////
////                transitionRollAmount = -(float)(multiplier * Math.acos(MathHelper.clamp_double(relativeCurrentVec.dotProduct(adjustedVec), -1.0, 1.0)) * 180/Math.PI);
////
//////                double prevRelativeYaw = (Math.atan2(-adjustedVec.xCoord, adjustedVec.zCoord) * (180D/Math.PI));
//////                double prevRelativePitch = -(Math.asin(adjustedVec.yCoord) * (180D/Math.PI));
//////
//////                relativeInterpolatedPitch = (prevRelativePitch + (relativeInterpolatedPitch - prevRelativePitch) * multiplier);
//////                relativeInterpolatedYaw = (prevRelativeYaw + (relativeInterpolatedYaw - prevRelativeYaw) * multiplier);
//////
//////                double cosOfAngle = Vec3dHelper.getPreciseVectorForRotation(relativeInterpolatedPitch, relativeInterpolatedYaw).dotProduct(adjustedVec);
//////                double angleBetweenVecs = Math.acos(cosOfAngle);
//////                transitionRollAmount = (float)angleBetweenVecs;
////            }
//
//            relativeInterpolatedPitch %= 360;
//            relativeInterpolatedYaw %= 360;
//
//            //TODO: Is it the relativeInterpolated values that need to also be interpolated between the previous and currect direction?
//
//            // Read these in reverse order
////            GlStateManager.rotate((float)interpolatedPitch, 1, 0, 0);
////            GlStateManager.rotate((float)interpolatedYaw, 0, 1, 0);
//            // 5: Rotate by the relative player's pitch, this, combined with the camera transformation set the correct camera pitch
//            GlStateManager.rotate((float)relativeInterpolatedPitch, 1, 0, 0);
//            // 4: Rotate by the relative player's yaw, this, combined with the camera transformation sets the correct camera yaw
//            GlStateManager.rotate((float)relativeInterpolatedYaw, 0, 1, 0);
//
//            // 3: Now that our look direction is effectively 0 yaw and 0 pitch, perform the rotation specific for this gravity
//            if (timeoutTicks != 0 && effectiveTimeoutTicks > rotationEnd) {
//                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
//                double denominator = rotationLength;
////
//                double multiplier = numerator/denominator;
//
//                Vec3i prevVars = capability.getPrevDirection().getCameraTransformVars();
//                Vec3i currentVars = gravityDirection.getCameraTransformVars();
////                float xRot = (float)(currentVars.getX() + (prevVars.getX() - currentVars.getX()) * multiplier);
////                float yRot = (float)(currentVars.getY() + (prevVars.getY() - currentVars.getY()) * multiplier);
////                float zRot = (float)(currentVars.getZ() + (prevVars.getZ() - currentVars.getZ()) * multiplier);
//
//                float xRot = (float)(prevVars.getX() + (currentVars.getX() - prevVars.getX()) * multiplier);
//                float yRot = (float)(prevVars.getY() + (currentVars.getY() - prevVars.getY()) * multiplier);
//                float zRot = (float)(prevVars.getZ() + (currentVars.getZ() - prevVars.getZ()) * multiplier);
//
//                if (xRot != 0) {
//                    GlStateManager.rotate(xRot, 1, 0, 0);
//                }
//                if (yRot != 0) {
//                    GlStateManager.rotate(yRot, 0, 1, 0);
//                }
//                if (zRot != 0) {
//                    GlStateManager.rotate(zRot, 0, 0, 1);
//                }
//            }
//            else {
//                gravityDirection.runCameraTransformation();
//            }
////            gravityDirection.runCameraTransformation();
////            GlStateManager.rotate((float)-relativeInterpolatedYaw, 0, 1, 0);
////            GlStateManager.rotate((float)-relativeInterpolatedPitch, 1, 0, 0);
//            // 2: Undo the absolute yaw rotation of the player
//            GlStateManager.rotate((float)-interpolatedYaw, 0, 1, 0);
//            // 1: Undo the absolute pitch rotation of the player
//            GlStateManager.rotate((float)-interpolatedPitch, 1, 0, 0);
////            event.setRoll(event.getRoll() + 90);
//            event.setRoll(event.getRoll() + transitionRollAmount);
//        }
////        player.chasingPosX;
////        player.isSpectator();

    }
}
