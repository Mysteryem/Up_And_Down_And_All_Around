package uk.co.mysterymayhem.gravitymod.client.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.util.Vec3dHelper;

/**
 * Used to roll the player's camera according to their current gravity
 * <p>
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class PlayerCameraListener {

    //TODO: Un-hardcode (remember to force >=1)
    private static final double rotationSpeed = 2;
    private static final double rotationLength = GravityDirectionCapability.DEFAULT_TIMEOUT / rotationSpeed;
    private static final double rotationEnd = GravityDirectionCapability.DEFAULT_TIMEOUT - rotationLength;

    //TODO: Add the movement of the player's eyes to the interpolation between old and new gravity direction
    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {
        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) renderViewEntity;
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

                    //See http://stackoverflow.com/a/33920320 for the maths
                    rotationAngle = (180d / Math.PI) * Math.atan2(
                            absoluteCurrentUpVector.crossProduct(absolutePrevUpVector).dotProduct(absoluteLookVec),
                            absoluteCurrentUpVector.dotProduct(absolutePrevUpVector));

                    capability.setTransitionAngle(rotationAngle);
                }
                else {
                    rotationAngle = capability.getTransitionAngle();
                }

                double numerator = GravityDirectionCapability.DEFAULT_TIMEOUT - effectiveTimeoutTicks;
                double denominator = rotationLength;

                double multiplier = 1 - numerator / denominator;

                transitionRollAmount = (float) (rotationAngle * multiplier);
            }

            relativeInterpolatedPitch %= 360;
            relativeInterpolatedYaw %= 360;

            // Read these in reverse order
            // 5: Rotate by the relative player's pitch, this, combined with the camera transformation set the correct camera pitch
            GlStateManager.rotate((float) relativeInterpolatedPitch, 1, 0, 0);
            // 4: Rotate by the relative player's yaw, this, combined with the camera transformation sets the correct camera yaw
            GlStateManager.rotate((float) relativeInterpolatedYaw, 0, 1, 0);

            // 3: Now that our look direction is effectively 0 yaw and 0 pitch, perform the rotation specific for this gravity
            gravityDirection.runCameraTransformation();
            // 2: Undo the absolute yaw rotation of the player
            GlStateManager.rotate((float) -interpolatedYaw, 0, 1, 0);
            // 1: Undo the absolute pitch rotation of the player
            GlStateManager.rotate((float) -interpolatedPitch, 1, 0, 0);
            event.setRoll(event.getRoll() + transitionRollAmount);
        }

    }
}
