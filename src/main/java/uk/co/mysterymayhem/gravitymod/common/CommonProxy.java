package uk.co.mysterymayhem.gravitymod.common;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
import uk.co.mysterymayhem.gravitymod.common.ModItems;
import uk.co.mysterymayhem.gravitymod.common.packets.PacketHandler;

/**
 * Created by Mysteryem on 2016-08-04.
 */
public class CommonProxy {

    public GravityManagerCommon gravityManagerCommon;

    public void preInit() {
        GravityDirectionCapability.registerCapability();
        this.registerGravityManager();
        PacketHandler.registerMessages();
        ModItems.initItems();
        ModItems.initRecipes();
    }

    public void init() {
        this.registerListeners();
    }

    public void postInit() {

    }

    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerCommon();
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
        MinecraftForge.EVENT_BUS.register(new ItemStackUseListener());
//        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
//        MinecraftForge.EVENT_BUS.register(new MovementInterceptionListener());
    }

    public GravityManagerCommon getGravityManager() {
        return this.gravityManagerCommon;
    }


    // Events that try to allow other mods using these events to modify the player's motion as if they have currently
    // have downwards gravity

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRightClickBlockHighest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRightClickBlockLowest(PlayerInteractEvent.RightClickBlock event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onRightClickItemHighest(PlayerInteractEvent.RightClickItem event) {
        Hooks.makeMotionAbsolute(event.getEntityPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onRightClickItemLowest(PlayerInteractEvent.RightClickItem event) {
        Hooks.popMotionStack(event.getEntityPlayer());
    }
}
