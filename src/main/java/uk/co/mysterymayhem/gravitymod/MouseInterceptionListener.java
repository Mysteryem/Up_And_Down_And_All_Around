package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;

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
                && GravityCapability.getGravityDirection(mc.thePlayer) == EnumGravityDirection.UP
                && event.phase == TickEvent.Phase.START) {
            mc.mouseHelper = wrapper;
            this.cleanupNeeded = true;
        }
        else if (cleanupNeeded == true && event.phase == TickEvent.Phase.END) {
            mc.mouseHelper = wrapper.getMouseHelper();
        }
    }

    //TODO: ASM the getLookMethod and use the vec3d rotation methods to get the correct 'look' of the player
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
