package uk.co.mysterymayhem.gravitymod.common;

import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import uk.co.mysterymayhem.gravitymod.GravityMod;
import uk.co.mysterymayhem.gravitymod.asm.Hooks;
import uk.co.mysterymayhem.gravitymod.common.capabilities.gravitydirection.GravityDirectionCapability;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityFloatingItem;
import uk.co.mysterymayhem.gravitymod.common.entities.EntityGravityItem;
import uk.co.mysterymayhem.gravitymod.common.items.materials.ItemGravityDust;
import uk.co.mysterymayhem.gravitymod.common.items.shared.IModItem;
import uk.co.mysterymayhem.gravitymod.common.listeners.GravityManagerCommon;
import uk.co.mysterymayhem.gravitymod.common.listeners.ItemStackUseListener;
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
        ModItems.preInitItems();
        EntityRegistry.registerModEntity(EntityGravityItem.class, EntityGravityItem.NAME, GravityMod.getNextEntityID(), GravityMod.INSTANCE, 32, 10, true);
        EntityRegistry.registerModEntity(EntityFloatingItem.class, EntityFloatingItem.NAME, GravityMod.getNextEntityID(), GravityMod.INSTANCE, 32, 10, true);
    }

    public void init() {
        this.registerListeners();
    }

    public void postInit() {
        ModItems.postInitRecipes();
    }

    public void registerGravityManager() {
        this.gravityManagerCommon = new GravityManagerCommon();
    }

    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this.getGravityManager());
        MinecraftForge.EVENT_BUS.register(new ItemStackUseListener());
        MinecraftForge.EVENT_BUS.register(new ItemGravityDust.BlockBreakListener());
//        MinecraftForge.EVENT_BUS.register(new DebugHelperListener());
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

    public <T extends Item & IModItem> T preInitItem(T item) {
        item.preInit();
        return item;
    }
}
