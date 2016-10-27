package uk.co.mysterymayhem.gravitymod.common;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
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
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.capabilities.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.capabilities.IGravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.events.GravityTransitionEvent;
import uk.co.mysterymayhem.gravitymod.packets.PacketHandler;
import uk.co.mysterymayhem.gravitymod.packets.config.ModCompatConfigCheckMessage;
import uk.co.mysterymayhem.gravitymod.util.item.ITickOnMouseCursor;
import uk.co.mysterymayhem.gravitymod.packets.gravitychange.GravityChangeMessage;
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

    public void doGravityTransition(EnumGravityDirection newDirection, EntityPlayerMP player, boolean noTimeout) {
        EnumGravityDirection oldDirection = GravityDirectionCapability.getGravityDirection(player);
        if (oldDirection != newDirection) {
            GravityTransitionEvent.Server event = new GravityTransitionEvent.Server.Pre(newDirection, oldDirection, player);
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                GravityDirectionCapability.setGravityDirection(event.player, event.newGravityDirection, noTimeout);
                player.connection.update();
                this.sendUpdatePacketToDimension(event.player, event.newGravityDirection, noTimeout);
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Server.Post(newDirection, oldDirection, player));
            }
        }
    }

    public void prepareGravityTransition(EnumGravityDirection newDirection, EntityPlayerMP player, int priority) {
        if (player == null) {
            return;
        }
        IGravityDirectionCapability gravityCapability = GravityDirectionCapability.getGravityCapability(player);
        if (gravityCapability == null) {
            return;
        }
        gravityCapability.setPendingDirection(newDirection, priority);
    }

    @SubscribeEvent
    public void onPlayerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(
                    new GravityChangeMessage(event.player.getName(), GravityDirectionCapability.getGravityDirection(event.player), true),
                    (EntityPlayerMP) event.player
            );
            PacketHandler.INSTANCE.sendTo(new ModCompatConfigCheckMessage(ItemStackUseListener.getHashCode()), (EntityPlayerMP) event.player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            PacketHandler.INSTANCE.sendTo(
                    new GravityChangeMessage(event.player.getName(), GravityDirectionCapability.getGravityDirection(event.player), true),
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
                PacketHandler.INSTANCE.sendTo(
                        new GravityChangeMessage(clone.getName(), GravityDirectionCapability.getGravityDirection(clone), true),
                        (EntityPlayerMP) clone
                );
            }
            else {
                EntityPlayerMP cloneMP = (EntityPlayerMP)clone;
                EntityPlayerMP originalMP = (EntityPlayerMP)original;

                EnumGravityDirection originalDirection = GravityDirectionCapability.getGravityDirection(original);

                // When a player entity enters the world, they are given a GravityCapability which defaults to DOWN
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Clone.Pre(originalDirection, EnumGravityDirection.DOWN, cloneMP, originalMP, event));
                GravityDirectionCapability.setGravityDirection(clone, originalDirection, false);
                MinecraftForge.EVENT_BUS.post(new GravityTransitionEvent.Clone.Post(originalDirection, EnumGravityDirection.DOWN, cloneMP, originalMP, event));
            }
        }

    }

    //TODO: Still needed?
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerUpdateTickStart(PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            IGravityDirectionCapability gravityCapability = GravityDirectionCapability.getGravityCapability(event.player);
            if (event.side == Side.SERVER) {
                EntityPlayer player = event.player;
                int timeOut = gravityCapability.getTimeoutTicks();
                if (timeOut == 0) {
                    EnumGravityDirection currentDirection = gravityCapability.getDirection();
                    EnumGravityDirection newDirection = gravityCapability.getPendingDirection();
                    if (currentDirection != newDirection) {
                        doGravityTransition(newDirection, (EntityPlayerMP) player, false);
                    }
                }

            }
            //decrements timeOut on both client and server
            gravityCapability.tick();
            Hooks.makeMotionRelative(event.player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerUpdateTickEnd(PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            //Phase = END
            //If done at start of tick, when putting an item onto the mouse cursor, when it's put back into the player's
            //inventory, there's a single tick where the item won't be ticked. Issue does not occur when done at the end of
            //the player tick.
            //VANILLA_BUG:
            //  The item on the mouse cursor is always null if the player is in creative and in their own inventory
            if (event.side == Side.SERVER) {
                EntityPlayer player = event.player;
                if (player.inventory != null) {
//                    ItemStack itemStack = event.player.inventoryContainer.inventorySlots.getItemStack();
                    ItemStack itemStack = event.player.inventory.getItemStack();
                    if (itemStack != null) {
                        Item item = itemStack.getItem();
                        if (item instanceof ITickOnMouseCursor) {
                            item.onUpdate(itemStack, player.worldObj, player, -1, false);
                        }
                    }
                }
            }
            Hooks.popMotionStack(event.player);
        }
    }

//    //@SubscribeEvent(priority = EventPriority.LOW)
//    public void onPlayerTick(PlayerTickEvent event) {
//        if (event.side == Side.SERVER) {
//            if (event.phase == TickEvent.Phase.START) {
//                ItemStack itemStack = event.player.inventory.armorInventory[0];
//                if (itemStack != null && itemStack.getItem() == ModItems.antiGravityBoots) {
//                    doGravityTransition(EnumGravityDirection.UP, (EntityPlayerMP)event.player);
//                }
//                else {
//                    doGravityTransition(EnumGravityDirection.DOWN, (EntityPlayerMP)event.player);
//                }
//            }
//        }
//    }

    //Dedicated/Integrated Server only
    private void sendUpdatePacketToDimension(EntityPlayerMP player, EnumGravityDirection newGravityDirection, boolean noTimeout) {
        //DEBUG
        //FMLLog.info("Sending gravity data for %s to all players in the same dimension", player.getName());
        PacketHandler.INSTANCE.sendToDimension(new GravityChangeMessage(player.getName(), newGravityDirection, noTimeout), player.dimension);

        //TODO: see if it works
//        if (player.worldObj instanceof WorldServer) {
//            WorldServer worldServer = (WorldServer)player.worldObj;
//            Chunk chunkFromBlockCoords = worldServer.getChunkFromBlockCoords(new BlockPos(player));
//            GravityChangeMessage gravityChangeMessage = new GravityChangeMessage(player.getName(), newGravityDirection, noTimeout);
//            for (EntityPlayer otherPlayer : player.worldObj.playerEntities) {
//                if (otherPlayer instanceof EntityPlayerMP) {
//                    EntityPlayerMP otherPlayerMP = (EntityPlayerMP)otherPlayer;
//                    if (worldServer.getPlayerChunkMap().isPlayerWatchingChunk(otherPlayerMP, chunkFromBlockCoords.xPosition, chunkFromBlockCoords.zPosition)) {
//                        PacketHandler.INSTANCE.sendTo(gravityChangeMessage, otherPlayerMP);
//                    }
//                }
//            }
//        }
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
