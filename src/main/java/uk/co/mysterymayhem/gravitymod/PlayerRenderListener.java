package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;

/**
 * Created by Mysteryem on 2016-08-07.
 */
@SideOnly(Side.CLIENT)
public class PlayerRenderListener {

    private EntityPlayer player = null;
    private boolean rotationNeedsUndo = false;

    private boolean needToPop = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(RenderLivingEvent.Pre<EntityPlayer> event) {
        EntityLivingBase entity = event.getEntity();
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer)entity;
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(entityPlayer);
            GlStateManager.pushMatrix();
            GlStateManager.translate(event.getX(), event.getY(), event.getZ());
            gravityDirection.applyOtherPlayerRenderTransformations(entityPlayer);
//            gravityDirection.runCameraTransformation();

//            double interpolatedPitch = (player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * event.getRenderPartialTicks());
//            double interpolatedYaw = (player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * event.getRenderPartialTicks());
//            float interpolatedYawHead = (float)(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * event.getRenderPartialTicks());
//
//            double[] relativeYawAndPitch = Hooks.getRelativeYawAndPitch(interpolatedYaw, interpolatedPitch, player);
//            double relativeInterpolatedPitch = relativeYawAndPitch[Hooks.PITCH];
//            double relativeInterpolatedYaw = relativeYawAndPitch[Hooks.YAW];
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
            gravityDirection.runCameraTransformation();
//            GlStateManager.rotate((float)-relativeInterpolatedYaw, 0, 1, 0);
//            GlStateManager.rotate((float)-relativeInterpolatedPitch, 1, 0, 0);
//            // 2: Undo the absolute yaw rotation of the player
//            GlStateManager.rotate((float)-interpolatedYaw, 0, 1, 0);
//            // 1: Undo the absolute pitch rotation of the player
//            GlStateManager.rotate((float)-interpolatedPitch, 1, 0, 0);
            GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
            this.needToPop = true;

//            if (GravityCapability.getGravityDirection(entityPlayer) == EnumGravityDirection.UP) {
//                this.rotationNeedsUndo = true;
//
//                GlStateManager.pushMatrix();
//                GlStateManager.translate(event.getX(), event.getY(), event.getZ());
//                EnumGravityDirection.UP.applyRenderTransformations(entityPlayer);
////                GlStateManager.rotate(180, 1, 0, 0);
////                GlStateManager.rotate(180, 0, 1, 0);
//                GlStateManager.translate(0, -entityPlayer.height, 0);
//                GlStateManager.translate(-event.getX(), -event.getY(), -event.getZ());
//
//                this.player = entityPlayer;
////                player.rotationYawHead *= -1;
////                player.rotationYaw *= -1;
////                player.rotationPitch *= -1;
////                player.prevRotationPitch *= -1;
////                player.prevRotationYaw *= -1;
////                player.renderYawOffset *= -1;
////                player.prevRenderYawOffset *= -1;
////                player.prevRotationYawHead *= -1;
//            }

            //if (entityPlayer == Minecraft.getMinecraft().thePlayer)


            //entityPlayer.cameraYaw += 180;
            //entityPlayer.cameraPitch += 180;
            //entityPlayer.prevCameraYaw += 180;
            //entityPlayer.prevCameraPitch += 180;
        }

    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRender(RenderLivingEvent.Post<EntityPlayer> event) {
//        if (this.rotationNeedsUndo) {
//            this.rotationNeedsUndo = false;
//            player.rotationYawHead *= -1;
//            player.rotationYaw *= -1;
//            player.rotationPitch *= -1;
//            player.prevRotationPitch *= -1;
//            player.prevRotationYaw *= -1;
//            player.renderYawOffset *= -1;
//            player.prevRenderYawOffset *= -1;
//            player.prevRotationYawHead *= -1;
//
//        }
        if (this.needToPop) {
            this.needToPop = false;
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
