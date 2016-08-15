package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import scala.collection.parallel.ParIterableLike;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityCapability;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangePacketHandler;

import java.util.Collection;

/**
 * Used to control/record the gravity of all players
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class GravityManagerClient extends GravityManagerCommon {

//    public boolean isClientUpsideDown() {
//        return this.isPlayerUpsideDown(Minecraft.getMinecraft().thePlayer);
//    }

    public EnumGravityDirection getClientGravity() {
        return GravityCapability.getGravityDirection(Minecraft.getMinecraft().thePlayer);
    }

    public void setClientGravity(EnumGravityDirection direction) {
        GravityCapability.setGravityDirection(Minecraft.getMinecraft().thePlayer, direction);

        if (direction == EnumGravityDirection.UP) {
            setClientUpsideDown(true);
        }
        else if (direction == EnumGravityDirection.DOWN) {
            setClientUpsideDown(false);
        }
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

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            Entity entity = event.getEntity();
            if (entity instanceof EntityOtherPlayerMP) {
                EntityPlayer player = (EntityPlayer)entity;
                //DEBUG
                //FMLLog.info("Requesting gravity data for %s", player.getName());
                this.sendRequestToServer(player.getName());
            }
        }
    }

    public void resetClient() {
        Minecraft mc = Minecraft.getMinecraft();
        if (leftAndRightKeyBindsSwapped) {
            this.swapLeftAndRightKeyBinds(mc.gameSettings);
        }
        this.playersUpsideDown.clear();
        EntityPlayerSP thePlayer = mc.thePlayer;
        // How can this be null?
        if (thePlayer != null) {
            thePlayer.eyeHeight = thePlayer.getDefaultEyeHeight();
        }
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

//    @Override
//    public void handleSinglePlayerUpdateServerPacket(String nameOfPlayerAffected, EnumGravityDirection newGravityDirection) {
//        if (Minecraft.getMinecraft().thePlayer.getName().equals(nameOfPlayerAffected)) {
//            setClientGravity(newGravityDirection);
//        }
//        this.setPlayerUpsideDown(nameOfPlayerAffected, newGravityDirection);
//    }

//    @Override
//    public void handleMultiplePlayerInitialiseServerPacket(Collection<String> playersWhoAreUpsideDown) {
//        for(String playerName : playersWhoAreUpsideDown) {
//            this.handleSinglePlayerUpdateServerPacket(playerName, true);
//        }
//    }

    @Override
    public void handlePacket(GravityChangeMessage message, MessageContext context) {
        switch(message.getPacketType()) {
            case SINGLE:
//                //DEBUG
//                FMLLog.info("Received gravity data for %s", message.getStringData());
                String playerName = message.getStringData();
                if (Minecraft.getMinecraft().thePlayer.getName().equals(playerName)) {
                    this.setClientGravity(message.getNewGravityDirection());
                }
                GravityCapability.setGravityCapability(playerName, message.getNewGravityDirection(), Minecraft.getMinecraft().theWorld);
                break;
            default:
                super.handlePacket(message, context);
                break;
        }

    }

    public void sendRequestToServer(String nameOfPlayerRequested) {
        GravityChangePacketHandler.INSTANCE.sendToServer(new GravityChangeMessage(nameOfPlayerRequested));
    }

    @Override
    public EnumGravityDirection getGravityDirection(String playerName) {
        if (Minecraft.getMinecraft().thePlayer.getName().equals(playerName)) {
            return this.getClientGravity();
        }
        return GravityCapability.getGravityDirection(playerName, FMLClientHandler.instance().getWorldClient());
    }

    @Override
    public void setGravitydirection(String playerName, EnumGravityDirection direction) {
        if (Minecraft.getMinecraft().thePlayer.getName().equals(playerName)) {
            this.setClientGravity(direction);
        }
        GravityCapability.setGravityCapability(playerName, direction, FMLClientHandler.instance().getWorldClient());
    }
}
