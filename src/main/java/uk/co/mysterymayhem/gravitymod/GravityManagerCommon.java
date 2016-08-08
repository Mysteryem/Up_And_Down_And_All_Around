package uk.co.mysterymayhem.gravitymod;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangePacketHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityManagerCommon {
    protected final HashSet<String> playersUpsideDown = new HashSet<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGravityTransition(GravityTransitionEvent event) {
        if (event.side == Side.SERVER) {
            boolean stateChanged = this.setPlayerUpsideDown(event.player, event.newGravityIsUpsideDown);

            if(stateChanged) {
                this.sendUpdatePacketToAll(event.player, event.newGravityIsUpsideDown);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogIn(PlayerLoggedInEvent event) {
        this.sendInitialPacketToSingle((EntityPlayerMP)event.player);
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerLoggedOutEvent event) {
        boolean stateChanged = this.setPlayerUpsideDown(event.player, false);

        if(stateChanged) {
            this.sendUpdatePacketToAll(event.player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!isPlayerUpsideDown(event.player) || event.player.worldObj.getGameRules().getBoolean("keepinventory")) {
            return;
        }
        else {
            boolean stateChanged = this.setPlayerUpsideDown(event.player, false);

            if (stateChanged) {
                this.sendUpdatePacketToAll(event.player, false);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.side == Side.SERVER) {
            if (event.phase == TickEvent.Phase.START) {
                ItemStack itemStack = event.player.inventory.armorInventory[0];
                if (itemStack != null) {
                    if(itemStack.getItem() == ModItems.antiGravityBoots) {
                        if (!this.isPlayerUpsideDown(event.player)) {
                            MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent(true, event.player, Side.SERVER));
                        }
                    }
                }
                else if (this.isPlayerUpsideDown(event.player)) {
                    MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent(false, event.player, Side.SERVER));
                }
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
    public void handleSinglePlayerUpdateServerPacket(String nameOfPlayerAffected, boolean gravityIsNowUpsideDown) {}

    //Client only
    public void handleMultiplePlayerInitialiseServerPacket(Collection<String> playersWhoAreUpsideDown) {}

    //Dedicated/Integrated Server only
    //TODO: Send packets to only near clients and then have clients request gravity information of new players that come into their view
    private void sendUpdatePacketToAll(EntityPlayer player, boolean gravityIsNowUpsideDown) {
        GravityChangePacketHandler.INSTANCE.sendToAll(new GravityChangeMessage(player.getName(), gravityIsNowUpsideDown));
    }

    //Dedicated/Integrated Server only
    private void sendInitialPacketToSingle(EntityPlayerMP player) {
        if (!this.playersUpsideDown.isEmpty()) {
            GravityChangePacketHandler.INSTANCE.sendTo(new GravityChangeMessage(new ArrayList<String>(playersUpsideDown)), player);
        }
    }
}
