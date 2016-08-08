package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;
import uk.co.mysterymayhem.gravitymod.packets.WhatsUpMessage;
import uk.co.mysterymayhem.gravitymod.packets.WhatsUpPacketHandler;

import java.util.HashSet;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityManagerServer {
    private final HashSet<String> playersUpsideDown = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGravityTransition(GravityTransitionEvent event) {
        if (event.side == Side.SERVER) {
            boolean stateChanged = this.setPlayerUpsideDown(event.player, event.newGravityIsUpsideDown);

            if(stateChanged) {
                //TODO: Send packets to only near clients and then have clients request gravity information of new players that come into their view
                WhatsUpPacketHandler.INSTANCE.sendToAll(new WhatsUpMessage(event.player.getName(), event.newGravityIsUpsideDown));
            }
        }
    }

    public boolean isPlayerUpsideDown(EntityPlayer player) {
        return this.isPlayerUpsideDown(player.getName());
    }

    public boolean isPlayerUpsideDown(String playerName) {
        return playersUpsideDown.contains(playerName);
    }

    public boolean setPlayerUpsideDown(EntityPlayer player, boolean newGravityIsUpsideDown) {
        return setPlayerUpsideDown(player.getName(), newGravityIsUpsideDown);
    }

    public boolean setPlayerUpsideDown(String playerName, boolean newGravityIsUpsideDown) {
        if (newGravityIsUpsideDown) {
            return this.playersUpsideDown.add(playerName);
        }
        else {
            return this.playersUpsideDown.remove(playerName);
        }
    }

    //Client only
    public void handleServerPacket(String nameOfPlayerAffected, boolean gravityIsNowUpsideDown) {}
}
