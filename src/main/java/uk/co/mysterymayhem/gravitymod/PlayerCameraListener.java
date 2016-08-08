package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Used to roll the player's camera according to their current gravity
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class PlayerCameraListener {

    @SubscribeEvent
    public void onCameraSetup(CameraSetup event) {
        if (GravityMod.proxy.gravityManagerServer.isPlayerUpsideDown(Minecraft.getMinecraft().thePlayer)) {
            event.setRoll(event.getRoll() + 180);
        }
    }
}
