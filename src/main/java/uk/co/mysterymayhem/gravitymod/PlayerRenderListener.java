package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class PlayerRenderListener {

    private EntityPlayer player = null;
    private boolean rotationNeedsUndo = false;

    @SubscribeEvent
    public void onRender(RenderPlayerEvent.Pre event) {
        EntityPlayer entityPlayer = event.getEntityPlayer();
        if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(entityPlayer)) {
            this.rotationNeedsUndo = true;

            GlStateManager.pushMatrix();
            GlStateManager.rotate(180, 1, 0, 0);
            GlStateManager.rotate(180, 0, 1, 0);
            GlStateManager.translate(0, -entityPlayer.height, 0);

            this.player = entityPlayer;
            player.rotationYawHead *= -1;
            player.rotationYaw *= -1;
            player.rotationPitch *= -1;
            player.prevRotationPitch *= -1;
            player.prevRotationYaw *= -1;
            player.renderYawOffset *= -1;
            player.prevRenderYawOffset *= -1;
            player.prevRotationYawHead *= -1;
        }

        //if (entityPlayer == Minecraft.getMinecraft().thePlayer)




        //entityPlayer.cameraYaw += 180;
        //entityPlayer.cameraPitch += 180;
        //entityPlayer.prevCameraYaw += 180;
        //entityPlayer.prevCameraPitch += 180;

    }

    @SubscribeEvent
    public void onRender(RenderPlayerEvent.Post event) {
        if (this.rotationNeedsUndo) {
            this.rotationNeedsUndo = false;
            player.rotationYawHead *= -1;
            player.rotationYaw *= -1;
            player.rotationPitch *= -1;
            player.prevRotationPitch *= -1;
            player.prevRotationYaw *= -1;
            player.renderYawOffset *= -1;
            player.prevRenderYawOffset *= -1;
            player.prevRotationYawHead *= -1;
            GlStateManager.popMatrix();
        }
        //player.rotationYawHead -= 180;
        //player.rotationYaw -= 180;
        //player.rotationPitch -= 180;
        //player.cameraYaw -= 180;
        //player.cameraPitch -= 180;
        //player.prevCameraYaw -= 180;
        //player.prevCameraPitch -= 180;
        //player.prevRotationPitch -= 180;
        //player.prevRotationYaw -= 180;
        //player.prevRotationYawHead -= 180;
    }
}
