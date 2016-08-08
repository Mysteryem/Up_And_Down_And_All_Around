package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.collection.parallel.ParIterableLike;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;

import java.util.HashSet;

/**
 * Used to control/record the gravity of all players
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class GravityManagerClient extends GravityManagerServer {

    public boolean isClientUpsideDown() {
        return this.isPlayerUpsideDown(Minecraft.getMinecraft().thePlayer);
    }

    public void setClientUpsideDown(boolean upsideDown) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        //if(this.setPlayerUpsideDown(Minecraft.getMinecraft().thePlayer, upsideDown)) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        KeyBinding keyBindLeft = settings.keyBindLeft;
        settings.keyBindLeft = settings.keyBindRight;
        settings.keyBindRight = keyBindLeft;

        if (upsideDown) {
            player.eyeHeight = player.height - player.eyeHeight;
        } else {
            player.eyeHeight = player.height - player.eyeHeight;
        }
        //}
    }

//    @Override
//    //@SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onGravityTransition(GravityTransitionEvent event) {
//        if (event.side == Side.CLIENT) {
//            if (Minecraft.getMinecraft().thePlayer == event.player) {
//                setClientUpsideDown(event.newGravityIsUpsideDown);
//            } else {
//                if (event.newGravityIsUpsideDown) {
//                    PLAYERS_UPSIDE_DOWN.add(event.player.getName());
//                } else {
//                    PLAYERS_UPSIDE_DOWN.remove((EntityOtherPlayerMP) event.player);
//                }
//            }
//        }
//    }

    @Override
    public void handleServerPacket(String nameOfPlayerAffected, boolean gravityIsNowUpsideDown) {
        if (Minecraft.getMinecraft().thePlayer.getName().equals(nameOfPlayerAffected)) {
            setClientUpsideDown(gravityIsNowUpsideDown);
        }
        else {
            this.setPlayerUpsideDown(nameOfPlayerAffected, gravityIsNowUpsideDown);
        }

    }

    //TODO: Remove players from the 'UPSIDE_DOWN' set when they log out?
}
