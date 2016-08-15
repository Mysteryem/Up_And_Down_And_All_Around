package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class PlayerRenderListener {

    private EntityPlayer player = null;
    private boolean rotationNeedsUndo = false;

    //TODO: begin replacing with differing transformations based on EnumGravityDirection
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRender(RenderLivingEvent.Pre<EntityPlayer> event) {
        EntityLivingBase entity = event.getEntity();
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;
            if (GravityCapability.getGravityDirection(entityPlayer) == EnumGravityDirection.UP) {
                this.rotationNeedsUndo = true;

                GlStateManager.pushMatrix();
                GlStateManager.translate(event.getX(), event.getY(), event.getZ());
                GlStateManager.rotate(180, 1, 0, 0);
                GlStateManager.rotate(180, 0, 1, 0);
                GlStateManager.translate(0, -entityPlayer.height, 0);
                GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());

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

    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRender(RenderLivingEvent.Post<EntityPlayer> event) {
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
