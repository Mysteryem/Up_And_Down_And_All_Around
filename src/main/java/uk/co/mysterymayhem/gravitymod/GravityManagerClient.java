package uk.co.mysterymayhem.gravitymod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangePacketHandler;

/**
 * Used to control/record the gravity of all players
 *
 * Created by Mysteryem on 2016-08-04.
 */
@SideOnly(Side.CLIENT)
public class GravityManagerClient extends GravityManagerCommon {

    public EnumGravityDirection getClientGravity() {
        return GravityDirectionCapability.getGravityDirection(Minecraft.getMinecraft().thePlayer);
    }

//    public void setClientGravity(EnumGravityDirection direction) {
//        GravityCapability.setGravityDirection(Minecraft.getMinecraft().thePlayer, direction);
//
////        if (direction == EnumGravityDirection.UP) {
////            setClientUpsideDown(true);
////        }
////        else if (direction == EnumGravityDirection.DOWN) {
////            setClientUpsideDown(false);
////        }
//    }

//    private boolean leftAndRightKeyBindsSwapped = false;

//    private void swapLeftAndRightKeyBinds(GameSettings settings) {
//        KeyBinding keyBindLeft = settings.keyBindLeft;
//        settings.keyBindLeft = settings.keyBindRight;
//        settings.keyBindRight = keyBindLeft;
//        leftAndRightKeyBindsSwapped = !leftAndRightKeyBindsSwapped;
//    }

//    public void setClientUpsideDown(boolean upsideDown) {
//        Minecraft mc = Minecraft.getMinecraft();
//        EntityPlayerSP player = mc.thePlayer;
//        if (upsideDown) {
//            if (!this.leftAndRightKeyBindsSwapped) {
//                this.swapLeftAndRightKeyBinds(mc.gameSettings);
//            }
//            player.eyeHeight = player.height - player.getDefaultEyeHeight();
//        } else {
//            if (this.leftAndRightKeyBindsSwapped) {
//                this.swapLeftAndRightKeyBinds(mc.gameSettings);
//            }
//            player.eyeHeight = player.getDefaultEyeHeight();
//        }
//    }

//    @SubscribeEvent
//    public void onDisconnectFromServer(ClientDisconnectionFromServerEvent event) {
//        this.resetClient();
//    }

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

//    public void resetClient() {
//        Minecraft mc = Minecraft.getMinecraft();
//        if (leftAndRightKeyBindsSwapped) {
//            this.swapLeftAndRightKeyBinds(mc.gameSettings);
//        }
//        this.playersUpsideDown.clear();
//        EntityPlayerSP thePlayer = mc.thePlayer;
//        // How can this be null?
//        if (thePlayer != null) {
//            thePlayer.eyeHeight = thePlayer.getDefaultEyeHeight();
//        }
//    }

    @Override
    public void handlePacket(GravityChangeMessage message, MessageContext context) {
        switch(message.getPacketType()) {
            case SINGLE:
//                //DEBUG
//                FMLLog.info("Received gravity data for %s", message.getStringData());
                this.setGravityDirection(message.getStringData(), message.getNewGravityDirection());
//                String playerName = message.getStringData();
//                if (Minecraft.getMinecraft().thePlayer.getName().equals(playerName)) {
//                    this.setClientGravity(message.getNewGravityDirection());
//                }
//                GravityCapability.setGravityCapability(playerName, message.getNewGravityDirection(), Minecraft.getMinecraft().theWorld);
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
        return GravityDirectionCapability.getGravityDirection(playerName, FMLClientHandler.instance().getWorldClient());
    }

    public void setGravityDirection(String playerName, EnumGravityDirection direction) {
        Minecraft mc = Minecraft.getMinecraft();
        AbstractClientPlayer player = mc.thePlayer;
        if (!player.getName().equals(playerName)) {
            EntityPlayer playerEntityByName = mc.theWorld.getPlayerEntityByName(playerName);
            if (!(playerEntityByName instanceof AbstractClientPlayer)) {
                return;
            }
            player = (AbstractClientPlayer)playerEntityByName;
        }
        EnumGravityDirection oldDirection = GravityDirectionCapability.getGravityDirection(player);
        MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Client.Pre(direction, oldDirection, player));
        GravityDirectionCapability.setGravityDirection(player, direction);
        MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Client.Post(direction, oldDirection, player));
    }
}
