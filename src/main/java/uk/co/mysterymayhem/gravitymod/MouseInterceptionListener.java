package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;

/**
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class MouseInterceptionListener {

    private final MouseHelperWrapper wrapper;
    private boolean cleanupNeeded = false;

    //Must be created in Init or later otherwise this is null
    public MouseInterceptionListener () {
        this.wrapper = new MouseHelperWrapper(Minecraft.getMinecraft().mouseHelper);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientRenderTick(RenderTickEvent event){
        Minecraft mc = Minecraft.getMinecraft();
        if (Display.isActive()
                && mc.inGameHasFocus
                && mc.thePlayer.getRidingEntity() == null
                && GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(mc.thePlayer)
                //&& GravityManagerClient.isClientUpsideDown()
                && event.phase == TickEvent.Phase.START) {
            mc.mouseHelper = wrapper;
            this.cleanupNeeded = true;
        }
        else if (cleanupNeeded == true && event.phase == TickEvent.Phase.END) {
            mc.mouseHelper = wrapper.getMouseHelper();
        }
    }

//    @SubscribeEvent(priority = EventPriority.HIGHEST)
//    public void onClientRenderTick(RenderTickEvent event) {
//        if (GravityManagerClient.isClientUpsideDown()) {
//            if (event.phase == TickEvent.Phase.START) {
//
//                Minecraft mc = Minecraft.getMinecraft();
//                boolean flag = Display.isActive();
//                EntityRenderer renderer = mc.entityRenderer;
//                if (mc.inGameHasFocus && flag)
//                {
//                    if (mc.thePlayer.getRidingEntity() == null)
//                        mc.mouseHelper.mouseXYChange();
//                    float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
//                    float f1 = f * f * f * 8.0F;
//                    float f2 = (float)mc.mouseHelper.deltaX * f1;
//                    float f3 = (float)mc.mouseHelper.deltaY * f1;
//                    int i = 1;
//
//                    if (mc.gameSettings.invertMouse)
//                    {
//                        i = -1;
//                    }
//                    Vec3d lookVec = mc.thePlayer.getLookVec();
//                    //Vec3d otherVec = mc.thePlayer.func_189651_aD()
//
//
//                    switch (clientGravity) {
//                        case NORTH:
//                            //float currentPitch = mc.thePlayer.rotationPitch;
//                            //break;
//                        case SOUTH:
//                            //break;
//                        default:
//                            float[] processedAngles = clientGravity.yawAndPitchMouseInputConverter.process(f2, f3 * (float) i);
//                            f2 = processedAngles[0];
//                            f3 = processedAngles[1];
//                            break;
//                    }
//
//                    setAngles(mc.thePlayer, f2, f3, clientGravity);
//
//                    EntityPlayerSP player = mc.thePlayer;
//                    float yaw = f2;
//                    float pitch = f3;
//
//                    float playerRotationPitch = player.rotationPitch;
//                    float playerRotationYaw = player.rotationYaw;
//
//                    //player.rotationYaw = (float)();
//
//                    player.rotationYaw = (float)((double)player.rotationYaw + (double)yaw * 0.15D);
//                    player.rotationPitch = (float)((double)player.rotationPitch - (double)pitch * 0.15D);
//                    float[] clampedAngles = clientGravity.resultantAngleClampFunction.process(player.rotationYaw, player.rotationPitch);
//                    //player.rotationPitch = MathHelper.clamp_float(player.rotationPitch, -90.0F, 90.0F);
//                    player.rotationYaw = clampedAngles[0];
//                    player.rotationPitch = clampedAngles[1];
//                    player.prevRotationPitch += player.rotationPitch - playerRotationPitch;
//                    player.prevRotationYaw += player.rotationYaw - playerRotationYaw;
//
////                if (mc.gameSettings.smoothCamera)
////                {
////                    renderer.smoothCamYaw += f2;
////                    renderer.smoothCamPitch += f3;
////                    float f4 = partialTicks - renderer.smoothCamPartialTicks;
////                    renderer.smoothCamPartialTicks = partialTicks;
////                    f2 = renderer.smoothCamFilterX * f4;
////                    f3 = renderer.smoothCamFilterY * f4;
////                    renderer.mc.thePlayer.setAngles(f2, f3 * (float)i);
////                }
////                else
////                {
////                    renderer.smoothCamYaw = 0.0F;
////                    renderer.smoothCamPitch = 0.0F;
////                    renderer.mc.thePlayer.setAngles(f2, f3 * (float)i);
////                }
//                }
//            }
//        }
//
//    }

//    /**
//     * Adds 15% to the entity's yaw and subtracts 15% from the pitch. Clamps pitch from -90 to 90. Both arguments in
//     * degrees.
//     */
//    @SideOnly(Side.CLIENT)
//    public void setAngles(EntityPlayerSP player, float yaw, float pitch, EnumGravityDirection clientGravity)
//    {
//        float playerRotationPitch = player.rotationPitch;
//        float playerRotationYaw = player.rotationYaw;
//        player.rotationYaw = (float)((double)player.rotationYaw + (double)yaw * 0.15D);
//        player.rotationPitch = (float)((double)player.rotationPitch - (double)pitch * 0.15D);
//        float[] clampedAngles = clientGravity.resultantAngleClampFunction.process(player.rotationYaw, player.rotationPitch);
//        //player.rotationPitch = MathHelper.clamp_float(player.rotationPitch, -90.0F, 90.0F);
//        player.rotationYaw = clampedAngles[0];
//        player.rotationPitch = clampedAngles[1];
//        player.prevRotationPitch += player.rotationPitch - playerRotationPitch;
//        player.prevRotationYaw += player.rotationYaw - playerRotationYaw;
//
//    }

//    public Vec3d rotatePitch(Vec3d vec3d, float pitch)
//    {
//        float f = MathHelper.cos(pitch);
//        float f1 = MathHelper.sin(pitch);
//        double d0 = vec3d.xCoord;
//        double d1 = vec3d.yCoord * (double)f + vec3d.zCoord * (double)f1;
//        double d2 = vec3d.zCoord * (double)f - vec3d.yCoord * (double)f1;
//        return new Vec3d(d0, d1, d2);
//    }
//
//    public Vec3d rotateYaw(Vec3d vec3d, float yaw)
//    {
//        float f = MathHelper.cos(yaw);
//        float f1 = MathHelper.sin(yaw);
//        double d0 = vec3d.xCoord * (double)f + vec3d.zCoord * (double)f1;
//        double d1 = vec3d.yCoord;
//        double d2 = vec3d.zCoord * (double)f - vec3d.xCoord * (double)f1;
//        return new Vec3d(d0, d1, d2);
//    }


}
