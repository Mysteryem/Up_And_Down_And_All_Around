package uk.co.mysterymayhem.gravitymod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import uk.co.mysterymayhem.gravitymod.api.EnumGravityDirection;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangeMessage;
import uk.co.mysterymayhem.gravitymod.packets.GravityChangePacketHandler;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class GravityManagerCommon {
//    protected final HashSet<String> playersUpsideDown = new HashSet<>();

//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public void onGravityTransition(GravityTransitionEvent event) {
//        //FMLLog.info("Received gravityTransitionEvent");
//        if (event.side == Side.SERVER) {
//            FMLLog.info("GravityTransitionEvent is server side");
////            final GravityCapability.IGravityCapability capability = GravityCapability.getGravityCapability(event.player);
//            final EnumGravityDirection currentDirection = GravityCapability.getGravityDirection(event.player);
//            boolean stateChanged = currentDirection != event.newGravityDirection;
//
//            if(stateChanged) {
//                FMLLog.info("Gravity has changed to " + event.newGravityDirection + " from " + currentDirection);
//                GravityCapability.setGravityDirection(event.player, event.newGravityDirection);
////                capability.setDirection(event.newGravityDirection);
//                this.sendUpdatePacketToDimension(event.player, event.newGravityDirection);
//            }
//        }
//    }

    public void doGravityTransition(EnumGravityDirection newDirection, EntityPlayerMP player) {
        EnumGravityDirection oldDirection = GravityDirectionCapability.getGravityDirection(player);
        if (oldDirection != newDirection) {
            GravityTransitionEvent.Server event = new GravityTransitionEvent.Server.Pre(newDirection, oldDirection, player);
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                GravityDirectionCapability.setGravityDirection(event.player, event.newGravityDirection);
                this.sendUpdatePacketToDimension(event.player, event.newGravityDirection);
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Server.Post(newDirection, oldDirection, player));
            }
        }
    }


    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            GravityChangePacketHandler.INSTANCE.sendTo(
                    new GravityChangeMessage(event.player.getName(), GravityDirectionCapability.getGravityDirection(event.player)),
                    (EntityPlayerMP) event.player
            );
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            GravityChangePacketHandler.INSTANCE.sendTo(
                    new GravityChangeMessage(event.player.getName(), GravityDirectionCapability.getGravityDirection(event.player)),
                    (EntityPlayerMP) event.player
            );
        }
    }

    //TODO: Test if players get updated correctly on their own client upon respawning,
    //and test if other clients see them correctly after dying and respawning in view of the other client

    // Fixes player controls not changing upon death to match the new gravity direction
    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        EntityPlayer clone = event.getEntityPlayer();
        EntityPlayer original = event.getOriginal();
        if (clone instanceof EntityPlayerMP && original instanceof EntityPlayerMP) {
            if (event.isWasDeath()) {
                GravityChangePacketHandler.INSTANCE.sendTo(
                        new GravityChangeMessage(clone.getName(), GravityDirectionCapability.getGravityDirection(clone)),
                        (EntityPlayerMP) clone
                );
            }
            else {
                EntityPlayerMP cloneMP = (EntityPlayerMP)clone;
                EntityPlayerMP originalMP = (EntityPlayerMP)original;

                EnumGravityDirection originalDirection = GravityDirectionCapability.getGravityDirection(original);

                // When a player entity enters the world, they are given a GravityCapability which defaults to DOWN
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Clone.Pre(originalDirection, EnumGravityDirection.DOWN, cloneMP, originalMP, event));
                GravityDirectionCapability.setGravityDirection(clone, originalDirection);
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Clone.Post(originalDirection, EnumGravityDirection.DOWN, cloneMP, originalMP, event));
            }
        }

    }

    // Prevent players from getting kicked for flying after dying
    // The "allowFlying = true" seems to be lost when the player respawns
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            EnumGravityDirection gravityDirection = GravityDirectionCapability.getGravityDirection(player);
            if (gravityDirection != EnumGravityDirection.DOWN) {
                player.capabilities.allowFlying = true;
            }
        }
    }

    //@SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.side == Side.SERVER) {
            if (event.phase == TickEvent.Phase.START) {
                ItemStack itemStack = event.player.inventory.armorInventory[0];
                if (itemStack != null && itemStack.getItem() == ModItems.antiGravityBoots) {
                    doGravityTransition(EnumGravityDirection.UP, (EntityPlayerMP)event.player);
                }
                else {
                    doGravityTransition(EnumGravityDirection.DOWN, (EntityPlayerMP)event.player);
                }
            }
        }
    }

    //Dedicated/Integrated Server only
    private void sendUpdatePacketToDimension(EntityPlayer player, EnumGravityDirection newGravityDirection) {
        //DEBUG
        //FMLLog.info("Sending gravity data for %s to all players in the same dimension", player.getName());
        GravityChangePacketHandler.INSTANCE.sendToDimension(new GravityChangeMessage(player.getName(), newGravityDirection), player.dimension);
    }

    public void handlePacket(GravityChangeMessage message, MessageContext context) {
        switch(message.getPacketType()) {
            case CLIENT_REQUEST_GRAVITY_OF_PLAYER:
                //TODO: Move all packet handling logic into this method (using the context to get the worldthread when needed), return type will need to change!
                // This is handled in the GravityChangeMessage.WhatsUpMessageHandler class currently so as to minimise response time
                break;
            default:
                break;
        }
    }

    public EnumGravityDirection getGravityDirection(String playerName) {
        return GravityDirectionCapability.getGravityDirection(playerName, FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
    }

//    public void setGravityDirection(String playerName, EnumGravityDirection direction) {
//        GravityCapability.setGravityCapability(playerName, direction, FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld());
//    }
}
