package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.util.Vec3dHelper;

/**
 * Used to roll the player's camera according to their current gravity
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class PlayerCameraListener {

    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {

        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
        if (renderViewEntity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)renderViewEntity;
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(player);

            double interpolatedPitch = (player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
            double interpolatedYaw = (player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());
            float interpolatedYawHead = (float)(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.getRenderPartialTicks());

            interpolatedPitch %= 360;
            interpolatedYaw %= 360;

            double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(interpolatedYaw, interpolatedPitch, player);
            double relativeInterpolatedPitch = relativeYawAndPitch[Hooks.PITCH];
            double relativeInterpolatedYaw = relativeYawAndPitch[Hooks.YAW];

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
//            event.setRoll(event.getRoll() + 90);
        }
//        player.chasingPosX;
//        player.isSpectator();

    }
}
