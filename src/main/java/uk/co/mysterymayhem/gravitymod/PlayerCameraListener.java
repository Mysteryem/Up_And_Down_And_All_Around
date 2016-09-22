package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

/**
 * Used to roll the player's camera according to their current gravity
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class PlayerCameraListener {

    //TODO: Begin replacing with rotations as defined in EnumGravityDirection
    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

//        Minecraft.getMinecraft().getRenderViewEntity();
//        player.chasingPosX;
        player.isSpectator();

        EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(player);

        float interpolatedPitch = (float)(player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
        float interpolatedYaw = (float)(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());
        //float interpolatedYawHead = (float)(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.getRenderPartialTicks());

        GlStateManager.rotate(interpolatedPitch, 1, 0, 0);
        GlStateManager.rotate(interpolatedYaw, 0, 1, 0);
        gravityDirection.runCameraTransformation();
        GlStateManager.rotate(-interpolatedYaw, 0, 1, 0);
        GlStateManager.rotate(-interpolatedPitch, 1, 0, 0);
    }
}
