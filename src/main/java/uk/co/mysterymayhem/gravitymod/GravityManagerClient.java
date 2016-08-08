package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import scala.collection.parallel.ParIterableLike;

import java.util.Collection;

/**
 * Used to control/record the gravity of all players
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class GravityManagerClient extends GravityManagerCommon {

    public boolean isClientUpsideDown() {
        return this.isPlayerUpsideDown(Minecraft.getMinecraft().thePlayer);
    }

    private boolean leftAndRightKeyBindsSwapped = false;

    private void swapLeftAndRightKeyBinds(GameSettings settings) {
        KeyBinding keyBindLeft = settings.keyBindLeft;
        settings.keyBindLeft = settings.keyBindRight;
        settings.keyBindRight = keyBindLeft;
        leftAndRightKeyBindsSwapped = !leftAndRightKeyBindsSwapped;
    }

    public void setClientUpsideDown(boolean upsideDown) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.thePlayer;
        if (upsideDown) {
            if (!this.leftAndRightKeyBindsSwapped) {
                this.swapLeftAndRightKeyBinds(mc.gameSettings);
            }
            player.eyeHeight = player.height - player.getDefaultEyeHeight();
        } else {
            if (this.leftAndRightKeyBindsSwapped) {
                this.swapLeftAndRightKeyBinds(mc.gameSettings);
            }
            player.eyeHeight = player.getDefaultEyeHeight();
        }
    }

    @SubscribeEvent
    public void onDisconnectFromServer(ClientDisconnectionFromServerEvent event) {
        this.resetClient();
    }

    public void resetClient() {
        Minecraft mc = Minecraft.getMinecraft();
        if (leftAndRightKeyBindsSwapped) {
            this.swapLeftAndRightKeyBinds(mc.gameSettings);
        }
        this.playersUpsideDown.clear();
        EntityPlayerSP thePlayer = mc.thePlayer;
        thePlayer.eyeHeight = thePlayer.getDefaultEyeHeight();
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
    public void handleSinglePlayerUpdateServerPacket(String nameOfPlayerAffected, boolean gravityIsNowUpsideDown) {
        if (Minecraft.getMinecraft().thePlayer.getName().equals(nameOfPlayerAffected)) {
            setClientUpsideDown(gravityIsNowUpsideDown);
        }
        this.setPlayerUpsideDown(nameOfPlayerAffected, gravityIsNowUpsideDown);
    }

    @Override
    public void handleMultiplePlayerInitialiseServerPacket(Collection<String> playersWhoAreUpsideDown) {
        for(String playerName : playersWhoAreUpsideDown) {
            this.handleSinglePlayerUpdateServerPacket(playerName, true);
        }
    }

    //TODO: Remove players from the 'UPSIDE_DOWN' set when they log out?
}
